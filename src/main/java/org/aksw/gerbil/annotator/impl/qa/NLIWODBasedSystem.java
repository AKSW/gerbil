package org.aksw.gerbil.annotator.impl.qa;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.annotator.QASystem;
import org.aksw.gerbil.annotator.impl.AbstractAnnotator;
import org.aksw.gerbil.config.GerbilConfiguration;
import org.aksw.gerbil.datatypes.ErrorTypes;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.qa.QAUtils;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.systems.ASystem;
import org.aksw.qa.systems.GANSWER2;
import org.aksw.qa.systems.HAWK;
import org.aksw.qa.systems.OKBQA;
import org.aksw.qa.systems.QAKIS;
import org.aksw.qa.systems.QANARY;
import org.aksw.qa.systems.SINA;
import org.aksw.qa.systems.START;
import org.aksw.qa.systems.YODA;
import org.aksw.qa.systems.QUEPY;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.reflections.Reflections;


public class NLIWODBasedSystem extends AbstractAnnotator implements QASystem {

	public static final String HAWK_SYSTEM_NAME = "HAWK";
	public static final String QAKIS_SYSTEM_NAME = "QAKIS";
	public static final String SINA_SYSTEM_NAME = "SINA";
	public static final String START_SYSTEM_NAME = "START";
	public static final String YODA_SYSTEM_NAME = "YODA";
	public static final String OKBQA_SYSTEM_NAME = "OKBQA";
	public static final String QANARY_SYSTEM_WIKIDATA_NAME = "QAnswer (wikidata)";
	public static final String QANARY_SYSTEM_DBPEDIA_NAME = "QAnswer (DBpedia)";
	public static final String GANSWER2_SYSTEM_NAME = "gAnswer2";
	public static final String QUEPY = "QUEPY";
	private static final int DEFAULT_WAITING_TIME = 60000;
	private static final String MAXIMUM_TIME_TO_WAIT_KEY = "org.aksw.gerbil.annotator.http.HttpManagement.maxWaitingTime";
	
	//GERBIL Issue 241: Adding KB names for QANARY
	public static final String QANARY_SYSTEM_WIKIDATA_KBNAME = "wikidata";
	public static final String QANARY_SYSTEM_DBPEDIA_KBNAME = "dbpedia";
	
	protected ASystem qaSystem;

	public NLIWODBasedSystem(String systemName) throws GerbilException {
		this(systemName, "");
	}
	
	public NLIWODBasedSystem(String systemName, String url) throws GerbilException {	
		int maxWaitingTime = DEFAULT_WAITING_TIME;
        try {
            maxWaitingTime = GerbilConfiguration.getInstance().getInt(MAXIMUM_TIME_TO_WAIT_KEY);
        } catch (Exception e) {
        }
		switch (systemName) {
		case HAWK_SYSTEM_NAME: {
			qaSystem = new HAWK();
			break;
		}
		case QAKIS_SYSTEM_NAME: {
			qaSystem = new QAKIS();
			break;
		}
		case SINA_SYSTEM_NAME: {
			qaSystem = new SINA();
			break;
		}
		case START_SYSTEM_NAME: {
			qaSystem = new START();
			break;
		}
		case YODA_SYSTEM_NAME: {
			qaSystem = new YODA();
			break;
		}
		case OKBQA_SYSTEM_NAME: {
			qaSystem = new OKBQA();
			break;
		}
		case QANARY_SYSTEM_WIKIDATA_NAME: {
			qaSystem = new QANARY(url, QANARY_SYSTEM_WIKIDATA_KBNAME);
			break;
		}
		case QANARY_SYSTEM_DBPEDIA_NAME:{
			qaSystem = new QANARY(url, QANARY_SYSTEM_DBPEDIA_KBNAME);
			break;
		}
		case GANSWER2_SYSTEM_NAME:{
			qaSystem = new GANSWER2(url);
			break;
		}
		case QUEPY:{
			qaSystem = new QUEPY(url);
			break;
		}
		default:
			Reflections ref = new Reflections("org.aksw.qa.systems");
			boolean hasSystem=false;
			for(Class<? extends ASystem> sys : ref.getSubTypesOf(ASystem.class)) {
				if(sys.getSimpleName().equals(systemName)) {
					try {
						if(url==null || url.isEmpty()) {
							qaSystem = sys.newInstance();
						}else {
							qaSystem = sys.getConstructor(String.class).newInstance(url);
						}
						hasSystem=true;
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						continue;
					}
				}
			}
			if(!hasSystem) {
				throw new GerbilException("Got an unknown system name \""
						+ systemName + "\".", ErrorTypes.ANNOTATOR_LOADING_ERROR);
			}
		}
		qaSystem.setSocketTimeOutMs(maxWaitingTime);

	}

	@Override
	public List<Marking> answerQuestion(Document document, String questionLang)
			throws GerbilException {
		IQuestion question;
		try {
			question = qaSystem.search(document.getText(), questionLang, true);

			if (question.getSparqlQuery() != null)
				question.setSparqlQuery(replacePrefixes(
						question.getSparqlQuery(), PrefixMapping.Extended));
			Document resultDoc = QAUtils.translateQuestion(question,
					document.getDocumentURI(), questionLang);
			if (resultDoc != null) {
				return resultDoc.getMarkings();
			} else {
				return new ArrayList<Marking>(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new GerbilException(ErrorTypes.UNEXPECTED_EXCEPTION);
		}
	}

	// TODO find a better class for this method
	public static String replacePrefixes(String query, PrefixMapping pmap) {
		/*
		 * With Prologue and the parse method, the queryString gets parsed
		 * without an error
		 */
		Prologue prog = new Prologue();
		prog.setPrefixMapping(pmap);
		Query q = QueryFactory.parse(new Query(prog), query, null, null);
		// Set Prefix Mapping
		q.setPrefixMapping(pmap);
		// remove PrefixMapping so the prefixes will get replaced by the full
		// uris
		q.setPrefixMapping(null);
		return q.serialize();
	}

}

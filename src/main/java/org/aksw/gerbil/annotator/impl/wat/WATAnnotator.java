/**
 * This file is part of General Entity Annotator Benchmark.
 *
 * General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.gerbil.annotator.impl.wat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.aksw.gerbil.annotator.A2KBAnnotator;
import org.aksw.gerbil.annotator.http.AbstractHttpBasedAnnotator;
import org.aksw.gerbil.config.GerbilConfiguration;
import org.aksw.gerbil.datatypes.ErrorTypes;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Meaning;
import org.aksw.gerbil.transfer.nif.MeaningSpan;
import org.aksw.gerbil.transfer.nif.Span;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.ScoredNamedEntity;
import org.aksw.gerbil.utils.WikipediaHelper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WATAnnotator extends AbstractHttpBasedAnnotator implements A2KBAnnotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WATAnnotator.class);

    private static final String WAT_CONFIG_FILE_PROPERTY_ENDPOINT = "org.aksw.gerbil.annotators.wat.disambiguateUrl";
    private static final String WAT_CONFIG_FILE_PROPERTY_PARAMETERS = "org.aksw.gerbil.annotators.wat.annotateUrl";

    private static final String SPANS_REQUEST_PARAMETER_KEY = "spans";
    private static final String TEXT_REQUEST_PARAMETER_KEY = "text";
    private static final String ENTITY_TITLE_KEY = "title";
    private static final String ENTITY_CONFIDENCE_KEY = "rho";
    private static final String ENTITY_START_KEY = "start";
    private static final String ENTITY_END_KEY = "end";
    private static final String TAGME_KEY_PARAMETER_KEY = "org.aksw.gerbil.annotators.TagMe.key";

    private String key;


    private final String annotateUrl;
    private final String disambiguateUrl;

    public WATAnnotator() throws GerbilException {
        this.annotateUrl = GerbilConfiguration.getInstance().getString(WAT_CONFIG_FILE_PROPERTY_PARAMETERS);
        if (annotateUrl == null) {
            throw new GerbilException("Couldn't load parameters (\"" + WAT_CONFIG_FILE_PROPERTY_PARAMETERS + "\".",
                    ErrorTypes.ANNOTATOR_LOADING_ERROR);
        }
        this.disambiguateUrl = GerbilConfiguration.getInstance().getString(WAT_CONFIG_FILE_PROPERTY_ENDPOINT);
        if (disambiguateUrl == null) {
            throw new GerbilException("Couldn't load endpoint (\"" + WAT_CONFIG_FILE_PROPERTY_ENDPOINT + "\".",
                    ErrorTypes.ANNOTATOR_LOADING_ERROR);
        }
        key = GerbilConfiguration.getInstance().getString(TAGME_KEY_PARAMETER_KEY);
        if (key == null) {
            throw new GerbilException("Couldn't load key from configuration (\"" + TAGME_KEY_PARAMETER_KEY + "\").",
                    ErrorTypes.ANNOTATOR_LOADING_ERROR);
        }
    }

    public WATAnnotator(String annotateUrl, String disambiguateUrl) throws GerbilException {
        this.disambiguateUrl = disambiguateUrl;
        this.annotateUrl = annotateUrl;
        key = GerbilConfiguration.getInstance().getString(TAGME_KEY_PARAMETER_KEY);
        if (key == null) {
            throw new GerbilException("Couldn't load key from configuration (\"" + TAGME_KEY_PARAMETER_KEY + "\").",
                    ErrorTypes.ANNOTATOR_LOADING_ERROR);
        }
    }

    public WATAnnotator(String annotateUrl, String disambiguateUrl, String key) {
        this.disambiguateUrl = disambiguateUrl;
        this.annotateUrl = annotateUrl;
        this.key  = key;
    }

    @Override
    public List<Meaning> performC2KB(Document document) throws GerbilException {
        return requestAnnotations(document, false).getMarkings(Meaning.class);
    }

    @Override
    public List<MeaningSpan> performA2KBTask(Document document) throws GerbilException {
        return requestAnnotations(document, false).getMarkings(MeaningSpan.class);
    }

    @Override
    public List<MeaningSpan> performD2KBTask(Document document) throws GerbilException {
        return requestAnnotations(document, true).getMarkings(MeaningSpan.class);
    }

    @Override
    public List<Span> performRecognition(Document document) throws GerbilException {
        return requestAnnotations(document, false).getMarkings(Span.class);
    }

    private Document requestAnnotations(Document document, boolean disambiguate) throws GerbilException {
        Document resultDoc = new DocumentImpl(document.getText(), document.getDocumentURI());
        JSONObject parameters = new JSONObject();
        if (disambiguate) {
            JSONArray mentionsJson = new JSONArray();
            for (Span span : document.getMarkings(Span.class)) {
                JSONObject mentionJson = new JSONObject();
                mentionJson.put(ENTITY_START_KEY, span.getStartPosition());
                mentionJson.put(ENTITY_END_KEY, span.getStartPosition() + span.getLength());
                mentionsJson.put(mentionJson);
            }
            parameters.put(SPANS_REQUEST_PARAMETER_KEY, mentionsJson);
        }
        parameters.put(TEXT_REQUEST_PARAMETER_KEY, document.getText());

        HttpRequestBase request = null;
        StringBuilder url= null;
        try {
            if(disambiguate){
                url=new StringBuilder(disambiguateUrl);
                url.append("?gcube-token=").append(key);
                url.append("&document=").append(URLEncoder.encode(parameters.toString(), "UTF-8"));
            }
            else{
                url = new StringBuilder(annotateUrl);
                url.append("?gcube-token=").append(key);
                url.append("&text=").append(URLEncoder.encode(document.getText(), "UTF-8"));
            }
                request = createGetRequest(url.toString());


        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            throw new GerbilException("Couldn't create HTTP request.", e, ErrorTypes.UNEXPECTED_EXCEPTION);
        }

        request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        request.addHeader(HttpHeaders.ACCEPT, "application/json");
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, "UTF-8");

        HttpEntity entity = null;
        CloseableHttpResponse response = null;
        try {
            response = sendRequest(request);
            entity = response.getEntity();
            try {
                String content = IOUtils.toString(entity.getContent(), "UTF-8");
                // parse results
                JSONObject outObj = new JSONObject(content);
                JSONArray jsAnnotations = outObj.getJSONArray("annotations");
                int start;
                for (int i = 0; i < jsAnnotations.length(); i++) {
                    JSONObject js_ann = jsAnnotations.getJSONObject(i);
                    if (js_ann.has(ENTITY_START_KEY) && js_ann.has(ENTITY_END_KEY) && js_ann.has(ENTITY_TITLE_KEY)) {
                        start = js_ann.getInt(ENTITY_START_KEY);
                        if (js_ann.has(ENTITY_CONFIDENCE_KEY)) {
                            resultDoc.addMarking(new ScoredNamedEntity(start, js_ann.getInt(ENTITY_END_KEY) - start,
                                    WikipediaHelper.generateUriSet(js_ann.getString(ENTITY_TITLE_KEY)),
                                    js_ann.getDouble(ENTITY_CONFIDENCE_KEY)));
                        } else {
                            resultDoc.addMarking(new NamedEntity(start, js_ann.getInt(ENTITY_END_KEY) - start,
                                    WikipediaHelper.generateUriSet(js_ann.getString(ENTITY_TITLE_KEY))));
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Couldn't parse the response.", e);
                throw new GerbilException("Couldn't parse the response.", e, ErrorTypes.UNEXPECTED_EXCEPTION);
            }
        } finally {
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e1) {
                }
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                }
            }
            closeRequest(request);
        }
        return resultDoc;
    }


}

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
package org.aksw.gerbil.annotator.impl.rel;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.aksw.gerbil.annotator.OKETask1Annotator;
import org.aksw.gerbil.annotator.http.AbstractHttpBasedAnnotator;
import org.aksw.gerbil.datatypes.ErrorTypes;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Meaning;
import org.aksw.gerbil.transfer.nif.MeaningSpan;
import org.aksw.gerbil.transfer.nif.Span;
import org.aksw.gerbil.transfer.nif.TypedSpan;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.ScoredTypedNamedEntity;
import org.aksw.gerbil.transfer.nif.data.TypedNamedEntity;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RELAnnotator extends AbstractHttpBasedAnnotator implements OKETask1Annotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RELAnnotator.class);

    private static final Charset CHARSET = Charset.forName("utf-8");
    private static final ContentType REQUEST_CONTENT_TYPE = ContentType.create("application/json", Consts.UTF_8);

    private static final String TEXT_REQUEST_PARAMETER_KEY = "text";
    private static final String SPANS_REQUEST_PARAMETER_KEY = "spans";

    private String serviceUrl;

    public RELAnnotator(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    protected Document requestAnnotations(Document doc) throws GerbilException {
        String text = doc.getText();
        String documentUri = doc.getDocumentURI();
        LOGGER.info("Started request for {}", documentUri);
        HttpPost request = null;
        try {
            request = createPostRequest(serviceUrl);
        } catch (Exception e) {
            throw new GerbilException("Couldn't create HTTP request.", e, ErrorTypes.UNEXPECTED_EXCEPTION);
        }

        JsonObject parameters = new JsonObject();
        parameters.addProperty(TEXT_REQUEST_PARAMETER_KEY, text);
        parameters.add(SPANS_REQUEST_PARAMETER_KEY, new JsonArray());
        request.setEntity(new StringEntity(parameters.toString(), CHARSET));
        request.addHeader(HttpHeaders.CONTENT_TYPE, REQUEST_CONTENT_TYPE.toString());
        request.addHeader(HttpHeaders.ACCEPT, REQUEST_CONTENT_TYPE.toString());

        HttpEntity entity = null;
        CloseableHttpResponse response = null;
        Document resultDoc = null;
        try {
            response = sendRequest(request);
            entity = response.getEntity();
            try {
                resultDoc = new DocumentImpl(text, documentUri);
                String out = IOUtils.toString(entity.getContent());
                JsonArray outArray = new JsonParser().parse(out).getAsJsonArray();
                parseMarkings(outArray, resultDoc);
            } catch (Exception e) {
                LOGGER.error("Couldn't parse the response.", e);
                throw new GerbilException("Couldn't parse the response.", e, ErrorTypes.UNEXPECTED_EXCEPTION);
            }
        } finally {
            closeRequest(request);
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e1) {
                }
            }
            IOUtils.closeQuietly(response);
        }
        LOGGER.info("Finished request for {}", documentUri);
        return resultDoc;
    }

    private void parseMarkings(JsonArray outArray, Document resultDoc) {
        for (JsonElement element : outArray) {
            JsonArray entity = element.getAsJsonArray();
            int start = entity.get(0).getAsInt();
            int length = entity.get(1).getAsInt();
            String uri = "http://dbpedia.org/resource/" + entity.get(3).getAsString();
            double confidence = entity.get(4).getAsDouble();
            Set<String> types = getType(entity.get(5).getAsString());
            if(confidence != -1) {
                resultDoc.addMarking(new ScoredTypedNamedEntity(start, length, new HashSet<String>(Arrays.asList(uri)), types, confidence));
            } else {
                resultDoc.addMarking(new TypedNamedEntity(start, length, new HashSet<String>(Arrays.asList(uri)), types));
            }
        }
    }

    private Set<String> getType(String type) {
        Set<String> types = new HashSet<>();
		switch (type) {
            case "PER":
                types.add("http://dbpedia.org/ontology/Person");
                break;
            case "LOC":
                types.add("http://dbpedia.org/ontology/Place");
                break;
            case "ORG":
                types.add("http://dbpedia.org/ontology/Organisation");
                break;
            default:
                break;
        }
        return types;
	}

    @Override
    public List<Meaning> performC2KB(Document document) throws GerbilException {
        return requestAnnotations(document).getMarkings(Meaning.class);
    }

    @Override
    public List<MeaningSpan> performA2KBTask(Document document) throws GerbilException {
        return requestAnnotations(document).getMarkings(MeaningSpan.class);
    }

    @Override
    public List<MeaningSpan> performD2KBTask(Document document) throws GerbilException {
        return requestAnnotations(document).getMarkings(MeaningSpan.class);
    }

    @Override
    public List<Span> performRecognition(Document document) throws GerbilException {
        return requestAnnotations(document).getMarkings(Span.class);
    }

    @Override
    public List<TypedSpan> performRT2KBTask(Document document) throws GerbilException {
        return requestAnnotations(document).getMarkings(TypedSpan.class);
    }

    @Override
    public List<TypedSpan> performTyping(Document document) throws GerbilException {
        return requestAnnotations(document).getMarkings(TypedSpan.class);
    }

    @Override
    public List<TypedNamedEntity> performTask1(Document document) throws GerbilException {
        return requestAnnotations(document).getMarkings(TypedNamedEntity.class);
    }
}

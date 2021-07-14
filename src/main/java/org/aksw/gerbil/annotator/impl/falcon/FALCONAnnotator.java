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
package org.aksw.gerbil.annotator.impl.falcon;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.aksw.gerbil.annotator.C2KBAnnotator;
import org.aksw.gerbil.annotator.http.AbstractHttpBasedAnnotator;
import org.aksw.gerbil.datatypes.ErrorTypes;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Meaning;
import org.aksw.gerbil.transfer.nif.data.Annotation;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
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

public class FALCONAnnotator extends AbstractHttpBasedAnnotator implements C2KBAnnotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FALCONAnnotator.class);

    private static final Charset CHARSET = Charset.forName("utf-8");
    private static final ContentType REQUEST_CONTENT_TYPE = ContentType.create("application/json", Consts.UTF_8);
    private static final String TEXT_REQUEST_PARAMETER_KEY = "text";

    private String serviceUrl;

    public FALCONAnnotator(String serviceUrl) {
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
                String content = IOUtils.toString(entity.getContent());
                JsonObject outJson = new JsonParser().parse(content).getAsJsonObject();
                parseMarkings(outJson, resultDoc);
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
        LOGGER.info("Finished request for {}", resultDoc.getDocumentURI());
        return resultDoc;
    }

    private void parseMarkings(JsonObject out, Document resultDoc) {
        JsonArray entities = out.get("entities_dbpedia").getAsJsonArray();
        for (int i = 0; i < entities.size(); i++) {
            JsonArray ent = entities.get(i).getAsJsonArray();
            String uri = ent.get(0).toString();
            resultDoc.addMarking(new Annotation(uri.substring(1, uri.length() - 1)));
        }
    }

    @Override
    public List<Meaning> performC2KB(Document document) throws GerbilException {
        return requestAnnotations(document).getMarkings(Meaning.class);
    }
}
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
package org.aksw.gerbil.dataset.impl.conll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.aksw.gerbil.dataset.InitializableDataset;
import org.aksw.gerbil.dataset.impl.AbstractDataset;
import org.aksw.gerbil.datatypes.ErrorTypes;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.TypedNamedEntity;
import org.apache.commons.io.IOUtils;

/**
 * Dataset Adapter to load CoNLL datasets
 */
public class GenericCoNLLDataset extends AbstractDataset implements InitializableDataset {

	protected static final String MARKING_START = "B-";
	protected static final String MARKING_INSIDE = "I-";

    protected String file;
    protected List<Document> documents;
	protected StringBuilder currentText;
	protected int firstDocId;
	protected int lastDocId;

    protected CoNLLTypeRetriever typeRetriever;
    protected int annotationColumn;
    protected int uriColumn;

    public GenericCoNLLDataset(String file, int annotationColumn, int uriColumn, CoNLLTypeRetriever typeRetriever) {
		this(file, annotationColumn, uriColumn, typeRetriever, -1, -1);
	}

	public GenericCoNLLDataset(String file, int annotationColumn, int uriColumn, CoNLLTypeRetriever typeRetriever,
				String firstDocId, String lastDocId) {
		this(file, annotationColumn, uriColumn, typeRetriever, Integer.parseInt(firstDocId), Integer.parseInt(lastDocId));
	}
	
	public GenericCoNLLDataset(String file, int annotationColumn, int uriColumn, CoNLLTypeRetriever typeRetriever,
				int firstDocId, int lastDocId) {
        this.file = file;
        this.annotationColumn = annotationColumn;
        this.uriColumn = uriColumn;
        this.typeRetriever = typeRetriever;
        this.firstDocId= firstDocId;
        this.lastDocId = lastDocId;
    }

    @Override
    public int size() {
        return documents.size();
    }

    @Override
    public List<Document> getInstances() {
        return documents;
    }

    @Override
    public void init() throws GerbilException {
		this.documents = loadDocuments(new File(file));
		if ((firstDocId > 0) && (lastDocId > 0)) {
			this.documents = this.documents.subList(firstDocId - 1, lastDocId);
		}
    }

    protected List<Document> loadDocuments(File file)
			throws GerbilException {
		BufferedReader reader = null;
		List<Document> documents = new ArrayList<Document>();
		String documentUriPrefix = "http://" + getName() + "/";
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
			String line = reader.readLine();
			int index = 0;
			List<Marking> markings = new ArrayList<Marking>();
			StringBuilder currentDoc = new StringBuilder("");
			while (line != null) {
				if (line.trim().isEmpty()) {
					// Get Markings
					markings = findMarkings(currentDoc.toString());
					// Save the document
					documents.add(new DocumentImpl(currentText.toString(), documentUriPrefix + index, markings));
					// New Document
					currentDoc.delete(0, currentDoc.length());
					line = reader.readLine();
					index++;
				} else {
					currentDoc.append(line + "\n");
					line = reader.readLine();
				}
			}
			//check if there is a document to be added
			if(currentDoc.length() > 0) {
				// Get Markings
				markings = findMarkings(currentDoc.toString());
				// Save last document
				documents.add(new DocumentImpl(currentText.toString(), documentUriPrefix + index, markings));
			}
		} catch (IOException e) {
			throw new GerbilException("Exception while reading dataset.", e,
					ErrorTypes.DATASET_LOADING_ERROR);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return documents;
    }
    
    public List<Marking> findMarkings(String currentDoc) {
		List<Marking> markings = new ArrayList<Marking>();
		String[] lines = currentDoc.split("\n");
		currentText = new StringBuilder("");
		int i = 0;
		for (String tokenFull : lines) {
			String[] token = tokenFull.split("\t+");
			if (token.length > annotationColumn && token[annotationColumn].startsWith(MARKING_START)) {
				markings.add(getWholeMarking(lines, i));
			}
			currentText.append(token[0] + " ");
			i++;
		}
		return markings;
    }
    
    protected TypedNamedEntity getWholeMarking(String line[], int pos) {
        String[] tokens = line[pos].split("\t");

        // get type of the marking
        String type = typeRetriever.getTypeURI(tokens[annotationColumn].substring(2));

		//get uri of the marking if given in the dataset
		String uri = "";
		if (uriColumn != -1 && tokens[uriColumn].startsWith("http")) {
            uri = tokens[uriColumn];
		} 

        // get surface form of the marking
        StringBuilder surfaceForm = new StringBuilder().append(tokens[0]);
		for (int i = pos + 1; i < line.length; i++) {
			tokens = line[i].split("\t");
			if (tokens[annotationColumn].startsWith(MARKING_INSIDE)) {
				surfaceForm.append(" ").append(tokens[0]);
			} else {
				break;
			}
		}
		return new TypedNamedEntity(currentText.length(), surfaceForm.length(), uri, 
				new HashSet<String>(Arrays.asList(type)));
	}
}
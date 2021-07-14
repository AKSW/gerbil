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
package org.aksw.gerbil.dataset.impl.ritter;

import org.aksw.gerbil.dataset.impl.conll.CoNLLTypeRetriever;
import org.aksw.gerbil.dataset.impl.conll.GenericCoNLLDataset;

public class RitterDataset extends GenericCoNLLDataset {
    
    private static final int ANNOTATION_COLUMN = 1;
    private static final int URI_COLUMN = -1;
    private static final CoNLLTypeRetriever TYPE_TAGS = new CoNLLTypeRetriever("geo-loc", null, "movie", "musicartist",
        "other", "person", "product", "sportsteam", "tvshow", null);

    public RitterDataset(String file) {
        super(file, ANNOTATION_COLUMN, URI_COLUMN, TYPE_TAGS);
        TYPE_TAGS.addTypeURI("facility", "http://dbpedia.org/ontology/Place");
        TYPE_TAGS.addTypeURI("company", "http://dbpedia.org/ontology/Company");
    }
}
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.TypedNamedEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RitterDatasetTest {

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        testConfigs.add(new Object[] { "Texans	O\nurged	O\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-geo-loc\ncoast	I-geo-loc\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", new String[]{"Texas coast", "http://dbpedia.org/ontology/Place"} });
        testConfigs.add(new Object[] { "Texans	B-movie\nurged	O\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-geo-loc\ncoast	I-geo-loc\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", new String[]{"Texans", "http://dbpedia.org/ontology/Film"} });
        testConfigs.add(new Object[] { "Texans	B-company\nurged	B-PER\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-geo-loc\ncoast	I-geo-loc\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", new String[]{"Texans", "http://dbpedia.org/ontology/Company"} });
        testConfigs.add(new Object[] { "Texans	B-facility\nurged	B-PER\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-geo-loc\ncoast	I-geo-loc\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", new String[]{"Texans", "http://dbpedia.org/ontology/Place"} });
        testConfigs.add(new Object[] { "Texans	B-musicartist\nurged	B-PER\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-geo-loc\ncoast	I-geo-loc\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", new String[]{"Texans", "http://dbpedia.org/ontology/MusicalArtist"} });
        testConfigs.add(new Object[] { "Texans	B-other\nurged	B-PER\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-geo-loc\ncoast	I-geo-loc\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", new String[]{"Texans", "http://dbpedia.org/ontology/Unknown"} });
        testConfigs.add(new Object[] { "Texans	B-person\nurged	B-PER\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-geo-loc\ncoast	I-geo-loc\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", new String[]{"Texans", "http://dbpedia.org/ontology/Person"} });
        testConfigs.add(new Object[] { "Texans	B-product\nurged	B-PER\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-geo-loc\ncoast	I-geo-loc\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", new String[]{"Texans", "http://dbpedia.org/ontology/product"} });
        testConfigs.add(new Object[] { "Texans	B-sportsteam\nurged	B-PER\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-geo-loc\ncoast	I-geo-loc\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", new String[]{"Texans", "http://dbpedia.org/ontology/SportsTeam"} });
        testConfigs.add(new Object[] { "Texans	B-tvshow\nurged	B-PER\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-geo-loc\ncoast	I-geo-loc\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", new String[]{"Texans", "http://dbpedia.org/ontology/TelevisionShow"} });
        return testConfigs;
    }

    private String text;
    private String[] expectedToken;
    private String tweet;

    public RitterDatasetTest(String text, String tweet, String[] expectedToken) {
        this.text = text;
        this.tweet = tweet;
        this.expectedToken = expectedToken;
    }

    @Test
    public void test() throws IOException {
        RitterDataset ritter = new RitterDataset("");
        List<Marking> markings = ritter.findMarkings(text);
        Assert.assertNotNull(markings);
        Assert.assertTrue(markings.size() > 0);
        Assert.assertTrue(markings.get(0) instanceof NamedEntity);
        TypedNamedEntity ne = (TypedNamedEntity) markings.get(0);
        String mention = tweet.substring(ne.getStartPosition(), ne.getStartPosition() + ne.getLength());
        Assert.assertEquals(expectedToken[0], mention);
        Assert.assertEquals(expectedToken[1], ne.getTypes().iterator().next());
        ritter.close();
    }

}

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
package org.aksw.gerbil.dataset.impl.umbc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class UMBCDatasetTest {

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        testConfigs.add(new Object[] { "Texans	O\nurged	O\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-LOC\ncoast	I-LOC\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", "Texas coast" });
        testConfigs.add(new Object[] { "Texans	B-LOC\nurged	O\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-LOC\ncoast	I-LOC\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", "Texans" });
        testConfigs.add(new Object[] { "Texans	B-LOC\nurged	B-PER\nto	O\nflee	O\nas	O\nIke	O\nmenaces	O\ncoast	O\n:	O\nAuthorities	O\nhave	O\nurged	O\nresidents	O\nto	O\nflee	O\nthe	O\nTexas	B-LOC\ncoast	I-LOC\n,	O\na	O\nURL	O", "Texans urged to flee as Ike menaces coast : Authorities have urged residents to flee the Texas coast , a URL ", "Texans" });

        return testConfigs;
    }

    private String text;
    private String expectedToken;
    private String tweet;

    public UMBCDatasetTest(String text, String tweet, String expectedToken) {
        this.text = text;
        this.tweet = tweet;
        this.expectedToken = expectedToken;
    }

    @Test
    public void test() throws IOException {
        UMBCDataset umbc = new UMBCDataset("");
        List<Marking> markings = umbc.findMarkings(text);
        Assert.assertNotNull(markings);
        Assert.assertTrue(markings.size() > 0);
        Assert.assertTrue(markings.get(0) instanceof NamedEntity);
        NamedEntity ne = (NamedEntity) markings.get(0);
        String mention = tweet.substring(ne.getStartPosition(), ne.getStartPosition() + ne.getLength());
        Assert.assertEquals(expectedToken, mention);
        umbc.close();
    }

}

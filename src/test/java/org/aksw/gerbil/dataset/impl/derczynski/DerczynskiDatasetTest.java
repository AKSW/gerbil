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
package org.aksw.gerbil.dataset.impl.derczynski;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.gerbil.dataset.impl.derczysnki.DerczynskiDataset;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DerczynskiDatasetTest {

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        testConfigs.add(new Object[] { "#Astros	http://dbpedia.org/resource/Houston_Astros	B-sportsteam	HT\nlineup		O	NN\nfor		O	IN\ntonight		O	NN\n.		O	0\nKeppinger	http://dbpedia.org/resource/Jeff_Keppinger	B-person	NNP\nsits		O	VBZ\n,		O	,\nDowns	http://dbpedia.org/resource/Brodie_Downs	B-person	NNP\nplays		O	VBZ\n2B		O	NN\n,		O	,\nCJ	NIL	B-person	NNP\nbats		O	VBZ\n5th		O	JJ\n.		O	0\n@alysonfooter		O	USR\nhttp://bit.ly/bHvgCS		O	URL", "#Astros lineup for tonight . Jeff Keppinger sits , Downs plays 2B , CJ bats 5th . @alysonfooter http://bit.ly/bHvgCS ", "#Astros" });
        testConfigs.add(new Object[] { "#Astros	http://dbpedia.org/resource/Houston_Astros	B-sportsteam	HT\nlineup		O	NN\nfor		http://bla.com	B-Person IN\ntonight		O	NN\n.		O	0\nKeppinger	http://dbpedia.org/resource/Jeff_Keppinger	B-person	NNP\nsits		O	VBZ\n,		O	,\nDowns	http://dbpedia.org/resource/Brodie_Downs	B-person	NNP\nplays		O	VBZ\n2B		O	NN\n,		O	,\nCJ	NIL	B-person	NNP\nbats		O	VBZ\n5th		O	JJ\n.		O	0\n@alysonfooter		O	USR\nhttp://bit.ly/bHvgCS		O	URL", "#Astros lineup for tonight . Keppinger sits , Downs plays 2B , CJ bats 5th . @alysonfooter http://bit.ly/bHvgCS ", "#Astros" });
        testConfigs.add(new Object[] { "#Astros	O	B-sportsteam	HT\nlineup	O	I-sportsteam	NN\nfor		O	IN\ntonight		O	NN\n.		O	0\nJeff	http://dbpedia.org/resource/Jeff_Keppinger	B-person	NNP\nKeppinger	http://dbpedia.org/resource/Jeff_Keppinger	I-person	NNP\nsits		O	VBZ\n,		O	,\nDowns	http://dbpedia.org/resource/Brodie_Downs	B-person	NNP\nplays		O	VBZ\n2B		O	NN\n,		O	,\nCJ	NIL	B-person	NNP\nbats		O	VBZ\n5th		O	JJ\n.		O	0\n@alysonfooter		O	USR\nhttp://bit.ly/bHvgCS		O	URL", "#Astros lineup for tonight . Jeff Keppinger sits , Downs plays 2B , CJ bats 5th . @alysonfooter http://bit.ly/bHvgCS ","#Astros lineup" });
        testConfigs.add(new Object[] { "#Astros	O	O	HT\nlineup		O	NN\nfor		O	IN\ntonight		O	NN\n.		O	0\nJeff	http://dbpedia.org/resource/Jeff_Keppinger	B-person	NNP\nKeppinger	http://dbpedia.org/resource/Jeff_Keppinger	I-person	NNP\nsits		O	VBZ\n,		O	,\nDowns	http://dbpedia.org/resource/Brodie_Downs	B-person	NNP\nplays		O	VBZ\n2B		O	NN\n,		O	,\nCJ	NIL	B-person	NNP\nbats		O	VBZ\n5th		O	JJ\n.		O	0\n@alysonfooter		O	USR\nhttp://bit.ly/bHvgCS		O	URL", "#Astros lineup for tonight . Jeff Keppinger sits , Downs plays 2B , CJ bats 5th . @alysonfooter http://bit.ly/bHvgCS ","Jeff Keppinger" });
        return testConfigs;
    }

    private String text;
    private String expectedToken;
	private String tweet;

    public DerczynskiDatasetTest(String text, String tweet, String expectedToken) {
        this.text = text;
        this.tweet = tweet;
        this.expectedToken = expectedToken;
    }

    @Test
    public void test() throws IOException {
        DerczynskiDataset derczynski = new DerczynskiDataset("");
        List<Marking> markings = derczynski.findMarkings(text);
        Assert.assertNotNull(markings);
        Assert.assertTrue(markings.size() > 0);
        Assert.assertTrue(markings.get(0) instanceof NamedEntity);
        NamedEntity ne = (NamedEntity) markings.get(0);
        String mention = tweet.substring(ne.getStartPosition(), ne.getStartPosition() + ne.getLength());
        Assert.assertEquals(expectedToken, mention);
        derczynski.close();
    }

}

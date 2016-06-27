package org.aksw.gerbil.dataset.impl.derczynski;

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
        testConfigs.add(new Object[] { "#Astros	http://dbpedia.org/resource/Houston_Astros	B-sportsteam	HT\nlineup		O	NN\nfor		O	IN\ntonight		O	NN\n.		O	0\nKeppinger	http://dbpedia.org/resource/Jeff_Keppinger	B-person	NNP\nsits		O	VBZ\n,		O	,\nDowns	http://dbpedia.org/resource/Brodie_Downs	B-person	NNP\nplays		O	VBZ\n2B		O	NN\n,		O	,\nCJ	NIL	B-person	NNP\nbats		O	VBZ\n5th		O	JJ\n.		O	0\n@alysonfooter		O	USR\nhttp://bit.ly/bHvgCS		O	URL", "#Astros" });
        testConfigs.add(new Object[] { "#Astros	O	B-sportsteam	HT\nlineup		O	NN\nfor		O	IN\ntonight		O	NN\n.		O	0\nKeppinger	http://dbpedia.org/resource/Jeff_Keppinger	B-person	NNP\nsits		O	VBZ\n,		O	,\nDowns	http://dbpedia.org/resource/Brodie_Downs	B-person	NNP\nplays		O	VBZ\n2B		O	NN\n,		O	,\nCJ	NIL	B-person	NNP\nbats		O	VBZ\n5th		O	JJ\n.		O	0\n@alysonfooter		O	USR\nhttp://bit.ly/bHvgCS		O	URL", "Keppinger" });
        return testConfigs;
    }

    private String text;
    private String expectedToken;

    public DerczynskiDatasetTest(String text, String expectedToken) {
        this.text = text;
        this.expectedToken = expectedToken;
    }

    @Test
    public void test() {
        List<Marking> markings = DerczynskiDataset.findMarkings(text);
        Assert.assertNotNull(markings);
        Assert.assertTrue(markings.size() > 0);
        Assert.assertTrue(markings.get(0) instanceof NamedEntity);
        NamedEntity ne = (NamedEntity) markings.get(0);
        String mention = text.substring(ne.getStartPosition(), ne.getStartPosition() + ne.getLength());
        Assert.assertEquals(expectedToken, mention);
    }

}

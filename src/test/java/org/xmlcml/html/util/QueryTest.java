package org.xmlcml.html.util;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.util.StyledText;
import org.xmlcml.graphics.html.HtmlElement;
import org.xmlcml.graphics.html.HtmlFactory;
import org.xmlcml.graphics.html.util.HtmlUtil;


public class QueryTest {

	public final static Logger LOG = Logger.getLogger(QueryTest.class);
	@Test
//	@Ignore // Jsoup fails on entities and namespaces 
	
	
	public void testQuery() throws Exception {
		StyledText st;
		HtmlFactory htmlFactory = new HtmlFactory();
		HtmlElement root = htmlFactory.parse(new File(Fixtures.HTML_DIR, "312.html"));
		Assert.assertTrue(68531 < root.toXML().length());
		List<HtmlElement> all = HtmlUtil.getQueryHtmlElements(root, ".//*");
		Assert.assertEquals("all",  938, all.size());
		List<HtmlElement> italics = HtmlUtil.getQueryHtmlElements(root, ".//*[local-name()='i']");
		Assert.assertEquals("italics",  221, italics.size());
		
		Assert.assertEquals("0",  "et al", italics.get(0).getValue().trim());
		testItalics(new String[]{
				"et al",
				"BMC Evolutionary Biology",
				"et al",
				"BMC Evolutionary Biology",
				"Macaca",
				"fascicularis",
				"Pan troglodytes",
				"P. t. verus",
				"Hylobates agilis",
				"H. klossii",
				"H. lar",
				"H. moloch",
				"H. muelleri",
				"H. pileatus",
				"Nomascus leucogenys",
				"Symphalangus syndactylus",
				"H. agilis",
				"H. lar",
				"S. syndactylus",
				"et al",
				"BMC Evolutionary Biology",
				"Hylobates",
				"N. leuco-",
				"genys",
				"S. syndactylus",
				"et al",
				"BMC Evolutionary Biology",
				"i.e",
				"i.e",
		},
		italics);
	}

	@Test 
	public void testAllItalics() throws Exception {
		HtmlFactory htmlFactory = new HtmlFactory();
		HtmlElement root = htmlFactory.parse(new File(Fixtures.HTML_DIR, "multiple-joined-italics.html"));
		List<HtmlElement> italics = HtmlUtil.getQueryHtmlElements(root, ".//*[local-name()='i']");
		testItalics(new String[]{
		
			    "et al",
			    "BMC Evolutionary Biology",
			    "et al",
			    "BMC Evolutionary Biology",
			    "Macaca fascicularis",
			    "Pan troglodytes",
			    "P. t. verus",
			    "Hylobates agilis",
			    "H. klossii",
			    "H. lar",
			    "H. moloch",
			    "H. muelleri",
			    "H. pileatus",
			    "Nomascus leucogenys",
			    "Symphalangus syndactylus",
			    "H. agilis",
			    "H. lar",
			    "S. syndactylus",
			    "et al",
			    "BMC Evolutionary Biology",
			    "Hylobates",
			    "N. leuco-",
			    "genys",
			    "S. syndactylus",
			    "et al",
			    "BMC Evolutionary Biology",
			    "i.e",
			    "i.e",
			    "H. lar",
			    "d",
			    "d",
			    "d",
			    "et al",
			    "BMC Evolutionary Biology",
			    "d",
			    "d",
			    "H. agilis",
			    "H. lar, H. pileatus",
			    "N. leucogenys",
			    "S. syndacty-",
			    "lus",
			    "H. klossii",
			    "H. moloch",
			    "H. muelleri",
			    "N. leucogenys",
			    "H. lar",
			    "H. agilis",
			    "H. pileatus",
			    "S. syndactylus",
			    "H. agilis",
			    "H. lar",
			    "H. pileatus",
			    "N. leucogenys",
			    "S. syndactylus",
			    "N. leucogenys",
			    "et al",
			    "BMC Evolutionary Biology",
			    "P",
			    "et al",
			    "BMC Evolutionary Biology",
			    "i.e",
			    "N. leucogenys",
			    "S. syndactylus",
			    "Hylobates",
			    "Nomascus",
			    "N. leucogenys",
			    "P",
			    "N",
			    "e",
			    "N",
			    "e",
			    "et al",
			    "BMC Evolutionary Biology",
			    "et al",
			    "BMC Evolutionary Biology",
			    "N",
			    "e",
			    "N",
			    "e",
			    "i.e",
			    "et al",
			    "BMC Evolutionary Biology",
			    "i.e",
			    "N. leucogenys",
			    "S. syndactylus",
			    "Hylobates",
			    "e.g",
			    "S. syndactylus",
			    "i.e",
			    "H. agilis",
			    "H. lar",
			    "H. pileatus",
			    "S. syndactylus",
//			    "et al",
//			    "BMC Evolutionary Biology",
//			    "et al",
//			    "BMC Evolutionary Biology",
//			    "Science",
//			    "Proc Natl Acad Sci USA",
//			    "Astyanax fasciatus",
//			    "Proc Natl Acad Sci USA",
//			    "Science",
//			    "Neuron",
//			    "Mol Biol Evol",
//			    "Genetics",
//			    "Genetics",
//			    "Curr Opin Genet Dev",
//			    "Proc Natl Acad Sci USA",
//			    "Proc Natl Acad Sci USA",
//			    "Mol Biol Evol",
//			    "Mol Biol Evol",
//			    "J Mol Evol",
//			    "Nature",
//			    "Hum Mol Genet",
//			    "Am J Hum Genet",
//			    "et al",
//			    "Nature",
//			    "Col Res Appl",
//			    "et al",
//			    "Vision Res",
//			    "et al",
//			    "Proc Natl Acad Sci USA",
//			    "et al",
//			    "Vision Res",
//			    "Pan troglodytes",
//			    "Primates",
//			    "Mol Biol Evol",
//			    "Proc R Soc Lond B",
//			    "Proc R Soc B",
//			    "Proc Natl Acad Sci USA",
//			    "Vision Res",
//			    "Folia Primatol",
//			    "Primate Adaptation and Evolution.",
//			    "Invest Ophthalmol Vis Sci",
//			    "et al",
//			    "BMC Evolutionary Biology",
//			    "Callithrix jacchus",
//			    "Gene",
//			    "Gene",
//			    "Science",
//			    "Science",
//			    "Science",
//			    "Vision Res",
//			    "Nat Genet",
//			    "Vision Res",
//			    "Biochemistry",
//			    "Nature",
//			    "Mol Biol Evol",
//			    "Nucleic Acids Res",
//			    "Mol Biol Evol",
//			    "Mol Biol Evol",
//			    "Molecular Evolution and Phylogenetics",
//			    "Mol Biol Evol",
//			    "Mol Biol Evol",
//			    "Mol Biol Evol",
//			    "Mol Phylogenet Evol",
//			    "Int J Primatol",
//			    "Hylobates",
//			    "PLoS ONE",
//			    "Mol Phylogenet Evol",
//			    "Mol Phylogenet Evol",
//			    "Principles of Population Genetics.",
//			    "Trends Genet",
//			    "Annu Rev Genomics Hum Genet",
//			    "Curr Biol",
//			    "Jpn J Ophthalmol",
//			    "Color vision: from genes to perception.",
//			    "Fundamentals of Molecular Evolution.",
//			    "Genomics",
//			    "Adh",
//			    "Drosophila",
//			    "Nature",
//			    "Genetics",
//			    "Alouatta seniculus",
//			    "Vision Res",
//			    "J Exp Biol",
//			    "Phil Trans R Soc B",
//			    "Am J Primatol",
//			    "Macaca fascicularis",
//			    "Folia Primatol",
//			    "Nature",
//			    "et al",
//			    "Evolution",
//			    "The Color Sense: Its Origin and Development",
//			    "Trends Ecol Evol",
//			    "Clin Exp Optom",
//			    "Biol Lett",
//			    "Am Nat",
//			    "Int J Primatol",
//			    "Proc Natl Acad Sci USA",
//			    "Mol Ecol",
//			    "Mol Biol Evol",
//			    "Proc R Soc Lond B",
//			    "et al",
//			    "BMC Evolutionary Biology",
//			    "Proc R Soc Lond B",
//			    "et al",
//			    "Am J Primatol",
//			    "Cebus capucinus",
//			    "Anim Behav",
//			    "Curr Zool",
//			    "Callithrix geoffroyi",
//			    "Biol Lett",
//			    "J Hum Evol",
//			    "Vision Res",
//			    "Curr Biol",
//			    "Vision Res",
//			    "J Vis",
		}, italics);
	}

	//==========================
	private void testItalics(String[] expected, List<HtmlElement> italics) {
		for (int i = 0; i < expected.length; i++) {
			Assert.assertEquals(""+i,  expected[i], italics.get(i).getValue().trim());
		}
	}
	
	
}

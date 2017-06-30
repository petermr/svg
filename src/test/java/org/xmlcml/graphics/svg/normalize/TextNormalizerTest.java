package org.xmlcml.graphics.svg.normalize;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.xml.XMLUtil;

import junit.framework.Assert;

public class TextNormalizerTest {
	private static final Logger LOG = Logger.getLogger(TextNormalizerTest.class);
	
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	/** normalize y coords in a single line
	 * 
	 */
	public void testNormalizeY1Line() {
		SVGSVG pageSvg = (SVGSVG) SVGElement.readAndCreateSVG(new File(Fixtures.TEXT_DIR, "oneLine.svg"));
		List<SVGText> texts = SVGText.extractSelfAndDescendantTexts(pageSvg);
		Assert.assertEquals(69,  texts.size());
		TextDecorator textDecorator = new TextDecorator();
		List<List<SVGText>> textListList = textDecorator.normalize(texts);
	}
	
	@Test
	/** normalize y coords in a paragraph
	 * 
	 */
	public void testNormalizePara() {
		String fileRoot = "onePara.svg";
		SVGSVG pageSvg = (SVGSVG) SVGElement.readAndCreateSVG(new File(Fixtures.TEXT_DIR, fileRoot));
		List<SVGText> texts = SVGText.extractSelfAndDescendantTexts(pageSvg);
		Assert.assertEquals(449,  texts.size());
		TextDecorator textDecorator = new TextDecorator();
		textDecorator.normalize(texts);
		SVGG g = textDecorator.convertTexts2Array();
		SVGSVG.wrapAndWriteAsSVG(g, new File(new File("target/text/normalize"), fileRoot+".svg"));
	}
	
	@Test
	/** normalize y coords in a page
	 * 
	 */
	public void testNormalizePage() {
		String fileRoot = "CM_pdf2svg_BMCCancer_9_page4.svg";
		File file = new File(Fixtures.TEXT_DIR, fileRoot);
		Assert.assertEquals("filesize",  1082352, FileUtils.sizeOf(file));
		SVGSVG pageSvg = (SVGSVG) SVGElement.readAndCreateSVG(file);
		List<SVGText> texts = SVGText.extractSelfAndDescendantTexts(pageSvg);
		Assert.assertEquals(4783,  texts.size());
		TextDecorator textDecorator = new TextDecorator();
		textDecorator.normalize(texts);
		SVGG g = textDecorator.convertTexts2Array();
		File file2 = new File(new File("target/text/normalize"), fileRoot+".svg");
		SVGSVG.wrapAndWriteAsSVG(g, file2);
		Assert.assertEquals("filesize",  109491, FileUtils.sizeOf(file2));
	}
}

package org.xmlcml.graphics.svg.cache;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;

/** tests svgElements containing lines
 * 
 * @author pm286
 *
 */
public class PageCacheTest {
	private static final Logger LOG = Logger.getLogger(PageCacheTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private ComponentCache componentCache;

	/** a page with a page header, two tables and some text
	 * get spanning rects
	 */
	@Test
	public void testPage6Rects() {
		List<? extends SVGElement> componentList = extractAndDisplayComponents(
				new File(Fixtures.TABLE_PAGE_DIR, "page6.svg"), new File(Fixtures.TARGET_TABLE_CACHE_DIR, "page6.svg"));
		Assert.assertEquals("components", 3012, componentList.size());
		RectCache rectCache = componentCache.getOrCreateRectCache();
		Assert.assertEquals("rects", 3, rectCache.getOrCreateRectList().size());
		List<SVGRect> spanningRectList = rectCache.getHorizontalPanelList();
		Assert.assertEquals("panels", 3, spanningRectList.size());
	}
	/** a page with a page header, two tables and some text
	 * get spanning rects
	 * 
	 */
	@Test
	public void testPage6Texts() {
		List<? extends SVGElement> componentList = extractAndDisplayComponents(
				new File(Fixtures.TABLE_PAGE_DIR, "page6.svg"), new File(Fixtures.TARGET_TABLE_CACHE_DIR, "page6.svg"));
		Assert.assertEquals("components", 2995, componentList.size());
		TextCache textCache = componentCache.getOrCreateTextCache();
		List<SVGText> textList = textCache.getTextList();
		Assert.assertEquals("components", 2964, textList.size());
		SVGG g = textCache.createCompactedTextsAndReplace();
		List<SVGText> convertedTextList = SVGText.extractSelfAndDescendantTexts(g);
		Assert.assertEquals("compacted", 100, convertedTextList.size());
		textList = textCache.getTextList();
		Assert.assertEquals("compacted", 100, textList.size());
		SVGSVG.wrapAndWriteAsSVG(g, new File(Fixtures.TARGET_TABLE_CACHE_DIR, "texts6.svg"));
	}

	/** a page with a page header, two tables and some text
	 * get spanning rects
	 * 
	 */
	@Test
	public void testFindWhitespace() {
		extractAndDisplayComponents(new File(Fixtures.TABLE_PAGE_DIR, "page6.svg"), new File(Fixtures.TARGET_TABLE_CACHE_DIR, "page6.svg"));
		TextCache textCache = componentCache.getOrCreateTextCache();
		SVGG g = textCache.createCompactedTextsAndReplace();
		Assert.assertEquals("bounding boxes", 131, componentCache.getBoundingBoxList().size());
		double dx = 5;
		double dy = 5;
		SVGG gg = componentCache.createWhitespaceG(dx, dy);
		SVGSVG.wrapAndWriteAsSVG(gg, new File(Fixtures.TARGET_TABLE_CACHE_DIR, "whitespace6.svg"));
	}
	
	@Test
	public void testArticleWhitespace() {
		String root = "10.1136_bmjopen-2016-011048";
		File outDir = new File(Fixtures.TARGET_TABLE_CACHE_DIR, root);
		File journalDir = new File(Fixtures.TABLE_DIR, root);
		File svgDir = new File(journalDir, "svg");
		for (File svgFile : svgDir.listFiles()) {
			System.out.print(".");
			String basename = FilenameUtils.getBaseName(svgFile.toString());
			extractAndDisplayComponents(svgFile, new File(outDir, basename+".convert.svg"));
			TextCache textCache = componentCache.getOrCreateTextCache();
			SVGG g = textCache.createCompactedTextsAndReplace();
			SVGG gg = componentCache.createWhitespaceG(5, 5);
			SVGSVG.wrapAndWriteAsSVG(gg, new File(outDir, basename+".textline.svg"));
		}
		
	}
	
	@Test
	public void testArticlesWhitespace() {
		File[] journalDirs = Fixtures.TABLE_DIR.listFiles();
		for (File journalDir : journalDirs) {
			System.out.print("*");
			String root = journalDir.getName();
			File outDir = new File(Fixtures.TARGET_TABLE_CACHE_DIR, root);
			File svgDir = new File(journalDir, "svg");
			if (svgDir.listFiles() == null) continue;
			for (File svgFile : svgDir.listFiles()) {
				System.out.print(".");
				String basename = FilenameUtils.getBaseName(svgFile.toString());
				extractAndDisplayComponents(svgFile, new File(outDir, basename+".convert.svg"));
				TextCache textCache = componentCache.getOrCreateTextCache();
				SVGG g = textCache.createCompactedTextsAndReplace();
				SVGG gg = componentCache.createWhitespaceG(5, 5);
				SVGSVG.wrapAndWriteAsSVG(gg, new File(outDir, basename+".textline.svg"));
			}
		}		
	}

	// ============================
	
	private List<? extends SVGElement> extractAndDisplayComponents(File infile, File outfile) {
		SVGElement svgElement = SVGElement.readAndCreateSVG(infile);
		componentCache = new ComponentCache();
		componentCache.readGraphicsComponents(svgElement);
		SVGSVG.wrapAndWriteAsSVG(componentCache.getOrCreateConvertedSVGElement(), outfile);
		List<? extends SVGElement> componentList = componentCache.getOrCreateElementList();
		return componentList;
	}
}

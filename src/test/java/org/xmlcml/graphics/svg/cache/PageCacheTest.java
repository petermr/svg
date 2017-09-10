package org.xmlcml.graphics.svg.cache;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.IntMatrix;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.util.SuperPixelArray;

/** analyses pages for components.
 * may extend to compete documents.
 * 
 * @author pm286
 *
 */
// @Ignore // too long
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
		Assert.assertEquals("components", 2995, componentList.size());
		RectCache rectCache = componentCache.getOrCreateRectCache();
		Assert.assertEquals("rects", 3, rectCache.getOrCreateRectList().size());
		List<SVGRect> spanningRectList = rectCache.getOrCreateHorizontalPanelList();
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
	@Ignore // too long
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
	@Ignore // DEVELOP// too long
	public void testSuperPixelArray() {
		String root = "10.1136_bmjopen-2016-011048";
		File outDir = new File(Fixtures.TARGET_TABLE_CACHE_DIR, root);
		File journalDir = new File(Fixtures.TABLE_DIR, root);
		File svgDir = new File(journalDir, "svg");
		SuperPixelArray versoPixelArray = null;
		SuperPixelArray rectoPixelArray = null;
		boolean verso = true;
		boolean recto = false;
		for (File svgFile : svgDir.listFiles()) {
			recto = !recto;
			verso = !verso;
			System.out.print(".");
			String basename = FilenameUtils.getBaseName(svgFile.toString());
			SVGElement svgElement = SVGElement.readAndCreateSVG(svgFile);
			componentCache = new ComponentCache();
			componentCache.readGraphicsComponentsAndMakeCaches(svgElement);
			TextCache textCache = componentCache.getOrCreateTextCache();
			textCache.createCompactedTextsAndReplace();
			Real2Range bbox = Real2Range.createTotalBox(componentCache.getBoundingBoxList());
			LOG.debug(">> "+bbox+" "+componentCache.getBoundingBoxList().size());
			SuperPixelArray superPixelArray = new SuperPixelArray(new Int2Range(bbox));
			superPixelArray.setPixels(1, componentCache.getBoundingBoxList());
			SVGG g = new SVGG();
			superPixelArray.draw(g, new File(outDir, basename+".superPixels.svg"));
			if (verso) {
				versoPixelArray = superPixelArray.plus(versoPixelArray);
			}
			if (recto) {
				rectoPixelArray = superPixelArray.plus(rectoPixelArray);
			}
			
		}
		versoPixelArray.draw(new SVGG(), new File(outDir, "versoPixels.svg"), true);
		rectoPixelArray.draw(new SVGG(), new File(outDir, "rectoPixels.svg"), true);
	}
	
	/** analyses a group of papers and outputs diagrams of the whitespace.
	 * 
	 */
	@Test
	@Ignore // DEVELOP// too long
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

	/** analyze group of articles for superpixels.
	 * 
	 */
	@Test
	@Ignore // DEVELOP// too long
	public void testSuperPixelArrayForArticles() {
		File[] journalDirs = Fixtures.TABLE_DIR.listFiles();
		for (File journalDir : journalDirs) {
			if (!journalDir.isDirectory()) continue;
			System.out.println(">>"+journalDir);
			String root = journalDir.getName();
			File outDir = new File(Fixtures.TARGET_TABLE_CACHE_DIR, root);
			File svgDir = new File(journalDir, "svg");
			SuperPixelArray versoPixelArray = null;
			SuperPixelArray rectoPixelArray = null;
			boolean verso = true;
			boolean recto = false;
			if (svgDir.listFiles() == null) continue;
			for (File svgFile : svgDir.listFiles()) {
				recto = !recto;
				verso = !verso;
				System.out.print(".");
				String basename = FilenameUtils.getBaseName(svgFile.toString());
				SVGElement svgElement = SVGElement.readAndCreateSVG(svgFile);
				componentCache = new ComponentCache();
				componentCache.readGraphicsComponentsAndMakeCaches(svgElement);
				TextCache textCache = componentCache.getOrCreateTextCache();
				textCache.createCompactedTextsAndReplace();
				Real2Range bbox = Real2Range.createTotalBox(componentCache.getBoundingBoxList());
				LOG.debug(">> "+bbox+" "+componentCache.getBoundingBoxList().size());
				SuperPixelArray superPixelArray = new SuperPixelArray(new Int2Range(bbox));
				superPixelArray.setPixels(1, componentCache.getBoundingBoxList());
				
				SVGG g = new SVGG();
				superPixelArray.draw(g, new File(outDir, basename+".superPixels.svg"));
				if (verso) {
					versoPixelArray = superPixelArray.plus(versoPixelArray);
				}
				if (recto) {
					rectoPixelArray = superPixelArray.plus(rectoPixelArray);
				}
				
			}
			versoPixelArray.draw(new SVGG(), new File(outDir, "versoPixels.svg"), true);
			rectoPixelArray.draw(new SVGG(), new File(outDir, "rectoPixels.svg"), true);
		}
	}
	

	// ============================
	
	private List<? extends SVGElement> extractAndDisplayComponents(File infile, File outfile) {
		SVGElement svgElement = SVGElement.readAndCreateSVG(infile);
		componentCache = new ComponentCache();
		componentCache.readGraphicsComponentsAndMakeCaches(svgElement);
		SVGSVG.wrapAndWriteAsSVG(componentCache.getOrCreateConvertedSVGElement(), outfile);
		List<? extends SVGElement> componentList = componentCache.getOrCreateElementList();
		return componentList;
	}
}

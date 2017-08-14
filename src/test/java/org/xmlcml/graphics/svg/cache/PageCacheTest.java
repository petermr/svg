package org.xmlcml.graphics.svg.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGCircle;
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
		List<? extends SVGElement> componentList = extractAndDisplayComponents(Fixtures.TABLE_PAGE_DIR, "page6.svg", "page6.svg");
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
		List<? extends SVGElement> componentList = extractAndDisplayComponents(Fixtures.TABLE_PAGE_DIR, "page6.svg", "page6.svg");
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
		List<? extends SVGElement> componentList = extractAndDisplayComponents(Fixtures.TABLE_PAGE_DIR, "page6.svg", "page6.svg");
		TextCache textCache = componentCache.getOrCreateTextCache();
		SVGG g = textCache.createCompactedTextsAndReplace();
		List<Real2Range> boundingBoxList = componentCache.getBoundingBoxList();
		Assert.assertEquals("bounding boxes", 131, boundingBoxList.size());
		double dx = 5;
		double dy = 5;
		Real2Range box = componentCache.getBoundingBox()
				.getReal2RangeExtendedInX(dx, dy).getReal2RangeExtendedInY(dx, dy);
		RealRange xRange = box.getRealRange(Direction.HORIZONTAL);
		RealRange yRange = box.getRealRange(Direction.VERTICAL);
		List<Real2> whitespaces = new ArrayList<Real2>();
		for (double xx = xRange.getMin(); xx < xRange.getMax(); xx+=dx) {
			for (double yy = yRange.getMin(); yy < yRange.getMax(); yy+=dy) {
				boolean inside = false;
				Real2[] xy = new Real2[]{
						new Real2(xx+dx/2, yy+dx/2),
						new Real2(xx+dx/2, yy-dx/2),
						new Real2(xx-dx/2, yy+dx/2),
						new Real2(xx-dx/2, yy-dx/2),
				};
//				Real2Range deltabox = new Real2Range(xy, xy)
//						.getReal2RangeExtendedInX(dx/2, dy/2).getReal2RangeExtendedInY(dx/2, dy/2);
				for (int k = 0; k < boundingBoxList.size(); k++) {
					for (Real2 xy0 : xy) {
						if (boundingBoxList.get(k).includes(xy0)) {
							inside = true;
							break;
						}
					}
				}
				if (!inside) {
					whitespaces.add(new Real2(xx, yy));
				}
			}
		}
		SVGG gg = new SVGG();
		for (Real2 xy : whitespaces) {
			gg.appendChild(new SVGCircle(xy, dx/2.));
		}
		SVGSVG.wrapAndWriteAsSVG(gg, new File(Fixtures.TARGET_TABLE_CACHE_DIR, "whitespace6.svg"));
	}

	

	// ============================
	
	private List<? extends SVGElement> extractAndDisplayComponents(File inDir, String svgName, String outName) {
		SVGElement svgElement = SVGElement.readAndCreateSVG(new File(inDir, svgName));
		componentCache = new ComponentCache();
		componentCache.readGraphicsComponents(svgElement);
		SVGSVG.wrapAndWriteAsSVG(componentCache.getOrCreateConvertedSVGElement(), new File(Fixtures.TARGET_TABLE_CACHE_DIR, outName));
		List<? extends SVGElement> componentList = componentCache.getOrCreateElementList();
		return componentList;
	}
}

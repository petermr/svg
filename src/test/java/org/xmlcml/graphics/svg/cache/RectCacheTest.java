package org.xmlcml.graphics.svg.cache;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;

/** tests svgElements containing rects
 * 
 * @author pm286
 *
 */
public class RectCacheTest {


	/** multiple blocks of rects
	 * based on a 5-col grid, with some ragged ends.
	 * most span the table and several are in blocks  which could make a spanned table
	 * 
	 */
	@Test
	public void testRect110() {
		List<SVGRect> rectList = extractAndDisplayRects("table110.svg", "rect110.svg");
		Assert.assertEquals("rects", 110, rectList.size());
	}

	/** a 2-level header bar which involves rowspans
	 * the actual body of the table is lines, not rects I think.
	 * 
	 */
	@Test
	public void testRect11() {
		List<SVGRect> rectList = extractAndDisplayRects("table11.svg", "rect11.svg");
		Assert.assertEquals("rects", 11, rectList.size());
	}

	// ============================
	
	private List<SVGRect> extractAndDisplayRects(String svgName, String outName) {
		SVGElement svgElement = SVGElement.readAndCreateSVG(new File(Fixtures.TABLE_RECT_DIR, svgName));
		ComponentCache componentCache = new ComponentCache();
		componentCache.readGraphicsComponentsAndMakeCaches(svgElement);
		RectCache rectCache = componentCache.getOrCreateRectCache();
		SVGSVG.wrapAndWriteAsSVG(rectCache.getOrCreateConvertedSVGElement(), new File("target/table/cache/" + outName));
		List<SVGRect> rectList = rectCache.getOrCreateRectList();
		return rectList;
	}
}

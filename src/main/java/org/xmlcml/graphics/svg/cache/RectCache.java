package org.xmlcml.graphics.svg.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGRect;

/** extracts texts within graphic area.
 * 
 * @author pm286
 *
 */
public class RectCache extends AbstractCache {
	static final Logger LOG = Logger.getLogger(RectCache.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private double panelEps = DEFAULT_PANEL_EPS = 3.0;

	private List<SVGRect> rectList;
	private List<SVGRect> horizontalPanelList;
	private double DEFAULT_PANEL_EPS;
	
	public RectCache(ComponentCache svgCache) {
		super(svgCache);
	}
	
	public List<SVGRect> getOrCreateRectList() {
		if (rectList == null) {
			rectList = shapeCache.getRectList();
			if (rectList == null) {
				rectList = new ArrayList<SVGRect>();
			}
		}
		return rectList;
	}

	public List<? extends SVGElement> getOrCreateElementList() {
		return getOrCreateRectList();
	}

	public List<SVGRect> getHorizontalPanelList() {
		if (horizontalPanelList == null) {
			horizontalPanelList = new ArrayList<SVGRect>();
			getOrCreateRectList();
			RealRange xrange = getOrCreateComponentCacheBoundingBox().getRealRange(Direction.HORIZONTAL);
			for (SVGRect rect : rectList) {
				if (RealRange.isEqual(xrange, rect.getRealRange(Direction.HORIZONTAL), panelEps)) {
					horizontalPanelList.add(rect);
				}
			}
		}
		return horizontalPanelList;
	}

}

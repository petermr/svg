package org.xmlcml.graphics.svg.cache;

import java.io.File;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;

/** superclass for caches.
 * 
 * @author pm286
 *
 */
public abstract class AbstractCache {
	private static final Logger LOG = Logger.getLogger(AbstractCache.class);
	
	static {
		LOG.setLevel(Level.DEBUG);
	}

	protected Real2Range boundingBox;
	protected ComponentCache componentCache;
	protected Real2Range componentCacheBoundingBox;
	protected ShapeCache shapeCache;

	protected AbstractCache() {
		
	}

	public AbstractCache(ComponentCache componentCache) {
		this.componentCache = componentCache;
		this.shapeCache = componentCache.shapeCache; // used in lineCache and rectCache at least
		getOrCreateElementList();
	}

	protected void drawBox(SVGG g, String col, double width) {
		Real2Range box = this.getBoundingBox();
		if (box != null) {
			SVGRect boxRect = SVGRect.createFromReal2Range(box);
			boxRect.setStrokeWidth(width);
			boxRect.setStroke(col);
			boxRect.setOpacity(0.3);
			g.appendChild(boxRect);
		}
	}
	
//	protected abstract Real2Range getBoundingBox();

	protected void writeDebug(String type, String outFilename, SVGG g) {
		File outFile = new File(outFilename);
		SVGSVG.wrapAndWriteAsSVG(g, outFile);
	}

	/** the bounding box of the cache
	 * 
	 * @return the bounding box of the containing svgCache (or null if none)
	 */
	public Real2Range getOrCreateComponentCacheBoundingBox() {
		if (componentCacheBoundingBox == null) {
			componentCacheBoundingBox = componentCache == null ? null : componentCache.getBoundingBox();
		}
		return componentCacheBoundingBox;
	}

	protected Real2Range getOrCreateBoundingBox(List<? extends SVGElement> elementList) {
		if (boundingBox == null) {
			boundingBox = (elementList == null || elementList.size() == 0) ? null :
			SVGElement.createBoundingBox(elementList);
		}
		return boundingBox;
	}

	/** the bounding box of the actual components
	 * The extent of the context (e.g. svgCache) may be larger
	 * @return the bounding box of the contained components
	 */
	public Real2Range getBoundingBox() {
		return getOrCreateBoundingBox(getOrCreateElementList());
	}
	
	public abstract List<? extends SVGElement> getOrCreateElementList();
	
	public SVGG getOrCreateConvertedSVGElement() {
		SVGG svgg = new SVGG();
		List<? extends SVGElement> elementList = getOrCreateElementList();
		for (SVGElement component : elementList) {
			svgg.appendChild(component.copy());
		}
		return svgg;
	}
}

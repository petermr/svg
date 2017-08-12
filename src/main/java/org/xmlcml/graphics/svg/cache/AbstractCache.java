package org.xmlcml.graphics.svg.cache;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;

/** superclass for extractorAnnotators.
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
	protected SVGCache svgCache;

	protected AbstractCache() {
		
	}

	public AbstractCache(SVGCache svgCache) {
		this.svgCache = svgCache;
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
	
	protected abstract Real2Range getBoundingBox();

	protected void writeDebug(String type, String outFilename, SVGG g) {
		File outFile = new File(outFilename);
		SVGSVG.wrapAndWriteAsSVG(g, outFile);
	}
}

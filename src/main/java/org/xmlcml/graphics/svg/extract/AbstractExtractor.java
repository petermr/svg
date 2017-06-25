package org.xmlcml.graphics.svg.extract;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.plot.PlotBox;
import org.xmlcml.graphics.svg.store.SVGStore;

/** superclass for extractorAnnotators.
 * 
 * @author pm286
 *
 */
public abstract class AbstractExtractor {
	private static final Logger LOG = Logger.getLogger(AbstractExtractor.class);
	
	static {
		LOG.setLevel(Level.DEBUG);
	}

	protected Real2Range boundingBox;
	protected SVGStore svgStore;

	protected AbstractExtractor() {
		
	}

	public AbstractExtractor(SVGStore svgStore) {
		this.svgStore = svgStore;
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
		LOG.debug("wrote "+type+": "+outFile.getAbsolutePath());
	}
}

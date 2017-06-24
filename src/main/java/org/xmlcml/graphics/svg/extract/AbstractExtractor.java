package org.xmlcml.graphics.svg.extract;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.plot.PlotBox;
import org.xmlcml.graphics.svg.plot.SVGLogger;

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

	protected PlotBox plotBox;
	protected SVGLogger svgLogger;
	protected Real2Range boundingBox;

	protected AbstractExtractor() {
		
	}

	protected AbstractExtractor(PlotBox plotBox) {
		this.plotBox = plotBox;
		this.svgLogger = plotBox.getSvgLogger();
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
}

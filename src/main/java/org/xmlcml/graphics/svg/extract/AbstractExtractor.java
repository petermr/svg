package org.xmlcml.graphics.svg.extract;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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

	protected AbstractExtractor() {
		
	}

	protected AbstractExtractor(PlotBox plotBox) {
		this.plotBox = plotBox;
		this.svgLogger = plotBox.getSvgLogger();
	}
}

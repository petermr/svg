package org.xmlcml.graphics.svg.plot;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.JodaDate;
import org.xmlcml.graphics.svg.SVGG;

/** a logger for SVG transformations and components.
 * May contain copies of actual elements
 *  
 * @author pm286
 *
 */
public class SVGLogger {
	private static final Logger LOG = Logger.getLogger(SVGLogger.class);
	
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private SVGG topG;

	public SVGLogger() {
		init();
	}
	
	private void init() {
		this.topG = new SVGG();
		topG.setClassName("extractedSVG");
		topG.setDate("creationDate", new JodaDate());
	}
}

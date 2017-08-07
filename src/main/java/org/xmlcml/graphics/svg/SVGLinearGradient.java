package org.xmlcml.graphics.svg;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/** only used for display, mainly debugging.
 * 
 * @author pm286
 *
 */
public class SVGLinearGradient extends AbstractSVGGradient {
	private static final Logger LOG = Logger.getLogger(SVGLinearGradient.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public final static String TAG = "linearGradient";

	public SVGLinearGradient() {
		super(TAG);
	}
}

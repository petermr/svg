package org.xmlcml.graphics.svg;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class AbstractSVGGradient extends SVGElement {
	private static final Logger LOG = Logger.getLogger(AbstractSVGGradient.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public AbstractSVGGradient(String tag) {
		super(tag);
	}

	
}

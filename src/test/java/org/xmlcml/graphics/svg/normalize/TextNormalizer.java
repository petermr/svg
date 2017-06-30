package org.xmlcml.graphics.svg.normalize;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGText;

/** normalizes (compacts) texts.
 * will extract common y coordinates  and create x-array
 * 
 * creates chunks whenever style attribute changes
 * 
 * @author pm286
 *
 */
public class TextNormalizer {
	private static final Logger LOG = Logger.getLogger(TextNormalizer.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
}

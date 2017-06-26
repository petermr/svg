package org.xmlcml.graphics.svg.store;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.plot.PlotBox;

public class SVGStoreTest {
	private static final Logger LOG = Logger.getLogger(SVGStoreTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testSingleFigure() throws IOException {
		String fileRoot = "10.1186_s12885-016-2685-3_page7";
		SVGStore store = new SVGStore();
		File inputSVGFile = new File(Fixtures.FIGURE_DIR, fileRoot+".svg");
		LOG.debug("reading: "+inputSVGFile);
		store.readGraphicsComponents(inputSVGFile);
	}

}

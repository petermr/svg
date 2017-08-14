package org.xmlcml.graphics.svg.cache;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.cache.ComponentCache;

import junit.framework.Assert;

public class SVGCacheTest {
	private static final Logger LOG = Logger.getLogger(SVGCacheTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testSingleFigure() throws IOException {
		String fileRoot = "10.1186_s12885-016-2685-3_page7";
		ComponentCache cache = new ComponentCache();
		File inputSVGFile = new File(Fixtures.FIGURE_DIR, fileRoot+".svg");
		cache.readGraphicsComponents(inputSVGFile);
		List<Real2Range> boundingBoxes = cache.getMergedBoundingBoxes(2.0);
		displayBoxes(new File("target/plot/debug"), cache, fileRoot, boundingBoxes, "pink");
		
	}

	@Test
	@Ignore // too many for testing
	public void testMultipleFigure() throws IOException {
		String fileRoot = "fulltext-page4";
		ComponentCache cache = new ComponentCache();
		File inputSVGFile = new File(Fixtures.FIGURE_DIR, fileRoot+".svg");
//		store.setPlotDebug(new File("target/plots/", name+"/"));
		cache.readGraphicsComponents(inputSVGFile);
		List<Real2Range> boundingBoxes = cache.getMergedBoundingBoxes(2.0);
		displayBoxes(new File("target/plot/debug"), cache, fileRoot, boundingBoxes, "pink");
	}
	
	@Test
	@Ignore // too many for testing
	public void testManyPapers() throws IOException {
		File[] files = Fixtures.FIGURE_DIR.listFiles();
		Assert.assertNotNull("files in "+Fixtures.FIGURE_DIR, files);
		for (File file : files) {
			if (file.toString().endsWith(".svg")) {
				ComponentCache cache = new ComponentCache();
				String root = FilenameUtils.getBaseName(file.toString());
				cache.readGraphicsComponents(file);
				List<Real2Range> boundingBoxes = cache.getMergedBoundingBoxes(2.0);
				displayBoxes(new File("target/plot/debug/"), cache, root, boundingBoxes, "pink");

			}
		}
	}

	@Test
	@Ignore // too many for tests
	public void testImages() throws IOException {
		File[] files = new File(Fixtures.IMAGE_DIR, "10.2147_OTT.S94348").listFiles();
		Assert.assertNotNull("files in "+Fixtures.IMAGE_DIR, files);
		for (File file : files) {
			if (file.toString().endsWith(".svg")) {
				ComponentCache cache = new ComponentCache();
				String root = FilenameUtils.getBaseName(file.toString());
				cache.readGraphicsComponents(file);
				List<Real2Range> imageBoxes = cache.getImageBoxes();
				displayBoxes(new File("target/plot/debug/images/"), cache, root, imageBoxes, "mauve");
			}
		}
	}
	
	@Test
	@Ignore // uncomment to re-test papers 
	public void testPapers() throws IOException {
		File[] dirs = Fixtures.TABLE_DIR.listFiles();
		for (File dir : dirs) {
			String base = FilenameUtils.getName(dir.toString());
			File svgDir = new File(dir, "svg");
			for (File svgFile : svgDir.listFiles()) {
				if (svgFile.toString().endsWith(".svg")) {
					String root = FilenameUtils.getBaseName(svgFile.toString());
					ComponentCache cache = new ComponentCache();
					cache.readGraphicsComponents(svgFile);
					List<Real2Range> boundingBoxes = cache.getMergedBoundingBoxes(2.0);
					if (boundingBoxes.size() > 0) {
						displayBoxes(new File("target/plot/debug/table/"+base+"/"), cache, root, boundingBoxes, "green");
					}
				}
			}
		}
	}
	
	@Test
	public void testShadowedPaths() throws Exception {
		File file = new File(Fixtures.PLOT_DIR, "tilburgVectors/10.1186_s13027-016-0058-9_1.svg");
		ComponentCache cache = new ComponentCache();
		String root = FilenameUtils.getBaseName(file.toString());
		cache.readGraphicsComponents(file);
//		displayBoxes(new File("target/plot/debug/images/"), store, root, imageBoxes, "mauve");
	}

	// =====================
	
	private void displayBoxes(File file, ComponentCache cache, String root, List<Real2Range> boundingBoxes, String fill) {
		SVGG g = (SVGG) cache.createSVGElement();
		for (Real2Range bbox : boundingBoxes) {
			SVGRect rect = SVGRect.createFromReal2Range(bbox);
			if (rect != null) {
				rect.setFill(fill);
				rect.setOpacity(0.3);
				g.appendChild(rect);
			}
		}
		File filex = new File(file, root+".boxes.svg");
		SVGSVG.wrapAndWriteAsSVG(g, filex);
	}

}

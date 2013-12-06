package org.xmlcml.graphics.svg.builder;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.*;

import java.io.File;
import java.util.List;

/**
 * @author pm286
 */
public class SimpleBuilderTest {

	private final static Logger LOG = Logger.getLogger(SimpleBuilderTest.class);
	public static final Angle MAX_ANGLE = new Angle(0.12, Units.RADIANS);
	public static final Double MAX_WIDTH = 2.0;

	File IMAGE_2_10_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.10.svg");
	File IMAGE_2_11_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.11.svg");
	File IMAGE_2_13_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.13.svg");
	File IMAGE_2_15_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.15.svg");
	File IMAGE_2_16_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.16.svg");
	File IMAGE_2_18_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.18.svg");
	File IMAGE_2_23_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.23.svg");
	File IMAGE_2_25_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.25.svg");
	File IMAGE_5_11_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.11.svg");
	File IMAGE_5_12_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.12.svg");
	File IMAGE_5_13_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.13.svg");
	File IMAGE_5_14_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.14.svg");
	File IMAGE_02_00100_6_5_SVG = new File(Fixtures.MOLECULES_DIR, "02.00100.g.6.5.svg");
	File SMALL_TEST_1 = new File(Fixtures.MOLECULES_DIR, "smalltest1.svg");
	File SMALL_TEST_2 = new File(Fixtures.MOLECULES_DIR, "smalltest2.svg");
	File SMALL_TEST_3 = new File(Fixtures.MOLECULES_DIR, "smalltest3.svg");
	File SMALL_TEST_4 = new File(Fixtures.MOLECULES_DIR, "smalltest4.svg");
	File SMALL_TEST_5 = new File(Fixtures.MOLECULES_DIR, "smalltest5.svg");

	@Test
	public void test2_10() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_10_SVG));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 44, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("texts", 19, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 20, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 2, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 0, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 7, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 48, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 56, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 24, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}
	
	@Test
	public void test2_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 6, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("texts", 6, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 4, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 0, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 0, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 1, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 11, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 8, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 6, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}
	
	@Test
	public void test2_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_13_SVG));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("texts", 6, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 7, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 0, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 0, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 3, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 16, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 17, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 10, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}
	
	@Test
	public void test2_15() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_15_SVG));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 40, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("texts", 24, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 21, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 1, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 2, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 7, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 55, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 59, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 24, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}
	
	@Test
	public void test2_16() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_16_SVG));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 20, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("texts", 7, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 10, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 0, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 0, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 5, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 22, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 24, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 14, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}
	
	//TODO 2_18
	
	@Test
	public void test2_23() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_23_SVG));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 24, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("texts", 19, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 16, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 0, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 0, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 4, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 39, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 37, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 21, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}
	
	//TODO 2_25
	
	@Test
	public void test5_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_11_SVG));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 46, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("texts", 0, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 28, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 3, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 4, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 4, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 39, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 68, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 23, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}	
	
	@Test
	public void test5_12() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_12_SVG));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 45, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("texts", 0, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 27, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 2, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 4, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 6, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 39, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 67, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 23, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}
	
	@Test
	public void test5_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_13_SVG));
		simpleBuilder.createHigherPrimitives();
		//first 37, 48, 26; second 39, 51, 27
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 86, simpleBuilder.getDerivedPrimitives().getLineList().size());//FIXME lines were missed in the PDF to SVG conversion stage;
		Assert.assertEquals("texts", 0, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 52, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 5, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 8, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 11, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 76, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 130, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 42, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}
	
	@Test
	public void test5_14() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_14_SVG));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 95, simpleBuilder.getDerivedPrimitives().getLineList().size());//FIXME lines were missed in the PDF to SVG conversion stage;
		Assert.assertEquals("texts", 0, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 58, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 5, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 7, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 13, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 83, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 143, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 48, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}
	
	@Test
	public void test6_5() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_02_00100_6_5_SVG));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 28, simpleBuilder.getDerivedPrimitives().getLineList().size());//FIXME lines were missed in the PDF to SVG conversion stage;
		Assert.assertEquals("texts", 43, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 16, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 0, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 0, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 6, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 65, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 41, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 19, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}
	
	@Test
	@Ignore
	public void testJunctionMerging2_25() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_25_SVG));
		simpleBuilder.createHigherPrimitives();
		//no hatches; should be 25, 32, 20; l of Cl not circular enough, =O too near other bonds
		Assert.assertEquals("lines", 22, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
		Assert.assertEquals("lines", 15, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("lines", 5, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}
	
	@Test
	@Ignore
	public void testWedges() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_18_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 21, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	
	@Test
	@Ignore
	public void testHard() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_25_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 25, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
		
	@Test
	public void testWithElementPNG5_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 46, simpleBuilder.getDerivedPrimitives().getLineList().size());//FIXME should be 47
	}
	@Test
	public void testWithElementPNG5_12() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_12_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 45, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	
	@Test
	public void testTramLinesG2_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//List<SVGLine> lineList = simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//TramLineManager tramLineManager = new TramLineManager();
		//List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		//Assert.assertEquals("tramLines", 1, tramLineList.size());
		Assert.assertEquals("tramLines", 1, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}
	
	@Test
	public void testTramLines() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_13_SVG));
		//Assert.assertNull("singleLines", simpleBuilder.getDerivedPrimitives().getLineList());
		//Assert.assertNull("explicitLines", simpleBuilder.getRawPrimitives().getLineList());
		//Assert.assertNull("implicitLines", simpleBuilder.getDerivedPrimitives().getLineList());
		Assert.assertNull("derivedPrimitives", simpleBuilder.getDerivedPrimitives());
		simpleBuilder.createHigherPrimitives();
		//Assert.assertNull("paths", simpleBuilder.getExplicitPathList());
		//simpleBuilder.createRawAndDerivedLines();
		//Assert.assertEquals("singleLines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//Assert.assertEquals("explicitLines", null, simpleBuilder.getRawPrimitives().getLineList().size());
		//Assert.assertEquals("implicitLines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
		// creating lines has removed paths
		//Assert.assertEquals("paths", 0, simpleBuilder.getCurrentPathList().size());
		//List<TramLine> tramLineList = simpleBuilder.createTramLineList();
		//Assert.assertEquals("implicitLines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("lLines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//TramLine creation removes single lines
		//Assert.assertEquals("tramLines", 3, tramLineList.size());
		Assert.assertEquals("tramLines", 3, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("singleLines", 7, simpleBuilder.getHigherPrimitives().getLineList().size());
		//Assert.assertEquals("explicitLines", 0, simpleBuilder.getRawPrimitives().getLineList().size());

	}
	@Test
	public void testTramLines2_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		Assert.assertEquals("lines", 4, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("tramLines", 1, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getRawPrimitives().getLineList().size());
	}
	@Test
	public void testTramLines2_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_13_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		Assert.assertEquals("tramLines", 3, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("singleLines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	@Test
	public void testTramLines2_18() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_18_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawPrimitives().getLineList().size());
		Assert.assertEquals("tramLines", 5, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("singleLines", 21, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	
	@Test
	public void testTramLines2_23() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_23_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		Assert.assertEquals("tramLines", 4, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getRawPrimitives().getLineList().size());
		Assert.assertEquals("paths", 16, simpleBuilder.getHigherPrimitives().getLineList().size());
	}
	
	@Test
	@Ignore
	public void testTramLines2_25() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_25_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		Assert.assertEquals("tramLines", 5, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawPrimitives().getLineList().size());
		Assert.assertEquals("paths", 15, simpleBuilder.getHigherPrimitives().getLineList().size());
	}
	
	@Test
	public void testTramLines5_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("tramLines", 4, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getRawPrimitives().getLineList().size());
		Assert.assertEquals("paths", 28, simpleBuilder.getHigherPrimitives().getLineList().size());//FIXME should be 39
	}
	
	@Test
	@Ignore
	public void testWedgeHash() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_18_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		// this contained a rect translated to a line
		//Assert.assertEquals("explicitLines", 1, simpleBuilder.getRawPrimitives().getLineList().size());
		//Assert.assertEquals("implicitLines", 21, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("singleLines", 21, simpleBuilder.getHigherPrimitives().getLineList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getCurrentPathList().size());
		// polygon and 5 circles
		//Assert.assertEquals("shapes", 6, simpleBuilder.getCurrentShapeList().size());
		// creating lines has removed paths
		//Assert.assertEquals("paths", 0, simpleBuilder.getCurrentPathList().size());
		//List<TramLine> tramLineList = simpleBuilder.createTramLineList();
		Assert.assertEquals("implicitLines", 21, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//TramLine creation removes single lines
		Assert.assertEquals("singleLines", 12, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("tramLines", 5, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("explicitLines", 1, simpleBuilder.getRawPrimitives().getLineList().size());

	}
		
	@Test
	public void testJunctionWithTram() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//List<SVGLine> lineList = simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//TramLineManager tramLineManager = new TramLineManager();
		//List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		//lineList = tramLineManager.removeUsedTramLinePrimitives(lineList);
		//Assert.assertEquals("tramLines", 1, tramLineList.size());
		Assert.assertEquals("tramLines", 1, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		//Assert.assertEquals("no tram", 4, joinableList.size());
		//joinableList.add(tramLineList.get(0));
		//Assert.assertEquals("joinable", 5, joinableList.size());
		//List<Junction> junctionList = simpleBuilder.createRawJunctionList();
		//Assert.assertEquals("junction", 7, junctionList.size());
		Assert.assertEquals("junction", 8, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
	}

	@Test
	public void testJunctionWithTramAndText() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//List<SVGLine> lineList = simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//TramLineManager tramLineManager = new TramLineManager();
		//List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		//lineList = tramLineManager.removeUsedTramLinePrimitives(lineList);
		//List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		//joinableList.addAll(tramLineList);
		//List<SVGText> textList = simpleBuilder.createRawTextList();
		//for (SVGText svgText : textList) {
			//joinableList.add(new JoinableText(svgText));
		//}
		Assert.assertEquals("text", 11, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		//List<Junction> junctionList = simpleBuilder.createRawJunctionList();
		//for (Junction junction : junctionList) {
		for (Junction junction : simpleBuilder.getHigherPrimitives().getRawJunctionList()) {
			LOG.trace(junction);
		}
		Assert.assertEquals("junction", 8, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
	}
	
	@Test
	public void testSmall1() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(SMALL_TEST_1));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
	}
	
	@Test
	public void testSmall2() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(SMALL_TEST_2));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
	}
	
	@Test
	public void testSmall3() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(SMALL_TEST_3));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 8, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("lines", 3, simpleBuilder.getHigherPrimitives().getLineList().size());
	}
	
	@Test
	public void testSmall4() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(SMALL_TEST_4));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 8, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("lines", 3, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("junctions", 7, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("junctions", 2, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}
	
	@Test
	public void testSmall5() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(SMALL_TEST_5));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("derived lines", 5, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("texts", 0, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("higher lines", 5, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("hatches", 0, simpleBuilder.getHigherPrimitives().getHatchList().size());
		Assert.assertEquals("polygons", 1, simpleBuilder.getDerivedPrimitives().getPolygonList().size());
		Assert.assertEquals("tram lines", 0, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("joinables", 6, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		Assert.assertEquals("raw junctions", 9, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
		Assert.assertEquals("merged junctions", 2, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}

	private void drawFromSimpleBuilder(SimpleBuilder simpleBuilder) {
		SVGG out = new SVGG();
		SVGG circles = new SVGG();
		out.appendChild(circles);
		for (Junction j : simpleBuilder.higherPrimitives.getRawJunctionList()) {
			Real2 coords = (j.getCoordinates() == null ? new Real2(0, 0) : j.getCoordinates());
			SVGCircle c = new SVGCircle(coords, 1.2);
			c.setFill("#FF9999");
			c.setOpacity(0.7);
			c.setStrokeWidth(0.0);
			circles.appendChild(c);
			SVGText t = new SVGText(coords.plus(new Real2(1.5, Math.random() * 6)), j.getId());
			circles.appendChild(t);
			for (Joinable joinable : j.getJoinableList()) {
				SVGLine line = new SVGLine(coords, (joinable.getBackbone() != null ? joinable.getBackbone().getMidPoint() : joinable.getPoint()));
				line.setStrokeWidth(0.05);
				circles.appendChild(line);
			}
		}
		for (SVGText t : simpleBuilder.getDerivedPrimitives().getTextList()) {
			SVGText o = (SVGText) t.copy();
			out.appendChild(o);
		}
		for (SVGLine l : simpleBuilder.getDerivedPrimitives().getLineList()) {
			SVGLine o = (SVGLine) l.copy();
			o.setStrokeWidth(0.4);
			out.appendChild(o);
		}
		for (SVGPolygon p : simpleBuilder.getDerivedPrimitives().getPolygonList()) {
			SVGPolygon o = (SVGPolygon) p.copy();
			o.setStrokeWidth(0.4);
			out.appendChild(o);
		}
		for (Joinable j : simpleBuilder.getHigherPrimitives().getJoinableList()) {
			for (JoinPoint p : j.getJoinPointList().getJoinPoints()) {
				Real2 coords = (p.getPoint() == null ? new Real2(0, 0) : p.getPoint());
				SVGCircle c = new SVGCircle(coords, 0.6);
				c.setFill("#9999FF");
				c.setOpacity(0.7);
				c.setStrokeWidth(0.0);
				circles.appendChild(c);
				//SVGText t = new SVGText(coords.plus(new Real2(1.5, Math.random() * 6)), j.getId());
				//out.appendChild(t);
			}
		}
		SVGSVG.wrapAndWriteAsSVG(out, new File("target/andy.svg"));
	}

}

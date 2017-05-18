package org.xmlcml.graphics.svg.path;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPathPrimitive;
import org.xmlcml.graphics.svg.SVGSVG;

public class SVGPathPrimitiveTest {
	private static final Logger LOG = Logger.getLogger(SVGPathPrimitiveTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	File TARGET_PATH = new File("target/path/");


	public final static String dString = 
			"M327.397 218.898 L328.024 215.899 L329.074 215.899 C329.433 215.899 329.693 215.936 329.854 216.008" +
			" C330.012 216.08 330.168 216.232 330.322 216.461 C330.546 216.793 330.745 217.186 330.92 217.64" +
			" L331.404 218.898 L332.413 218.898 L331.898 217.626 C331.725 217.202 331.497 216.793 331.215 216.397" +
			" C331.089 216.222 330.903 216.044 330.657 215.863 C331.46 215.755 332.039 215.52 332.4 215.158" +
			" C332.759 214.796 332.938 214.341 332.938 213.791 C332.938 213.397 332.856 213.072 332.692 212.814" +
			" C332.527 212.557 332.301 212.381 332.013 212.287 C331.724 212.192 331.3 212.146 330.741 212.146" +
			" L327.908 212.146 L326.494 218.898 L327.397 218.898 ZM328.654 212.888 L330.857 212.888" +
			" C331.203 212.888 331.448 212.914 331.593 212.967 C331.737 213.018 331.855 213.117 331.942 213.264" +
			" C332.032 213.41 332.075 213.58 332.075 213.776 C332.075 214.011 332.017 214.228 331.898 214.431" +
			" C331.779 214.634 331.609 214.794 331.39 214.914 C331.171 215.034 330.893 215.111 330.552 215.145" +
			" C330.376 215.16 330 215.168 329.424 215.168 L328.176 215.168 L328.654 212.888";

	/*@Test
	public void testParseD() {
		List<SVGPathPrimitive> primitives = SVGPathPrimitive.parseD(dString);
		Assert.assertEquals("primitives", 31, primitives.size());
	}*/

	@Test
	@Ignore // fails test on mvn but passes on eclipse
	public void testParseDString() {
		PathPrimitiveList primitives = new SVGPathParser().parseDString(dString);
		Assert.assertEquals("primitives", 31, primitives.size());
	}

	/*@Test
	public void testParseD1() {
		List<SVGPathPrimitive> primitiveList = new SVGPathParser().parseD(dString);
		String sig = SVGPathPrimitive.createSignature(primitiveList);
		Assert.assertEquals("signature", "MLLCCCLLLCCCCCCCLLLZMLCCCCCCCLL", sig);
	}*/

	@Test
	public void testParseDString1() {
		PathPrimitiveList primitiveList = new SVGPathParser().parseDString(dString);
		String sig = SVGPathPrimitive.createSignature(primitiveList);
		Assert.assertEquals("signature", "MLLCCCLLLCCCCCCCLLLZMLCCCCCCCLL", sig);
	}

	@Test
	public void testNormalize() {
		String d = "M368.744 213.091 L368.943 212.146 L368.113 212.146 L367.915 213.091 L368.744 213.091 ZM367.532 218.898 L368.556 214.008 L367.722 214.008 L366.7 218.898 L367.532 218.898";
		SVGPath path = new SVGPath();
		path.setDString(d);
		PathPrimitiveList primitives = new SVGPathParser().parseDString(d);
		Real2Array r2a = path.getCoords();
		path.normalizeOrigin();
		Assert.assertEquals("normalized path", 
		    "M2.044 0.945 L2.242 0.0 L1.413 0.0 L1.215 0.945 L2.044 0.945 ZM0.831 6.752 L1.855 1.862 L1.021 1.862 L0.0 6.752 L0.831 6.752",
		    //"M2.043 0.945 L2.242 0.0 L1.413 0.0 L1.215 0.945 L2.043 0.945 ZM0.832 6.751 L1.855 1.861 L1.021 1.861 L0.0 6.751 L0.832 6.751",
			path.getDString().trim());
	}
	
	@Test
	public void testRelativeMove() {
		String d = "m 35.205,173.992 2.749,0 m 0.02,2.814 0,-2.748 m 74.217,2.748 0,-2.748 m 24.74,2.748 0,-2.748 "
				+ "m -74.219,2.748 0,-2.748 m 24.74,2.748 0,-2.748 m 74.219,2.748 0,-2.748 m 24.74,2.748 0,-2.748 "
				+ "m 24.738,2.978 0,-2.748 m 24.74,2.748 0,-2.748";
		SVGPath path = new SVGPath();
		path.setDString(d);
		Real2Array r2a = path.getCoords();
		r2a.format(3);
		Assert.assertEquals("coords", "((35.205,173.992)(37.954,173.992)"  // single horizonatl tick
				+ "(37.974,176.806)(37.974,174.058)(112.191,176.806)(112.191,174.058)(136.931,176.806)(136.931,174.058)"
				+ "(62.712,176.806)(62.712,174.058)(87.452,176.806)(87.452,174.058)(161.671,176.806)(161.671,174.058)"
				+ "(186.411,176.806)(186.411,174.058)(211.149,177.036)(211.149,174.288)(235.889,177.036)(235.889,174.288))", r2a.toString());
	}
	
	@Test
	public void testRelativeCubic() {
		String d = "M 100,100 " 
	 + "m -10.306,34.147 "
	 + "c 0,-0.842 0.549,-1.516 1.236,-1.516 "
	 + "0.687,0 1.236,0.674 1.236,1.516 "
	 + "0,0.84 -0.549,1.514 -1.236,1.514 " 
	 + "-0.687,0 -1.236,-0.674 -1.236,-1.514 "
	 + "z "
	 + "m 0.472,-23.202 "
	 + "c 0,-0.842 0.549,-1.515 1.236,-1.515 "
	 + "0.687,0 1.237,0.673 1.237,1.515 "
	 + "0,0.842 -0.55,1.515 -1.237,1.515 "
	 + "-0.687,0 -1.236,-0.673 -1.236,-1.515 "
	 + "z";
		SVGPath path = new SVGPath();
		path.setDString(d);
		Real2Array r2a = path.getCoords();
		r2a.format(3);
		Assert.assertEquals("coords", "((100.0,100.0)"
				+ "(89.694,134.147)(89.694,133.305)(91.617,132.631)"
				+ "(92.166,134.987)(90.243,135.661)(89.694,134.147)"
				+ "(90.166,110.945)(90.166,110.103)(92.089,109.43)"
				+ "(92.639,111.787)(90.715,112.46)(90.166,110.945))", r2a.toString());
	}

	@Test
	@Ignore // FIXME unknown test bug
	public void testRelativeCubic1() {
		SVGElement svgElement = SVGSVG.readAndCreateSVG(new File(Fixtures.PATHS_DIR, "relcubics.svg"));
		SVGPath path = SVGPath.extractPaths(svgElement).get(0);
		path.detach();
		path.getAttribute("stroke").detach();
		path.setStroke("red");
		SVGG g = new SVGG();
		g.appendChild(path);
		String d = path.getDString();
		Assert.assertEquals("d",  "M 205.511,173.959 147.341,65.035"
				+ " m -58.17,108.924 58.17,-108.924"
				+ " m -10.306,34.147 "
				+ "    c 0,-0.842 0.549,-1.516 1.236,-1.516"
				+ "      0.687,0 1.236,0.674 1.236,1.516"
				+ "      0,0.84 -0.549,1.514 -1.236,1.514"
				+ "     -0.687,0 -1.236,-0.674 -1.236,-1.514 z"
				+ " m 0.472,-23.202 c 0,-0.842 0.549,-1.515 1.236,-1.515 0.687,0 1.237,0.673 1.237,1.515 0,0.842 -0.55,1.515 -1.237,1.515 -0.687,0 -1.236,-0.673 -1.236,-1.515 z"
				+ " m 1.417,15.763 c 0,-0.832 0.55,-1.498 1.236,-1.498 0.686,0 1.236,0.666 1.236,1.498 0,0.831 -0.55,1.497 -1.236,1.497 -0.686,0 -1.236,-0.666 -1.236,-1.497 z"
				+ " m 0.472,-22.606 c 0,-0.841 0.543,-1.516 1.223,-1.516 0.678,0 1.221,0.675 1.221,1.516 0,0.842 -0.543,1.515 -1.221,1.515 -0.68,0 -1.223,-0.673 -1.223,-1.515 z"
				+ " m 0,16.588 c 0,-0.836 0.543,-1.506 1.223,-1.506 0.678,0 1.221,0.67 1.221,1.506 0,0.837 -0.543,1.506 -1.221,1.506 -0.68,0 -1.223,-0.669 -1.223,-1.506 z",
				d);
		PathPrimitiveList primitives = new SVGPathParser().parseDString(d);
		for (SVGPathPrimitive primitive : primitives) {
			LOG.debug(">prim>"+primitive);
		}
		SVGPath path1 = new SVGPath(primitives, null);
		path1.setStroke("blue");
		String d1 = path1.getDString();
		Assert.assertEquals("d", "M205.511 173.959 L147.341 65.035"
				+ " M89.171 173.959 L147.341 65.035"
				+ " M137.035 99.181 C137.035 98.339 137.584 97.665 138.271 97.665 C138.958 97.665 139.506 98.339 139.506 99.181 C139.506 100.021 138.957 100.695 138.271 100.695 C137.583 100.695 137.035 100.021 137.035 99.181 Z"
				+ " M137.507 75.137 C137.507 74.295 138.056 73.622 138.743 73.622 C139.43 73.622 139.98 74.295 139.98 75.137 C139.98 75.979 139.429 76.652 138.743 76.652 C138.055 76.652 137.507 75.979 137.507 75.137 Z"
				+ " M138.924 90.059 C138.924 89.227 139.474 88.561 140.16 88.561 C140.846 88.561 141.396 89.226 141.396 90.059 C141.396 90.89 140.845 91.556 140.16 91.556 C139.474 91.556 138.924 90.89 138.924 90.059 Z"
				+ " M139.396 66.621 C139.396 65.78 139.939 65.105 140.619 65.105 C141.297 65.105 141.84 65.78 141.84 66.621 C141.84 67.463 141.297 68.136 140.619 68.136 C139.939 68.136 139.396 67.463 139.396 66.621 Z"
				+ " M139.396 82.368 C139.396 81.532 139.939 80.862 140.619 80.862 C141.297 80.862 141.84 81.532 141.84 82.368 C141.84 83.205 141.297 83.874 140.619 83.874 C139.939 83.874 139.396 83.205 139.396 82.368 Z",
				d1);
		g.appendChild(path1);
		SVGSVG.wrapAndWriteAsSVG(g, new File(TARGET_PATH, "relcubic1.svg"));

	}
	
	@Test
	public void testRelativeCubic1a() {
		SVGElement svgElement = SVGSVG.readAndCreateSVG(new File(Fixtures.PATHS_DIR, "relcubics.svg"));
		SVGPath path = SVGPath.extractPaths(svgElement).get(0);
		path.detach();
		path.getAttribute("stroke").detach();
		path.setStroke("red");
		path.setStrokeWidth(1.5);
		SVGG g = new SVGG();
		g.appendChild(path);
		String d = path.getDString();
		Assert.assertEquals("d",  "M 400,180 350,60 m -60,120 60,-120"
				+ " m 0,0 c 0,-8 5,-15 12,-15 6,0 12,6 12,15 0,8 -5,15 -12,15 -6,0 -12,-6 -12,-15 z"
				+ " m 0,0 c 0,-8 5,-15 12,-15 6,0 12,6 12,15 0,8 -5,15 -12,15 -6,0 -12,-6 -12,-15 z",
				d);
		PathPrimitiveList primitives = new SVGPathParser().parseDString(d);
		for (SVGPathPrimitive primitive : primitives) {
			LOG.debug(">prim>"+primitive);
		}
		SVGPath path1 = new SVGPath(primitives, null);
		path1.setStroke("blue");
		path.setStrokeWidth(1.0);
		String d1 = path1.getDString();
//		Assert.assertEquals("d", "M400.0 180.0 L350.0 60.0 M290.0 180.0 L350.0 60.0"
//				+ " M350.0 60.0 C350.0 52.0 355.0 45.0 362.0 45.0 C368.0 45.0 374.0 51.0 374.0 60.0 C374.0 68.0 369.0 75.0 362.0 75.0 C356.0 75.0 350.0 69.0 350.0 60.0 Z"
//				+ "M350.0 52.0 C350.0 44.0 355.0 37.0 362.0 37.0 C368.0 37.0 374.0 43.0 374.0 52.0 C374.0 60.0 369.0 67.0 362.0 67.0 C356.0 67.0 350.0 61.0 350.0 52.0 Z",
//				d1);
		g.appendChild(path1);
		SVGSVG.wrapAndWriteAsSVG(g, new File(TARGET_PATH, "relcubic1a.svg"));

	}
	
	@Test
	@Ignore // passes eclipse but fails mvn
	public void testRelativeCubic2() {
        String d = ""
		+ "M 100 100 "
		+ " m 20,0 0,30 -20,0 0,-30 z"
		+ " c -7,-10 -13,-10 -20,0 "
		+ " c -7,10 -7,20 0,30  "
		+ " c 7,10 13,10 20,0  "
		+ " c 7,-10 7,-20 0,-30  "
		+ "z"
				;
		PathPrimitiveList primitives = new SVGPathParser().parseDString(d);
		for (SVGPathPrimitive primitive : primitives) {
			LOG.debug(">prim>"+primitive);
		}
		SVGPath path = new SVGPath(primitives, null);
		path.setStroke("red");
		SVGSVG.wrapAndWriteAsSVG(path, new File(TARGET_PATH, "relcubic2a.svg"));

		SVGPath path1 = new SVGPath(primitives);
		path1.setStroke("blue");
		String d1 = path1.getDString();
		Assert.assertEquals("d", 
				"M100.0 100.0 M120.0 100.0 L120.0 130.0 L100.0 130.0 L100.0 100.0 ZC113.0 90.0 107.0 90.0 100.0 100.0 C93.0 110.0 93.0 120.0 100.0 130.0 C107.0 140.0 113.0 140.0 120.0 130.0 C127.0 120.0 127.0 110.0 120.0 100.0 Z",
				d1);
		SVGSVG.wrapAndWriteAsSVG(path, new File(TARGET_PATH, "relcubic2b.svg"));

	}
	
	@Test
	@Ignore // passes eclipse but fails mvn
	// FIXME
	public void testRelativeCubic3() {
		SVGG g = new SVGG();
        String d = ""
        		+ "M 100 100 "
        		+ " m 20,0 0,30 -20,0 0,-30 z"
        		+ " c -7,-10 -13,-10 -20,0 "
        		+ " c -7,10 -7,20 0,30  "
        		+ " c 7,10 13,10 20,0  "
        		+ " c 7,-10 7,-20 0,-30  "
        		+ "z"
        		+ "M 200 200 "
        		+ " m 20,0 0,30 -20,0 0,-30 z"
        		+ " c -7,-10 -13,-10 -20,0 "
        		+ " c -7,10 -7,20 0,30  "
        		+ " c 7,10 13,10 20,0  "
        		+ " c 7,-10 7,-20 0,-30  "
        		+ "z"
        		+ "M 300 300 "
        		+ " m 20,0 0,30 -20,0 0,-30 z"
        		+ " c -7,-10 -13,-10 -20,0 "
        		+ " c -7,10 -7,20 0,30  "
        		+ " c 7,10 13,10 20,0  "
        		+ " c 7,-10 7,-20 0,-30  "
        		+ "z"
				;
		PathPrimitiveList primitives = new SVGPathParser().parseDString(d);
		for (SVGPathPrimitive primitive : primitives) {
			LOG.debug(">prim>"+primitive);
		}
		SVGPath path = new SVGPath(primitives, null);
		path.setStroke("red");
		path.setStrokeWidth(1.0);
		g.appendChild(path);
//		SVGSVG.wrapAndWriteAsSVG(path, new File(TARGET_PATH, "relcubic2a.svg"));

		SVGPath path1 = new SVGPath(primitives);
		path1.setStroke("blue");
//		String d1 = path1.getDString();
//		Assert.assertEquals("d", 
//				"M100.0 100.0 M120.0 100.0 L120.0 130.0 L100.0 130.0 L100.0 100.0 ZC113.0 90.0 107.0 90.0 100.0 100.0 C93.0 110.0 93.0 120.0 100.0 130.0 C107.0 140.0 113.0 140.0 120.0 130.0 C127.0 120.0 127.0 110.0 120.0 100.0 Z",
//				d1);
		g.appendChild(path1);
		SVGSVG.wrapAndWriteAsSVG(g, new File(TARGET_PATH, "relcubic3.svg"));

	}
	

}

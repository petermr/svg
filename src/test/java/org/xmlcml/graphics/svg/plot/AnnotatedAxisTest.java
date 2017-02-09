package org.xmlcml.graphics.svg.plot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.SVGUtil;
import org.xmlcml.graphics.svg.plot.AnnotatedAxis;
import org.xmlcml.graphics.svg.plot.AxisFactory;

import junit.framework.Assert;

public class AnnotatedAxisTest {

	private static final Logger LOG = Logger.getLogger(AnnotatedAxisTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	@Test
	public void testHorizontalAxis() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.PLOT_DIR, "horizontalAxis.svg")));
		List<SVGPath> pathList = SVGPath.extractPaths(svgElement);
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svgElement);
		AxisFactory axisFactory = new AxisFactory();
		axisFactory.setTextsAndPaths(textList, pathList);
		AnnotatedAxis axis = axisFactory.getAxis();
		Assert.assertEquals("dir: HORIZONTAL; range: (340.174,544.017)\n"
				+ "majorTicks: (340.174,369.197,398.357,427.516,456.676,485.836,514.857,544.017)\n"
				+ "minorTicks: (345.979,351.783,357.588,363.392,375.139,380.943,386.748,392.552,404.16,410.103,415.908,421.712,433.32,439.125,445.068,450.871,462.48,468.285,474.089,479.894,491.64,497.445,503.249,509.054,520.799,526.605,532.409,538.213)\n"
				+ "tickValues: (0.0,0.5,1.0,1.5,2.0,2.5,3.0,3.5)\n"
				+ "tickValuePositions: (340.174,369.197,398.357,427.516,456.676,485.836,514.857,544.017)\n", axis.toString());
	}
	
	@Test
	public void testVerticalAxis() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.PLOT_DIR, "verticalAxis.svg")));
		List<SVGPath> pathList = SVGPath.extractPaths(svgElement);
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svgElement);
		AxisFactory axisFactory = new AxisFactory();
		axisFactory.setTextsAndPaths(textList, pathList);
		AnnotatedAxis axis = axisFactory.getAxis();
		Assert.assertEquals("dir: VERTICAL; range: null\n"
				+ "majorTicks: (253.945,219.554,185.026,150.498,116.108,81.58,81.58)\n"
				+ "minorTicks: (247.094,240.244,233.256,226.405,212.566,205.716,198.865,191.877,178.176,171.188,164.337,157.486,143.648,136.797,129.81,122.958,109.12,102.27,95.418,88.43,74.73,67.742,60.89,54.04)\n"
				+ "tickValues: (0.0,0.5,1.0,1.5,2.0,2.5)\n"
				+ "tickValuePositions: null\n", axis.toString());
	}
}

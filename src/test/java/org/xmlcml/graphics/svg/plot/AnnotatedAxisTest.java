package org.xmlcml.graphics.svg.plot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.SVGUtil;
import org.xmlcml.graphics.svg.plot.PlotBox.AxisType;

public class AnnotatedAxisTest {

	private static final Logger LOG = Logger.getLogger(AnnotatedAxisTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	@Test
	@Ignore // obsolete 
	public void testHorizontalAxis() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.PLOT_DIR, "horizontalAxis.svg")));
		List<SVGPath> pathList = SVGPath.extractPaths(svgElement);
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svgElement);
		PlotBox plotBox = new PlotBox();
		plotBox.processTextsPathsLines(textList, pathList, null, true);
		AnnotatedAxis axis = plotBox.createAxis(PlotBox.AxisType.BOTTOM);
		axis.calculateAxisPropertiesAndReturnAxis();
		Assert.assertEquals("dir: HORIZONTAL; range: (340.174,544.017)\n"
				+ "majorTicks: (340.174,369.197,398.357,427.516,456.676,485.836,514.857,544.017)\n"
				+ "minorTicks: (345.979,351.783,357.588,363.392,375.139,380.943,386.748,392.552,404.16,410.103,415.908,421.712,433.32,439.125,445.068,450.871,462.48,468.285,474.089,479.894,491.64,497.445,503.249,509.054,520.799,526.605,532.409,538.213)\n"
				+ "tickValues: (0.0,0.5,1.0,1.5,2.0,2.5,3.0,3.5)\n"
				+ "tickValuePositions: (340.174,369.197,398.357,427.516,456.676,485.836,514.857,544.017)\n", axis.toString());
	}
	
	@Test
	@Ignore // obsolete 
	public void testVerticalAxis() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.PLOT_DIR, "verticalAxis.svg")));
		List<SVGPath> pathList = SVGPath.extractPaths(svgElement);
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svgElement);
		PlotBox plotBox = new PlotBox();
		plotBox.processTextsPathsLines(textList, pathList, null, true);
		AnnotatedAxis axis = new AnnotatedAxis(plotBox, AxisType.LEFT);
		axis.calculateAxisPropertiesAndReturnAxis();
		Assert.assertEquals("dir: VERTICAL; range: null\n"
			+ "majorTicks: (253.945,219.554,185.026,150.498,116.108,81.58,81.58)\n"
			+ "minorTicks: "
			+ "(247.094,240.244,233.256,226.405,212.566,205.716,198.865,191.877,178.176,171.188,164.337,"
			+ "157.486,143.648,136.797,129.81,122.958,109.12,102.27,95.418,88.43,74.73,67.742,60.89,54.04)\n"
			+ "tickValues: (0.0,0.5,1.0,1.5,2.0,2.5)\n"
			+ "tickValuePositions: null\n", axis.toString());
	}
	
	@Test
	@Ignore // obsolete 
	public void testFunnelXAxisRaw() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.PLOT_DIR, "bakker2014-page11xaxis.svg")));
		List<SVGPath> pathList = SVGPath.extractPaths(svgElement);
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svgElement);
		PlotBox plotBox = new PlotBox();
		plotBox.processTextsPathsLines(textList, pathList, null, true);
		AnnotatedAxis axis = new AnnotatedAxis(plotBox, AxisType.BOTTOM);
		axis.calculateAxisPropertiesAndReturnAxis();
		LOG.debug("axis> "+axis);
		Assert.assertEquals("dir: HORIZONTAL; range: (340.174,544.017)\n"
			+ "majorTicks: ()\n"
			+ "minorTicks: ()\n"
			+ "tickValues: (-2.0,-1.5,-1.0,-0.5,0.0,0.5,1.0,1.5,2.0)\n"
			+ "tickValuePositions: ()\n", axis.toString());
	}
	
	@Test
	@Ignore // obsolete 
	public void testFunnelXAxisLines() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.PLOT_DIR, "funnelXaxis.svg")));
		List<SVGPath> pathList = SVGPath.extractPaths(svgElement);
		List<SVGLine> lineList = SVGLine.extractSelfAndDescendantLines(svgElement);
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svgElement);
		PlotBox plotBox = new PlotBox();
		boolean useRange = true;
		plotBox.processTextsPathsLines(textList, pathList, lineList, useRange);
		AnnotatedAxis axis = new AnnotatedAxis(plotBox, AxisType.BOTTOM);
		axis.calculateAxisPropertiesAndReturnAxis();
		LOG.debug("axis> "+axis);
		Assert.assertEquals("dir: HORIZONTAL; range: (140.41499,426.01599)\n"
			+ "majorTicks: (140.41499,176.127,211.812,247.524,283.22299,318.92999,354.638,390.327,426.01599)\n"
			+ "minorTicks: ()\n"
			+ "tickValues: (-2.0,-1.5,-1.0,-0.5,0.0,0.5,1.0,1.5,2.0)\n"
			+ "tickValuePositions: (140.41499,176.127,211.812,247.524,283.22299,318.92999,354.638,390.327,426.01599)\n", 
			axis.toString());
		double userCoord = axis.transformScreenToUser(140.41499);
		Assert.assertEquals("axis0", -2.0, userCoord, 0.01);
		userCoord = axis.transformScreenToUser(426.01599);
		Assert.assertEquals("axis1", 2.0, userCoord, 0.01);
		userCoord = axis.transformScreenToUser(283.22299);
		Assert.assertEquals("axis1", 0.0, userCoord, 0.01);

	}
	
	
	
	@Test
	@Ignore // obsolete 
	public void testFunnelYAxisLines() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.PLOT_DIR, "funnelYaxis.svg")));
		List<SVGPath> pathList = SVGPath.extractPaths(svgElement);
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svgElement);
		PlotBox plotBox = new PlotBox();
		plotBox.processTextsPathsLines(textList, pathList, null, true);
		AnnotatedAxis axis = new AnnotatedAxis(plotBox, AxisType.LEFT);
		axis.calculateAxisPropertiesAndReturnAxis();
		LOG.debug("axis> "+axis);
		Assert.assertEquals("dir: VERTICAL; range: (340.174,544.017)\n"
			+ "majorTicks: ()\n"
			+ "minorTicks: ()\n"
			+ "tickValues: (0.0,0.02,0.04,0.06,0.08,0.1,0.12,0.14,0.16,0.18)\n"
			+ "tickValuePositions: ()\n", axis.toString());
	}
	
	@Test
	public void testFunnelXYAxis() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.PLOT_DIR, "bakker2014-page11b.svg")));
		PlotBox plotBox = new PlotBox();
		plotBox.readAndExtractPrimitives(svgElement);
		SVGRect fullLineBbox = plotBox.getFullLineBox();
		fullLineBbox.format(3);
		Assert.assertEquals("full box",  "((140.415,426.016),(483.056,650.628))", fullLineBbox.toString());
		AnnotatedAxis[] axisArray = plotBox.getAxisArray();
		Assert.assertEquals("axes", 4,  axisArray.length);
		AnnotatedAxis axis0 = axisArray[0];
		Assert.assertEquals("axis0", "dir: HORIZONTAL; range: (140.415,426.016)\n"
		+"majorTicks: (176.127,211.812,247.524,283.223,318.93,354.638,390.327)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: (-2.0,-1.5,-1.0,-0.5,0.0,0.5,1.0,1.5,2.0)\n"
		+"tickNumberScreenCoords: (135.925,171.61,207.322,243.007,279.626,315.339,351.024,386.732,422.42)\n",
		axis0.toString());
		Assert.assertEquals("axis0", LineDirection.HORIZONTAL, axis0.getDirection());
		Assert.assertEquals("axis0", "(176.127,211.812,247.524,283.223,318.93,354.638,390.327)", 
				axis0.getMajorTicksScreenCoords().toString());
		Assert.assertEquals("axis0", "(-2.0,-1.5,-1.0,-0.5,0.0,0.5,1.0,1.5,2.0)", 
				axis0.getTickNumberUserCoords().toString());
		Assert.assertEquals("axis0 numberScreen", "(135.925,171.61,207.322,243.007,279.626,315.339,351.024,386.732,422.42)", 
				axis0.getTickNumberScreenCoords().toString());
		
		AnnotatedAxis axis1 = axisArray[1];
		Assert.assertEquals("axis1", "dir: VERTICAL; range: (483.056,650.628)\n"
		+"majorTicks: (510.979,538.913,566.837,594.781,622.704)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: (0.0,0.1,0.2,0.3,0.4,0.5,0.6)\n"
		+"tickNumberScreenCoords: (485.07,513.02,540.954,568.877,596.822,624.745,652.679)\n",
		axis1.toString());
		
		Assert.assertEquals("axis1", LineDirection.VERTICAL, axis1.getDirection());
		Assert.assertEquals("axis1", "(510.979,538.913,566.837,594.781,622.704)", 
				axis1.getMajorTicksScreenCoords().toString());
		Assert.assertEquals("axis1", "(0.0,0.1,0.2,0.3,0.4,0.5,0.6)", 
				axis1.getTickNumberUserCoords().toString());
		Assert.assertEquals("axis1 numberScreen", "(485.07,513.02,540.954,568.877,596.822,624.745,652.679)", 
				axis1.getTickNumberScreenCoords().toString());
		
		AnnotatedAxis axis2 = axisArray[2];
		Assert.assertEquals("axis2", "dir: HORIZONTAL; range: null\n"
		+"majorTicks: null\n"
		+"minorTicks: null\n"
		+"tickNumberUserCoords: null\n"
		+"tickNumberScreenCoords: null\n",
		axis2.toString());
		
		AnnotatedAxis axis3 = axisArray[3];
		Assert.assertEquals("axis2", "dir: VERTICAL; range: null\n"
		+"majorTicks: null\n"
		+"minorTicks: null\n"
		+"tickNumberUserCoords: null\n"
		+"tickNumberScreenCoords: null\n",
		axis3.toString());
		
	}
	
	@Test
	public void testFunnelXYAxisSBarra() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.PLOT_DIR, "sbarraplot.svg")));
		PlotBox plotBox = new PlotBox();
		plotBox.readAndExtractPrimitives(svgElement);
		SVGRect fullLineBbox = plotBox.getFullLineBox();
		fullLineBbox.format(3);
		Assert.assertEquals("full box",  "((38.028,235.888),(65.035,173.959))", fullLineBbox.toString());
		AnnotatedAxis[] axisArray = plotBox.getAxisArray();
		Assert.assertEquals("axes", 4,  axisArray.length);
		AnnotatedAxis axis0 = axisArray[0];
		Assert.assertEquals("axis0", "dir: HORIZONTAL; range: (38.028,235.888)\n"
		+"majorTicks: (37.974,112.191,136.931,62.712,87.452,161.671,186.411,211.149,235.889)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: null\n" // FIXME
		+"tickNumberScreenCoords: (29.212997,53.954002,78.695,103.436,130.679,155.42,180.161,204.90199,229.64301)\n",
		axis0.toString());
		Assert.assertEquals("axis0", LineDirection.HORIZONTAL, axis0.getDirection());
		Assert.assertEquals("axis0", "(37.974,112.191,136.931,62.712,87.452,161.671,186.411,211.149,235.889)", 
				axis0.getMajorTicksScreenCoords().toString());
//		Assert.assertEquals("axis0", "()", 
//				axis0.getTickNumberUserCoords().toString());
//		Assert.assertEquals("axis0 numberScreen", "()", 
//				axis0.getTickNumberScreenCoords().toString());
		
		AnnotatedAxis axis1 = axisArray[1];
		Assert.assertEquals("axis1", "dir: VERTICAL; range: (65.035,173.959)\n"
		+"majorTicks: (65.023,137.663,83.184,101.344,119.504,155.823,173.992)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: (0.0,0.1,0.2,0.3,0.4,0.5,0.6)\n" //FIXME
		+"tickNumberScreenCoords: (67.092003,85.251999,103.412,121.57201,139.731,157.89101,176.05)\n",
		axis1.toString());
		
		Assert.assertEquals("axis1", LineDirection.VERTICAL, axis1.getDirection());
		Assert.assertEquals("axis1", "(65.023,137.663,83.184,101.344,119.504,155.823,173.992)", 
				axis1.getMajorTicksScreenCoords().toString());
//		Assert.assertEquals("axis1", "(0.0,0.1,0.2,0.3,0.4,0.5,0.6)", 
//				axis1.getTickNumberUserCoords().toString());
//		Assert.assertEquals("axis1 numberScreen", "(485.07,513.02,540.954,568.877,596.822,624.745,652.679)", 
//				axis1.getTickNumberScreenCoords().toString());
	}

	

}

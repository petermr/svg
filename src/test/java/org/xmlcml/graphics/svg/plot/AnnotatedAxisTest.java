package org.xmlcml.graphics.svg.plot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGUtil;

@Ignore
public class AnnotatedAxisTest {

	private static final Logger LOG = Logger.getLogger(AnnotatedAxisTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	@Test
	public void testFunnelXYAxis() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.PLOT_DIR, "bakker2014-page11b.svg")));
		PlotBox plotBox = new PlotBox();
		plotBox.createPlot(svgElement);
		SVGSVG.wrapAndWriteAsSVG(plotBox.createSVGElement(), new File("target/plot/bakker.svg"));
		SVGRect fullLineBbox = plotBox.getFullLineBox();
		fullLineBbox.format(3);
		Assert.assertEquals("full box",  "((140.415,426.016),(483.056,650.628))", fullLineBbox.toString());
		AnnotatedAxis[] axisArray = plotBox.getAxisArray();
		Assert.assertEquals("axes", 4,  axisArray.length);
		AnnotatedAxis axis0 = axisArray[0];
		AxisTickBox axisTickBox0 = axis0.getAxisTickBox();
		AxisTextBox axisTextBox0 = axis0.getValueTextBox();
		Assert.assertEquals("axis0", "type: BOTTOM; dir: HORIZONTAL; range: (140.415,426.016)\n"
			+ "axisTickBox: box: extendedBox: ((135.415,431.016),(647.628,660.628)) bbox: ((176.127,390.327),(649.397,651.845))\n"
			+ "DIR: HORIZONTAL; inside/outside/line/extension deltas:3.0, 10.0, 5.0\n"
			+ "HOR: 1; [line: from((140.415,650.628)) to((426.016,650.628)) v((285.601,0.0))]\n"
			+ "VERT: 9; [line: from((140.415,483.056)) to((140.415,650.628)) v((0.0,167.57200000000006)), line: from((176.127,649.397)) to((176.127,651.845)) v((0.0,2.447999999999979)), line: from((211.812,649.397)) to((211.812,651.845)) v((0.0,2.447999999999979)), line: from((247.524,649.397)) to((247.524,651.845)) v((0.0,2.447999999999979)), line: from((283.223,649.397)) to((283.223,651.845)) v((0.0,2.447999999999979)), line: from((318.93,649.397)) to((318.93,651.845)) v((0.0,2.447999999999979)), line: from((354.638,649.397)) to((354.638,651.845)) v((0.0,2.447999999999979)), line: from((390.327,649.397)) to((390.327,651.845)) v((0.0,2.447999999999979)), line: from((426.035,483.056)) to((426.035,650.628)) v((0.0,167.57200000000006))]\n"
			+"majorTicks: (176.127,211.812,247.524,283.223,318.93,354.638,390.327)\n"
			+"minorTicks: ()\n"
			+ "\n"
			+"tickValues: tickNumberUserCoords: (-2.0,-1.5,-1.0,-0.5,0.0,0.5,1.0,1.5,2.0)\n"
			+"tickNumberScreenCoords: (135.925,171.61,207.322,243.007,279.626,315.339,351.024,386.732,422.42)\n"
			+ "\n",
		axis0.toString());
		Assert.assertEquals("axis0", LineDirection.HORIZONTAL, axis0.getLineDirection());
		Assert.assertEquals("axis0", "(176.127,211.812,247.524,283.223,318.93,354.638,390.327)", 
				axisTickBox0.getMajorTicksScreenCoords().toString());
		// the last value is wrong
		Assert.assertEquals("axis0", "(-2.0,-1.5,-1.0,-0.5,0.0,0.5,1.0,1.5,2.0)", 
				axisTextBox0.getTickNumberUserCoords().toString());
		// the last value is wrong
		Assert.assertEquals("axis0 numberScreen", "(135.925,171.61,207.322,243.007,279.626,315.339,351.024,386.732,422.42)", 
				axisTextBox0.getTickNumberScreenCoords().toString());
		
		AnnotatedAxis axis1 = axisArray[1];
		AxisTickBox axisTickBox1 = axis1.getAxisTickBox();
		AxisTextBox axisTextBox1 = axis1.getValueTextBox();
		Assert.assertEquals("axis1", "type: LEFT; dir: VERTICAL; range: (483.056,650.628)\n"
		+"axisTickBox: box: extendedBox: ((130.415,143.415),(478.056,655.628)) bbox: ((139.342,140.398),(510.979,622.704))\n"
		+"DIR: VERTICAL; inside/outside/line/extension deltas:3.0, 10.0, 5.0\n"
		+"HOR: 7; [line: from((140.415,483.03)) to((426.016,483.03)) v((285.601,0.0)), line: from((139.342,510.979)) to((140.398,510.979)) v((1.0559999999999832,0.0)), line: from((139.342,538.913)) to((140.398,538.913)) v((1.0559999999999832,0.0)), line: from((139.342,566.837)) to((140.398,566.837)) v((1.0559999999999832,0.0)), line: from((139.342,594.781)) to((140.398,594.781)) v((1.0559999999999832,0.0)), line: from((139.342,622.704)) to((140.398,622.704)) v((1.0559999999999832,0.0)), line: from((140.415,650.628)) to((426.016,650.628)) v((285.601,0.0))]\n"
		+"VERT: 1; [line: from((140.415,483.056)) to((140.415,650.628)) v((0.0,167.57200000000006))]\n"
		+"majorTicks: (510.979,538.913,566.837,594.781,622.704)\n"
		+"minorTicks: ()\n"
		+ "\n"
		+"tickValues: tickNumberUserCoords: (0.0,0.1,0.2,0.3,0.4,0.5,0.6)\n"
		+"tickNumberScreenCoords: (485.07,513.02,540.954,568.877,596.822,624.745,652.679)\n"
		+ "\n",
		axis1.toString());

		
		Assert.assertEquals("axis1", LineDirection.VERTICAL, axis1.getLineDirection());
		Assert.assertEquals("axis1", "(510.979,538.913,566.837,594.781,622.704)", 
				axisTickBox1.getMajorTicksScreenCoords().toString());
		Assert.assertEquals("axis1", "(0.0,0.1,0.2,0.3,0.4,0.5,0.6)", 
				axisTextBox1.getTickNumberUserCoords().toString());
		Assert.assertEquals("axis1 numberScreen", "(485.07,513.02,540.954,568.877,596.822,624.745,652.679)", 
				axisTextBox1.getTickNumberScreenCoords().toString());
		
		// this grabs too much of the neighbours
		AnnotatedAxis axis2 = axisArray[2];
		Assert.assertEquals("type: TOP; dir: HORIZONTAL; range: (140.415,426.016)\n"
				+ "axisTickBox: box: extendedBox: ((135.415,431.016),(473.056,486.056)) bbox: null\n"
				+ "DIR: HORIZONTAL; inside/outside/line/extension deltas:3.0, 10.0, 5.0\n"
				+ "HOR: 1; [line: from((140.415,483.03)) to((426.016,483.03)) v((285.601,0.0))]\n"
				+ "VERT: 2; [line: from((140.415,483.056)) to((140.415,650.628)) v((0.0,167.57200000000006)), line: from((426.035,483.056)) to((426.035,650.628)) v((0.0,167.57200000000006))]\n"
		+"majorTicks: null\n"
		+"minorTicks: null\n"
		+ "\n"
		// these are wrong
		+"tickValues: tickNumberUserCoords: null\n"
		+"tickNumberScreenCoords: ()\n"
		+ "\n",
		axis2.toString());
		
		AnnotatedAxis axis3 = axisArray[3];
		Assert.assertEquals("type: RIGHT; dir: VERTICAL; range: (483.056,650.628)\n"
				+ "axisTickBox: box: extendedBox: ((423.016,436.016),(478.056,655.628)) bbox: null\n"
				+ "DIR: VERTICAL; inside/outside/line/extension deltas:3.0, 10.0, 5.0\n"
				+ "HOR: 2; [line: from((140.415,483.03)) to((426.016,483.03)) v((285.601,0.0)), line: from((140.415,650.628)) to((426.016,650.628)) v((285.601,0.0))]\n"
				+ "VERT: 1; [line: from((426.035,483.056)) to((426.035,650.628)) v((0.0,167.57200000000006))]\n"
		+"majorTicks: null\n"
		+"minorTicks: null\n"
		+ "\n"
		+"tickValues: tickNumberUserCoords: ()\n"
		+"tickNumberScreenCoords: ()\n"
		+ "\n",
		axis3.toString());
		
	}
	
	@Test
	public void testFunnelFullBoxSBarra() throws FileNotFoundException {
		AnnotatedAxis[] axisArray = getAxisArrayAndTestFullBox("sbarraplot.svg", "((38.028,235.888),(65.035,173.959))");
		AnnotatedAxis axis0 = axisArray[0];
		AxisTickBox axisTickBox0 = axis0.getAxisTickBox();
		Assert.assertEquals("axis0", "dir: HORIZONTAL; range: (38.028,235.888)\n"
		+"majorTicks: (37.974,112.191,136.931,62.712,87.452,161.671,186.411,211.149,235.889)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: null\n" // FIXME
		+"tickNumberScreenCoords: (29.212997,53.954002,78.695,103.436,130.679,155.42,180.161,204.90199,229.64301)\n",
		axis0.toString());
		Assert.assertEquals("axis0", LineDirection.HORIZONTAL, axis0.getLineDirection());
		Assert.assertEquals("axis0", "(37.974,112.191,136.931,62.712,87.452,161.671,186.411,211.149,235.889)", 
				axisTickBox0.getMajorTicksScreenCoords().toString());
		
		AnnotatedAxis axis1 = axisArray[1];
		AxisTickBox axisTickBox1 = axis1.getAxisTickBox();
		Assert.assertEquals("axis1", "dir: VERTICAL; range: (65.035,173.959)\n"
		+"majorTicks: (65.023,137.663,83.184,101.344,119.504,155.823,173.992)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: (0.0,0.1,0.2,0.3,0.4,0.5,0.6)\n" //FIXME
		+"tickNumberScreenCoords: (67.092003,85.251999,103.412,121.57201,139.731,157.89101,176.05)\n",
		axis1.toString());
		
		Assert.assertEquals("axis1", LineDirection.VERTICAL, axis1.getLineDirection());
		Assert.assertEquals("axis1", "(65.023,137.663,83.184,101.344,119.504,155.823,173.992)", 
				axisTickBox1.getMajorTicksScreenCoords().toString());
	}

	@Test
	public void testFunneLCalvinPlot() throws FileNotFoundException {
		AnnotatedAxis[] axisArray = getAxisArrayAndTestFullBox("calvinplot.svg", "((87.096,286.894),(510.354,658.197))");
		AnnotatedAxis axis0 = axisArray[0];
		Assert.assertEquals("axis0", "dir: HORIZONTAL; range: (87.096,286.894)\n"
		+"majorTicks: (87.096,106.955,127.108,146.967,167.119,186.978,206.869,226.99,246.881,267.001,286.894)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: (8.0,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0,1.1,1.2)\n" // FIXME
		+"tickNumberScreenCoords: (79.268,82.229,102.21,122.19,142.169,162.148,182.129,202.109,222.089,244.988,262.048,282.026)\n",
		axis0.toString());
		
		AnnotatedAxis axis1 = axisArray[1];
		Assert.assertEquals("axis1", "dir: VERTICAL; range: (510.354,658.197)\n"
		+"majorTicks: (510.354,526.777,543.2,559.657,576.081,592.504,608.928,625.351,641.774,658.197)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: (0.0,0.02,0.04,0.06,0.08,0.1,0.12,0.14,0.16,0.18,0.2)\n" //FIXME
		+"tickNumberScreenCoords: (511.698,528.125,544.552,560.979,577.406,593.833,610.26,626.687,643.113,659.54,667.408)\n",
		axis1.toString());
	}

	@Test
	public void testFunnelLDongPlot() throws FileNotFoundException {
		AnnotatedAxis[] axisArray = getAxisArrayAndTestFullBox("dongplot.svg", "((56.318,384.031),(416.087,581.038))");
		AnnotatedAxis axis0 = axisArray[0];
		Assert.assertEquals("axis0", "dir: HORIZONTAL; range: (56.318,384.031)\n"
		+"majorTicks: (56.318,138.257,220.196,302.093,384.031)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: null\n" // FIXME
		+"tickNumberScreenCoords: (50.998,52.714,134.653,136.368,217.622,219.338,299.517,301.233,379.741,381.456)\n",
		axis0.toString());
		
		AnnotatedAxis axis1 = axisArray[1];
		Assert.assertEquals("axis1", "dir: VERTICAL; range: (416.087,581.038)\n"
		+"majorTicks: (416.087,449.207,482.281,514.842,547.919)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: (0.0,1.0,2.0,3.0,4.0,NaN,-10.0)\n" //FIXME
		+"tickNumberScreenCoords: (418.315,451.435,484.552,517.071,550.189,589.443,589.443)\n",
		axis1.toString());
	}

	@Test
	public void testFunnelKerrPlot() throws FileNotFoundException {
		AnnotatedAxis[] axisArray = getAxisArrayAndTestFullBox("kerrplot.svg", "((56.318,384.031),(416.087,581.038))");
		Assert.assertNull(axisArray);
//		AnnotatedAxis axis0 = axisArray[0];
//		Assert.assertEquals("axis0", "dir: HORIZONTAL; range: (56.318,384.031)\n"
//		+"majorTicks: (56.318,138.257,220.196,302.093,384.031)\n"
//		+"minorTicks: ()\n"
//		+"tickNumberUserCoords: null\n" // FIXME
//		+"tickNumberScreenCoords: (50.998,52.714,134.653,136.368,217.622,219.338,299.517,301.233,379.741,381.456)\n",
//		axis0.toString());
//		
//		AnnotatedAxis axis1 = axisArray[1];
//		Assert.assertEquals("axis1", "dir: VERTICAL; range: (416.087,581.038)\n"
//		+"majorTicks: (416.087,449.207,482.281,514.842,547.919)\n"
//		+"minorTicks: ()\n"
//		+"tickNumberUserCoords: (0.0,1.0,2.0,3.0,4.0,NaN,-10.0)\n" //FIXME
//		+"tickNumberScreenCoords: (418.315,451.435,484.552,517.071,550.189,589.443,589.443)\n",
//		axis1.toString());
	}

	@Test
	public void testFunnelNairPlot() throws FileNotFoundException {
		AnnotatedAxis[] axisArray = getAxisArrayAndTestFullBox("nairplot.svg", "((167.179,445.533),(501.844,687.931))");
		AnnotatedAxis axis0 = axisArray[0];
		Assert.assertEquals("axis0", "dir: HORIZONTAL; range: (167.179,445.533)\n"
		+"majorTicks: (201.98,236.775,271.57,306.365,341.159,375.959,410.754)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: (0.4,-2.0,-1.5,-1.0,-0.5,0.0,0.5,1.0,1.5,2.0)\n" // FIXME
		+"tickNumberScreenCoords: (158.544,160.119,195.017,229.914,264.811,300.874,335.771,370.669,405.566,440.464)\n",
		axis0.toString());
		
		AnnotatedAxis axis1 = axisArray[1];
		Assert.assertEquals("axis1", "dir: VERTICAL; range: (501.844,687.931)\n"
		+"majorTicks: (548.344,594.878,641.403)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: (0.0,0.1,0.2,0.3,0.4)\n" //FIXME
		+"tickNumberScreenCoords: (503.645,550.169,596.696,643.223,689.747)\n",
		axis1.toString());
		
	}

	@Test
	public void testFunnelRogersPlot() throws FileNotFoundException {
		AnnotatedAxis[] axisArray = getAxisArrayAndTestFullBox("rogersplot.svg", "((167.418,443.782),(121.26,274.235))");
		AnnotatedAxis axis0 = axisArray[0];
		Assert.assertEquals("axis0", "dir: HORIZONTAL; range: (167.418,443.782)\n"
		+"majorTicks: (201.964,167.418,236.509,271.055,305.6,340.147,374.692,409.237,443.782)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: null\n" // FIXME
		+"tickNumberScreenCoords: (158.534,160.479,298.503,438.886,442.926,444.942)\n",
		axis0.toString());
		
		AnnotatedAxis axis1 = axisArray[1];
		Assert.assertEquals("axis1", "dir: VERTICAL; range: (121.26,274.235)\n"
		+"majorTicks: (274.235,255.113,235.991,216.868,197.748,178.625,159.503,140.381,121.26)\n"
		+"minorTicks: ()\n"
		+"tickNumberUserCoords: (NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN,NaN)\n" //FIXME
		+"tickNumberScreenCoords: (123.79,123.79,142.91,142.91,162.03,162.03,181.15,181.15,200.27,200.27,219.39,219.39,238.782,238.782,257.814,257.814,277.43,277.43,277.43)\n",
		axis1.toString());
		
	}

	// ================================================
	
	private static AnnotatedAxis[] getAxisArrayAndTestFullBox(String svgName, String boxCoords) throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.PLOT_DIR, svgName)));
		PlotBox plotBox = new PlotBox();
		plotBox.createPlot(svgElement);
		SVGSVG.wrapAndWriteAsSVG(plotBox.createSVGElement(), new File(new File("target/plot/"), svgName));
		SVGRect fullLineBbox = plotBox.getFullLineBox();
		AnnotatedAxis[] axisArray = null;
		if (fullLineBbox != null) {
			fullLineBbox.format(3);
			Assert.assertEquals("full box",  boxCoords, fullLineBbox.toString());
			axisArray = plotBox.getAxisArray();
		}
		return axisArray;
	}

}

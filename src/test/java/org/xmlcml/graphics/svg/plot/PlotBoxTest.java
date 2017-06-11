package org.xmlcml.graphics.svg.plot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;

/** tests PlotBox class (interprets scatterplots at present).
 * 
 * @author pm286
 *
 */
public class PlotBoxTest {
	private static final Logger LOG = Logger.getLogger(PlotBoxTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testBakker() throws IOException {
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.PLOT_DIR, "bakker2014-page11b.svg");
		plotBox.setCsvOutFile(new File("target/plot/bakker1.csv"));
		plotBox.setSvgOutFile(new File("target/plot/bakker1.svg"));
		plotBox.readAndCreatePlot(new FileInputStream(inputSVGFile));
	}
	
	@Test
	public void testSingle() throws IOException {
//		String fileRoot = "bakker";
//		String fileRoot = "calvin";
		String fileRoot = "kerr";
//		String fileRoot = "dong";
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.PLOT_DIR, fileRoot+"plot.svg");
		LOG.debug("reading: "+inputSVGFile);
		plotBox.readAndCreatePlot(new FileInputStream(inputSVGFile));
		plotBox.writeProcessedSVG(new File("target/plot/"+fileRoot+".svg"));
		plotBox.writeCSV(new File("target/plot/"+fileRoot+".csv"));
	}
	
	@Test
	public void testConvertAllSVG2CSV() throws IOException {
		String[] fileRoots = {
				"bakkerplot",
				"calvinplot",
				"dongplot",
				"kerrplot",
				"nairplot",
				"sbarraplot"
				};
		for (String fileRoot : fileRoots) {
			PlotBox plotBox = new PlotBox();
			File inputSVGFile = new File(Fixtures.PLOT_DIR, fileRoot + ".svg");
			try {
				plotBox.readAndCreatePlot(new FileInputStream(inputSVGFile));
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			plotBox.writeProcessedSVG(new File("target/plot/"+fileRoot+".svg"));
			plotBox.writeCSV(new File("target/plot/"+fileRoot+".csv"));
		}
	}
	
	@Test
	public void testBarPlot() throws IOException {
		String fileRoot = "barchart1.10";
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.PLOT_DIR, fileRoot+".svg");
		plotBox.readAndCreatePlot(new FileInputStream(inputSVGFile));
		plotBox.writeProcessedSVG(new File("target/plot/"+fileRoot+".svg"));
	}
	
	
	@Test
	public void testFunnelPlot1() throws IOException {
		String fileRoot = "6400831a1";
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.PLOT_DIR, fileRoot+".svg");
		plotBox.readAndCreatePlot(new FileInputStream(inputSVGFile));
		plotBox.writeProcessedSVG(new File("target/plot/"+fileRoot+".svg"));
	}
	
	@Test
	public void testScatter13148() throws IOException {
		String fileRoot = "13148-016-0230-5fig2";
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.PLOT_DIR, fileRoot+".svg");
		plotBox.readAndCreatePlot(new FileInputStream(inputSVGFile));
		plotBox.writeProcessedSVG(new File("target/plot/"+fileRoot+".svg"));
	}
	

	
}

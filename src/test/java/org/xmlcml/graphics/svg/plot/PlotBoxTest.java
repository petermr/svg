package org.xmlcml.graphics.svg.plot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;

/** tests PlotBox class (interprets scatterplots at present).
 * 
 * @author pm286
 *
 */
public class PlotBoxTest {

	@Test
	public void testBakker() throws IOException {
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.PLOT_DIR, "bakker2014-page11b.svg");
		plotBox.readAndCreatePlot(new FileInputStream(inputSVGFile));
		plotBox.writeProcessedSVG(new File("target/plot/bakker1.svg"));
		plotBox.writeCSV(new File("target/plot/bakker1.csv"));
	}
	
	@Test
	public void testSingle() throws IOException {
		String fileRoot = "calvin";
//		String fileRoot = "kerr";
//		String fileRoot = "dong";
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.PLOT_DIR, fileRoot+"plot.svg");
		plotBox.readAndCreatePlot(new FileInputStream(inputSVGFile));
		plotBox.writeProcessedSVG(new File("target/plot/"+fileRoot+".svg"));
		plotBox.writeCSV(new File("target/plot/"+fileRoot+".csv"));
	}
	
	@Test
	public void testConvertAllSVG2CSV() throws IOException {
		String[] fileRoots = {
				"bakker",
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
	

	
}

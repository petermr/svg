package org.xmlcml.graphics.svg.bar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.plot.PlotBox;

public class BarTest {
	private static final Logger LOG = Logger.getLogger(BarTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testBar1() throws IOException {
		String fileRoot = "../bar/art%3A10.1186%2Fs13148-016-0230-5/svg/fig3";
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.PLOT_DIR, fileRoot+".svg");
		plotBox.readGraphicsComponents(inputSVGFile);
		plotBox.writeProcessedSVG(new File("target/bar/"+fileRoot+".svg"));
	}

	@Test
	/** several bars
	 * 
	 * @throws IOException
	 */
	public void testBarPlot() throws IOException {
		String fileRoot = "barchart1.10";
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.BAR_DIR, fileRoot+".svg");
		plotBox.readAndCreateCSVPlot(inputSVGFile);
		plotBox.writeProcessedSVG(new File("target/bar/"+fileRoot+".svg"));
	}
	
	@Test
	/** two simple bars
	 * 
	 * @throws IOException
	 */
	public void testBarPlot1() throws IOException {
		String fileRoot = "figure4.2";
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.BAR_DIR, fileRoot+".svg");
		plotBox.readAndCreateCSVPlot(inputSVGFile);
		plotBox.writeProcessedSVG(new File("target/bar/"+fileRoot+".svg"));
	}
	
	

}

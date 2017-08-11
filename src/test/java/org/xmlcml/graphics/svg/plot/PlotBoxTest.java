package org.xmlcml.graphics.svg.plot;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;

import junit.framework.Assert;

/** tests PlotBox class (interprets scatterplots at present).
 * 
 * @author pm286
 *
 */
public class PlotBoxTest {
	private static final String TARGET_PLOT = "target/plot/";
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
		plotBox.readAndCreateCSVPlot(inputSVGFile);
	}
	
	@Test
	public void testSingle() throws IOException {
		String fileRoot = "bakker";
//		String fileRoot = "calvin";
//		String fileRoot = "kerr";
//		String fileRoot = "dong";
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.PLOT_DIR, fileRoot+"plot.svg");
		plotBox.readAndCreateCSVPlot(inputSVGFile);
		plotBox.writeProcessedSVG(new File(TARGET_PLOT+fileRoot+".svg"));
		plotBox.writeCSV(new File(TARGET_PLOT+fileRoot+".csv"));
	}
	
	@Test
	public void testConvertAllSVG2CSV() throws IOException {
		String[] fileRoots = {
				"bakkerplot", // OK
				"calvinplot", // OK
				"dongplot",   // OK
				"kerrplot",   // No fullbox
				"nairplot",   // OK
				"sbarraplot"  // OK
				};
		for (String fileRoot : fileRoots) {
			PlotBox plotBox = new PlotBox();
			File inputSVGFile = new File(Fixtures.PLOT_DIR, fileRoot + ".svg");
			try {
				plotBox.readAndCreateCSVPlot(inputSVGFile);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			plotBox.writeProcessedSVG(new File(TARGET_PLOT+fileRoot+".svg"));
			plotBox.writeCSV(new File(TARGET_PLOT+fileRoot+".csv"));
		}
	}
	
	@Test
	public void test6400831a1() throws IOException {
		String fileRoot = "6400831a1";
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.PLOT_DIR, fileRoot+".svg");
		plotBox.setCsvOutFile(new File(TARGET_PLOT+fileRoot+".csv"));
		plotBox.readAndCreateCSVPlot(inputSVGFile);
		plotBox.writeProcessedSVG(new File(TARGET_PLOT+fileRoot+".svg"));
	}
	
	@Test
	/** outline glyphs not yet processed.
	 * 
	 * @throws IOException
	 */
	public void testScatter13148() throws IOException {
		String fileRoot = "13148-016-0230-5fig2";
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(Fixtures.PLOT_DIR, fileRoot+".svg");
		plotBox.setCsvOutFile(new File(TARGET_PLOT+fileRoot+".csv"));
		try {
			plotBox.readAndCreateCSVPlot(inputSVGFile);
			Assert.fail("should throw exception as cannot yet do glyphs");
		} catch (RuntimeException e) {
			Assert.assertEquals("cannot parse glyphs", "No axial tickbox: BOTTOM", e.getMessage());
		}
		plotBox.writeProcessedSVG(new File(TARGET_PLOT+fileRoot+".svg"));
	}
	
	@Test
	@Ignore // too many for routine tests
	public void testTilburgVectors() throws IOException {
		File TILBURG_DIR = new File(Fixtures.PLOT_DIR, "tilburgVectors");
		String[] roots = {
				"10.1186_s12885-016-2685-3_1",
				"10.1186_s12889-016-3083-0_1",
				"10.1186_s13027-016-0058-9_1",
				"10.1186_s40064-016-3064-x_1",
				"10.1186_s40064-016-3064-x_2",
				"10.1186_s40064-016-3064-x_3",
				"10.1515_med-2016-0052_1",
				"10.1515_med-2016-0052_2",
				"10.1515_med-2016-0052_3",
				"10.1515_med-2016-0099_1",
				"10.1515_med-2016-0099_2",
				"10.1515_med-2016-0099_3",
				"10.1515_med-2016-0099_4",
				"10.1590_S1518-8787.2016050006236_1",
				"10.21053_ceo.2016.9.1.1_1",
				"10.21053_ceo.2016.9.1.1_2",
				"10.21053_ceo.2016.9.1.1_3",
				"10.21053_ceo.2016.9.1.1_4",
				"10.2147_BCTT.S94617_1",
				"10.3349_ymj.2016.57.5.1260_1",
				"10.3349_ymj.2016.57.5.1260_2",
				"10.3390_ijerph13050458_1",
				"10.5114_aoms.2016.61916_1",
				"10.5114_aoms.2016.61916_2",
				"10.5812_ircmj.40061_1",

		};
		for (String root : roots) {
			LOG.info("\n#########################################"+root+"###############################################\n");
			PlotBox plotBox = new PlotBox();
			File inputSVGFile = new File(TILBURG_DIR, root+".svg");
			try {
				plotBox.setCsvOutFile(new File(TARGET_PLOT+root+".csv"));
				plotBox.setSvgOutFile(new File(TARGET_PLOT+root+"_0.svg"));
				plotBox.readAndCreateCSVPlot(inputSVGFile);
//				plotBox.writeProcessedSVG(new File("target/plot/tilburg/"+root+".svg"));
			} catch (Exception e) {
				LOG.error("Exception in "+root, e);
			}

		}
	}
	
	@Test
	public void testTilburgVector0() throws IOException {
		File TILBURG_DIR = new File(Fixtures.PLOT_DIR, "tilburgVectors");
		String root =
// 25 examples				
//				"10.1186_s12885-016-2685-3_1"        // OK
				"10.1186_s12889-016-3083-0_1"        // Y bad phrase
//				"10.1186_s13027-016-0058-9_1"        // OK
//				"10.1186_s40064-016-3064-x_1"        // X NaN Y NaN // TITLE in BOX
//				"10.1186_s40064-016-3064-x_2"        // X NaN Y NaN // TITLE in BOX
//				"10.1186_s40064-016-3064-x_3"        // X NaN Y NaN // TITLE in BOX
//				"10.1515_med-2016-0052_1"            // OK
//				"10.1515_med-2016-0052_2"            // OK
//				"10.1515_med-2016-0052_3"            // OK
//				"10.1515_med-2016-0099_1"            // OK
//				"10.1515_med-2016-0099_2"            // OK
//				"10.1515_med-2016-0099_3"            // OK
//				"10.1515_med-2016-0099_4"            // OK
//				"10.1590_S1518-8787.2016050006236_1" // OK
//				"10.21053_ceo.2016.9.1.1_1"          // Missing a tick // still bad 
//				"10.21053_ceo.2016.9.1.1_2"          // Y NaN
//				"10.21053_ceo.2016.9.1.1_3"          // OK
//				"10.21053_ceo.2016.9.1.1_4"          // OK
//				"10.2147_BCTT.S94617_1"              // OK
//				"10.3349_ymj.2016.57.5.1260_1"       // OK
//				"10.3349_ymj.2016.57.5.1260_2"       // OK
//				"10.3390_ijerph13050458_1"           // OK
//				"10.5114_aoms.2016.61916_1"          // OK // single rect
//				"10.5114_aoms.2016.61916_2"          // OK
//				"10.5812_ircmj.40061_1"              // OK
		;
		PlotBox plotBox = new PlotBox();
		File inputSVGFile = new File(TILBURG_DIR, root+".svg");
		try {
			plotBox.setCsvOutFile(new File(TARGET_PLOT+root+".csv"));
			plotBox.setSvgOutFile(new File(TARGET_PLOT+root+"0.svg"));
			plotBox.readAndCreateCSVPlot(inputSVGFile);
			plotBox.writeProcessedSVG(new File("target/plot/tilburg/"+root+".svg"));
		} catch (Exception e) {
			LOG.error("Exception in "+root, e);
		}
	}
	
}

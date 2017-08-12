package org.xmlcml.graphics.svg.plot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.GraphicsElement;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.SVGUtil;
import org.xmlcml.graphics.svg.cache.LineCache;
import org.xmlcml.graphics.svg.cache.SVGCache;

/** creates axes from ticks, scales, titles.
 * 
 * @author pm286
 *
 */
public class PlotBox {
	
	public static final Logger LOG = Logger.getLogger(PlotBox.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public enum AxisType {
		BOTTOM(0, LineDirection.HORIZONTAL, 1),
		LEFT(1, LineDirection.VERTICAL, -1),
		TOP(2, LineDirection.HORIZONTAL, -1),
		RIGHT(3, LineDirection.VERTICAL, 1);
		private int serial;
		private LineDirection direction;
		/** if 1 adds outsideWidth to maxBox, else if -1 adds insideWidth */
		private int outsidePositive;
		private AxisType(int serial, LineDirection direction, int outsidePositive) {
			this.serial = serial;
			this.direction = direction;
			this.outsidePositive = outsidePositive;
		}
		public static int getSerial(AxisType axisType) {
			for (int i = 0; i < values().length; i++) {
				if (values()[i].equals(axisType)) {
					return i;
				}
			}
			return -1;
		}
		public static final int BOTTOM_AXIS = AxisType.getSerial(AxisType.BOTTOM);
		public static final int LEFT_AXIS   = AxisType.getSerial(AxisType.LEFT);
		public static final int TOP_AXIS    = AxisType.getSerial(AxisType.TOP);
		public static final int RIGHT_AXIS  = AxisType.getSerial(AxisType.RIGHT);
		public LineDirection getLineDirection() {
			return direction;
		}
		/** 
		 * 
		 * @return if 1 adds outsideWidth to max dimension of initial box and
		 *                   insideWidth min dimension
		 *         if 0 adds outsideWidth to min dimension of initial box and
		 *                   insideWidth max dimension
		 *                   
		 *   
		 */
		public int getOutsidePositive() {
			return outsidePositive;
		}
	}
	public enum BoxType {
		HLINE("bottom x-axis only"), 
		UBOX("bottom x-axis L-y-axis R-y-axis"),
		PIBOX("top x-axis L-y-axis R-y-axis"),
		LBOX("bottom x-axis L-y-axis"),
		RBOX("bottom x-axis R-y-axis"),
		FULLBOX("bottom x-axis top x-axis L-y-axis R-y-axis"),
		;
		private String title;
		private BoxType(String title) {
			this.title = title;
		}
	}

	static final String MINOR_CHAR = "i";
	static final String MAJOR_CHAR = "I";
	public static int FORMAT_NDEC = 3; // format numbers; to start with
	

	private AnnotatedAxis[] axisArray;

	private BoxType boxType;
	private int ndecimal = FORMAT_NDEC;
	private Real2Array screenXYs;
	private Real2Array scaledXYs;

	private File svgOutFile;
	private String csvContent;
	private File csvOutFile;
	private SVGCache svgCache;
	private String fileRoot;
		

	public SVGCache getSVGCache() {
		return svgCache;
	}

	public PlotBox() {
		setDefaults();
	}
	
	private void setDefaults() {
		axisArray = new AnnotatedAxis[AxisType.values().length];
		for (AxisType axisType : AxisType.values()) {
			AnnotatedAxis axis = createAxis(axisType);
			axisArray[axisType.serial] = axis;
		}
		ndecimal = FORMAT_NDEC;
	}

	/** MAIN ENTRY METHOD for processing plots.
	 * 
	 * @param svgElement
	 * @throws FileNotFoundException 
	 */
	public void readAndCreateCSVPlot(File file) throws FileNotFoundException {
		InputStream inputStream = new FileInputStream(file);
		this.fileRoot = FilenameUtils.getName(file.toString());
		readAndCreateCSVPlot(inputStream);
	}
	/** MAIN ENTRY METHOD for processing plots.
	 * 
	 * @param inputStream
	 */
	private void readAndCreateCSVPlot(InputStream inputStream) {
		if (inputStream == null) {
			throw new RuntimeException("Null input stream");
		}
		SVGElement svgElement = SVGUtil.parseToSVGElement(inputStream);
		if (svgElement == null) {
			throw new RuntimeException("Null svgElement");
		}
		readAndCreateCSVPlot(svgElement);
	}

	/** ENTRY METHOD for processing figures.
	 * 
	 * @param svgElement
	 */
	public void readGraphicsComponents(File inputFile) {
		if (inputFile == null) {
			throw new RuntimeException("Null input file");
		}
		if (!inputFile.exists() || inputFile.isDirectory()) {
			throw new RuntimeException("nonexistent file or isDirectory "+inputFile);
		}
		fileRoot = inputFile.getName();
		svgCache = new SVGCache(this);
		try {
			svgCache.readGraphicsComponents(new FileInputStream(inputFile));
		} catch (IOException e) {
			throw new RuntimeException("Cannot read inputFile", e);
		}
	}

	public void readAndCreateCSVPlot(SVGElement svgElement) {
		svgCache = new SVGCache(this);
		svgCache.setFileRoot(fileRoot);
		svgCache.readGraphicsComponents(svgElement);
		makeAxialTickBoxesAndPopulateContents();
		makeRangesForAxes();
		extractScaleTextsAndMakeScales();
		extractTitleTextsAndMakeTitles();
		extractDataScreenPoints();
		scaleDataPointsToValues();
		createCSVContent();
		writeProcessedSVG(svgOutFile);
		writeCSV(csvOutFile);
	}


	private void makeAxialTickBoxesAndPopulateContents() {
		LineCache lineCache = svgCache.getOrCreateLineCache();
		for (AnnotatedAxis axis : axisArray) {
			axis.getOrCreateSingleLine();		
			axis.createAndFillTickBox(lineCache.getHorizontalLines(), lineCache.getVerticalLines());
		}
	}

	private void extractScaleTextsAndMakeScales() {
		for (AnnotatedAxis axis : this.axisArray) {
			axis.extractScaleTextsAndMakeScales();
		}
	}

	private void extractTitleTextsAndMakeTitles() {
		for (AnnotatedAxis axis : this.axisArray) {
			axis.extractTitleTextsAndMakeTitles();
		}
	}

	private void makeRangesForAxes() {
		for (AnnotatedAxis axis : this.axisArray) {
			axis.createAxisRanges();
		}
	}

	private void scaleDataPointsToValues() {
		scaledXYs = null;
		AnnotatedAxis xAxis = axisArray[AxisType.BOTTOM_AXIS];
		AnnotatedAxis yAxis = axisArray[AxisType.LEFT_AXIS];
		xAxis.ensureScales();
		yAxis.ensureScales();
		if (xAxis.getScreenToUserScale() == null ||
				xAxis.getScreenToUserConstant() == null ||
				yAxis.getScreenToUserScale() == null ||
				yAxis.getScreenToUserConstant() == null) {
			LOG.trace("XAXIS "+xAxis+"\n"+"YAXIS "+yAxis+"\n"+"Cannot get conversion constants: abort");
			return;
		}

		if (screenXYs != null && screenXYs.size() > 0) {
			scaledXYs = new Real2Array();
			for (int i = 0; i < screenXYs.size(); i++) {
				Real2 screenXY = screenXYs.get(i);
				double x = screenXY.getX();
				double scaledX = xAxis.getScreenToUserScale() * x + xAxis.getScreenToUserConstant();
				double y = screenXY.getY();
				double scaledY = yAxis.getScreenToUserScale() * y + yAxis.getScreenToUserConstant();
				Real2 scaledXY = new Real2(scaledX, scaledY);
				scaledXYs.add(scaledXY);
			}
			scaledXYs.format(ndecimal + 1);
		}
	}

	public void writeCSV(File file) {
		if (file != null) {
			try {
				IOUtils.write(csvContent, new FileOutputStream(file));
			} catch (IOException e) {
				throw new RuntimeException("cannot write CSV: ", e);
			}
		}
	}

	private String createCSVContent() {
		// use CSVBuilder later
		StringBuilder sb = new StringBuilder();
		if (scaledXYs != null) {
			for (Real2 scaledXY : scaledXYs) {
				sb.append(scaledXY.getX()+","+scaledXY.getY()+"\n");
			}
		}
		csvContent = sb.toString();
		return csvContent;
	}

	private void extractDataScreenPoints() {
		screenXYs = new Real2Array();
		for (SVGCircle circle : svgCache.getOrCreateShapeCache().getCircleList()) {
			screenXYs.add(circle.getCXY());
		}
		if (screenXYs.size() == 0) {
			LOG.trace("NO CIRCLES IN PLOT");
		}
		if (screenXYs.size() == 0) {
			// this is really messy
			for (SVGLine line : svgCache.getOrCreateShapeCache().getLineList()) {
				Real2 vector = line.getEuclidLine().getVector();
				double angle = vector.getAngle();
				double length = vector.getLength();
				if (length < 3.0) {
					if (Real.isEqual(angle, 2.35, 0.03)) {
						screenXYs.add(line.getMidPoint());
					}
				}
			}
		}
		screenXYs.format(getNdecimal());
	}

	// graphics
	

	private SVGG copyAnnotatedAxes() {
		SVGG g = new SVGG();
		g.setClassName("plotBox");
		for (AnnotatedAxis axis : axisArray) {
			g.appendChild(axis.getSVGElement().copy());
		}
		return g;
	}

	
	// getters and setters
	

//	public List<SVGText> getTextList() {
//		return textList;
//	}
//
//	public void setTextList(List<SVGText> textList) {
//		this.textList = textList;
//	}

//	public List<SVGLine> getLineList() {
//		return lineList;
//	}
//
//	public void setLineList(List<SVGLine> lineList) {
//		this.lineList = lineList;
//	}
//
//	public List<SVGCircle> getCircleList() {
//		return circleList;
//	}
//
//	public void setCircleList(List<SVGCircle> circleList) {
//		this.circleList = circleList;
//	}

//	public SVGRect getFullLineBox() {
//		return fullLineBox;
//	}
//
//	public void setFullLineBox(SVGRect fullLineBox) {
//		this.fullLineBox = fullLineBox;
//	}
//
//	public SVGElement getSvgElement() {
//		return svgElement;
//	}
//
//	public void setSvgElement(SVGElement svgElement) {
//		this.svgElement = svgElement;
//	}

	public BoxType getBoxType() {
		return boxType;
	}

	public void setBoxType(BoxType boxType) {
		this.boxType = boxType;
	}

	public AnnotatedAxis[] getAxisArray() {
		return axisArray;
	}

	private AnnotatedAxis createAxis(AxisType axisType) {
		AnnotatedAxis axis = new AnnotatedAxis(this, axisType);
		return axis;
	}

	public int getNdecimal() {
		return ndecimal;
	}

	public void setNdecimal(int ndecimal) {
		this.ndecimal = ndecimal;
	}

	public List<SVGText> getHorizontalTexts() {
		return svgCache.getOrCreateTextCache().getHorizontalTexts();
	}

	public List<SVGText> getVerticalTexts() {
		return svgCache.getOrCreateTextCache().getVerticalTexts();
	}
	
	// static methods
	
	public void writeProcessedSVG(File file) {
		if (file != null) {
			GraphicsElement processedSVGElement = svgCache.createSVGElement();
			processedSVGElement.appendChild(copyAnnotatedAxes());
			SVGSVG.wrapAndWriteAsSVG(processedSVGElement, file);
		}
	}
	
	public String getCSV() {
		return csvContent;
	}

	public File getSvgOutFile() {
		return svgOutFile;
	}

	public void setSvgOutFile(File svgOutFile) {
		this.svgOutFile = svgOutFile;
	}

	public File getCsvOutFile() {
		return csvOutFile;
	}

	public void setCsvOutFile(File csvOutFile) {
		this.csvOutFile = csvOutFile;
	}


}

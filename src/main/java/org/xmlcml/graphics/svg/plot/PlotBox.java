package org.xmlcml.graphics.svg.plot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGDefs;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGLineList;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.SVGTitle;
import org.xmlcml.graphics.svg.SVGUtil;
import org.xmlcml.graphics.svg.extract.PathExtractor;
import org.xmlcml.graphics.svg.extract.ShapeExtractor;
import org.xmlcml.graphics.svg.extract.TextExtractor;
import org.xmlcml.graphics.svg.linestuff.AxialLineList;

/** creates axes from ticks, scales, titles.
 * 
 * @author pm286
 *
 */
public class PlotBox {
	
	static final Logger LOG = Logger.getLogger(PlotBox.class);
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
	private static Double CORNER_EPS = 0.5; // to start with
	private static Double BBOX_PADDING = 5.0; // to start with
	private static int FORMAT_NDEC = 3; // format numbers; to start with
	
//	public List<SVGText> textList;
	// lines
	private List<SVGLine> horizontalLines;
	private List<SVGLine> verticalLines;
	private List<SVGText> horizontalTexts;
	private List<SVGText> verticalTexts;
	private AnnotatedAxis[] axisArray;
	private AxialLineList longHorizontalEdgeLines;
	private AxialLineList longVerticalEdgeLines;
	private SVGRect fullLineBox;
	
	private SVGElement svgElement;
	private BoxType boxType;
	private int ndecimal = FORMAT_NDEC;
	private Real2Array screenXYs;
	private Real2Array scaledXYs;

	private ShapeExtractor shapeExtractor;
	private File svgOutFile;
	private String csvContent;
	private File csvOutFile;
	public boolean removeWhitespace = false;
		
	public SVGLogger svgLogger;
	private PathExtractor pathExtractor;
	private Real2Range positiveXBox;
	private TextExtractor textExtractor;
	private String fileRoot;

	public PlotBox() {
		setDefaults();
	}
	
	private void setDefaults() {
		axisArray = new AnnotatedAxis[AxisType.values().length];
		for (AxisType axisType : AxisType.values()) {
			LOG.debug("AxisType: "+axisType);
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

	public void readAndCreateCSVPlot(SVGElement svgElement) {
		extractGraphicsElements(svgElement);
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
		try {
			readGraphicsComponents(new FileInputStream(inputFile));
		} catch (IOException e) {
			throw new RuntimeException("Cannot read inputFile", e);
		}
	}
	
	/** ENTRY METHOD for processing figures.
	 * 
	 * @param svgElement
	 */
	public void readGraphicsComponents(InputStream inputStream) {
		extractGraphicsElements(inputStream);
	}
	/** extract graphics components from Stream.
	 * uses extractGraphicsElements(SVGElement)
	 * 
	 * @param inputStream
	 */
	private void extractGraphicsElements(InputStream inputStream) {
		if (inputStream == null) {
			throw new RuntimeException("Null input stream");
		}
		SVGElement svgElement = SVGUtil.parseToSVGElement(inputStream);
		if (svgElement == null) {
			throw new RuntimeException("Null svgElement");
		}
		extractGraphicsElements(svgElement);
	}

	private void extractGraphicsElements(SVGElement svgElement) {
		Manifest figureManifest = new Manifest();
		createManifest(figureManifest, svgElement);
		makeFullLineBoxAndRanges(figureManifest);
	}

	public Manifest createManifest(Manifest manifest, SVGElement svgElement) {
		extractSVGComponents(manifest, svgElement);
		createHorizontalAndVerticalLines(manifest);
		createHorizontalAndVerticalTexts(manifest);
		makeLongHorizontalAndVerticalEdges(manifest);
		return manifest;
	}

	private void extractSVGComponents(Manifest manifest, SVGElement svgElem) {
		svgLogger = new SVGLogger();
		LOG.debug("********* made SVG components *********");
		this.svgElement = (SVGElement) svgElem.copy();
		SVGG g;
		SVGG gg = new SVGG();
		
		 // is this a good idea? These are clipping boxes. 
		 SVGDefs.removeDefs(svgElement);
		
		positiveXBox = new Real2Range(new RealRange(-100., 10000), new RealRange(-10., 10000));
		removeEmptyTextElements(manifest);
		removeNegativeXorYElements(manifest);
		
		pathExtractor = new PathExtractor(this);
		pathExtractor.extractPaths(svgElement);
//		Real2Range pathBox = pathExtractor.getBoundingBox();
		g = pathExtractor.debug("target/paths/"+fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("path"));
//		gg.appendChild(g.copy());
		

		textExtractor = new TextExtractor(this);
		textExtractor.extractTexts(svgElement);
		g = textExtractor.debug("target/texts/"+fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("text"));
		gg.appendChild(g.copy());
		
//		Real2Range totalBox = textBox.plus(pathBox);

		shapeExtractor = new ShapeExtractor(this);
		List<SVGPath> currentPathList = pathExtractor.getCurrentPathList();
		shapeExtractor.extractShapes(currentPathList, svgElement);
		g = shapeExtractor.debug("target/shapes/"+fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("shape"));
		gg.appendChild(g.copy());
		
		SVGSVG.wrapAndWriteAsSVG(gg, new File("target/plot/"+fileRoot+".debug.svg"));
	}

	/** some plots have publisher cruft outside the limits, especially negative Y.
	 * remove these elements from svgElement
	 * 
	 */
	private void removeNegativeXorYElements(Manifest manifest) {
		List<SVGText> texts = SVGText.extractSelfAndDescendantTexts(svgElement);
		for (int i = texts.size() - 1; i >= 0; i--) {
			SVGText text = texts.get(i);
			Real2 xy = text.getXY();
			if (xy.getX() < 0.0 || xy.getY() < 0.0) {
				texts.remove(i);
				text.detach();
			}
		}
	}

	private void removeEmptyTextElements(Manifest manifest) {
		List<SVGText> texts = SVGText.extractSelfAndDescendantTexts(svgElement);
		for (int i = texts.size() - 1; i >= 0; i--) {
			SVGText text = texts.get(i);
			String s = text.getValue();
			if (s == null || "".equals(s.trim())) {
				texts.remove(i);
				text.detach();
			}
		}
	}

	private void createHorizontalAndVerticalLines(Manifest manifest) {
		LOG.debug("********* make Horizontal/Vertical lines *********");
		horizontalLines = SVGLine.findHorizontalOrVerticalLines(shapeExtractor.getLineList(), LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		verticalLines = SVGLine.findHorizontalOrVerticalLines(shapeExtractor.getLineList(), LineDirection.VERTICAL, AnnotatedAxis.EPS);
		List<SVGPolyline> polylineList = shapeExtractor.getPolylineList();
		List<SVGPolyline> axialLShapes = SVGPolyline.findLShapes(polylineList);
		for (int i = axialLShapes.size() - 1; i >= 0; i--) {
			SVGPolyline axialLShape = axialLShapes.get(i);
			verticalLines.add(axialLShape.getLineList().get(0));
			horizontalLines.add(axialLShape.getLineList().get(1));
//			axialLShapes.remove(i);
			polylineList.remove(axialLShape);
		}
	}

	private void createHorizontalAndVerticalTexts(Manifest manifest) {
		LOG.debug("********* make Horizontal/Vertical texts *********");
		horizontalTexts = SVGText.findHorizontalOrRot90Texts(textExtractor.getTextList(), LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		verticalTexts = SVGText.findHorizontalOrRot90Texts(textExtractor.getTextList(), LineDirection.VERTICAL, AnnotatedAxis.EPS);
		
		StringBuilder sb = new StringBuilder();
		for (SVGText verticalText : verticalTexts) {
			sb.append("/"+verticalText.getValue());
		}
		LOG.debug("TEXT horiz: " + horizontalTexts.size()+"; vert: " + verticalTexts.size()+"; " /*+ "/"+sb*/);
	}

	private void makeLongHorizontalAndVerticalEdges(Manifest manifest) {
		LOG.debug("********* make Horizontal/Vertical edges *********");
		List<SVGLine> lineList = shapeExtractor.getLineList();
		if (lineList != null && lineList.size() > 0) {
			Real2Range lineBbox = SVGElement.createBoundingBox(lineList);
			LOG.debug("LINES "+lineList);
			longHorizontalEdgeLines = getSortedLinesCloseToEdge(horizontalLines, LineDirection.HORIZONTAL, lineBbox.getXRange());
			longVerticalEdgeLines = getSortedLinesCloseToEdge(verticalLines, LineDirection.VERTICAL, lineBbox.getYRange());
		}
	}

	private void makeFullLineBoxAndRanges(Manifest manifest) {
		LOG.debug("********* make FullineBox and Ranges *********");
		fullLineBox = null;
		RealRange fullboxXRange = null;
		RealRange fullboxYRange = null;
		if (longHorizontalEdgeLines != null && longHorizontalEdgeLines.size() > 0) {
			LOG.debug("longHorizontalEdgeLines "+longHorizontalEdgeLines.size());
			fullboxXRange = createRange(manifest, longHorizontalEdgeLines, Direction.HORIZONTAL);
			fullboxXRange = fullboxXRange == null ? null : fullboxXRange.format(PlotBox.FORMAT_NDEC);
		}
		if (longVerticalEdgeLines != null && longVerticalEdgeLines.size() > 0) {
			LOG.debug("longVerticalEdgeLines "+longVerticalEdgeLines.size());
			fullboxYRange = createRange(manifest, longVerticalEdgeLines, Direction.VERTICAL);
			fullboxYRange = fullboxYRange == null ? null : fullboxYRange.format(PlotBox.FORMAT_NDEC);
		}
		if (fullboxXRange != null && fullboxYRange != null) {
			fullLineBox = SVGRect.createFromRealRanges(fullboxXRange, fullboxYRange);
			fullLineBox.format(PlotBox.FORMAT_NDEC);
		}
		LOG.debug("fullbox "+fullLineBox);
	}

	private void makeAxialTickBoxesAndPopulateContents() {
		LOG.debug("*********  makeAxialTickBoxesAndPopulateContents *********");
		for (AnnotatedAxis axis : axisArray) {
			axis.getOrCreateSingleLine();		
			axis.createAndFillTickBox(horizontalLines, verticalLines);
		}
	}

	private void extractScaleTextsAndMakeScales() {
		LOG.debug("********* extractScaleTextsAndMakeScales *********");
		for (AnnotatedAxis axis : this.axisArray) {
			axis.extractScaleTextsAndMakeScales();
		}
	}

	private void extractTitleTextsAndMakeTitles() {
		LOG.debug("********* extractTitleTextsAndMakeTitles *********");
		for (AnnotatedAxis axis : this.axisArray) {
			axis.extractTitleTextsAndMakeTitles();
		}
	}

	private void makeRangesForAxes() {
		LOG.debug("********* makeRangesForAxes *********");
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
			LOG.debug("XAXIS "+xAxis);
			LOG.debug("YAXIS "+yAxis);
			LOG.error("Cannot get conversion constants: abort");
			return;
		}

		if (screenXYs != null && screenXYs.size() > 0) {
			scaledXYs = new Real2Array();
			LOG.debug("screenXY: "+screenXYs);
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
			LOG.trace("scaledXY: "+scaledXYs);
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
		for (SVGCircle circle : shapeExtractor.getCircleList()) {
			screenXYs.add(circle.getCXY());
		}
		if (screenXYs.size() == 0) {
			LOG.warn("NO CIRCLES IN PLOT");
		}
		if (screenXYs.size() == 0) {
			// this is really messy
			LOG.debug("trying short pi/4 lines");
			for (SVGLine line : shapeExtractor.getLineList()) {
				Real2 vector = line.getEuclidLine().getVector();
				double angle = vector.getAngle();
				double length = vector.getLength();
				if (length < 3.0) {
					LOG.debug(angle + " "+ length + " " +line);
					if (Real.isEqual(angle, 2.35, 0.03)) {
						screenXYs.add(line.getMidPoint());
					}
				}
			}
		}
		screenXYs.format(getNdecimal());
	}

	// graphics
	
	public SVGElement createSVGElement() {
		SVGG g = new SVGG();
		g.appendChild(copyOriginalElements());
		g.appendChild(shapeExtractor.createSVGAnnotations());
		g.appendChild(copyAnnotatedAxes());
		g.appendChild(pathExtractor.createSVGAnnotation().copy());
		return g;
	}
	
	private SVGG copyOriginalElements() {
		SVGG g = new SVGG();
		ShapeExtractor.addList(g, new ArrayList<SVGPath>(pathExtractor.getOriginalPathList()));
		ShapeExtractor.addList(g, new ArrayList<SVGText>(textExtractor.getTextList()));
		g.setStroke("pink");
		return g;
	}

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

	public SVGRect getFullLineBox() {
		return fullLineBox;
	}

	public void setFullLineBox(SVGRect fullLineBox) {
		this.fullLineBox = fullLineBox;
	}

	public SVGElement getSvgElement() {
		return svgElement;
	}

	public void setSvgElement(SVGElement svgElement) {
		this.svgElement = svgElement;
	}

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
		return horizontalTexts;
	}

	public List<SVGText> getVerticalTexts() {
		return verticalTexts;
	}
	
	// static methods
	
	private static RealRange createRange(Manifest manifest, SVGLineList lines, Direction direction) {
		RealRange hRange = null;
		if (lines.size() > 0) {
			SVGLine line0 = lines.get(0);
			hRange = line0.getReal2Range().getRealRange(direction);
			SVGLine line1 = lines.get(1);
			if (line1 != null && !line1.getReal2Range().getRealRange(direction).isEqualTo(hRange, CORNER_EPS)) {
				hRange = null;
	//				throw new RuntimeException("Cannot make box from HLines: "+line0+"; "+line1);
			}
		}
		return hRange;
	}
	
	private static AxialLineList getSortedLinesCloseToEdge(List<SVGLine> lines, LineDirection direction, RealRange range) {
		RealRange.Direction rangeDirection = direction.isHorizontal() ? RealRange.Direction.HORIZONTAL : RealRange.Direction.VERTICAL;
		AxialLineList axialLineList = new AxialLineList(direction);
		for (SVGLine line : lines) {
			RealRange lineRange = line.getRealRange(rangeDirection);
			if (lineRange.isEqualTo(range, BBOX_PADDING)) {
				axialLineList.add(line);
				line.normalizeDirection(AnnotatedAxis.EPS);
			}
		}
		axialLineList.sort();
		return axialLineList;
	}

	public void writeProcessedSVG(File file) {
		if (file != null) {
			SVGElement processedSVGElement = this.createSVGElement();
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

	public SVGLogger getSvgLogger() {
		return svgLogger;
	}

	public Real2Range getPositiveXBox() {
		return positiveXBox;
	}

	public boolean isRemoveWhitespace() {
		return removeWhitespace;
	}

}

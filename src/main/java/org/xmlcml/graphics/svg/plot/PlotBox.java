package org.xmlcml.graphics.svg.plot;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGLineList;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGText;
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
	
	private List<SVGPath> pathList;
	private List<SVGText> textList;
	private List<SVGLine> lineList;
	
	// derived
	private List<SVGCircle> circleList;
	
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
	 */
	public void createPlot(SVGElement svgElement) {
		extractSVGComponents(svgElement);
		createHorizontalAndVerticalLines();
		createHorizontalAndVerticalTexts();
		makeLongHorizontalAndVerticalEdges();
		makeFullLineBoxAndRanges();
		makeAxialTickBoxesAndPopulateContents();
		makeRangesForAxes();
		extractScaleTextsAndMakeScales();
		extractTitleTextsAndMakeTitles();
	}

	private void createHorizontalAndVerticalLines() {
		LOG.debug("********* make Horizontal/Vertical lines *********");
		if (lineList == null || lineList.size() == 0) {
			lineList = SVGPath.createLinesFromPaths(pathList);
		}
		horizontalLines = SVGLine.findHorizontalOrVerticalLines(lineList, LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		verticalLines = SVGLine.findHorizontalOrVerticalLines(lineList, LineDirection.VERTICAL, AnnotatedAxis.EPS);
	}

	private void createHorizontalAndVerticalTexts() {
		LOG.debug("********* make Horizontal/Vertical texts *********");
		horizontalTexts = SVGText.findHorizontalOrVerticalTexts(textList, LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		verticalTexts = SVGText.findHorizontalOrVerticalTexts(textList, LineDirection.VERTICAL, AnnotatedAxis.EPS);
		
		StringBuilder sb = new StringBuilder();
		for (SVGText verticalText : verticalTexts) {
			sb.append("/"+verticalText.getValue());
		}
		LOG.trace("TEXT horiz: " + horizontalTexts.size()+"; vert: " + verticalTexts.size()+"; " /*+ "/"+sb*/);
	}

	private void makeLongHorizontalAndVerticalEdges() {
		LOG.debug("********* make Horizontal/Vertical edges *********");
		Real2Range lineBbox = SVGElement.createBoundingBox(lineList);
		longHorizontalEdgeLines = getSortedLinesCloseToEdge(horizontalLines, LineDirection.HORIZONTAL, lineBbox.getXRange());
		longVerticalEdgeLines = getSortedLinesCloseToEdge(verticalLines, LineDirection.VERTICAL, lineBbox.getYRange());
	}

	private void makeFullLineBoxAndRanges() {
		LOG.debug("********* make FullineBox and Ranges *********");
		fullLineBox = null;
		RealRange fullboxXRange = createRange(longHorizontalEdgeLines, Direction.HORIZONTAL);
		fullboxXRange = fullboxXRange.format(PlotBox.FORMAT_NDEC);
		RealRange fullboxYRange = createRange(longVerticalEdgeLines, Direction.VERTICAL);
		fullboxYRange = fullboxYRange.format(PlotBox.FORMAT_NDEC);
		fullLineBox = SVGRect.createFromRealRanges(fullboxXRange, fullboxYRange);
		fullLineBox.format(PlotBox.FORMAT_NDEC);
	}

	private void makeAxialTickBoxesAndPopulateContents() {
		LOG.debug("*********  makeAxialTickBoxesAndPopulateContents *********");
		for (AnnotatedAxis axis : axisArray) {
			axis.getOrCreateSingleLine();		
			/*AxialBox axisTickBox = */ axis.createAndFillTickBox(horizontalLines, verticalLines);
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

	private void extractSVGComponents(SVGElement svgElement) {
		LOG.debug("********* made SVG components *********");
		this.svgElement = svgElement;
		pathList = SVGPath.extractPaths(svgElement);
		lineList = SVGLine.extractSelfAndDescendantLines(svgElement);
		textList = SVGText.extractSelfAndDescendantTexts(svgElement);
		circleList = SVGCircle.extractSelfAndDescendantCircles(svgElement);
	}
	
	// graphics
	
	SVGElement createSVGElement() {
		SVGG g = new SVGG();
		g.appendChild(copyOriginalElements());
		g.appendChild(copyAnnotatedAxes());
		return g;
	}
	
	private SVGG copyOriginalElements() {
		SVGG g = new SVGG();
		addList(g, pathList);
		addList(g, lineList);
		addList(g, textList);
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

	private void addList(SVGG g, List<? extends SVGElement> list) {
		for (SVGElement element : list) {
			g.appendChild(element.copy());
		}
	}
	
	// getters and setters
	

	public List<SVGPath> getPathList() {
		return pathList;
	}

	public void setPathList(List<SVGPath> pathList) {
		this.pathList = pathList;
	}

	public List<SVGText> getTextList() {
		return textList;
	}

	public void setTextList(List<SVGText> textList) {
		this.textList = textList;
	}

	public List<SVGLine> getLineList() {
		return lineList;
	}

	public void setLineList(List<SVGLine> lineList) {
		this.lineList = lineList;
	}

	public List<SVGCircle> getCircleList() {
		return circleList;
	}

	public void setCircleList(List<SVGCircle> circleList) {
		this.circleList = circleList;
	}

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
	
	private static RealRange createRange(SVGLineList lines, Direction direction) {
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




}

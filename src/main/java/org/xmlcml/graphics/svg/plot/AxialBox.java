package org.xmlcml.graphics.svg.plot;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
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
public class AxialBox {
	
	static final Logger LOG = Logger.getLogger(AxialBox.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public enum AxisType {
		BOTTOM(0, LineDirection.HORIZONTAL),
		LEFT(1, LineDirection.VERTICAL),
		TOP(2, LineDirection.HORIZONTAL),
		RIGHT(3, LineDirection.VERTICAL);
		private int serial;
		private LineDirection direction;
		private AxisType(int serial, LineDirection direction) {
			this.serial = serial;
			this.direction = direction;
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
		public LineDirection getDirection() {
			return direction;
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
	
	private List<SVGPath> pathList;
	private List<SVGText> textList;
	private List<SVGLine> lineList;
	private List<SVGCircle> circleList;
	
	private List<SVGLine> horizontalLines;
	private List<SVGLine> verticalLines;
	private AnnotatedAxis[] axisArray;
	boolean useRange;
	private AxialLineList longHorizontalEdgeLines;
	private AxialLineList longVerticalEdgeLines;
	private SVGRect fullLineBox;
	private SVGElement svgElement;
	private BoxType boxType;

	public AxialBox() {
		setDefaults();
	}
	
	private void setDefaults() {
		axisArray = new AnnotatedAxis[AxisType.values().length];
	}

	private void processPaths() {
		createHorizontalAndVerticalLines();
		createAxesAndAxisBox();
		LOG.debug("axes: "+axisArray);
		for (AnnotatedAxis axis : axisArray) {
			if (axis != null) {
				LOG.debug("axis: "+axis);
				axis.createAxisAndRanges(this);
			}
		}
		return;
	}
	
	void processTextsPathsLines(List<SVGText> textList, List<SVGPath> pathList, List<SVGLine> lineList, boolean useRange) {
		setPrimitives(textList, pathList, lineList, useRange);
		
		processPaths();
		for (AnnotatedAxis axis : axisArray) {
			if (axis != null) {
				axis.processScalesTitle(this);
			}
		}
	}

	private void setPrimitives(List<SVGText> textList, List<SVGPath> pathList, List<SVGLine> lineList, boolean useRange) {
		this.pathList = pathList;
		this.textList = textList;
		this.useRange = useRange;
		this.lineList = lineList;
	}

	private void createHorizontalAndVerticalLines() {
		if (lineList == null || lineList.size() == 0) {
			lineList = SVGPath.createLinesFromPaths(pathList);
		}
		horizontalLines = SVGLine.findHorizontalOrVerticalLines(lineList, LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		verticalLines = SVGLine.findHorizontalOrVerticalLines(lineList, LineDirection.VERTICAL, AnnotatedAxis.EPS);
	}

	private void createAxesAndAxisBox() {
		LOG.debug("createAxesAndAxisBox");
		// we'll assume one horizontal and many vertical lines 
		// the first two are probably obsolete in the medium term
		if (horizontalLines.size() == 0 || verticalLines.size() == 0) {
			LOG.debug("no lines for box");
		} else if (horizontalLines.size() == 1 && verticalLines.size() > 1) {
			AnnotatedAxis axis = new AnnotatedAxis(this);
			axis.createMainAndTickLines(LineDirection.HORIZONTAL, horizontalLines.get(0), verticalLines);
		} else if (verticalLines.size() == 1 && horizontalLines.size() > 1) {
			AnnotatedAxis axis = new AnnotatedAxis(this);
			axis.createMainAndTickLines(LineDirection.VERTICAL, verticalLines.get(0), horizontalLines);
		} else {
			findAxisBox();
		}
	}

	private Real2Range findAxisBox() {
		Real2Range lineBbox = SVGElement.createBoundingBox(lineList);
		longHorizontalEdgeLines = getSortedLinesCloseToEdge(horizontalLines, LineDirection.HORIZONTAL, lineBbox.getXRange());
		longVerticalEdgeLines = getSortedLinesCloseToEdge(verticalLines, LineDirection.VERTICAL, lineBbox.getYRange());
		makeFullBox();
		return lineBbox;
	}

	/** box with all 4 axes
	 * 
	 */
	private SVGRect makeFullBox() {
		fullLineBox = null;
		if (longHorizontalEdgeLines != null && longVerticalEdgeLines != null) {
			RealRange fullboxXRange = createRange(longHorizontalEdgeLines, Direction.HORIZONTAL);
			fullboxXRange = fullboxXRange.format(3);
			RealRange fullboxYRange = createRange(longVerticalEdgeLines, Direction.VERTICAL);
			fullboxYRange = fullboxYRange.format(3);
			fullLineBox = SVGRect.createFromRealRanges(fullboxXRange, fullboxYRange);
			fullLineBox.format(3);
			LOG.debug("full box "+fullLineBox);
			makeAxesAndAxialTickBoxes();
		}
		return fullLineBox;
	}

	private static RealRange createRange(SVGLineList edgeLines, Direction direction) {
		SVGLine line0 = edgeLines.get(0);
		RealRange hRange = line0.getReal2Range().getRealRange(direction);
		SVGLine line1 = edgeLines.get(1);
		if (line1 != null && !line1.getReal2Range().getRealRange(direction).isEqualTo(hRange, CORNER_EPS)) {
			hRange = null;
//				throw new RuntimeException("Cannot make box from HLines: "+line0+"; "+line1);
		}
		return hRange;
	}

	private void makeAxesAndAxialTickBoxes() {
		LOG.debug("makeAxesAndAxialTickBoxes");
		for (AxisType axisType : AxisType.values()) {
			LOG.debug("AxisType: "+axisType);
			axisArray[axisType.serial] = null;
			AnnotatedAxis axis = this.createAxis(axisType);
//			SVGLine line = this.createEdge(axisType);
//			axis.setSingleLine(line);
			AxisTickBox axisTickBox = axis.createTickBoxAndAxialLines(axis.getSingleLine(), horizontalLines, verticalLines);
			if (axisTickBox != null) {
				axisArray[axisType.serial] = axis;
				axis.setAxisTickBox(axisTickBox);
				LOG.debug("axisTickBox "+axisTickBox);
			}
		}
	}

	private SVGLine createEdge(AxisType axisType) {
		SVGLine edge = null;
		if (axisType != null && fullLineBox != null) {
			Real2Range bbox = fullLineBox.getBoundingBox();
			Real2[] corners = bbox.getCorners();
			if (AxisType.TOP.equals(axisType)) {
				edge = new SVGLine(corners[0], new Real2(corners[1].getX(), corners[0].getY())); 
			} else if (AxisType.BOTTOM.equals(axisType)) {
				edge = new SVGLine(new Real2(corners[0].getX(), corners[1].getY()), corners[1]); 
			} else if (AxisType.LEFT.equals(axisType)) {
				edge = new SVGLine(corners[0], new Real2(corners[0].getX(), corners[1].getY())); 
			} else if (AxisType.RIGHT.equals(axisType)) {
				edge = new SVGLine(new Real2(corners[1].getX(), corners[1].getY()), corners[1]); 
			} else {
				LOG.error("Unknown axis type: "+axisType);
			}
		}
		return edge;
	}

//	private AnnotatedAxis createAxis(LineDirection direction) {
//		return new AnnotatedAxis(this, direction);
//	}

	private AxialLineList getSortedLinesCloseToEdge(List<SVGLine> lines, LineDirection direction, RealRange range) {
		RealRange.Direction rangeDirection = LineDirection.HORIZONTAL.equals(direction) ? RealRange.Direction.HORIZONTAL : RealRange.Direction.VERTICAL;
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

	public AnnotatedAxis createAxis(AxisType axisType) {
		AnnotatedAxis axis = new AnnotatedAxis(this, axisType);
		axis.setSingleLine(this.createEdge(axisType));
		return axis;
	}

	public void readAndExtractPrimitives(SVGElement svgElement) {
		this.svgElement = svgElement;
		pathList = SVGPath.extractPaths(svgElement);
		lineList = SVGLine.extractSelfAndDescendantLines(svgElement);
		textList = SVGText.extractSelfAndDescendantTexts(svgElement);
		circleList = SVGCircle.extractSelfAndDescendantCircles(svgElement);
		this.processTextsPathsLines(textList, pathList, lineList, useRange);
	}
	
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



}

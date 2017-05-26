package org.xmlcml.graphics.svg.plot;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.IntArray;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGLineList;
import org.xmlcml.graphics.svg.plot.PlotBox.AxisType;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * An axis (vertical of horizontal) with (probably) one or more
 *   axial line (SVGLine)
 *   tick marks (major and minor)
 *   scales (list of numbers aligned with ticks)
 *   axial titles
 *   
 * @author pm286
 *
 */
public class AnnotatedAxis {

	private static final Logger LOG = Logger.getLogger(AnnotatedAxis.class);
	static final double AXIS_END_EPS = 1.0;
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	static double EPS = 0.01;
	LineDirection lineDirection;
	RealRange range;
	SVGLine singleLine;
	private AxisTickBox axisTickBox;
	private AxisScaleBox axialScaleTextBox;
	private AxisScaleBox axialTitleTextBox;
	private PlotBox plotBox;
	private AxisType axisType;
	private Double screenToUserScale;
	private Double screenToUserConstant;


	protected AnnotatedAxis(PlotBox plotBox) {
		this.plotBox = plotBox;
	}
	
	public AnnotatedAxis(PlotBox plotBox, AxisType axisType) {
		this(plotBox);
		this.axisType = axisType;
		this.lineDirection = axisType == null ? null : axisType.getLineDirection();		
	}

//	private void setRange(RealRange range) {
//		this.range = range;
//	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("type: "+axisType+ "; dir: "+lineDirection+"; ");
		sb.append("range: "+range+"\n");
		sb.append("axisTickBox: "+axisTickBox+"\n");
		sb.append("tickValues: "+axialScaleTextBox+"\n");
		return sb.toString();
	}

	public RealRange getRange() {
		return range;
	}
	public void setSingleLine(SVGLine singleLine) {
		this.singleLine = singleLine;
	}

	public SVGLine getSingleLine() {
		return singleLine;
	}
	
	public LineDirection getLineDirection() {
		if (lineDirection == null && axisType != null) {
			lineDirection = axisType.getLineDirection();
		}
		return lineDirection;
	}

	public void setLineDirection(LineDirection direction) {
		this.lineDirection = direction;
	}

	public AxisTickBox getAxisTickBox() {
		return axisTickBox;
	}

	public void setScreenToUserScale(Double screenToUserScale) {
		this.screenToUserScale = screenToUserScale;
	}

	public Double getScreenToUserConstant() {
		return screenToUserConstant;
	}

	public AxisScaleBox getValueTextBox() {
		return axialScaleTextBox;
	}

	public void setValueTextBox(AxisScaleBox valueTextBox) {
		this.axialScaleTextBox = valueTextBox;
	}

	public Double getScreenToUserScale() {
		return screenToUserScale;
	}


	public PlotBox getPlotBox() {
		return plotBox;
	}

	public void setAxisTickBox(AxisTickBox axisTickBox) {
		this.axisTickBox = axisTickBox;
	}

	public AxisType getAxisType() {
		return axisType;
	}

	SVGLine getOrCreateSingleLine() {
		if (singleLine == null) {
			if (plotBox.getFullLineBox() != null) {
				Real2Range bbox = plotBox.getFullLineBox().getBoundingBox();
				Real2[] corners = bbox.getCorners();
				if (AxisType.TOP.equals(axisType)) {
					singleLine = new SVGLine(corners[0], new Real2(corners[1].getX(), corners[0].getY())); 
				} else if (AxisType.BOTTOM.equals(axisType)) {
					singleLine = new SVGLine(new Real2(corners[0].getX(), corners[1].getY()), corners[1]); 
				} else if (AxisType.LEFT.equals(axisType)) {
					singleLine = new SVGLine(corners[0], new Real2(corners[0].getX(), corners[1].getY())); 
				} else if (AxisType.RIGHT.equals(axisType)) {
					singleLine = new SVGLine(new Real2(corners[1].getX(), corners[0].getY()), corners[1]); 
				} else {
					LOG.error("Unknown axis type: "+axisType);
				}
			} else {
				LOG.warn("no fullLineBox");
			}
		}
		return singleLine;
	}

//	private void mapTicksToTickValues() {
//		if (axialScaleTextBox.getTickNumberValues() == null) {
//			if (axialScaleTextBox.getTickNumberValues() != null && axisTickBox.getMajorTicksScreenCoords() != null) {
//				int missingTickCount = axialScaleTextBox.getTickNumberValues().size() - axisTickBox.getMajorTicksScreenCoords().size();
//				if (missingTickCount == 0) {
//					// we ought to check values of tick values?
//					axialScaleTextBox.setTickNumberValues(new RealArray(axisTickBox.getMajorTicksScreenCoords()));
//				} else if (missingTickCount == 1 || missingTickCount == 2) {
//					int missingEndTicks = axisTickBox.addMissingEndTicks(this);
//					missingTickCount -= missingEndTicks;
//					if (missingTickCount == 0) {
//						axialScaleTextBox.setTickNumberScreenCoords(new RealArray(axisTickBox.getMajorTicksScreenCoords()));
//					} else {
//						LOG.error("missing "+missingTickCount+" from axis");
//					}
//				}
//			} else {
//				LOG.debug("missing tickNumberUserCoords and/or majorTicksScreenCoords");
//			}
//		} else {
//			LOG.debug("Cannot map ticks to pixels");
//		}
//	}

	private void createScreenToUserTransform() {
		if (axisTickBox.getMajorTicksScreenCoords() != null && axialScaleTextBox.getTickNumberValues() != null) {
			screenToUserScale = axisTickBox.getMajorTicksScreenCoords().getRange().getScaleTo(axialScaleTextBox.getTickNumberValues().getRange());
			screenToUserConstant = axisTickBox.getMajorTicksScreenCoords().getRange().getConstantTo(axialScaleTextBox.getTickNumberValues().getRange());
			LOG.debug("screen2User: "+screenToUserScale+"; "+screenToUserConstant);
		} else {
			LOG.debug("no majorTicksScreenCoords or tickNumberUserCoords");
		}
	}
	
	/** transform screen coords on this axis to user coords (numbers on axis).
	 * 
	 * @param xscreen
	 * @return
	 */
	private double transformScreenToUser(double xscreen) {
		return axisTickBox.getMajorTicksScreenCoords().getRange().transformToRange(axialScaleTextBox.getTickNumberValues().getRange(), xscreen);
	}


	private void processTitle() {
		LOG.trace("AxisTitle title NYI");
	}

	void createAxisRanges() {
		if (singleLine == null) {
			LOG.warn("null singleLine in :"+this);
		} else if (axisTickBox == null) {
			LOG.error("null axisTickBox :"+this);
		} else if (axisTickBox.getTickLines() != null && axisTickBox.getTickLines().size() > 0) {
			Real2Range bbox = singleLine.getBoundingBox();
			range = (lineDirection.isHorizontal()) ? bbox.getXRange() : bbox.getYRange();
			range.format(decimalPlaces());
			// assume sorted - we'll need to add sort later
			Real2Range tick2Range = SVGLine.getReal2Range(axisTickBox.getTickLines());
			axisTickBox.setTickRange(lineDirection.isHorizontal() ? tick2Range.getXRange() : tick2Range.getYRange());
			LOG.debug("tickRange: " + axisTickBox.getTickRange());
		}
	}

	private int decimalPlaces() {
		return plotBox.getNdecimal();
	}

//	/**
//	 * public only because of test
//	 */
//	private void calculateAxisPropertiesAndReturnAxis() {
//		mapTicksToTickValues();
//		createScreenToUserTransform();
//	}


	void extractScaleTextsAndMakeScales() {
		if (axisTickBox == null) {
			LOG.warn("no ticks so no scale texts captured");
			return;
		}
		this.axialScaleTextBox = new AxisScaleBox(this);
		axialScaleTextBox.makeCaptureBox();
		this.axialScaleTextBox.setTexts(plotBox.getHorizontalTexts(), plotBox.getVerticalTexts());
		axialScaleTextBox.extractScaleValueList();
		RealArray tickValues = axialScaleTextBox.getTickNumberValues();
		RealArray tickValueCoords = axialScaleTextBox.getTickValueScreenCoords();
		RealArray tickCoords = axisTickBox.getMajorTicksScreenCoords();
		LOG.debug("TICK coords\n"+tickCoords+"\n"+tickValueCoords+"\n"+tickValues);
		if (tickValues != null && tickCoords != null) {
			int nplaces = 1;
			Multiset<Double> deltaValueSet = tickValues.createDoubleDifferenceMultiset(nplaces);
			Multiset<Integer> deltaValueCoordSet = tickValueCoords.createIntegerDifferenceMultiset();
			Multiset<Integer> deltaTickCoordSet = tickCoords.createIntegerDifferenceMultiset();
			LOG.debug("DELTA coords\n"+deltaTickCoordSet+"\n"+deltaValueCoordSet+"\n"+deltaValueSet);
			
			if (true) { // fill conditions for equality
				matchTicksToValuesAndCalculateScales(tickValues, tickValueCoords, tickCoords, nplaces);
			}
		}
	}

private void matchTicksToValuesAndCalculateScales(RealArray tickValues, RealArray tickValueCoords, RealArray tickCoords, int nplaces) {
	if (tickValueCoords.size() - tickCoords.size() == 2) { // probably missing end points
		LOG.info("missing 2 ticks; taking axes as ends ticks"); 
		tickCoords.addElement(range.getMax());
		tickCoords.insertElementAt(0, range.getMin());
	} else if (tickValueCoords.size() - tickValues.size() == 1) { // have to work out which end point
		throw new RuntimeException("cannot match ticks with values; single missing tick");
	} else if (tickValueCoords.size() == tickValues.size() ) {
	} else {
		LOG.error("cannot match ticks with values");
	}
	RealArray tick2ValueDiffs = tickCoords.subtract(tickValueCoords);
	tick2ValueDiffs.format(0);
	Multiset<Double> tick2ValueSet = tick2ValueDiffs.createDoubleDifferenceMultiset(nplaces);
	LOG.debug("tick2ValueCoordsDiffs "+tick2ValueSet);
	screenToUserScale = tickCoords.getRange().getScaleTo(tickValues.getRange());
	screenToUserConstant = tickCoords.getRange().getConstantTo(tickValues.getRange());
	LOG.debug("screen2User: "+screenToUserScale+"; "+screenToUserConstant);
}

	void extractTitleTextsAndMakeTitles() {
		axialTitleTextBox = new AxisScaleBox(this);
		this.axialTitleTextBox.setTexts(plotBox.getHorizontalTexts(), plotBox.getVerticalTexts());
		axialTitleTextBox.extractText();
	}

	boolean isHorizontal() {
		return getLineDirection().isHorizontal();
	}

	public SVGElement getSVGElement() {
		SVGG g = new SVGG();
		g.setClassName("axis");
		g.appendChild(axisTickBox.createSVGElement());
		g.appendChild(axialScaleTextBox.createSVGElement());
		return g;
	}

	private void buildTickBoxContents(AxisTickBox axisTickBox) {
		LOG.debug("MADE axisTickBox "+axisTickBox + axisTickBox.hashCode());
		setAxisTickBox(axisTickBox);
		SVGLineList potentialTickLines = axisTickBox.getPotentialTickLines();
		LOG.debug("potential tickLines: "+potentialTickLines.size());
		axisTickBox.createMainAndTickLines(this, potentialTickLines.getLineList());
	}

	/** make tick box from knowing only the axis Type
	 * 
	 * 
	 * @param line
	 * @param lineDirection
	 */
	private AxisTickBox createAxisTickBox() {
		AxisTickBox axisTickBox = null;
		if (getSingleLine() != null && getAxisType() != null) {
			axisTickBox = new AxisTickBox(this);
			axisTickBox.makeCaptureBox();
		}
		return axisTickBox;
	}

	AxialBox createAndFillTickBox(List<SVGLine> horizontalLines, List<SVGLine> verticalLines) {
		AxisTickBox.LOG.trace("****** making tick box for "+getAxisType()+" from: hor "+horizontalLines.size()+"; vert "+verticalLines.size()+" in "+getPlotBox().getFullLineBox());
		AxisTickBox axisTickBox = createTickBoxAndAxialLines(horizontalLines, verticalLines);
		if (axisTickBox != null) {
			buildTickBoxContents(axisTickBox);
		} else {
			AxisTickBox.LOG.debug("Null axisTickBox");
		}
		return axisTickBox;
	}

	private AxisTickBox createTickBoxAndAxialLines(List<SVGLine> horizontalLines, List<SVGLine> verticalLines) {
		AxisTickBox axisTickBox = null;
		if (singleLine != null) {
			List<SVGLine> possibleTickLines = lineDirection.isHorizontal() ? verticalLines : horizontalLines;
			if (possibleTickLines.size() > 0) {
				axisTickBox = createAxisTickBox();
				axisTickBox.extractIntersectingLines(horizontalLines, verticalLines);
			}
		} else {
			AxisTickBox.LOG.warn("no single line for "+this);
		}
		return axisTickBox;
	}

	
}

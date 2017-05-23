package org.xmlcml.graphics.svg.plot;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.plot.PlotBox.AxisType;

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
	private AxisTextBox valueTextBox;
	private PlotBox plotBox;
	private AxisType axisType;
	private List<SVGText> textList;
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

	void setRange(RealRange range) {
		this.range = range;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("type: "+axisType+ "; dir: "+lineDirection+"; ");
		sb.append("range: "+range+"\n");
		sb.append("axisTickBox: "+axisTickBox+"\n");
		sb.append("tickValues: "+valueTextBox+"\n");
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

	public AxisTextBox getValueTextBox() {
		return valueTextBox;
	}

	public void setValueTextBox(AxisTextBox valueTextBox) {
		this.valueTextBox = valueTextBox;
	}

	public Double getScreenToUserScale() {
		return screenToUserScale;
	}


	private void mapTicksToTickValues() {
		if (valueTextBox.getTickNumberValues() == null) {
			if (valueTextBox.getTickNumberValues() != null && axisTickBox.getMajorTicksScreenCoords() != null) {
				int missingTickCount = valueTextBox.getTickNumberValues().size() - axisTickBox.getMajorTicksScreenCoords().size();
				if (missingTickCount == 0) {
					// we ought to check values of tick values?
					valueTextBox.setTickNumberValues(new RealArray(axisTickBox.getMajorTicksScreenCoords()));
				} else if (missingTickCount == 1 || missingTickCount == 2) {
					int missingEndTicks = axisTickBox.addMissingEndTicks(this);
					missingTickCount -= missingEndTicks;
					if (missingTickCount == 0) {
						valueTextBox.setTickNumberScreenCoords(new RealArray(axisTickBox.getMajorTicksScreenCoords()));
					} else {
						LOG.error("missing "+missingTickCount+" from axis");
					}
				}
			} else {
				LOG.debug("missing tickNumberUserCoords and/or majorTicksScreenCoords");
			}
		} else {
			LOG.debug("Cannot map ticks to pixels");
		}
	}

	private void createScreenToUserTransform() {
		if (axisTickBox.getMajorTicksScreenCoords() != null && valueTextBox.getTickNumberValues() != null) {
			screenToUserScale = axisTickBox.getMajorTicksScreenCoords().getRange().getScaleTo(valueTextBox.getTickNumberValues().getRange());
			screenToUserConstant = axisTickBox.getMajorTicksScreenCoords().getRange().getConstantTo(valueTextBox.getTickNumberValues().getRange());
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
	 public double transformScreenToUser(double xscreen) {
		return axisTickBox.getMajorTicksScreenCoords().getRange().transformToRange(valueTextBox.getTickNumberValues().getRange(), xscreen);
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

	/**
	 * public only because of test
	 */
	public void calculateAxisPropertiesAndReturnAxis() {
		mapTicksToTickValues();
		createScreenToUserTransform();
	}


	void extractScaleTextsAndMakeScales() {
		valueTextBox.extractScaleValueList();
		processTitle();
	}

	public PlotBox getPlotBox() {
		return plotBox;
	}

	public void setAxisTickBox(AxisTickBox axisTickBox) {
		this.axisTickBox = axisTickBox;
	}
	
	public void makeAxialScaleBox() {
		valueTextBox = new AxisTextBox(this);
	}

	boolean isHorizontal() {
		return getLineDirection().isHorizontal();
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

	public SVGElement getSVGElement() {
		SVGG g = new SVGG();
		g.setClassName("axis");
		g.appendChild(axisTickBox.createSVGElement());
		return g;
	}

	
}

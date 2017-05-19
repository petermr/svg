package org.xmlcml.graphics.svg.plot;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGLineList;

public class AxisTickBox /*extends Real2Range*/ {
	private static final Logger LOG = Logger.getLogger(AxisTickBox.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private static final double SMALL_DELTA = 10.0;
	private static final double LARGE_DELTA = 15.0;

	private LineDirection direction;
	private double deltaX;
	private double deltaY;
	private SVGLineList horizontalLines;
	private SVGLineList verticalLines;
	private Real2Range bbox;

	public AxisTickBox(SVGLine line, LineDirection direction) {
		this.direction = direction;
		this.deltaX = LineDirection.HORIZONTAL.equals(direction) ? SMALL_DELTA : LARGE_DELTA;
		this.deltaY = LineDirection.HORIZONTAL.equals(direction) ? LARGE_DELTA : SMALL_DELTA;
		double xExtension = LineDirection.HORIZONTAL.equals(direction) ? SMALL_DELTA : LARGE_DELTA;
		double yExtension = LineDirection.HORIZONTAL.equals(direction) ? LARGE_DELTA : SMALL_DELTA;

		this.bbox = line.getBoundingBox().getReal2RangeExtendedInX(xExtension, xExtension);
		this.bbox = this.bbox.getReal2RangeExtendedInY(yExtension, yExtension);
		LOG.trace("BBOX "+bbox);
	}

	public void extractContainedAxialLines(List<SVGLine> horizontalLines, List<SVGLine> verticalLines) {
		this.horizontalLines = extractContainedLines(new SVGLineList(horizontalLines));
		this.verticalLines = extractContainedLines(new SVGLineList(verticalLines));
	}

	private SVGLineList extractContainedLines(SVGLineList lines) {
		SVGLineList lineList = new SVGLineList();
		for (SVGLine line : lines) {
			if (line.isIncludedBy(this.bbox)) {
				line.format(3);
				lineList.add(line);
			}
		}
		return lineList;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(bbox.toString());
		sb.append("DIR: " + direction + "; deltaX,Y:" + deltaX+", "+deltaY+"\n");
		sb.append("HOR: " + horizontalLines+"\n");
		sb.append("VERT: " + verticalLines+"\n");
		return sb.toString();
	}
	
	public SVGLineList getPotentialTickLines() {
		return (LineDirection.HORIZONTAL.equals(direction)) ? verticalLines : horizontalLines;
	}

	public Real2Range getBoundingBox() {
		return bbox;
	}
}

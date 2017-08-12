package org.xmlcml.graphics.svg.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGLineList;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.linestuff.AxialLineList;
import org.xmlcml.graphics.svg.plot.AnnotatedAxis;
import org.xmlcml.graphics.svg.plot.PlotBox;

import com.google.common.collect.Multiset;

/** extracts texts within graphic area.
 * 
 * @author pm286
 *
 */
public class LineCache extends AbstractCache {
	static final Logger LOG = Logger.getLogger(LineCache.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public List<SVGLine> horizontalLines;
	public List<SVGLine> verticalLines;

	private List<SVGLine> lineList;
	private SVGLineList longHorizontalLineList;
	private SVGLineList shortHorizontalLineList;
	private SVGLineList topHorizontalLineList;
	private SVGLineList bottomHorizontalLineList;
	private Multiset<Double> horizontalLineStrokeWidthSet;

	private AxialLineList longHorizontalEdgeLines;
	private AxialLineList longVerticalEdgeLines;
	private SVGRect fullLineBox;
	public Real2Range lineBbox;
	
	public Double axialLinePadding = 10.0; // to start with
	public Double cornerEps = 0.5; // to start with
	private List<SVGLine> allLines;

	public LineCache(SVGCache svgCache) {
		super(svgCache);
		lineList = svgCache.getOrCreateShapeCache().getLineList();
	}

	public Real2Range getBoundingBox() {
		boundingBox = SVGElement.createBoundingBox(lineList);
		return boundingBox;
	}

	public void makeLongHorizontalAndVerticalEdges() {
		if (lineList != null && lineList.size() > 0) {
			this.lineBbox = SVGElement.createBoundingBox(lineList);
			this.longHorizontalEdgeLines = this.getSortedLinesCloseToEdge(this.horizontalLines, LineDirection.HORIZONTAL, this.lineBbox);
			this.longVerticalEdgeLines = this.getSortedLinesCloseToEdge(this.verticalLines, LineDirection.VERTICAL, this.lineBbox);
			SVGSVG.wrapAndWriteAsSVG(this.longHorizontalEdgeLines.getLineList(), new File(svgCache.debugRoot+svgCache.fileRoot+".horizEdges.svg"));
			SVGSVG.wrapAndWriteAsSVG(this.longVerticalEdgeLines.getLineList(), new File(svgCache.debugRoot+svgCache.fileRoot+".vertEdges.svg"));
		}
		return;
	}

	public void makeFullLineBoxAndRanges() {
		
		this.fullLineBox = null;
		RealRange fullboxXRange = null;
		RealRange fullboxYRange = null;
		if (this.longHorizontalEdgeLines != null && this.longHorizontalEdgeLines.size() > 0) {
			fullboxXRange = this.createRange(this.longHorizontalEdgeLines, Direction.HORIZONTAL);
			fullboxXRange = fullboxXRange == null ? null : fullboxXRange.format(PlotBox.FORMAT_NDEC);
		}
		if (this.longVerticalEdgeLines != null && this.longVerticalEdgeLines.size() > 0) {
			fullboxYRange = this.createRange(this.longVerticalEdgeLines, Direction.VERTICAL);
			fullboxYRange = fullboxYRange == null ? null : fullboxYRange.format(PlotBox.FORMAT_NDEC);
		}
		if (fullboxXRange != null && fullboxYRange != null) {
			this.fullLineBox = SVGRect.createFromRealRanges(fullboxXRange, fullboxYRange);
			this.fullLineBox.format(PlotBox.FORMAT_NDEC);
		}
		if (this.fullLineBox == null && svgCache.pathBox != null) {
			for (SVGRect rect : svgCache.shapeExtractor.getRectList()) {
				Real2Range rectRange = rect.getBoundingBox();
				if (svgCache.pathBox.isEqualTo(rectRange, this.axialLinePadding)) {
					this.fullLineBox = rect;
					break;
				}
			}
		}
	}

	public SVGLineList getTopHorizontalLineList() {
		return topHorizontalLineList;
	}

	public SVGLineList getBottomHorizontalLineList() {
		return bottomHorizontalLineList;
	}

	public SVGLineList getLongHorizontalLineList() {
		return longHorizontalLineList;
	}

	public SVGLineList getShortHorizontalLineList() {
		return shortHorizontalLineList;
	}

	public Multiset<Double> getHorizontalLineStrokeWidthSet() {
		if (horizontalLineStrokeWidthSet == null) {
		}
		return horizontalLineStrokeWidthSet;
	}

	/** creates horizntal and vertical lines
	 * splits "L"-shaped polylines into two lines
	 * this may or may not be a good idea.
	 * 
	 * @param svgElement modified
	 */
	public void createHorizontalAndVerticalLines(SVGElement svgElement) {
		
		this.horizontalLines = SVGLine.findHorizontalOrVerticalLines(svgCache.shapeExtractor.getLineList(), LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		this.verticalLines = SVGLine.findHorizontalOrVerticalLines(svgCache.shapeExtractor.getLineList(), LineDirection.VERTICAL, AnnotatedAxis.EPS);
		List<SVGPolyline> polylineList = svgCache.shapeExtractor.getPolylineList();
		List<SVGPolyline> axialLShapes = SVGPolyline.findLShapes(polylineList);
		for (int i = axialLShapes.size() - 1; i >= 0; i--) {
			this.removeLShapesAndReplaceByLines(polylineList, axialLShapes.get(i), svgElement);
		}
		allLines = new ArrayList<SVGLine>();
		allLines.addAll(this.horizontalLines);
		allLines.addAll(this.verticalLines);
	}

	public RealRange createRange(SVGLineList lines, Direction direction) {
		RealRange hRange = null;
		if (lines.size() > 0) {
			SVGLine line0 = lines.get(0);
			hRange = line0.getReal2Range().getRealRange(direction);
			SVGLine line1 = lines.get(1);
			if (line1 != null && !line1.getReal2Range().getRealRange(direction).isEqualTo(hRange, cornerEps)) {
				hRange = null;
	//				throw new RuntimeException("Cannot make box from HLines: "+line0+"; "+line1);
			}
		}
		return hRange;
	}

	void removeLShapesAndReplaceByLines(List<SVGPolyline> polylineList, SVGPolyline axialLShape, SVGElement svgElement) {
		LOG.trace("replacing LShapes by splitLines");
		SVGLine vLine = axialLShape.getLineList().get(0);
		svgElement.appendChild(vLine);
		this.verticalLines.add(vLine);
		SVGLine hLine = axialLShape.getLineList().get(1);
		svgElement.appendChild(hLine);
		this.horizontalLines.add(hLine);
		polylineList.remove(axialLShape);
		axialLShape.detach();
	}

	public AxialLineList getSortedLinesCloseToEdge(List<SVGLine> lines, LineDirection direction, Real2Range bbox) {
		RealRange.Direction rangeDirection = direction.isHorizontal() ? RealRange.Direction.HORIZONTAL : RealRange.Direction.VERTICAL;
		RealRange parallelRange = direction.isHorizontal() ? bbox.getXRange() : bbox.getYRange();
		RealRange perpendicularRange = direction.isHorizontal() ? bbox.getYRange() : bbox.getXRange();
		AxialLineList axialLineList = new AxialLineList(direction);
		for (SVGLine line : lines) {
			Real2 xy = line.getXY(0);
			Double perpendicularCoord = direction.isHorizontal() ? xy.getY() : xy.getX();
			RealRange lineRange = line.getRealRange(rangeDirection);
			if (lineRange.isEqualTo(parallelRange, axialLinePadding)) {
				if (isCloseToBoxEdge(perpendicularRange, perpendicularCoord, axialLinePadding)) {
					axialLineList.add(line);
					line.normalizeDirection(AnnotatedAxis.EPS);
				}
			}
		}
		axialLineList.sort();
		return axialLineList;
	}


	private static boolean isCloseToBoxEdge(RealRange parallelRange, Double parallelCoord, Double axialLinePadding) {
		return Real.isEqual(parallelCoord, parallelRange.getMin(), axialLinePadding) || Real.isEqual(parallelCoord, parallelRange.getMax(), axialLinePadding);
	}

	public List<SVGLine> getHorizontalLines() {
		return horizontalLines;
	}

	public List<SVGLine> getVerticalLines() {
		return verticalLines;
	}

	public SVGRect getFullLineBox() {
		return fullLineBox;
	}
	

}

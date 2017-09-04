package org.xmlcml.graphics.svg.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.euclid.util.MultisetUtil;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGLineList;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.linestuff.AxialLineList;
import org.xmlcml.graphics.svg.plot.AnnotatedAxis;
import org.xmlcml.graphics.svg.plot.SVGMediaBox;

import com.google.common.collect.HashMultiset;
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

	private List<SVGLine> horizontalLines;
	private List<SVGLine> verticalLines;

	private List<SVGLine> lineList;
	private SVGLineList longHorizontalLineList;
	private SVGLineList shortHorizontalLineList;
	private SVGLineList topHorizontalLineList;
	private SVGLineList bottomHorizontalLineList;
	private Multiset<Double> horizontalLineStrokeWidthSet;

	private AxialLineList longHorizontalEdgeLines;
	private AxialLineList longVerticalEdgeLines;
	private SVGRect fullLineBox;
	private Real2Range lineBbox;
	
	private Double axialLinePadding = 10.0; // to start with
	private Double cornerEps = 0.5; // to start with
	private List<SVGLine> allLines;
	private Double lineEps = 5.0; // to start with
	// for display only
	private Double STROKE_WIDTH_FACTOR = 5.0;
	private ShapeCache siblingShapeCache;
	
	public LineCache(ComponentCache containingComponentCache) {
		super(containingComponentCache);
		siblingShapeCache = containingComponentCache.getOrCreateShapeCache();
		lineList =  siblingShapeCache == null ? new ArrayList<SVGLine>() : siblingShapeCache.getLineList();
		init();
	}
	
	private void init() {
		longHorizontalLineList = new SVGLineList();
		shortHorizontalLineList = new SVGLineList();
		longHorizontalEdgeLines = new AxialLineList(LineDirection.HORIZONTAL);
	}

	/** the bounding box of the actual line components
	 * The extent of the context (e.g. svgCache) may be larger
	 * @return the bounding box of the contained lines
	 */
	public Real2Range getBoundingBox() {
		return getOrCreateBoundingBox(lineList);
	}

	public List<? extends SVGElement> getOrCreateElementList() {
		return lineList;
	}

	public List<SVGLine> getOrCreateLineList() {
		return lineList;
	}
	
	public void makeLongHorizontalAndVerticalEdges() {
		
		if (lineList != null && lineList.size() > 0) {
			lineBbox = SVGElement.createBoundingBox(lineList);
			getOrCreateLongHorizontalEdgeLines();
			getOrCreateLongVerticalEdgeLines();
		}
		return;
	}

	private void getOrCreateLongVerticalEdgeLines() {
		if (longVerticalEdgeLines == null) {
			longVerticalEdgeLines = getSortedLinesCloseToEdge(verticalLines, LineDirection.VERTICAL, lineBbox);
		}
	}

	private void getOrCreateLongHorizontalEdgeLines() {
		if (longHorizontalEdgeLines == null) {
			longHorizontalEdgeLines = getSortedLinesCloseToEdge(horizontalLines, LineDirection.HORIZONTAL, lineBbox);
		}
	}

	public void makeFullLineBoxAndRanges() {
		
		fullLineBox = null;
		RealRange fullboxXRange = null;
		RealRange fullboxYRange = null;
		if (longHorizontalEdgeLines != null && longHorizontalEdgeLines.size() > 0) {
			fullboxXRange = createRange(longHorizontalEdgeLines, Direction.HORIZONTAL);
			fullboxXRange = fullboxXRange == null ? null : fullboxXRange.format(SVGMediaBox.FORMAT_NDEC);
		}
		if (longVerticalEdgeLines != null && longVerticalEdgeLines.size() > 0) {
			fullboxYRange = createRange(longVerticalEdgeLines, Direction.VERTICAL);
			fullboxYRange = fullboxYRange == null ? null : fullboxYRange.format(SVGMediaBox.FORMAT_NDEC);
		}
		if (fullboxXRange != null && fullboxYRange != null) {
			fullLineBox = SVGRect.createFromRealRanges(fullboxXRange, fullboxYRange);
			fullLineBox.format(SVGMediaBox.FORMAT_NDEC);
		}
		if (fullLineBox == null && ownerComponentCache.pathBox != null) {
			for (SVGRect rect : ownerComponentCache.getOrCreateShapeCache().getRectList()) {
				Real2Range rectRange = rect.getBoundingBox();
				if (ownerComponentCache.pathBox.isEqualTo(rectRange, axialLinePadding)) {
					fullLineBox = rect;
					break;
				}
			}
		}
	}

	public SVGLineList getTopHorizontalLineList() {
		topHorizontalLineList = new SVGLineList();
		getOrCreateLongHorizontalLineList();
		for (SVGLine line : longHorizontalLineList) {
			if (Real.isEqual(ownerComponentCache.getBoundingBox().getYMin(), line.getMidPoint().getY(), lineEps )) {
				topHorizontalLineList.add(line);
			}
		}
		return topHorizontalLineList;
	}

	public SVGLineList getBottomHorizontalLineList() {
		bottomHorizontalLineList = new SVGLineList();
		getOrCreateLongHorizontalLineList();
		for (SVGLine line : longHorizontalLineList) {
			if (Real.isEqual(ownerComponentCache.getBoundingBox().getYMax(), line.getMidPoint().getY(), lineEps )) {
				bottomHorizontalLineList.add(line);
			}
		}
		return bottomHorizontalLineList;
	}

	public SVGLineList getOrCreateLongHorizontalLineList() {
		longHorizontalLineList = new SVGLineList();
		getOrCreateHorizontalLineList();
		RealRange xrange = getOrCreateComponentCacheBoundingBox().getRealRange(Direction.HORIZONTAL);
		for (SVGLine line : horizontalLines) {
			if (RealRange.isEqual(xrange, line.getRealRange(Direction.HORIZONTAL), lineEps )) {
				longHorizontalLineList.add(line);
			}
		}

		return longHorizontalLineList;
	}

	public SVGLineList getShortHorizontalLineList() {
		shortHorizontalLineList = new SVGLineList();
		getOrCreateHorizontalLineList();
		RealRange xrange = getOrCreateComponentCacheBoundingBox().getRealRange(Direction.HORIZONTAL);
		for (SVGLine line : horizontalLines) {
			if (line.getLength() < xrange.getRange() - lineEps ) {
				shortHorizontalLineList.add(line);
			}
		}
		return shortHorizontalLineList;
	}

	public Multiset<Double> getHorizontalLineStrokeWidthSet() {
		if (horizontalLineStrokeWidthSet == null) {
			horizontalLineStrokeWidthSet = HashMultiset.create();
			getOrCreateHorizontalLineList();
			for (SVGLine horizontalLine : horizontalLines) {
				Double strokeWidth = horizontalLine.getStrokeWidth(); 
				if (strokeWidth == null) strokeWidth = 0.0;
				horizontalLineStrokeWidthSet.add(strokeWidth);
			}
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
		
		getOrCreateHorizontalLineList();
		getOrCreateVerticalLineList();
		List<SVGPolyline> polylineList = siblingShapeCache.getPolylineList();
		List<SVGPolyline> axialLShapes = SVGPolyline.findLShapes(polylineList);
		for (int i = axialLShapes.size() - 1; i >= 0; i--) {
			removeLShapesAndReplaceByLines(polylineList, axialLShapes.get(i), svgElement);
		}
		allLines = new ArrayList<SVGLine>();
		allLines.addAll(this.horizontalLines);
		allLines.addAll(this.verticalLines);
	}

	public List<SVGLine> getOrCreateVerticalLineList() {
		if (verticalLines == null) {
			verticalLines = SVGLine.findHorizontalOrVerticalLines(lineList, LineDirection.VERTICAL, AnnotatedAxis.EPS);
		}
		return verticalLines;
	}

	public List<SVGLine> getOrCreateHorizontalLineList() {
		if (horizontalLines == null) {
			horizontalLines = SVGLine.findHorizontalOrVerticalLines(lineList, LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		}
		return horizontalLines;
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
		return Real.isEqual(parallelCoord, parallelRange.getMin(), axialLinePadding) ||
				Real.isEqual(parallelCoord, parallelRange.getMax(), axialLinePadding);
	}

	public SVGRect getFullLineBox() {
		return fullLineBox;
	}

	
	public SVGG createColoredHorizontalLineStyles() {
		return createColoredHorizontalLineStyles(ComponentCache.MAJOR_COLORS);
	}

	/** we are now emphasing lines by width and blackness, not color, 
	 * so this arg is 
	 * @param color
	 * @return
	 */
	public SVGG createColoredHorizontalLineStyles(String[] color) {
		List<SVGLine> lines = getOrCreateHorizontalLineList();
		Multiset<Double> lineWidths = getHorizontalLineStrokeWidthSet();
		SVGG g = new SVGG();
		if (lineWidths != null) {
			List<Multiset.Entry<Double>> sortedLineWidths = MultisetUtil.createDoubleListSortedByCount(lineWidths);
			for (SVGLine line : lines) {
				Double strokeWidth = line.getStrokeWidth();
				for (int i = 0; i < sortedLineWidths.size(); i++) {
					Multiset.Entry<Double> entry = sortedLineWidths.get(i);
					if (entry.getElement().equals(strokeWidth)) {
						SVGLine line1 = (SVGLine) line.copy();
						line1.setStrokeWidth(strokeWidth * STROKE_WIDTH_FACTOR );
						line1.setStroke("black");
						line1.addTitle(""+strokeWidth);
						g.appendChild(line1);
						break;
					}
				}
			}
		}
		return g;
	}

	@Override
	public String toString() {
		String s = ""
			+ "hor: "+horizontalLines.size()+"; "
			+ "vert: "+verticalLines.size()+"; "
			+ "line: "+lineList.size()+"; "
			+ "longH: "+longHorizontalLineList.size()+"; "
			+ "shortH: "+shortHorizontalLineList.size()+"; ";
		return s;

	}


	

}

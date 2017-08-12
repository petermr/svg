package org.xmlcml.graphics.svg.cache;

import java.io.File;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGLineList;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.plot.PlotBox;

/** extracts texts within graphic area.
 * 
 * @author pm286
 *
 */
public class RectCache extends AbstractCache {
	static final Logger LOG = Logger.getLogger(RectCache.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private List<SVGRect> rectList;
	private List<SVGRect> horizontalPanelList;
	
	public RectCache(SVGCache svgCache) {
		super(svgCache);
	}

	public Real2Range getBoundingBox() {
		boundingBox = SVGElement.createBoundingBox(rectList);
		return boundingBox;
	}

//	public void makeLongHorizontalAndVerticalEdges(SVGCache svgCache) {
//		List<SVGLine> lineList = svgCache.shapeExtractor.getLineList();
//		if (lineList != null && lineList.size() > 0) {
//			svgCache.lineBbox = SVGElement.createBoundingBox(lineList);
//			svgCache.longHorizontalEdgeLines = svgCache.getSortedLinesCloseToEdge(svgCache.horizontalLines, LineDirection.HORIZONTAL, svgCache.lineBbox);
//			svgCache.longVerticalEdgeLines = svgCache.getSortedLinesCloseToEdge(svgCache.verticalLines, LineDirection.VERTICAL, svgCache.lineBbox);
//			SVGSVG.wrapAndWriteAsSVG(svgCache.longHorizontalEdgeLines.getLineList(), new File(svgCache.debugRoot+svgCache.fileRoot+".horizEdges.svg"));
//			SVGSVG.wrapAndWriteAsSVG(svgCache.longVerticalEdgeLines.getLineList(), new File(svgCache.debugRoot+svgCache.fileRoot+".vertEdges.svg"));
//		}
//		return;
//	}
//
//	public void makeFullLineBoxAndRanges(SVGCache svgCache) {
//		
//		this.fullLineBox = null;
//		RealRange fullboxXRange = null;
//		RealRange fullboxYRange = null;
//		if (this.longHorizontalEdgeLines != null && this.longHorizontalEdgeLines.size() > 0) {
//			fullboxXRange = this.createRange(this.longHorizontalEdgeLines, Direction.HORIZONTAL);
//			fullboxXRange = fullboxXRange == null ? null : fullboxXRange.format(PlotBox.FORMAT_NDEC);
//		}
//		if (this.longVerticalEdgeLines != null && this.longVerticalEdgeLines.size() > 0) {
//			fullboxYRange = this.createRange(this.longVerticalEdgeLines, Direction.VERTICAL);
//			fullboxYRange = fullboxYRange == null ? null : fullboxYRange.format(PlotBox.FORMAT_NDEC);
//		}
//		if (fullboxXRange != null && fullboxYRange != null) {
//			this.fullLineBox = SVGRect.createFromRealRanges(fullboxXRange, fullboxYRange);
//			this.fullLineBox.format(PlotBox.FORMAT_NDEC);
//		}
//		if (this.fullLineBox == null && this.pathBox != null) {
//			for (SVGRect rect : this.shapeExtractor.getRectList()) {
//				Real2Range rectRange = rect.getBoundingBox();
//				if (this.pathBox.isEqualTo(rectRange, this.axialLinePadding)) {
//					this.fullLineBox = rect;
//					break;
//				}
//			}
//		}
//	}

	public List<SVGRect> getHorizontalPanelList() {
		return horizontalPanelList;
	}
}

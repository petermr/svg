package org.xmlcml.graphics.svg.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.graphics.svg.SVGDefs;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGImage;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGLineList;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.SVGTitle;
import org.xmlcml.graphics.svg.SVGUtil;
import org.xmlcml.graphics.svg.extract.AbstractExtractor;
import org.xmlcml.graphics.svg.extract.ImageExtractor;
import org.xmlcml.graphics.svg.extract.PathExtractor;
import org.xmlcml.graphics.svg.extract.ShapeExtractor;
import org.xmlcml.graphics.svg.extract.TextExtractor;
import org.xmlcml.graphics.svg.linestuff.AxialLineList;
import org.xmlcml.graphics.svg.plot.AnnotatedAxis;
import org.xmlcml.graphics.svg.plot.PlotBox;

public class SVGStore {
	private static final Logger LOG = Logger.getLogger(SVGStore.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public static int ZERO_PLACES = 0;

	// FIXME change to getters
	private List<SVGLine> horizontalLines;
	private List<SVGLine> verticalLines;
	private List<SVGText> horizontalTexts;
	private List<SVGText> verticalTexts;

	private ImageExtractor imageExtractor;
	private PathExtractor pathExtractor;
	private ShapeExtractor shapeExtractor;
	private TextExtractor textExtractor;
	
	private Real2Range positiveXBox;

	private String fileRoot;
	private SVGElement svgElement;
	private String debugRoot = "target/debug/";
	private String imageDebug = "target/images/";
	private String pathDebug = "target/paths/";
	private String shapeDebug = "target/shapes/";
	private String textDebug = "target/texts/";
	private File plotDebug = new File("target/plots/");

	private AxialLineList longHorizontalEdgeLines;
	private AxialLineList longVerticalEdgeLines;
	private SVGRect fullLineBox;
	private PlotBox plotBox; // may not be required
	private Double axialLinePadding = 10.0; // to start with
	public Double cornerEps = 0.5; // to start with
	
	private boolean removeWhitespace = false;
	private boolean removeDuplicatePaths = true;
	private Real2Range imageBox;
	private Real2Range lineBbox;
	private Real2Range pathBox;
	private Real2Range textBox;
	private Real2Range totalBox;




	/** this may change as we decide what types of object interact with store
	 * 
	 * @param plotBox
	 */
	public SVGStore(PlotBox plotBox) {
		this.plotBox = plotBox;
	}
	
	public SVGStore() {
	}

	public void readGraphicsComponents(File file) throws FileNotFoundException {
		this.fileRoot = FilenameUtils.getBaseName(file.getName());
		LOG.debug(">fr>"+fileRoot);
		readGraphicsComponents(new FileInputStream(file));
	}
	
	public void readGraphicsComponents(InputStream inputStream) {
		if (inputStream == null) {
			throw new RuntimeException("Null input stream");
		}
		SVGElement svgElement = SVGUtil.parseToSVGElement(inputStream);
		readGraphicsElements(svgElement);
	}

	public void readGraphicsElements(SVGElement svgElement) {
		if (svgElement != null) {
			this.extractSVGComponents(svgElement);
			this.createHorizontalAndVerticalLines();
			this.createHorizontalAndVerticalTexts();
			this.makeLongHorizontalAndVerticalEdges();
			this.makeFullLineBoxAndRanges();
		} else {
			throw new RuntimeException("Null svgElement");
		}
	}

	private void extractSVGComponents(SVGElement svgElem) {
		LOG.debug("********* made SVG components *********");
		svgElement = (SVGElement) svgElem.copy();
		SVGG g;
		SVGG gg = new SVGG();
		
		 // is this a good idea? These are clipping boxes. 
		 SVGDefs.removeDefs(svgElement);
		
		positiveXBox = new Real2Range(new RealRange(-100., 10000), new RealRange(-10., 10000));
		removeEmptyTextElements();
		removeNegativeXorYElements();
		
		this.pathExtractor = new PathExtractor(this);
		this.pathExtractor.extractPaths(this.svgElement);
		pathBox = pathExtractor.getBoundingBox();
		g = this.pathExtractor.debug(pathDebug+this.fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("path"));
	//		gg.appendChild(g.copy());
		
		this.imageExtractor = new ImageExtractor(this);
		this.imageExtractor.extractImages(this.svgElement);
		imageBox = imageExtractor.getBoundingBox();
		g = this.pathExtractor.debug(imageDebug+this.fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("image"));
	//		gg.appendChild(g.copy());
		
	
		this.textExtractor = new TextExtractor(this);
		this.textExtractor.extractTexts(this.svgElement);
		textBox = textExtractor.getBoundingBox();
		g = this.textExtractor.debug(textDebug + this.fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("text"));
		gg.appendChild(g.copy());
		
		totalBox = textBox == null ? pathBox : textBox.plus(pathBox);
	
		this.shapeExtractor = new ShapeExtractor(this);
		List<SVGPath> currentPathList = this.pathExtractor.getCurrentPathList();
		this.shapeExtractor.extractShapes(currentPathList, svgElement);
		g = this.shapeExtractor.debug(shapeDebug + fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("shape"));
		gg.appendChild(g.copy());
		
		SVGSVG.wrapAndWriteAsSVG(gg, new File(plotDebug, fileRoot+".debug.svg"));
	}

	/** some plots have publisher cruft outside the limits, especially negative Y.
	 * remove these elements from svgElement
	 * @param plotBox TODO
	 * 
	 */
	public void removeNegativeXorYElements() {
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

	public void removeEmptyTextElements() {
		List<SVGText> texts = SVGText.extractSelfAndDescendantTexts(this.svgElement);
		for (int i = texts.size() - 1; i >= 0; i--) {
			SVGText text = texts.get(i);
			String s = text.getValue();
			if (s == null || "".equals(s.trim())) {
				texts.remove(i);
				text.detach();
			}
		}
	}

	public void createHorizontalAndVerticalLines() {
		LOG.debug("********* make Horizontal/Vertical lines *********");
		SVGSVG.wrapAndWriteAsSVG(horizontalLines, new File(debugRoot+fileRoot+".horiz0.svg"));
		SVGSVG.wrapAndWriteAsSVG(verticalLines, new File(debugRoot+fileRoot+".vert0.svg"));
		SVGSVG.wrapAndWriteAsSVG(svgElement, new File(debugRoot+fileRoot+".debug0.svg"));
		this.horizontalLines = SVGLine.findHorizontalOrVerticalLines(this.shapeExtractor.getLineList(), LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		this.verticalLines = SVGLine.findHorizontalOrVerticalLines(this.shapeExtractor.getLineList(), LineDirection.VERTICAL, AnnotatedAxis.EPS);
		List<SVGPolyline> polylineList = this.shapeExtractor.getPolylineList();
		List<SVGPolyline> axialLShapes = SVGPolyline.findLShapes(polylineList);
		for (int i = axialLShapes.size() - 1; i >= 0; i--) {
			removeLShapesAndReplaceByLines(polylineList, axialLShapes.get(i));
		}
		SVGSVG.wrapAndWriteAsSVG(horizontalLines, new File(debugRoot+fileRoot+".horiz.svg"));
		SVGSVG.wrapAndWriteAsSVG(verticalLines, new File(debugRoot+fileRoot+".vert.svg"));
		List<SVGLine> allLines = new ArrayList<SVGLine>();
		allLines.addAll(horizontalLines);
		allLines.addAll(verticalLines);
		SVGSVG.wrapAndWriteAsSVG(allLines, new File(debugRoot+fileRoot+".lines.svg"));
	}


	private void removeLShapesAndReplaceByLines(List<SVGPolyline> polylineList, SVGPolyline axialLShape) {
		LOG.debug("replacing LShapes by splitLines");
		SVGSVG.wrapAndWriteAsSVG(polylineList, new File(debugRoot+fileRoot+".debug1.svg"));
		SVGLine vLine = axialLShape.getLineList().get(0);
		svgElement.appendChild(vLine);
		this.verticalLines.add(vLine);
		SVGLine hLine = axialLShape.getLineList().get(1);
		svgElement.appendChild(hLine);
		this.horizontalLines.add(hLine);
		polylineList.remove(axialLShape);
		axialLShape.detach();
		SVGSVG.wrapAndWriteAsSVG(polylineList, new File(debugRoot+fileRoot+".debug2.svg"));
	}

	public void createHorizontalAndVerticalTexts() {
		LOG.debug("********* make Horizontal/Vertical texts *********");
		this.horizontalTexts = SVGText.findHorizontalOrRot90Texts(this.textExtractor.getTextList(), LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		this.verticalTexts = SVGText.findHorizontalOrRot90Texts(this.textExtractor.getTextList(), LineDirection.VERTICAL, AnnotatedAxis.EPS);
		
		StringBuilder sb = new StringBuilder();
		for (SVGText verticalText : this.verticalTexts) {
			sb.append("/"+verticalText.getValue());
		}
		LOG.debug("TEXT horiz: " + this.horizontalTexts.size()+"; vert: " + this.verticalTexts.size()+"; " /*+ "/"+sb*/);
	}

	public void makeLongHorizontalAndVerticalEdges() {
		LOG.debug("********* make Horizontal/Vertical edges *********");
		List<SVGLine> lineList = this.shapeExtractor.getLineList();
		if (lineList != null && lineList.size() > 0) {
			lineBbox = SVGElement.createBoundingBox(lineList);
			LOG.debug("lineBBox: "+lineBbox);
			LOG.debug("LINES "+lineList);
			this.longHorizontalEdgeLines = this.getSortedLinesCloseToEdge(this.horizontalLines, LineDirection.HORIZONTAL, lineBbox);
			this.longVerticalEdgeLines = this.getSortedLinesCloseToEdge(this.verticalLines, LineDirection.VERTICAL, lineBbox);
			SVGSVG.wrapAndWriteAsSVG(longHorizontalEdgeLines.getLineList(), new File(debugRoot+fileRoot+".horizEdges.svg"));
			SVGSVG.wrapAndWriteAsSVG(longVerticalEdgeLines.getLineList(), new File(debugRoot+fileRoot+".vertEdges.svg"));
		}
		return;
	}

	// 
	public void makeFullLineBoxAndRanges() {
		LOG.debug("********* make FullineBox and Ranges *********");
		
		this.fullLineBox = null;
		RealRange fullboxXRange = null;
		RealRange fullboxYRange = null;
		if (this.longHorizontalEdgeLines != null && this.longHorizontalEdgeLines.size() > 0) {
			LOG.debug("longHorizontalEdgeLines "+this.longHorizontalEdgeLines.size());
			fullboxXRange = createRange(this.longHorizontalEdgeLines, Direction.HORIZONTAL);
			fullboxXRange = fullboxXRange == null ? null : fullboxXRange.format(PlotBox.FORMAT_NDEC);
		}
		if (this.longVerticalEdgeLines != null && this.longVerticalEdgeLines.size() > 0) {
			LOG.debug("longVerticalEdgeLines "+this.longVerticalEdgeLines.size());
			fullboxYRange = createRange(this.longVerticalEdgeLines, Direction.VERTICAL);
			fullboxYRange = fullboxYRange == null ? null : fullboxYRange.format(PlotBox.FORMAT_NDEC);
		}
		if (fullboxXRange != null && fullboxYRange != null) {
			this.fullLineBox = SVGRect.createFromRealRanges(fullboxXRange, fullboxYRange);
			this.fullLineBox.format(PlotBox.FORMAT_NDEC);
		}
		if (fullLineBox == null && pathBox != null) {
			LOG.debug("path> "+pathBox);
			for (SVGRect rect : shapeExtractor.getRectList()) {
				Real2Range rectRange = rect.getBoundingBox();
				LOG.debug("rect> "+rectRange);
				if (pathBox.isEqualTo(rectRange, axialLinePadding)) {
					fullLineBox = rect;
					break;
				}
			}
		}
		LOG.debug("fullbox "+this.fullLineBox);
	}

	public AxialLineList getSortedLinesCloseToEdge(List<SVGLine> lines, LineDirection direction, Real2Range bbox) {
		RealRange.Direction rangeDirection = direction.isHorizontal() ? RealRange.Direction.HORIZONTAL : RealRange.Direction.VERTICAL;
		RealRange parallelRange = direction.isHorizontal() ? bbox.getXRange() : bbox.getYRange();
		RealRange perpendicularRange = direction.isHorizontal() ? bbox.getYRange() : bbox.getXRange();
		LOG.debug("para "+parallelRange+"; perp "+perpendicularRange);
		AxialLineList axialLineList = new AxialLineList(direction);
		for (SVGLine line : lines) {
			Real2 xy = line.getXY(0);
			Double perpendicularCoord = direction.isHorizontal() ? xy.getY() : xy.getX();
			RealRange lineRange = line.getRealRange(rangeDirection);
			LOG.trace("line: "+line);
			if (lineRange.isEqualTo(parallelRange, axialLinePadding)) {
				LOG.trace("poss axis: "+line);
				if (isCloseToBoxEdge(perpendicularRange, perpendicularCoord)) {
					LOG.debug("close to axis: "+line);
					axialLineList.add(line);
					line.normalizeDirection(AnnotatedAxis.EPS);
				}
			}
		}
		axialLineList.sort();
		return axialLineList;
	}


	private boolean isCloseToBoxEdge(RealRange parallelRange, Double parallelCoord) {
		return Real.isEqual(parallelCoord, parallelRange.getMin(), axialLinePadding) || Real.isEqual(parallelCoord, parallelRange.getMax(), axialLinePadding);
	}


	public SVGElement createSVGElement() {
		SVGG g = new SVGG();
		g.appendChild(copyOriginalElements());
		g.appendChild(shapeExtractor.createSVGAnnotations());
//		g.appendChild(copyAnnotatedAxes());
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

	public Real2Range getPositiveXBox() {
		return positiveXBox;
	}

	public SVGRect getFullLineBox() {
		return fullLineBox;
	}

	public boolean isRemoveWhitespace() {
		return removeWhitespace;
	}

	public PathExtractor getPathExtractor() {
		return pathExtractor;
	}


	public ShapeExtractor getShapeExtractor() {
		return shapeExtractor;
	}


	public AbstractExtractor getTextExtractor() {
		return textExtractor;
	}


	public List<SVGLine> getHorizontalLines() {
		return horizontalLines;
	}


	public List<SVGLine> getVerticalLines() {
		return verticalLines;
	}


	public List<SVGText> getHorizontalTexts() {
		return horizontalTexts;
	}


	public List<SVGText> getVerticalTexts() {
		return verticalTexts;
	}

	public void setFileRoot(String fileRoot) {
		this.fileRoot = fileRoot;
	}

	public String getFileRoot() {
		return fileRoot;
	}


	public File getPlotDebug() {
		return plotDebug;
	}

	public void setPlotDebug(File plotDebug) {
		this.plotDebug = plotDebug;
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

	public List<Real2Range> getImageBoxes() {
		List<Real2Range> boxes = new ArrayList<Real2Range>();
		for (SVGImage image : imageExtractor.getImageList()) {
			Real2Range box = image.getBoundingBox();
			boxes.add(box);
		}
		return boxes;
	}
	
	/** expand bounding boxes and merge
	 * 
	 * @param d
	 */
	public List<Real2Range> getMergedBoundingBoxes(double d) {
		List<Real2Range> boundingBoxes = new ArrayList<Real2Range>();
		for (SVGShape shape : shapeExtractor.getOrCreateAllShapeList()) {
			Real2Range bbox = shape.getBoundingBox();
			bbox.extendBothEndsBy(Direction.HORIZONTAL, d, d);
			bbox.extendBothEndsBy(Direction.VERTICAL, d, d);
			boundingBoxes.add(bbox);
		}
		mergeBoundingBoxes(boundingBoxes);
		return boundingBoxes;
	}

	private void mergeBoundingBoxes(List<Real2Range> boundingBoxes) {
		boolean change = true;
		while (change) {
			Real2Range bbox0 = null;
			Real2Range bbox1 = null;
			Real2Range bbox2 = null;
			change = false;
//			LOG.debug("BBOXES: "+boundingBoxes.size());
			for (int i = 0; i < boundingBoxes.size(); i++) {
				bbox0 = boundingBoxes.get(i);
				for (int j = i + 1; j < boundingBoxes.size(); j++) {
					bbox1 = boundingBoxes.get(j);
					Real2Range bbox3 = bbox0.intersectionWith(bbox1);
					if (bbox3 != null && bbox3.isValid()) {
						bbox2 = bbox0.plus(bbox1);
						change = true;
						break;
					}
				}
				if (change) break;
			}
			if (change) {
				boundingBoxes.remove(bbox0);
				boundingBoxes.remove(bbox1);
				boundingBoxes.add(bbox2);
			}
		}
	}


}

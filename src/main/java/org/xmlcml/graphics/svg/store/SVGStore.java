package org.xmlcml.graphics.svg.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.graphics.svg.SVGDefs;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
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
import org.xmlcml.graphics.svg.plot.AnnotatedAxis;
import org.xmlcml.graphics.svg.plot.PlotBox;

public class SVGStore {
	private static final Logger LOG = Logger.getLogger(SVGStore.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	// FIXME change to getters
	private List<SVGLine> horizontalLines;
	private List<SVGLine> verticalLines;
	private List<SVGText> horizontalTexts;
	private List<SVGText> verticalTexts;

	private PathExtractor pathExtractor;
	private ShapeExtractor shapeExtractor;
	private TextExtractor textExtractor;
	
	
	private Real2Range positiveXBox;

	private String fileRoot;
	private SVGElement svgElement;

	private AxialLineList longHorizontalEdgeLines;
	private AxialLineList longVerticalEdgeLines;
	private SVGRect fullLineBox;
	private PlotBox plotBox; // may not be required
	
	private boolean removeWhitespace = false;


	public SVGStore(PlotBox plotBox) {
		this.plotBox = plotBox;
	}

	
	/** ENTRY METHOD for processing figures.
	 * 
	 * @param svgElement
	 */
	public void readGraphicsComponents(InputStream inputStream) {
		extractGraphicsElements(inputStream);
	}

	public void readAndProcess(SVGElement svgElement) {
		if (svgElement != null) {
			this.extractSVGComponents(svgElement);
			this.createHorizontalAndVerticalLines();
			this.createHorizontalAndVerticalTexts();
			this.makeLongHorizontalAndVerticalEdges();
		}
	}

	public void extractGraphicsElements(SVGElement svgElement) {
		readAndProcess(svgElement);
		this.makeFullLineBoxAndRanges();
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
	//		Real2Range pathBox = pathExtractor.getBoundingBox();
		g = this.pathExtractor.debug("target/paths/"+this.fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("path"));
	//		gg.appendChild(g.copy());
		
	
		this.textExtractor = new TextExtractor(this);
		this.textExtractor.extractTexts(this.svgElement);
		g = this.textExtractor.debug("target/texts/"+this.fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("text"));
		gg.appendChild(g.copy());
		
	//		Real2Range totalBox = textBox.plus(pathBox);
	
		this.shapeExtractor = new ShapeExtractor(this);
		List<SVGPath> currentPathList = this.pathExtractor.getCurrentPathList();
		this.shapeExtractor.extractShapes(currentPathList, svgElement);
		g = this.shapeExtractor.debug("target/shapes/"+fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("shape"));
		gg.appendChild(g.copy());
		
		SVGSVG.wrapAndWriteAsSVG(gg, new File("target/plot/"+fileRoot+".debug.svg"));
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
		this.horizontalLines = SVGLine.findHorizontalOrVerticalLines(this.shapeExtractor.getLineList(), LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		this.verticalLines = SVGLine.findHorizontalOrVerticalLines(this.shapeExtractor.getLineList(), LineDirection.VERTICAL, AnnotatedAxis.EPS);
		List<SVGPolyline> polylineList = this.shapeExtractor.getPolylineList();
		List<SVGPolyline> axialLShapes = SVGPolyline.findLShapes(polylineList);
		for (int i = axialLShapes.size() - 1; i >= 0; i--) {
			SVGPolyline axialLShape = axialLShapes.get(i);
			this.verticalLines.add(axialLShape.getLineList().get(0));
			this.horizontalLines.add(axialLShape.getLineList().get(1));
	//			axialLShapes.remove(i);
			polylineList.remove(axialLShape);
		}
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
			Real2Range lineBbox = SVGElement.createBoundingBox(lineList);
			LOG.debug("LINES "+lineList);
			this.longHorizontalEdgeLines = PlotBox.getSortedLinesCloseToEdge(this.horizontalLines, LineDirection.HORIZONTAL, lineBbox.getXRange());
			this.longVerticalEdgeLines = PlotBox.getSortedLinesCloseToEdge(this.verticalLines, LineDirection.VERTICAL, lineBbox.getYRange());
		}
	}

	public void makeFullLineBoxAndRanges() {
		LOG.debug("********* make FullineBox and Ranges *********");
		this.fullLineBox = null;
		RealRange fullboxXRange = null;
		RealRange fullboxYRange = null;
		if (this.longHorizontalEdgeLines != null && this.longHorizontalEdgeLines.size() > 0) {
			LOG.debug("longHorizontalEdgeLines "+this.longHorizontalEdgeLines.size());
			fullboxXRange = PlotBox.createRange(this, this.longHorizontalEdgeLines, Direction.HORIZONTAL);
			fullboxXRange = fullboxXRange == null ? null : fullboxXRange.format(PlotBox.FORMAT_NDEC);
		}
		if (this.longVerticalEdgeLines != null && this.longVerticalEdgeLines.size() > 0) {
			LOG.debug("longVerticalEdgeLines "+this.longVerticalEdgeLines.size());
			fullboxYRange = PlotBox.createRange(this, this.longVerticalEdgeLines, Direction.VERTICAL);
			fullboxYRange = fullboxYRange == null ? null : fullboxYRange.format(PlotBox.FORMAT_NDEC);
		}
		if (fullboxXRange != null && fullboxYRange != null) {
			this.fullLineBox = SVGRect.createFromRealRanges(fullboxXRange, fullboxYRange);
			this.fullLineBox.format(PlotBox.FORMAT_NDEC);
		}
		LOG.debug("fullbox "+this.fullLineBox);
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


	public TextExtractor getTextExtractor() {
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


}

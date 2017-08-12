package org.xmlcml.graphics.svg.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.omg.PortableInterceptor.HOLDING;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.euclid.util.MultisetUtil;
import org.xmlcml.graphics.svg.GraphicsElement;
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
import org.xmlcml.graphics.svg.StyleAttributeFactory;
import org.xmlcml.graphics.svg.cache.SVGCache.Feature;
import org.xmlcml.graphics.svg.extract.ImageExtractor;
import org.xmlcml.graphics.svg.extract.PathExtractor;
import org.xmlcml.graphics.svg.extract.ShapeExtractor;
import org.xmlcml.graphics.svg.extract.TextExtractor;
import org.xmlcml.graphics.svg.linestuff.AxialLineList;
import org.xmlcml.graphics.svg.plot.AnnotatedAxis;
import org.xmlcml.graphics.svg.plot.PlotBox;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/** stores SVG primitives for access by analysis programs
 * 
 * @author pm286
 *
 */
public class SVGCache {
	private static final Logger LOG = Logger.getLogger(SVGCache.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public enum Feature {
		HORIZONTAL_TEXT_COUNT("htxt"),
		VERTICAL_TEXT_COUNT("vtxt"),
		LINE_COUNT("lines"),
		RECT_COUNT("rects"),
		PATH_COUNT("paths"),
		CIRCLE_COUNT("circs"),
		ELLIPSE_COUNT("ellips"),
		POLYGONS_COUNT("pgons"),
		POLYLINE_COUNT("plines"),
		SHAPE_COUNT("shapes"),
		;
		public String abbrev;

		private Feature(String abbrev) {
			this.abbrev = abbrev;
		}
		public static List<String> getAbbreviations() {
			List<String> abbrevs = new ArrayList<String>();
			for (Feature feature : values()) {
				abbrevs.add(feature.abbrev);
			}
			return abbrevs;
		}
		
		public static Feature getFeatureFromAbbreviation(String abbrev) {
			for (Feature feature : values()) {
				if (feature.abbrev.equals(abbrev)) {
					return feature;
				}
			}
			return null;
		}
		public static List<String> getAbbreviations(List<Feature> features) {
			List<String> abbreviations = new ArrayList<String>();
			for (Feature feature : features) {
				abbreviations.add(feature.abbrev);
			}
			return abbreviations;
		}
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
	private GraphicsElement svgElement;
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
	private boolean splitAtMove = true;
	
	private Real2Range imageBox;
	private Real2Range lineBbox;
	private Real2Range pathBox;
	private Real2Range textBox;
	private Real2Range totalBox;

	private SVGG extractedSVGElement;





	/** this may change as we decide what types of object interact with store
	 * 
	 * @param plotBox
	 */
	public SVGCache(PlotBox plotBox) {
		this.plotBox = plotBox;
	}
	
	public SVGCache() {
	}

	public void readGraphicsComponents(File file) throws FileNotFoundException {
		this.fileRoot = FilenameUtils.getBaseName(file.getName());
		readGraphicsComponents(new FileInputStream(file));
	}
	
	public void readGraphicsComponents(InputStream inputStream) {
		if (inputStream == null) {
			throw new RuntimeException("Null input stream");
		}
		GraphicsElement svgElement = SVGUtil.parseToSVGElement(inputStream);
		readGraphicsComponents(svgElement);
	}

	public void readGraphicsComponents(GraphicsElement svgElement) {
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

	private void extractSVGComponents(GraphicsElement svgElem) {
		LOG.trace("********* made SVG components *********");
		svgElement = (GraphicsElement) svgElem.copy();
		SVGG g;
		SVGG gg = new SVGG();
		extractedSVGElement = new SVGG();
		
		 // is this a good idea? These are clipping boxes. 
		SVGDefs.removeDefs(svgElement);
		StyleAttributeFactory.convertElementAndChildrenFromOldStyleAttributesToCSS(svgElement);
		
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
		
		this.shapeExtractor = new ShapeExtractor(this);
		List<SVGPath> currentPathList = this.pathExtractor.getCurrentPathList();
		this.shapeExtractor.extractShapes(currentPathList, svgElement);
		g = this.shapeExtractor.debug(shapeDebug + fileRoot+".debug.svg");
		List<SVGShape> shapeList = shapeExtractor.getOrCreateConvertedShapeList();
		addElementsToExtractedElement(shapeList);
		g.appendChild(new SVGTitle("shape"));
		gg.appendChild(g.copy());

		this.textExtractor = new TextExtractor(this);
		this.textExtractor.extractTexts(this.svgElement);
		textBox = textExtractor.getBoundingBox();
		g = this.textExtractor.debug(textDebug + this.fileRoot+".debug.svg");
		addElementsToExtractedElement(textExtractor.getTextList());
		
		g.appendChild(new SVGTitle("text"));
		gg.appendChild(g.copy());
		
		totalBox = textBox == null ? pathBox : textBox.plus(pathBox);
		
		SVGSVG.wrapAndWriteAsSVG(gg, new File(plotDebug, fileRoot+".debug.svg"));
//		SVGSVG.wrapAndWriteAsSVG(extractedSVGElement, new File(plotDebug, fileRoot+".debug.svg"));
	}

	private void addElementsToExtractedElement(List<? extends SVGElement> elementList) {
		for (SVGElement element : elementList) {
			SVGElement elementCopy = (SVGElement) element.copy();
			
			StyleAttributeFactory.convertElementAndChildrenFromOldStyleAttributesToCSS(elementCopy);
//			StyleAttributeFactory.createUpdatedStyleAttribute(elementCopy, AttributeStrategy.MERGE);
			extractedSVGElement.appendChild(elementCopy);
		}
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
		LOG.trace("********* make Horizontal/Vertical lines *********");
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
		LOG.trace("replacing LShapes by splitLines");
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
		LOG.trace("********* make Horizontal/Vertical texts *********");
		this.horizontalTexts = SVGText.findHorizontalOrRot90Texts(this.textExtractor.getTextList(), LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		this.verticalTexts = SVGText.findHorizontalOrRot90Texts(this.textExtractor.getTextList(), LineDirection.VERTICAL, AnnotatedAxis.EPS);
		
		StringBuilder sb = new StringBuilder();
		for (SVGText verticalText : this.verticalTexts) {
			sb.append("/"+verticalText.getValue());
		}
		LOG.trace("TEXT horiz: " + this.horizontalTexts.size()+"; vert: " + this.verticalTexts.size()+"; " /*+ "/"+sb*/);
	}

	public void makeLongHorizontalAndVerticalEdges() {
		LOG.trace("********* make Horizontal/Vertical edges *********");
		List<SVGLine> lineList = this.shapeExtractor.getLineList();
		if (lineList != null && lineList.size() > 0) {
			lineBbox = SVGElement.createBoundingBox(lineList);
			this.longHorizontalEdgeLines = this.getSortedLinesCloseToEdge(this.horizontalLines, LineDirection.HORIZONTAL, lineBbox);
			this.longVerticalEdgeLines = this.getSortedLinesCloseToEdge(this.verticalLines, LineDirection.VERTICAL, lineBbox);
			SVGSVG.wrapAndWriteAsSVG(longHorizontalEdgeLines.getLineList(), new File(debugRoot+fileRoot+".horizEdges.svg"));
			SVGSVG.wrapAndWriteAsSVG(longVerticalEdgeLines.getLineList(), new File(debugRoot+fileRoot+".vertEdges.svg"));
		}
		return;
	}

	// 
	public void makeFullLineBoxAndRanges() {
		LOG.trace("********* make FullineBox and Ranges *********");
		
		this.fullLineBox = null;
		RealRange fullboxXRange = null;
		RealRange fullboxYRange = null;
		if (this.longHorizontalEdgeLines != null && this.longHorizontalEdgeLines.size() > 0) {
			fullboxXRange = createRange(this.longHorizontalEdgeLines, Direction.HORIZONTAL);
			fullboxXRange = fullboxXRange == null ? null : fullboxXRange.format(PlotBox.FORMAT_NDEC);
		}
		if (this.longVerticalEdgeLines != null && this.longVerticalEdgeLines.size() > 0) {
			fullboxYRange = createRange(this.longVerticalEdgeLines, Direction.VERTICAL);
			fullboxYRange = fullboxYRange == null ? null : fullboxYRange.format(PlotBox.FORMAT_NDEC);
		}
		if (fullboxXRange != null && fullboxYRange != null) {
			this.fullLineBox = SVGRect.createFromRealRanges(fullboxXRange, fullboxYRange);
			this.fullLineBox.format(PlotBox.FORMAT_NDEC);
		}
		if (fullLineBox == null && pathBox != null) {
			for (SVGRect rect : shapeExtractor.getRectList()) {
				Real2Range rectRange = rect.getBoundingBox();
				if (pathBox.isEqualTo(rectRange, axialLinePadding)) {
					fullLineBox = rect;
					break;
				}
			}
		}
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
				if (isCloseToBoxEdge(perpendicularRange, perpendicularCoord)) {
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


	public GraphicsElement createSVGElement() {
		SVGG g = new SVGG();
		g.appendChild(copyOriginalElements());
		g.appendChild(shapeExtractor.createSVGAnnotations());
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

	public SVGG getExtractedSVGElement() {
		return extractedSVGElement;
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

	public void setSplitAtMove(boolean b) {
		this.splitAtMove = b;
	}

	public boolean getSplitAtMove() {
		return this.splitAtMove;
	}

	/** gets feature values.
	 * Mainly counts of occurrences
	 * null ot zero is replaced by ""
	 * 
	 * @param features
	 * @return
	 */
	public List<String> getFeatureValues(List<Feature> features) {
		List<String> featureValues = new ArrayList<String>();
		for (Feature feature : features) {
			String featureValue = getFeatureValue(feature);
			featureValues.add(featureValue == null || featureValue.equals("0") ? "" : featureValue);
		}
		return featureValues;
	}

	public String getFeatureValue(String abbrev) {
		Feature feature = Feature.getFeatureFromAbbreviation(abbrev);
		return (feature == null) ? null : getFeatureValue(feature);
	}
	public String getFeatureValue(Feature feature) {
		ShapeExtractor shapeExtractor = getShapeExtractor();
		String value = null;
		if (Feature.HORIZONTAL_TEXT_COUNT.equals(feature)) {
			value = String.valueOf(getHorizontalTexts().size());
		} else if (Feature.VERTICAL_TEXT_COUNT.equals(feature)) {
			value = String.valueOf(getVerticalTexts().size());
		} else if(Feature.PATH_COUNT.equals(feature)) {
			value = String.valueOf(shapeExtractor.getPathList().size());
		} else if(Feature.CIRCLE_COUNT.equals(feature)) {
			value = String.valueOf(shapeExtractor.getCircleList().size());
		} else if(Feature.ELLIPSE_COUNT.equals(feature)) {
			value = String.valueOf(shapeExtractor.getEllipseList().size());
		} else if(Feature.LINE_COUNT.equals(feature)) {
			value = String.valueOf(shapeExtractor.getLineList().size());
		} else if(Feature.POLYGONS_COUNT.equals(feature)) {
			value = String.valueOf(shapeExtractor.getPolygonList().size());
		} else if(Feature.POLYLINE_COUNT.equals(feature)) {
			value = String.valueOf(shapeExtractor.getPolylineList().size());
		} else if(Feature.RECT_COUNT.equals(feature)) {
			value = String.valueOf(shapeExtractor.getRectList().size());
		} else if(Feature.SHAPE_COUNT.equals(feature)) {
			value = String.valueOf(shapeExtractor.getShapeList().size());
		} else {
			LOG.warn("No extractor for "+feature);
		}
		return value;
	}

	public Multiset<String> getHorizontalTextStyleMultiset() {
		return getTextStyleMultiset(horizontalTexts);
	}

	public Multiset<String> getVerticalTextStyles() {
		return getTextStyleMultiset(verticalTexts);
	}

	private Multiset<String> getTextStyleMultiset(List<SVGText> texts) {
		Multiset<String> styleSet = HashMultiset.create();
		for (SVGText text : texts) {
			String style = text.getStyle();
			style = style.replaceAll("clip-path\\:url\\(#clipPath\\d+\\);", "");
			styleSet.add(style);
		}
		return styleSet;
	}

	/** replaces long form os style by abbreviations.
	 * remove clip-paths
	 * Remove String values and attributes
	 * "font-family, Helvetica, font-weight, normal,font-size, px, font-style, #fff(fff), stroke, none, fill"
	 * 
	 * resultant string is of form:
	 * color (optional)
	 * ~ // serif (optional)
	 * ddd // font-size x 10 (mandatory)
	 * B // bold (optional
	 * I // italic (optional
	 * stroke (optional)
	 * 
	 * colors are flattened to hex hex hex
	 * color abbreviations (with some tolerances)
	 * . grey
	 * * black
	 * r g b
	 * 
	 * @return
	 */
	public Multiset<String> createAbbreviatedHorizontalTextStyleMultiset() {
		Multiset<String> styleSet = getHorizontalTextStyleMultiset();
		Multiset<String> abbreviatedStyleSet = HashMultiset.create();
		for (Multiset.Entry<String> entry : styleSet.entrySet()) {
			int count = entry.getCount();
			String style = entry.getElement();
			style = abbreviateStyle(style);
			abbreviatedStyleSet.add(style, count);
		}
		return abbreviatedStyleSet;
	}
	
	/** replaces long form of style by abbreviations.
	 * remove clip-paths
	 * Remove String values and attributes
	 * "font-family, Helvetica, font-weight, normal,font-size, px, font-style, #fff(fff), stroke, none, fill"
	 * 
	 * resultant string is of form:
	 * color (optional)
	 * ~ // serif (optional)
	 * ddd // font-size x 10 (mandatory)
	 * B // bold (optional
	 * I // italic (optional
	 * stroke (optional)
	 * 
	 * colors are flattened to hex hex hex
	 * color abbreviations (with some tolerances)
	 * . grey
	 * * black
	 * r g b
	 * 
	 * @return
	 */

	public static String abbreviateStyle(String style) {
		style = style.replaceAll("font-family:", "");
		style = style.replaceAll("TimesNewRoman;", "~");
		style = style.replaceAll("Helvetica;", "");
		style = style.replaceAll("font-weight:", "");
		style = style.replaceAll("normal;", "");
		style = style.replaceAll("bold;", "B");
		style = style.replaceAll("font-size:", "");
		style = style.replaceAll("(\\d+)\\.(\\d)\\d*", "$1$2");
		style = style.replaceAll("px;", "");
		style = style.replaceAll("font-style:", "");
		style = style.replaceAll("italic;", "I");
		style = style.replaceAll("#(.)(.)(.)(.)(.)(.);", "#$1$3$5;"); // compress rgb
		style = style.replaceAll("#000;", "*");
		style = style.replaceAll("#fff;", "");
		style = style.replaceAll("#[12][12][12];", "."); // grey
		style = style.replaceAll("#[012][012][cdef];", "b");
		style = style.replaceAll("#[012][cdef][012];", "g");
		style = style.replaceAll("#[cdef][012][012];", "r");
		style = style.replaceAll("stroke:", "");
		style = style.replaceAll("none;", "");
		style = style.replaceAll("fill:", "");
		style = style.replaceAll("clip-path\\:url\\(#clipPath\\d+\\);", "");
		return style;
	}


}

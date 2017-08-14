package org.xmlcml.graphics.svg.cache;

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
import org.xmlcml.graphics.svg.linestuff.AxialLineList;
import org.xmlcml.graphics.svg.plot.AnnotatedAxis;
import org.xmlcml.graphics.svg.plot.PlotBox;

/** stores SVG primitives for access by analysis programs
 * 
 * @author pm286
 *
 */
public class SVGCache {
	public static final Logger LOG = Logger.getLogger(SVGCache.class);
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
		
		LONG_HORIZONTAL_RULE_COUNT("hr"),
		SHORT_HORIZONTAL_RULE_COUNT("shr"),
		TOP_HORIZONTAL_RULE_COUNT("thr"),
		BOTTOM_HORIZONTAL_RULE_COUNT("bhr"),
		LONG_HORIZONTAL_RULE_THICKNESS_COUNT("hrthick"),
		HORIZONTAL_PANEL_COUNT("hpanel"),
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
	

	public ImageCache imageCache;
	private PathCache pathCache;
	ShapeCache shapeExtractor; // package since used between LineCache and RectCache
	private TextCache textExtractor;
	private LineCache lineCache;
	private RectCache rectCache;
	
	private Real2Range positiveXBox;

	public String fileRoot;
	public GraphicsElement svgElement;
	public String debugRoot = "target/debug/";
	private String imageDebug = "target/images/";
	private String pathDebug = "target/paths/";
	private String shapeDebug = "target/shapes/";
	private String textDebug = "target/texts/";
	private File plotDebug = new File("target/plots/");

	private PlotBox plotBox; // may not be required
	
	private boolean removeWhitespace = false;
	private boolean removeDuplicatePaths = true;
	private boolean splitAtMove = true;
	
	public Real2Range imageBox;
	public Real2Range pathBox;
	private Real2Range textBox;
	private Real2Range totalBox;

	private SVGG extractedSVGElement;

	/** this may change as we decide what types of object interact with store
	 * may need to move to GraphicsCache
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
		SVGElement svgElement = SVGUtil.parseToSVGElement(inputStream);
		readGraphicsComponents(svgElement);
	}

	public void readGraphicsComponents(SVGElement svgElement) {
		if (svgElement != null) {
			this.extractSVGComponents(svgElement);
			lineCache = new LineCache(this);
			lineCache.createHorizontalAndVerticalLines(svgElement);
			lineCache.makeLongHorizontalAndVerticalEdges();
			lineCache.makeFullLineBoxAndRanges();
			textExtractor.createHorizontalAndVerticalTexts();
		} else {
			throw new RuntimeException("Null svgElement");
		}
	}

	private void extractSVGComponents(GraphicsElement svgElem) {
		svgElement = (GraphicsElement) svgElem.copy();
		extractedSVGElement = new SVGG();
		
		 // is this a good idea? These are clipping boxes. 
		SVGDefs.removeDefs(svgElement);
		StyleAttributeFactory.convertElementAndChildrenFromOldStyleAttributesToCSS(svgElement);
		
		positiveXBox = new Real2Range(new RealRange(-100., 10000), new RealRange(-10., 10000));
		removeEmptyTextElements();
		removeNegativeXorYElements();
		
		getOrCreatePathExtractor();
		extractedSVGElement.getOrCreateImageCache(this);
		getOrCreateShapeCache();
		getOrCreateTextCache();
		totalBox = textBox == null ? pathBox : textBox.plus(pathBox);
		
		debugComponents();
	}

	private void debugComponents() {
		SVGG g;
		SVGG gg = new SVGG();
		g = this.pathCache.debug(pathDebug+this.fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("path"));
	//		gg.appendChild(g.copy());
		
		
		g = this.imageCache.debug(imageDebug+this.fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("image"));
	//		gg.appendChild(g.copy());
		
		
		g = this.shapeExtractor.debug(shapeDebug + fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("shape"));
		gg.appendChild(g.copy());

		
		g = this.textExtractor.debug(textDebug + this.fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("text"));
		gg.appendChild(g.copy());
		
		
		SVGSVG.wrapAndWriteAsSVG(gg, new File(plotDebug, fileRoot+".debug.svg"));
	}

	public PathCache getOrCreatePathExtractor() {
		if (pathCache == null) {
			this.pathCache = new PathCache(this);
			this.pathCache.extractPaths(this.svgElement);
			pathBox = pathCache.getBoundingBox();
		}
		return pathCache;
	}

	public TextCache getOrCreateTextCache() {
		if (textExtractor == null) {
			this.textExtractor = new TextCache(this);
			this.textExtractor.extractTexts(this.svgElement);
			textBox = textExtractor.getBoundingBox();
			addElementsToExtractedElement(textExtractor.getTextList());
		}
		return textExtractor;
	}

	public ShapeCache getOrCreateShapeCache() {
		if (shapeExtractor == null) {
			this.shapeExtractor = new ShapeCache(this);
			List<SVGPath> currentPathList = this.pathCache.getCurrentPathList();
			this.shapeExtractor.extractShapes(currentPathList, svgElement);
			List<SVGShape> shapeList = shapeExtractor.getOrCreateConvertedShapeList();
			addElementsToExtractedElement(shapeList);
		}
		return shapeExtractor;
	}

	public LineCache getOrCreateLineCache() {
		if (lineCache == null) {
			this.lineCache = new LineCache(this);
		}
		return lineCache;
	}

	public RectCache getOrCreateRectCache() {
		if (rectCache == null) {
			this.rectCache = new RectCache(this);
		}
		return rectCache;
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



	public GraphicsElement createSVGElement() {
		SVGG g = new SVGG();
		g.appendChild(copyOriginalElements());
		g.appendChild(shapeExtractor.createSVGAnnotations());
		g.appendChild(pathCache.createSVGAnnotation().copy());
		return g;
	}
	
	private SVGG copyOriginalElements() {
		SVGG g = new SVGG();
		ShapeCache.addList(g, new ArrayList<SVGPath>(pathCache.getOriginalPathList()));
		ShapeCache.addList(g, new ArrayList<SVGText>(textExtractor.getTextList()));
		g.setStroke("pink");
		return g;
	}

	public Real2Range getPositiveXBox() {
		return positiveXBox;
	}

	public boolean isRemoveWhitespace() {
		return removeWhitespace;
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


	public List<Real2Range> getImageBoxes() {
		List<Real2Range> boxes = new ArrayList<Real2Range>();
		for (SVGImage image : imageCache.getImageList()) {
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
		ShapeCache shapeExtractor = getOrCreateShapeCache();
		String value = null;
		if (Feature.HORIZONTAL_TEXT_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateTextCache().getHorizontalTexts().size());
		} else if (Feature.VERTICAL_TEXT_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateTextCache().getVerticalTexts().size());
		} else if(Feature.PATH_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateShapeCache().getPathList().size());
		} else if(Feature.CIRCLE_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateShapeCache().getCircleList().size());
		} else if(Feature.ELLIPSE_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateShapeCache().getEllipseList().size());
		} else if(Feature.LINE_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateShapeCache().getLineList().size());
		} else if(Feature.POLYGONS_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateShapeCache().getPolygonList().size());
		} else if(Feature.POLYLINE_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateShapeCache().getPolylineList().size());
		} else if(Feature.RECT_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateShapeCache().getRectList().size());
		} else if(Feature.SHAPE_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateShapeCache().getShapeList().size());
		} else if(Feature.LONG_HORIZONTAL_RULE_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateLineCache().getLongHorizontalLineList().size());
		} else if(Feature.SHORT_HORIZONTAL_RULE_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateLineCache().getShortHorizontalLineList().size());
		} else if(Feature.TOP_HORIZONTAL_RULE_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateLineCache().getTopHorizontalLineList().size());
		} else if(Feature.BOTTOM_HORIZONTAL_RULE_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateLineCache().getBottomHorizontalLineList().size());
		} else if(Feature.LONG_HORIZONTAL_RULE_THICKNESS_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateLineCache().getHorizontalLineStrokeWidthSet().size());
		} else if(Feature.HORIZONTAL_PANEL_COUNT.equals(feature)) {
			value = String.valueOf(getOrCreateRectCache().getHorizontalPanelList().size());
		} else {
			LOG.warn("No extractor for "+feature);
		}
		return value;
	}

}

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
public class ComponentCache extends AbstractCache {
	public static final Logger LOG = Logger.getLogger(ComponentCache.class);
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
	
	// =======================================
	
	public static int ZERO_PLACES = 0;
	

	public ImageCache imageCache;
	private PathCache pathCache;
	private TextCache textCache;
	private LineCache lineCache;
	private RectCache rectCache;
	// shapeCache is in superclass
	
	private Real2Range positiveXBox;

	public String fileRoot;
	public SVGElement svgElement;
	public String debugRoot = "target/debug/";
	private String imageDebug = "target/images/";
	private String pathDebug = "target/paths/";
	private String shapeDebug = "target/shapes/";
	private String textDebug = "target/texts/";
	private File plotDebug = new File("target/plots/");

	private PlotBox plotBox; // may not be required
	
	private boolean removeWhitespace = false;
//	private boolean removeDuplicatePaths = true;
	private boolean splitAtMove = true;
	
	public Real2Range imageBox;
	public Real2Range pathBox;
	private Real2Range textBox;
	private Real2Range totalBox;

	private SVGG extractedSVGElement;
	List<SVGElement> allElementList;
	List<Real2Range> boundingBoxList;

	/** this may change as we decide what types of object interact with store
	 * may need to move to GraphicsCache
	 * 
	 * @param plotBox
	 */
	public ComponentCache(PlotBox plotBox) {
		this.plotBox = plotBox;
	}
	
	public ComponentCache() {
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
		} else {
			throw new RuntimeException("Null svgElement");
		}
	}

	private void extractSVGComponents(SVGElement svgElem) {
		svgElement = (SVGElement) svgElem.copy();
		extractedSVGElement = new SVGG();
		
		 // is this a good idea? These are clipping boxes. 
		SVGDefs.removeDefs(svgElement);
		StyleAttributeFactory.convertElementAndChildrenFromOldStyleAttributesToCSS(svgElement);
		
		positiveXBox = new Real2Range(new RealRange(-100., 10000), new RealRange(-10., 10000));
		removeEmptyTextElements();
		removeNegativeXorYElements();
		
		getOrCreatePathCache();
		getOrCreateImageCache();
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
		
		
		g = this.shapeCache.debug(shapeDebug + fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("shape"));
		gg.appendChild(g.copy());

		
		g = this.textCache.debug(textDebug + this.fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("text"));
		gg.appendChild(g.copy());
		
		
		SVGSVG.wrapAndWriteAsSVG(gg, new File(plotDebug, fileRoot+".debug.svg"));
	}

	
	public PathCache getOrCreatePathCache() {
		if (pathCache == null) {
			this.pathCache = new PathCache(this);
			this.pathCache.extractPaths(this.svgElement);
			pathBox = pathCache.getBoundingBox();
		}
		return pathCache;
	}

	public ImageCache getOrCreateImageCache() {
		if (imageCache == null) {
			this.imageCache = new ImageCache(this);
			this.imageCache.getOrCreateImageList();
			imageBox = imageCache.getBoundingBox();
		}
		return imageCache;
	}

	public TextCache getOrCreateTextCache() {
		if (textCache == null) {
			this.textCache = new TextCache(this);
			this.textCache.extractTexts(this.svgElement);
			textBox = textCache.getBoundingBox();
			addElementsToExtractedElement(textCache.getTextList());
			textCache.createHorizontalAndVerticalTexts();
		}
		return textCache;
	}

	public ShapeCache getOrCreateShapeCache() {
		if (shapeCache == null) {
			this.shapeCache = new ShapeCache(this);
			List<SVGPath> currentPathList = this.pathCache.getCurrentPathList();
			this.shapeCache.extractShapes(currentPathList, svgElement);
			List<SVGShape> shapeList = shapeCache.getOrCreateConvertedShapeList();
			addElementsToExtractedElement(shapeList);
		}
		return shapeCache;
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
		g.appendChild(shapeCache.createSVGAnnotations());
		g.appendChild(pathCache.createSVGAnnotation().copy());
		return g;
	}
	
	private SVGG copyOriginalElements() {
		SVGG g = new SVGG();
		ShapeCache.addList(g, new ArrayList<SVGPath>(pathCache.getOriginalPathList()));
		ShapeCache.addList(g, new ArrayList<SVGText>(textCache.getTextList()));
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
		for (SVGShape shape : shapeCache.getOrCreateAllShapeList()) {
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
		String value = null;
		getOrCreateCaches();
		if (Feature.HORIZONTAL_TEXT_COUNT.equals(feature)) {
			value = String.valueOf(textCache.getOrCreateHorizontalTexts().size());
		} else if (Feature.VERTICAL_TEXT_COUNT.equals(feature)) {
			value = String.valueOf(textCache.getOrCreateVerticalTexts().size());
		} else if(Feature.PATH_COUNT.equals(feature)) {
			value = String.valueOf(shapeCache.getPathList().size());
		} else if(Feature.CIRCLE_COUNT.equals(feature)) {
			value = String.valueOf(shapeCache.getCircleList().size());
		} else if(Feature.ELLIPSE_COUNT.equals(feature)) {
			value = String.valueOf(shapeCache.getEllipseList().size());
		} else if(Feature.LINE_COUNT.equals(feature)) {
			value = String.valueOf(shapeCache.getLineList().size());
		} else if(Feature.POLYGONS_COUNT.equals(feature)) {
			value = String.valueOf(shapeCache.getPolygonList().size());
		} else if(Feature.POLYLINE_COUNT.equals(feature)) {
			value = String.valueOf(shapeCache.getPolylineList().size());
		} else if(Feature.RECT_COUNT.equals(feature)) {
			value = String.valueOf(shapeCache.getRectList().size());
		} else if(Feature.SHAPE_COUNT.equals(feature)) {
			value = String.valueOf(shapeCache.getShapeList().size());
		} else if(Feature.LONG_HORIZONTAL_RULE_COUNT.equals(feature)) {
			value = String.valueOf(lineCache.getOrCreateLongHorizontalLineList().size());
		} else if(Feature.SHORT_HORIZONTAL_RULE_COUNT.equals(feature)) {
			value = String.valueOf(lineCache.getShortHorizontalLineList().size());
		} else if(Feature.TOP_HORIZONTAL_RULE_COUNT.equals(feature)) {
			value = String.valueOf(lineCache.getTopHorizontalLineList().size());
		} else if(Feature.BOTTOM_HORIZONTAL_RULE_COUNT.equals(feature)) {
			value = String.valueOf(lineCache.getBottomHorizontalLineList().size());
		} else if(Feature.LONG_HORIZONTAL_RULE_THICKNESS_COUNT.equals(feature)) {
			value = String.valueOf(lineCache.getHorizontalLineStrokeWidthSet().size());
		} else if(Feature.HORIZONTAL_PANEL_COUNT.equals(feature)) {
			value = String.valueOf(rectCache.getHorizontalPanelList().size());
		} else {
			LOG.warn("No cache for "+feature);
		}
		return value;
	}

	public Real2Range getBoundingBox() {
		return totalBox;
	}

	/** aggregates all elements, include derived ones.
	 * 
	 * @return single list of all raw and derived SVGElements
	 */
	@Override
	public List<? extends SVGElement> getOrCreateElementList() {
		if (allElementList == null) {
			allElementList = new ArrayList<SVGElement>();
			getOrCreateCaches();
			// don't add paths as we have already converted to shapes
//			allElementList.addAll(pathCache.getOrCreateElementList());
			allElementList.addAll(imageCache.getOrCreateElementList());
			allElementList.addAll(shapeCache.getOrCreateElementList());
			allElementList.addAll(rectCache.getOrCreateElementList());
			allElementList.addAll(lineCache.getOrCreateElementList());
			// this goes last in case it would be hidden
			allElementList.addAll(textCache.getOrCreateElementList());
		}
		return allElementList;
	}

	private void getOrCreateCaches() {
		getOrCreatePathCache();
		getOrCreateTextCache();
		getOrCreateImageCache();
		getOrCreateShapeCache();
		getOrCreateRectCache();
		getOrCreateLineCache();
	}

	public List<Real2Range> getBoundingBoxList() {
		if (boundingBoxList == null) {
			boundingBoxList = new ArrayList<Real2Range>();
			getOrCreateElementList();
			for (SVGElement element : allElementList) {
				boundingBoxList.add(element.getBoundingBox());
			}
		}
		return boundingBoxList;
	}

}

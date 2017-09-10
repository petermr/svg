package org.xmlcml.graphics.svg.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.Int2Range;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.graphics.svg.GraphicsElement;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGDefs;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGImage;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.SVGTitle;
import org.xmlcml.graphics.svg.SVGUtil;
import org.xmlcml.graphics.svg.StyleAttributeFactory;
import org.xmlcml.graphics.svg.plot.SVGMediaBox;
import org.xmlcml.graphics.svg.util.SuperPixelArray;

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
		// texts
		HORIZONTAL_TEXT_COUNT("htxt"),
		HORIZONTAL_TEXT_STYLE_COUNT("htsty"),
		VERTICAL_TEXT_COUNT("vtxt"),
		VERTICAL_TEXT_STYLE_COUNT("vtsty"),
		// shapes
		LINE_COUNT("lines"),
		RECT_COUNT("rects"),
		PATH_COUNT("paths"),
		CIRCLE_COUNT("circs"),
		ELLIPSE_COUNT("ellips"),
		POLYGONS_COUNT("pgons"),
		POLYLINE_COUNT("plines"),
		SHAPE_COUNT("shapes"),
		// rules
		LONG_HORIZONTAL_RULE_COUNT("lnghr"),
		SHORT_HORIZONTAL_RULE_COUNT("shthr"),
		TOP_HORIZONTAL_RULE_COUNT("tophr"),
		BOTTOM_HORIZONTAL_RULE_COUNT("bothr"),
		LONG_HORIZONTAL_RULE_THICKNESS_COUNT("hrthick"),
		// panels
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
		
		public final static List<Feature> TEXT_SHAPE_FEATURES = Arrays.asList(new Feature[] {
				Feature.HORIZONTAL_TEXT_COUNT,
				Feature.HORIZONTAL_TEXT_STYLE_COUNT,
				Feature.VERTICAL_TEXT_COUNT,
				Feature.VERTICAL_TEXT_STYLE_COUNT,
				
				Feature.LINE_COUNT,
				Feature.RECT_COUNT,
				Feature.PATH_COUNT,
				Feature.CIRCLE_COUNT,
				Feature.ELLIPSE_COUNT,
				Feature.POLYGONS_COUNT,
				Feature.POLYLINE_COUNT,
				Feature.SHAPE_COUNT,
			}
			);

		public final static List<Feature> RECT_LINE_FEATURES = Arrays.asList(new Feature[] {
				Feature.LONG_HORIZONTAL_RULE_COUNT,
				Feature.SHORT_HORIZONTAL_RULE_COUNT,
				Feature.TOP_HORIZONTAL_RULE_COUNT,
				Feature.BOTTOM_HORIZONTAL_RULE_COUNT,
				Feature.LONG_HORIZONTAL_RULE_THICKNESS_COUNT,
				Feature.HORIZONTAL_PANEL_COUNT,
			}
			);

	}

	public static final String FILE = "file";

	public final static String MAJOR_COLORS[] = {
			"red",
			"green",
			"blue",
			"cyan",
			"magenta",
			"yellow",
			"pink",
			"gray",
			"purple",
		};



	// =======================================
	
	public static int ZERO_PLACES = 0;
	

	private ImageCache imageCache;
	private PathCache pathCache;
	private TextCache textCache;
	private LineCache lineCache;
	private RectCache rectCache;
	private ShapeCache shapeCache;
	// other caches as they are developed
	private List<AbstractCache> abstractCacheList;
	
	private Real2Range positiveXBox;

	public String fileRoot;
	public SVGElement originalSvgElement;
	public String debugRoot = "target/debug/";
	private String imageDebug = "target/images/";
	private String pathDebug = "target/paths/";
	private String shapeDebug = "target/shapes/";
	private String textDebug = "target/texts/";
	private File plotDebug = new File("target/plots/");

	private boolean removeWhitespace = false;
	private boolean splitAtMove = true;
	
	private SVGG extractedSVGElement;
	List<SVGElement> allElementList;
	List<Real2Range> boundingBoxList;

	private List<Real2> whitespaceSpixels;

	private double outerBoxEps = 3.0; // outer bbox error


	/** this may change as we decide what types of object interact with store
	 * may need to move to AbstractCache
	 * 
	 * @param plotBox
	 */
	public ComponentCache(SVGMediaBox plotBox) {
		super(plotBox);
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
		readGraphicsComponentsAndMakeCaches(svgElement);
	}

	public void readGraphicsComponentsAndMakeCaches(SVGElement svgElement) {
		if (svgElement != null) {
			this.extractSVGComponentsAndMakeCaches(svgElement);
		} else {
			throw new RuntimeException("Null svgElement");
		}
	}

	private void extractSVGComponentsAndMakeCaches(SVGElement svgElem) {
		originalSvgElement = (SVGElement) svgElem.copy();
		extractedSVGElement = new SVGG();
		
		 // is this a good idea? These are clipping boxes. 
		SVGDefs.removeDefs(originalSvgElement);
		StyleAttributeFactory.convertElementAndChildrenFromOldStyleAttributesToCSS(originalSvgElement);
		
		positiveXBox = new Real2Range(new RealRange(-100., 10000), new RealRange(-10., 10000));
		removeEmptyTextElements();
		removeNegativeXorYElements();
		
		getOrCreateCascadingCaches();
		lineCache.createSpecializedLines();

		debugComponents();
	}

	private void debugComponents() {
		SVGG g;
		SVGG gg = new SVGG();
		g = this.getOrCreatePathCache().debugToSVG(pathDebug+this.fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("path"));
	//		gg.appendChild(g.copy());
		
		
		g = this.imageCache.debugToSVG(imageDebug+this.fileRoot+".debug.svg");
		g.appendChild(new SVGTitle("image"));
	//		gg.appendChild(g.copy());
		
		
		g = this.getOrCreateShapeCache().debugToSVG(shapeDebug + fileRoot+".debug.svg");
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
			this.pathCache.extractPaths(this.originalSvgElement);
		}
		return pathCache;
	}

	public ImageCache getOrCreateImageCache() {
		if (imageCache == null) {
			this.imageCache = new ImageCache(this);
			this.imageCache.getOrCreateImageList();
		}
		return imageCache;
	}

	public TextCache getOrCreateTextCache() {
		if (textCache == null) {
			this.textCache = new TextCache(this);
			if (this.originalSvgElement != null) {
				this.textCache.extractTexts(this.originalSvgElement);
				addElementsToExtractedElement(textCache.getTextList());
				textCache.createHorizontalAndVerticalTexts();
			}
		}
		return textCache;
	}

	public ShapeCache getOrCreateShapeCache() {
		if (shapeCache == null) {
			shapeCache = new ShapeCache(this);
			List<SVGPath> currentPathList = this.pathCache.getCurrentPathList();
			this.getOrCreateShapeCache().extractShapes(currentPathList, originalSvgElement);
			List<SVGShape> shapeList = /*getOrCreateShapeCache()*/shapeCache.getOrCreateConvertedShapeList();
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
		List<SVGText> texts = SVGText.extractSelfAndDescendantTexts(originalSvgElement);
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
		List<SVGText> texts = SVGText.extractSelfAndDescendantTexts(this.originalSvgElement);
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
		g.appendChild(getOrCreateShapeCache().createSVGAnnotations());
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
		for (SVGShape shape : getOrCreateShapeCache().getOrCreateAllShapeList()) {
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
		getOrCreateCascadingCaches();
		if (Feature.HORIZONTAL_TEXT_COUNT.equals(feature)) {
			value = String.valueOf(textCache.getOrCreateHorizontalTexts().size());
		} else if (Feature.HORIZONTAL_TEXT_STYLE_COUNT.equals(feature)) {
			value = String.valueOf(textCache.getOrCreateHorizontalTextStyleMultiset().entrySet().size());
		} else if (Feature.VERTICAL_TEXT_COUNT.equals(feature)) {
			value = String.valueOf(textCache.getOrCreateVerticalTexts().size());
		} else if (Feature.VERTICAL_TEXT_STYLE_COUNT.equals(feature)) {
			value = String.valueOf(textCache.getOrCreateVerticalTextStyleMultiset().entrySet().size());
			
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
			value = String.valueOf(lineCache.getOrCreateLongHorizontalLineList().size());
		} else if(Feature.SHORT_HORIZONTAL_RULE_COUNT.equals(feature)) {
			value = String.valueOf(lineCache.getOrCreateShortHorizontalLineList().size());
		} else if(Feature.TOP_HORIZONTAL_RULE_COUNT.equals(feature)) {
			value = String.valueOf(lineCache.getTopHorizontalLineList().size());
		} else if(Feature.BOTTOM_HORIZONTAL_RULE_COUNT.equals(feature)) {
			value = String.valueOf(lineCache.getBottomHorizontalLineList().size());
		} else if(Feature.LONG_HORIZONTAL_RULE_THICKNESS_COUNT.equals(feature)) {
			value = String.valueOf(lineCache.getHorizontalLineStrokeWidthSet().entrySet().size());
			
		} else if(Feature.HORIZONTAL_PANEL_COUNT.equals(feature)) {
			value = String.valueOf(rectCache.getOrCreateHorizontalPanelList().size());
			
		} else {
			LOG.warn("No cache for "+feature);
		}
		return value;
	}

	/* cached boundingBox.
	 * The bbox may be reset 
	 * 
	 */
	public Real2Range getBoundingBox() {
		if (boundingBox == null) {
			Real2Range imageBox = getOrCreateImageCache().getBoundingBox();
			Real2Range textBox = getOrCreateTextCache().getBoundingBox();
			Real2Range pathBox = getOrCreatePathCache().getBoundingBox();
			addBoxToTotalBox(textBox);
			addBoxToTotalBox(pathBox);
			addBoxToTotalBox(imageBox);
		}
		return boundingBox;
	}

	private void addBoxToTotalBox(Real2Range box) {
		if (boundingBox == null && box != null) {
			boundingBox = new Real2Range(box);
		} else if (box != null) {
			boundingBox = boundingBox.plus(box);
		} else {
			// leave bounding box as is
		}
	}

	/** aggregates all elements, include derived ones.
	 * 
	 * @return single list of all raw and derived SVGElements
	 */
	@Override
	public List<? extends SVGElement> getOrCreateElementList() {
		if (allElementList == null) {
			allElementList = new ArrayList<SVGElement>();
			getOrCreateCascadingCaches();
			// don't add paths as we have already converted to shapes
//			allElementList.addAll(pathCache.getOrCreateElementList());
			allElementList.addAll(imageCache.getOrCreateElementList());
			allElementList.addAll(getOrCreateShapeCache().getOrCreateElementList());
			allElementList.addAll(rectCache.getOrCreateElementList());
			allElementList.addAll(lineCache.getOrCreateElementList());
			// this goes last in case it would be hidden
			allElementList.addAll(textCache.getOrCreateElementList());
		}
		return allElementList;
	}

	/** creates caches in order
	 * path, text , image are primitives
	 * then path->shape->line->rect
	 * 
	 * each subsequent cache removes the elemnts from any earlier one it uses.
	 * the original SVG element is NOT altered.
	 * 
	 * There is some backtracking required. For example later elements (e.g. borderingRect)
	 * may be removed and this will affect not only the rect list but also the bounding boxes.
	 * These in turn affect the judgment of axial lines and long lines.
	 * 
	 * 
	 */
	public void getOrCreateCascadingCaches() {
		getOrCreatePathCache();
		getOrCreateTextCache();
		getOrCreateImageCache();
		
		// first pass creates raw caches which may be elaborated later
		getOrCreateShapeCache();
		getOrCreateLineCache();
		getOrCreateRectCache();
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

	public List<Real2> getWhitespaces(double dx, double dy) {
		List<Real2Range> boundingBoxList = this.getBoundingBoxList();
		Real2Range box = getBoundingBox()
				.getReal2RangeExtendedInX(dx, dy).getReal2RangeExtendedInY(dx, dy);
		RealRange xRange = box.getRealRange(Direction.HORIZONTAL);
		RealRange yRange = box.getRealRange(Direction.VERTICAL);
		List<Real2> whitespaces = new ArrayList<Real2>();
		for (double xx = xRange.getMin(); xx < xRange.getMax(); xx+=dx) {
			for (double yy = yRange.getMin(); yy < yRange.getMax(); yy+=dy) {
				boolean inside = false;
				Real2[] xy = new Real2[]{
						new Real2(xx+dx/2, yy+dx/2),
						new Real2(xx+dx/2, yy-dx/2),
						new Real2(xx-dx/2, yy+dx/2),
						new Real2(xx-dx/2, yy-dx/2),
				};
				for (int k = 0; k < boundingBoxList.size(); k++) {
					for (Real2 xy0 : xy) {
						if (boundingBoxList.get(k).includes(xy0)) {
							inside = true;
							break;
						}
					}
				}
				if (!inside) {
					whitespaces.add(new Real2(xx, yy));
				}
			}
		}
		return whitespaces;
	}

	public SuperPixelArray getWhitespaceSuperPixelArray(double dx, double dy) {
		List<Real2> whitespaces = getWhitespaces(dx, dy);
		SuperPixelArray superPixelArray = new SuperPixelArray(new Int2Range(this.getBoundingBox()));
		for (Real2 whitespace : whitespaces) {
			superPixelArray.setPixel(1, Int2.getInt2(whitespace));
		}
		return superPixelArray;
	}

	public SuperPixelArray getWhitespaceSuperPixelArray(List<Real2Range> boundingBoxLists) {
		SuperPixelArray superPixelArray = new SuperPixelArray(new Int2Range(this.getBoundingBox()));
		for (Real2Range boundingBox : boundingBoxLists) {
			superPixelArray.setPixels(1, new Int2Range(boundingBox));
		}
		return superPixelArray;
	}

	public SVGG createWhitespaceG(double dx, double dy) {
		whitespaceSpixels = getWhitespaces(dx, dy);
		SVGG gg = new SVGG();
		for (Real2 xy : whitespaceSpixels) {
			gg.appendChild(new SVGCircle(xy, dx/2.));
		}
		return gg;
	}

	@Override
	public String toString() {
		String s = ""
		+"image: "+String.valueOf(imageCache)+"\n"
		+"path: "+String.valueOf(pathCache)+"\n"
		+"text: "+String.valueOf(textCache)+"\n"
		+"line: "+String.valueOf(lineCache)+"\n"
		+"rect: "+String.valueOf(rectCache)+"\n"
		+"shape: "+String.valueOf(getOrCreateShapeCache()+"\n");
		if (abstractCacheList != null) {
			for (AbstractCache abstractCache : abstractCacheList) {
				s += abstractCache.getClass().getSimpleName()+": "+String.valueOf(abstractCache)+"\n";
			}
		}
		return s;
	}

	// because it's in svg2xml
//	public void setContentBoxCache(ContentBoxCache contentBoxCache) {
//	}
	
	public void addCache(AbstractCache abstractCache) {
		if (abstractCacheList == null) {
			abstractCacheList = new ArrayList<AbstractCache>();
		}
		if (!abstractCacheList.contains(abstractCache)) {
			abstractCacheList.add(abstractCache);
		}
		
	}

	public void removeBorderingRects() {
		Real2Range outerBbox = this.getBoundingBox();
		List<SVGRect> rectList = rectCache.getOrCreateRectList();
		LOG.debug("pre removal rect bbox "+outerBbox + "; "+rectCache.getOrCreateRectList().size());
		boolean removed = false;
		for (int i = rectList.size() - 1; i >= 0; i--) {
			SVGRect rect = rectList.get(i);
			Real2Range bbox = rect.getBoundingBox();
			if (outerBbox.isEqualTo(bbox, outerBoxEps)) {
				removed = rectCache.remove(rect);
				LOG.info("removed outer rect "+rect.toXML());
			}
		}
		if (removed) {
			rectCache.clearAll();
			outerBbox = rectCache.getBoundingBox();
			LOG.debug("post removal rect bbox "+outerBbox + "; "+rectCache.getOrCreateRectList().size());
		}
		
	}
	
	public SVGElement getOriginalSVGElement() {
		return originalSvgElement;
	}
	
	@Override
	public void clearAll() {
		superClearAll();
		imageCache = null;
		pathCache = null;
		textCache = null;
		lineCache = null;
		rectCache = null;
		shapeCache = null;
		abstractCacheList = null;
		
		positiveXBox = null;

		fileRoot = null;
		originalSvgElement = null;

		extractedSVGElement = null;
		allElementList = null;
		boundingBoxList = null;

		whitespaceSpixels = null;

	}
}

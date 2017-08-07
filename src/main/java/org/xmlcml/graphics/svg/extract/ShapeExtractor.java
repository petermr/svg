package org.xmlcml.graphics.svg.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.GraphicsElement;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGEllipse;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPolygon;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.linestuff.Path2ShapeConverter;
import org.xmlcml.graphics.svg.objects.SVGTriangle;
import org.xmlcml.graphics.svg.store.SVGStore;

/** extracts and tidies shapes read from SVG.
 * 
 * @author pm286
 *
 */
public class ShapeExtractor extends AbstractExtractor {
	private static final Logger LOG = Logger.getLogger(ShapeExtractor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private List<SVGPath> originalPathList;
	// this holds any paths that we can't convert
	private List<SVGPath> pathList;
	// derived
	private List<SVGCircle> circleList;
	private List<SVGEllipse> ellipseList;
	private List<SVGLine> lineList;
	private List<SVGPolygon> polygonList;
	private List<SVGPolyline> polylineList;
	private List<SVGRect> rectList;
	private List<SVGTriangle> triangleList;
	private List<SVGShape> unknownShapeList;
	private List<SVGShape> allShapeList;
	private List<List<SVGShape>> convertedShapeListList;
	private List<SVGShape> convertedShapeList;
	
	public ShapeExtractor(SVGStore svgStore) {
		super(svgStore);
		init();
	}
	
	private void init() {
		pathList = new ArrayList<SVGPath>();
		unknownShapeList = new ArrayList<SVGShape>();
		
		circleList = new ArrayList<SVGCircle>();
		ellipseList = new ArrayList<SVGEllipse>();
		lineList = new ArrayList<SVGLine>();
		polygonList = new ArrayList<SVGPolygon>();
		polylineList = new ArrayList<SVGPolyline>();
		rectList = new ArrayList<SVGRect>();
		triangleList = new ArrayList<SVGTriangle>();
	}
	
	/** converts paths to shapes.
	 * if it can convert to a shape, adds to the appropriate list, else adds as path/s
	 * to the pathList
	 * 
	 * @param paths
	 */
	public void convertToShapes(List<SVGPath> paths) {
		Path2ShapeConverter path2ShapeConverter = new Path2ShapeConverter();
		path2ShapeConverter.setSplitAtMoveCommands(svgStore.getSplitAtMove());
		convertedShapeListList = path2ShapeConverter.convertPathsToShapesAndSplitAtMoves(paths);
		for (List<SVGShape> shapeList : convertedShapeListList) {
			for (SVGShape shape : shapeList) {
				if (shape instanceof SVGCircle) {
					circleList.add((SVGCircle) shape);
				} else if (shape instanceof SVGEllipse) {
					ellipseList.add((SVGEllipse) shape);
				} else if (shape instanceof SVGLine) {
					lineList.add((SVGLine) shape);
				} else if (shape instanceof SVGPolygon) {
					polygonList.add((SVGPolygon) shape);
				} else if (shape instanceof SVGPolyline) {
					polylineList.add((SVGPolyline) shape);
				} else if (shape instanceof SVGRect) {
					rectList.add((SVGRect) shape);
				} else if (shape instanceof SVGTriangle) {
					triangleList.add((SVGTriangle) shape);
				} else if (shape instanceof SVGPath) {
					this.pathList.add((SVGPath) shape);
					LOG.info("unprocessed shape: "+shape);
				} else {
					LOG.warn("Unexpected shape: "+shape.getClass()); 
					unknownShapeList.add(shape);
				}
			}
		}
		return;
	}

	public List<SVGPath> getPathList() {
		return pathList;
	}

	public List<SVGCircle> getCircleList() {
		return circleList;
	}

	public List<SVGEllipse> getEllipseList() {
		return ellipseList;
	}

	public List<SVGLine> getLineList() {
		return lineList;
	}

	public List<SVGPolygon> getPolygonList() {
		return polygonList;
	}

	public List<SVGPolyline> getPolylineList() {
		return polylineList;
	}

	public List<SVGRect> getRectList() {
		return rectList;
	}

	public List<SVGTriangle> getTriangleList() {
		return triangleList;
	}

	public List<SVGShape> getShapeList() {
		return unknownShapeList;
	}

	public List<List<SVGShape>> getConvertedShapeListList() {
		return convertedShapeListList;
	}

	public List<SVGShape> getOrCreateConvertedShapeList() {
		if (convertedShapeList == null) {
			if (convertedShapeListList != null) {
				convertedShapeList = new ArrayList<SVGShape>();
				for (List<SVGShape> shapeList : convertedShapeListList) {
					convertedShapeList.addAll(shapeList);
				}
			}
		}
		return convertedShapeList;
	}

	public void debug() {
		LOG.debug(
		"paths: " + pathList.size() 
		+ "; circles: "   + circleList.size()
		+ "; ellipses: "  + ellipseList.size()
		+ "; lines: "     + lineList.size() 
		+ "; polygons: "  + polygonList.size() 
		+ "; polylines: " + polylineList.size() 
		+ "; rects: "     + rectList.size() 
		+ "; shapes: "    + unknownShapeList.size() 
		);
	}

	public SVGG createSVGAnnotations() {
		SVGG g = new SVGG();
		addList(g, polylineList);
		addList(g, circleList);
		g.setFill("orange");
		return g;
	}
	
	public static void addList(SVGG g, List<? extends SVGElement> list) {
		for (GraphicsElement element : list) {
			g.appendChild(element.copy());
		}
	}

	public void createListsOfShapes(GraphicsElement svgElement) {
		List<SVGCircle> circles = SVGCircle.extractSelfAndDescendantCircles(svgElement);
		circleList.addAll(circles);
		List<SVGEllipse> ellipses = SVGEllipse.extractSelfAndDescendantEllipses(svgElement);
		ellipseList.addAll(ellipses);
		List<SVGLine> lines = SVGLine.extractSelfAndDescendantLines(svgElement);
		lineList.addAll(lines);
		List<SVGPolygon> polygons = SVGPolygon.extractSelfAndDescendantPolygons(svgElement);
		polygonList.addAll(polygons);
		List<SVGPolyline> polylines = SVGPolyline.extractSelfAndDescendantPolylines(svgElement);
		polylineList.addAll(polylines);
		List<SVGRect> rects = SVGRect.extractSelfAndDescendantRects(svgElement);
		rectList.addAll(rects);
		
	}

	public void removeElementsOutsideBox(Real2Range positiveXBox) {
		SVGElement.removeElementsOutsideBox(circleList, positiveXBox);
		SVGElement.removeElementsOutsideBox(ellipseList, positiveXBox);
		SVGElement.removeElementsOutsideBox(lineList, positiveXBox);
		SVGElement.removeElementsOutsideBox(polylineList, positiveXBox);
		SVGElement.removeElementsOutsideBox(polygonList, positiveXBox);
		SVGElement.removeElementsOutsideBox(rectList, positiveXBox);
		SVGElement.removeElementsOutsideBox(triangleList, positiveXBox);
		SVGElement.removeElementsOutsideBox(unknownShapeList, positiveXBox);
	}

	public void removeElementsInsideBox(Real2Range positiveXBox) {
		SVGElement.removeElementsInsideBox(circleList, positiveXBox);
		SVGElement.removeElementsInsideBox(ellipseList, positiveXBox);
		SVGElement.removeElementsInsideBox(lineList, positiveXBox);
		SVGElement.removeElementsInsideBox(polylineList, positiveXBox);
		SVGElement.removeElementsInsideBox(polygonList, positiveXBox);
		SVGElement.removeElementsInsideBox(rectList, positiveXBox);
		SVGElement.removeElementsInsideBox(triangleList, positiveXBox);
		SVGElement.removeElementsInsideBox(unknownShapeList, positiveXBox);
	}

	public void extractShapes(List<SVGPath> pathList, GraphicsElement svgElement) {
		convertToShapes(pathList);
		createListsOfShapes(svgElement);
		removeElementsOutsideBox(svgStore.getPositiveXBox());
		
		debug();
	}

	public SVGG debug(String outFilename) {
		SVGG g = new SVGG();
//		debug(g, originalPathList, "black", "yellow", 0.3);
//		private List<SVGPath> pathList;
		// derived
		debug(g, rectList, "black", "#ffff77", 0.2);
		debug(g, polygonList, "black", "orange", 0.3);
		debug(g, triangleList, "black", "#ffeeff", 0.3);
		debug(g, ellipseList, "black", "red", 0.3);
		debug(g, lineList, "cyan", "red", 0.3);
		debug(g, polylineList, "magenta", "green", 0.3);
		debug(g, circleList, "black", "blue", 0.3); // highest priority
		debug(g, pathList, "purple", "pink", 0.3);
		debug(g, unknownShapeList, "cyan", "orange", 0.3);
		
		writeDebug("shapes",outFilename, g);
		return g;
	}

//	debug(g, rectList, "black", "#ffff77", 0.2);
//	debug(g, polygonList, "black", "orange", 0.3);
//	debug(g, triangleList, "black", "#ffeeff", 0.3);
//	debug(g, ellipseList, "black", "red", 0.3);
//	debug(g, lineList, "cyan", "red", 0.3);
//	debug(g, polylineList, "magenta", "green", 0.3);
//	debug(g, circleList, "black", "blue", 0.3); // highest priority
//	debug(g, pathList, "purple", "pink", 0.3);

//	private void debugRects(SVGG g, List<SVGRect> rectList, String stroke, String fill, double opacity) {
//		
//	}
//
//	private void debugPolygons(SVGG g, List<SVGLine> polygonsList, String stroke, String fill, double opacity) {
//		
//	}
//
//	private void debugTriangles(SVGG g, List<SVGLine> triangleList, String stroke, String fill, double opacity) {
//		
//	}
//
//	private void debugEllipses(SVGG g, List<SVGLine> ellipseList, String stroke, String fill, double opacity) {
//		
//	}
//
//	private void debugLines(SVGG g, List<SVGLine> lineList, String stroke, String fill, double opacity) {
//		
//	}
//
//	private void debugPolylines(SVGG g, List<SVGLine> polylineList, String stroke, String fill, double opacity) {
//		
//	}
//
//	private void debugCircles(SVGG g, List<SVGLine> circleList, String stroke, String fill, double opacity) {
//		
//	}
//
//	private void debugPaths(SVGG g, List<SVGLine> debugList, String stroke, String fill, double opacity) {
//		
//	}

//	debug(g, rectList, "black", "#ffff77", 0.2);
//	debug(g, polygonList, "black", "orange", 0.3);
//	debug(g, triangleList, "black", "#ffeeff", 0.3);
//	debug(g, ellipseList, "black", "red", 0.3);
//	debug(g, lineList, "cyan", "red", 0.3);
//	debug(g, polylineList, "magenta", "green", 0.3);
//	debug(g, circleList, "black", "blue", 0.3); // highest priority
//	debug(g, pathList, "purple", "pink", 0.3);

	private void debug(SVGG g, List<? extends SVGElement> elementList, String stroke, String fill, double opacity) {
		for (GraphicsElement e : elementList) {
			SVGShape shape = (SVGShape) e.copy();
			SVGShape shape1 = (SVGShape) shape.copy();
			Double strokeWidth = shape.getStrokeWidth();
			if (strokeWidth == null) strokeWidth = 0.2;
			double border = Math.max(strokeWidth, 1.5);
			if (shape instanceof SVGLine || shape instanceof SVGPolyline || shape instanceof SVGPath) {
				styleAndDraw(g, stroke, "none", opacity, strokeWidth+border, shape1);
				styleAndDraw(g, fill, "none", opacity, strokeWidth, shape);
			} else if (shape instanceof SVGCircle || shape instanceof SVGPolygon || shape instanceof SVGEllipse) {
				styleAndDraw(g, stroke, "none", opacity, strokeWidth+border, shape1);
				styleAndDraw(g, fill, "none", opacity, strokeWidth, shape);
			} else if (shape instanceof SVGRect || shape instanceof SVGTriangle) {
				styleAndDraw(g, stroke, "none", opacity, strokeWidth+border, shape1);
				styleAndDraw(g, stroke, fill, opacity, strokeWidth, shape);
			} else {
				styleAndDraw(g, stroke, "none", opacity, strokeWidth+border, shape1);
				styleAndDraw(g, stroke, fill, opacity, strokeWidth, shape);
			}
		}
	}

	private void styleAndDraw(SVGG g, String stroke, String fill, double opacity, double strokeWidth, SVGShape shape) {
		shape.setStroke(stroke);
		shape.setStrokeWidth(strokeWidth);
		shape.setFill(fill);
		shape.setOpacity(opacity);
		shape.addTitle(shape.getClass().getSimpleName());
		g.appendChild(shape);
		
	}

	public Real2Range getBoundingBox() {
		boundingBox = SVGElement.createBoundingBox(originalPathList);
		return boundingBox;
	}

//	debug(g, rectList, "black", "#ffff77", 0.2);
//	debug(g, polygonList, "black", "orange", 0.3);
//	debug(g, triangleList, "black", "#ffeeff", 0.3);
//	debug(g, ellipseList, "black", "red", 0.3);
//	debug(g, lineList, "cyan", "red", 0.3);
//	debug(g, polylineList, "magenta", "green", 0.3);
//	debug(g, circleList, "black", "blue", 0.3); // highest priority
//	debug(g, pathList, "purple", "pink", 0.3);

	public List<SVGShape> getOrCreateAllShapeList() {
		if (allShapeList == null) {
			allShapeList = new ArrayList<SVGShape>();
		// how to do this properly? help!
			addShapes(rectList);
			addShapes(polygonList);
			addShapes(polylineList);
			addShapes(triangleList);
			addShapes(ellipseList);
			addShapes(lineList);
			addShapes(circleList);
			addShapes(pathList);
		}
		return allShapeList;
	}

	private void addShapes(List<? extends SVGShape> shapeList) {
		for (SVGShape shape : shapeList) {
			allShapeList.add(shape);
		}
	}



}

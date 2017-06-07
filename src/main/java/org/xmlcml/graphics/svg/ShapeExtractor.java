package org.xmlcml.graphics.svg;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.graphics.svg.linestuff.Path2ShapeConverter;
import org.xmlcml.graphics.svg.objects.SVGTriangle;

/** extracts and tidies shapes read from SVG.
 * 
 * @author pm286
 *
 */
public class ShapeExtractor {
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
	private List<SVGShape> shapeList;

	public ShapeExtractor() {
		init();
	}
	
	private void init() {
		pathList = new ArrayList<SVGPath>();
		shapeList = new ArrayList<SVGShape>();
		
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
	 * @param pathList
	 */
	public void convertToShapes(List<SVGPath> inputPathList) {
		this.originalPathList = inputPathList;
		Path2ShapeConverter path2ShapeConverter = new Path2ShapeConverter();
//		for (SVGPath path : originalPathList) {
		List<List<SVGShape>> shapeListList = path2ShapeConverter.convertPathsToShapesAndSplitAtMoves(originalPathList);
		for (List<SVGShape> shapeList : shapeListList) {
			for (SVGShape shape : shapeList) {
//				LOG.debug("S "+shape.getClass());
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
					pathList.add((SVGPath) shape);
				} else {
					LOG.warn("Unexpected shape: "+shape.getClass());
					shapeList.add(shape);
				}
			}
		}
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
		return shapeList;
	}

	public void debug() {
		LOG.debug(
				"paths: "+pathList.size() 
		+ "; circles: " + circleList.size()
		+ "; ellipses: " + ellipseList.size()
		+ "; lines: " + lineList.size() 
		+ "; polygons: " + polygonList.size() 
		+ "; polylines: " + polylineList.size() 
		+ "; rects: " + rectList.size() 
		+ "; shapes: " + shapeList.size() 
		);
	}

	public SVGG createSVG() {
		SVGG g = new SVGG();
		addList(g, lineList);
		addList(g, circleList);
		g.setFill("orange");
		return g;
	}
	
	public static void addList(SVGG g, List<? extends SVGElement> list) {
		for (SVGElement element : list) {
			g.appendChild(element.copy());
		}
	}

}

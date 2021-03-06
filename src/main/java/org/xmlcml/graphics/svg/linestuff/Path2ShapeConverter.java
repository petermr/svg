package org.xmlcml.graphics.svg.linestuff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGEllipse;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPathPrimitive;
import org.xmlcml.graphics.svg.SVGPoly;
import org.xmlcml.graphics.svg.SVGPolygon;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.SVGUtil;
import org.xmlcml.graphics.svg.StyleBundle;
import org.xmlcml.graphics.svg.objects.SVGTriangle;
import org.xmlcml.graphics.svg.path.Arc;
import org.xmlcml.graphics.svg.path.LinePrimitive;
import org.xmlcml.graphics.svg.path.MovePrimitive;
import org.xmlcml.graphics.svg.path.PathPrimitiveList;
import org.xmlcml.graphics.svg.path.SVGPathParser;
import org.xmlcml.xml.XMLUtil;

import nu.xom.Attribute;
import nu.xom.ParentNode;

/** 
 * Converts SVGPaths to SVGShapes.
 * <p>
 * Uses a variety of heuristics to split and combine primitives. See the SVG wiki for more details.
 * <p>
 * Customisable through setters.
 * 
 * @author pm286
 */
//@Deprecated // use SVG
public class Path2ShapeConverter {


	public static final String Z_COORDINATE = "z";

	private final static Logger LOG = Logger.getLogger(Path2ShapeConverter.class);
	static {LOG.setLevel(Level.DEBUG);}
	
	private static final String MLCCLCC = "MLCCLCC";
	private static final String MLCCLCCZ = "MLCCLCCZ";
	private static final String MLLLL = "MLLLL";
	private static final String MLLL = "MLLL";
	private static final String MCLC = "MCLC";

	private static final double CIRCLE_EPS = 0.7;
	private static final double MOVE_EPS = 0.001;
	private static final double RECT_EPS = 0.03;
	private static final double ROUNDED_BOX_EPS = 0.4;
	
	private static final Angle DEFAULT_MAX_ANGLE_FOR_PARALLEL = new Angle(0.15, Units.RADIANS);
	private static final double DEFAULT_MAX_WIDTH_FOR_PARALLEL = 2.0;
	public static final Angle DEFAULT_MAX_ANGLE = new Angle(0.15, Units.RADIANS);
	private static final Double DEFAULT_MAX_LINE_FROM_RECT_THICKNESS = 0.99;
	private static final double DEFAULT_MAX_PATH_WIDTH = 1.5;
	private static final int DEFAULT_LINES_IN_POLYLINE = 8;
	private static final int DEFAULT_DECIMAL_PLACES = 3;
	
	private static final String SVG = "svg";
	private static final Angle ANGLE_EPS = new Angle(0.01);

	private int decimalPlaces = DEFAULT_DECIMAL_PLACES;
	private int minLinesInPolyline = DEFAULT_LINES_IN_POLYLINE;
	private boolean removeDuplicatePaths = true;
	private boolean removeRedundantMoveCommands = true;
	private boolean removeRedundantLineCommands = true;
	private boolean splitAtMoveCommands = true;
	private double maxPathWidth = DEFAULT_MAX_PATH_WIDTH;
	private Angle maxAngle = DEFAULT_MAX_ANGLE;
	private double maxRectThickness = DEFAULT_MAX_LINE_FROM_RECT_THICKNESS;
	private Angle maxAngleForParallel = DEFAULT_MAX_ANGLE_FOR_PARALLEL;
	private double maxWidthForParallel = DEFAULT_MAX_WIDTH_FOR_PARALLEL;

	//Input and output
	private List<SVGPath> pathListIn;
	private List<SVGPath> splitPathList;
	private SVGPath svgPath;
	private List<SVGShape> shapeListOut;

	private boolean splitPolylines;

	private double rectEpsilon = RECT_EPS;

	private boolean makeRelativePathsAbsolute = true;

	public Path2ShapeConverter() {
		
	}

	/**
	 * @param svgPath the path to process
	 */
	@Deprecated
	public Path2ShapeConverter(SVGPath svgPath) {
		setSVGPath(svgPath);
	}
	
	/**
	 * @param pathListIn the paths to process
	 */
	@Deprecated
	public Path2ShapeConverter(List<SVGPath> pathListIn) {
		setPathList(pathListIn);
	}
	
	/** 
	 * Maximum width to be considered for condensing outline paths
	 * 
	 * @param maxPathWidth
	 */
	public void setMaxPathWidth(double maxPathWidth) {
		this.maxPathWidth = maxPathWidth;
	}

	/** 
	 * Maximum angle TODO
	 * 
	 * @param maxPathWidth
	 */	
	public void setMaxAngle(Angle maxAngle) {
		this.maxAngle = maxAngle;
	}
	
	/** 
	 * Main routine if path list has been read in
	 * <p>
	 * @deprecated Use {@link convertPathsToShapes(pathList)}
	 * 
	 * @return a list of shapes; each a rect, circle, line, polygon or polyline as appropriate; if none are then the original path
	 */
//	@Deprecated
//	public List<SVGShape> convertPathsToShapes() {
//		List<SVGShape> shapeListOut = null;
//		if (pathListIn != null) {
//			shapeListOut = convertPathsToShapes0(pathListIn);
//		}
//		return shapeListOut;
//	}
	
	/** 
	 * Main routine for list of paths. Doesn't observe splitAtMoveCommands.
	 * 
	 * @param pathList
	 * @return a list of shapes; each a rect, circle, line, polygon or polyline as appropriate; if none are then the original path
	 */
	public List<List<SVGShape>> convertPathsToShapesAndSplitAtMoves(List<SVGPath> pathList) {
		return convertPathsToShapes0(pathList);
	}
	
	public List<List<SVGShape>> convertPathsToShapes0(List<SVGPath> inputPathList) {
		setPathList(inputPathList);
		List<List<SVGShape>> shapeListList = new ArrayList<List<SVGShape>>();
		int id = 0;
		LOG.trace("cps0");
		if (makeRelativePathsAbsolute ) {
			makeRelativePathsAbsolute(inputPathList);
		}
		LOG.trace("cps1");
		if (removeRedundantLineCommands) {
			inputPathList = removeRedundantLineCommands(inputPathList);
		}
		LOG.trace("cps2");
		if (removeRedundantMoveCommands) {
			inputPathList = removeRedundantMoveCommands(inputPathList);
		}
		LOG.trace(">inputPath>"+inputPathList);
		List<List<SVGPath>> pathListList;
		if (splitAtMoveCommands) {
			LOG.trace("split at moves0");
			pathListList = splitAtMoveCommands(inputPathList);
			LOG.trace("split at moves1");
		} else {
			pathListList = new ArrayList<List<SVGPath>>();
			for (SVGPath path : inputPathList) {
				LOG.trace("split at moves?");
				List<SVGPath> singlePath = new ArrayList<SVGPath>();
				singlePath.add(path);
				pathListList.add(singlePath);
			}
		}
		
		for (List<SVGPath> pathList : pathListList) {
			List<SVGShape> shapeList = new ArrayList<SVGShape>();
			for (SVGPath path : pathList) {
				SVGShape shape = convertPathToShape(path);
				// unconverted path, add as raw
				if (shape == null) {
					shape = path;
				}
				shape.setId(shape.getClass().getSimpleName().toLowerCase().substring(SVG.length())+"."+id);
				shapeList.add(shape);
				id++;
			}
			if (splitPolylines) {
				shapeList = splitPolylines(shapeList);
			}
			shapeListList.add(shapeList);
		}
		if (removeDuplicatePaths) {
			//shapeListOut = removeDuplicateShapes(shapeListOut);
		}
		LOG.trace(">shapeListList>"+shapeListList);
		return shapeListList;
	}

	private void makeRelativePathsAbsolute(List<SVGPath> pathList) {
		if (pathList == null) {
			LOG.warn(" ****** Empty path list");
		} else {
			for (SVGPath path : pathList) {
				String s = path.getDString();
				LOG.trace("path absolute: "+s.substring(0, Math.min(100, s.length()))+"; "+s.length());
				path.makeRelativePathsAbsolute();
			}
		}
	}

	private List<SVGShape> splitPolylines(List<SVGShape> shapeList) {
		List<SVGShape> shapeList1 = new ArrayList<SVGShape>();
		for (SVGShape shape : shapeList) {
			if (shape instanceof SVGPolyline) {
				SVGPolyline polyline = ((SVGPolyline) shape);
				List<SVGLine> lineList = polyline.createLineList();
				copyAttributes(shape, lineList);
//				shape.format(decimalPlaces);
				shapeList1.addAll(lineList);
			} else {
				shapeList1.add(shape);
			}
		}
		return shapeList1;
	}

	/** 
	 * Main routine for list of paths. Doesn't observe splitAtMoveCommands.
	 * 
	 * @param pathList
	 * @return a list of shapes; each a rect, circle, line, polygon or polyline as appropriate; if none are then the original path
	 * @deprecated Use convertPathsToShapesAndSplitAtMoves().
	 */
	public List<SVGShape> convertPathsToShapes(List<SVGPath> pathList) {
		List<List<SVGShape>> convertedShapeList = convertPathsToShapes0(pathList);
		List<SVGShape> shapeList = new ArrayList<SVGShape>();
		for (List<SVGShape> shapeListFromPath : convertedShapeList) {
			for (SVGShape shape : shapeListFromPath) {
				shapeList.add(shape);
				if (shape.isZeroDimensional()) {
					LOG.trace("Zero dimensional shape: "+shape.toXML());
				}
			}
		}
		return shapeList;
	}

	private static List<SVGPath> removeRedundantMoveCommands(List<SVGPath> pathList) {
		List<SVGPath> newPaths = new ArrayList<SVGPath>();
		for (SVGPath path : pathList) {
			newPaths.add(removeRedundantMoveCommands(path, MOVE_EPS));
		}
		return newPaths;
	}

	private static List<SVGPath> removeRedundantLineCommands(List<SVGPath> pathList) {
		List<SVGPath> newPaths = new ArrayList<SVGPath>();
		for (SVGPath path : pathList) {
			newPaths.add(removeRedundantLineCommands(path, MOVE_EPS));
		}
		return newPaths;
	}

	/** 
	 * Main routine for a single path
	 * 
	 * @param path
	 * @return a rect, circle, line, polygon or polyline as appropriate; if none are then the original path
	 */
	public SVGShape convertPathToShape(SVGPath path) {
		if (path == null) {
			return null;
		}
		SVGShape shape = null;
		SVGShape polygon = null;
		SVGShape triangle = null;
		SVGShape rect = null;
		shape = createRectOrAxialLine(path, rectEpsilon);
		if (shape == null) {
			shape = path.createRoundedBox(ROUNDED_BOX_EPS);
		}
		if (shape == null) {
			shape = path.createCircle(CIRCLE_EPS);
		}
		if (shape == null) {
			SVGPolyline polyline = (SVGPolyline) path.createPolyline();
			//Not a polyline, return unchanged path
			if (polyline == null) {
				shape = new SVGPath(path);
			} else {
				//SVG is a polyline, try the variants
				//Is it a line?
				shape = polyline.createSingleLine();
				if (shape == null) {
					//Or a polygon?
					shape = createPolygonRectTriangleOrLine(shape, polyline);
					if (shape instanceof SVGPolygon) {
						polygon = (SVGPolygon) shape;
						shape = polygon;
					}
				}
				//No, reset to polyline
				if (shape == null || shape instanceof SVGPolygon) {
					shape = createNarrowLine((SVGPolygon) shape);
					if (shape == null) {
						shape = createNarrowLine(polyline);
						if (shape == null) {
							if (polygon == null) {
								shape = polyline;
							} else {
								shape = polygon;
							}
						}
					} 
				}
			}
		}
		if (shape instanceof SVGPath) {
			shape = applyHeuristics((SVGPath)shape);
		}
		if (shape != null) {
			// lines created from thin rects may have a different stroke-width to the original
			Double strokeWidth = null;
			if (shape instanceof SVGLine) {
				strokeWidth = ((SVGLine) shape).getStrokeWidth();
			}
			copyAttributes(path, shape);
			if (strokeWidth != null) {
				shape.setStrokeWidth(strokeWidth);
			}
			shape.format(decimalPlaces);
		}
		return shape;
	}

	/** try to convert from some more complex artifacts.
	 * 
	 * @param shape
	 * @return
	 */
	private SVGShape applyHeuristics(SVGPath path) {
		SVGShape shape = path; // no change
		String signature = path.getSignature();
		LOG.trace("SIG "+signature);
		if (signature == null) {
			LOG.warn("Null signature for path");
		} else if (signature.equals(SVGTriangle.CONVEX_ARROWHEAD)) {
			SVGTriangle triangle = SVGTriangle.getPseudoTriangle(path);
			LOG.trace("PSEUDO TRIANGLE");
			if (triangle != null) {
				shape = triangle;
			}
		} else if (signature.equals(SVGEllipse.ELLIPSE_MCCCC) || signature.equals(SVGEllipse.ELLIPSE_MCCCCZ)){
/* d="
 *  M350.644 164.631 
    C350.644 170.705 327.979 175.631 300.02 175.631 
	C272.06 175.631 249.395 170.705 249.395 164.631 
	C249.395 158.555 272.06 153.631 300.02 153.631 
	C327.979 153.631 350.644 158.555 350.644 164.631 "/>			
*/			
			SVGShape ellipseOrCircle = SVGEllipse.getEllipseOrCircle(path, rectEpsilon);
			if (ellipseOrCircle != null) {
				shape = ellipseOrCircle;
				ellipseOrCircle.setFill("none");
			}
		}
		
		return shape;
	}

	/** 
	 * Set the path to use
	 * <p>
	 * @deprecated Use {@link convertPathToShape(path)}
	 * 
	 * @param path
	 */
	@Deprecated
	public void setSVGPath(SVGPath path) {
		svgPath = path;
	}
	
	/** 
	 * The number of decimal places for coordinates in output
	 * 
	 * @param places
	 */
	public void setDecimalPlaces(int places) {
		decimalPlaces = places;
	}
	
	private SVGLine createLineFromMLLLLOrMLCCLCC(SVGPath path) {
		SVGLine line = null;
		if (path != null) {
			path = removeRoundedCapsFromPossibleLine(path);
			//If signature is now MLLLL continue
			line = path.createLineFromMLLLL(maxAngleForParallel, maxWidthForParallel);
		}
		return line;
	}

	private SVGPath removeRoundedCapsFromPossibleLine(SVGPath path) {
		String signature = path.getSignature();
		if (MLCCLCC.equals(signature) || MLCCLCCZ.equals(signature)) {
			SVGPath newPath = path.replaceAllUTurnsByButt(maxAngleForParallel, true);
			if (newPath != null) {
				path = newPath;
			}
		}
		return path;
	}
	
	/** 
	 * Creates best guess at higher SVGElement
	 * <p>
	 * @deprecated Use {@link #convertPathToShape(path)}
	 * 
	 * @param shapeListOut
	 * @return a rect, circle, line, polygon or polyline as appropriate; if none are then the original path
	 */
	@Deprecated
	private SVGShape convertPathToShape() {
		return convertPathToShape(svgPath);
	}

	/**
	 * Converts a polygon into a narrow line
	 * 
	 * @return null on failure
	 */
	public SVGLine createNarrowLine(SVGPolygon polygon) {
		SVGLine line = null;
		if (polygon != null && polygon.size() == 4) {
			SVGLine line0 = polygon.createLineList(true).get(0);
			SVGLine line1 = polygon.getLineList().get(1);
			SVGLine line2 = polygon.getLineList().get(2);
			SVGLine line3 = polygon.getLineList().get(3);
			SVGLine newLine1 = createNarrowLine(line0, line2);
			SVGLine newLine2 = createNarrowLine(line1, line3);
			line = (newLine1 == null ? newLine2 : (newLine2 == null ? newLine1 : (newLine1.getLength() > newLine2.getLength() ? newLine1 : newLine2)));
		}
		return line;
	}
	
	/**
	 * Converts a polyline into a narrow line (assumes one end is open)
	 * 
	 * @return null on failure
	 */
	public SVGLine createNarrowLine(SVGPolyline polyline) {
		SVGLine line = null;
		if (polyline != null && polyline.size() == 3) {
			SVGLine line0 = polyline.getLineList().get(0);
			SVGLine line2 = polyline.getLineList().get(2);
			line = createNarrowLine(line0, line2);
		}
		return line;
	}

	public void setRectEpsilon(double rectEps) {
		this.rectEpsilon = rectEps;
		
		
	}
	private SVGShape createPolygonRectTriangleOrLine(SVGShape shape, SVGPolyline polyline) {
		if (polyline != null) {
			SVGPolygon polygon = (SVGPolygon) polyline.createPolygon(rectEpsilon);
			if (polygon != null) {
				if (polygon.size() == 2) {
					shape = new SVGLine(polygon);
				} else if (polygon.size() == 3) {
						shape = new SVGTriangle(polygon);
				} else {
					SVGRect rect = polygon.createRect(rectEpsilon);
					SVGLine line = createLineFromRect(rect);
					if (line != null) {
						shape = line;
					} else if (rect != null){
						shape = rect;
					} else {
						shape = polygon;
					}
				}
			}
		}
		return shape;
	}

	private SVGShape createRectOrAxialLine(SVGPath path, double eps) {
//		String sig = path.getSignature();
//		LOG.debug(sig);
//		if ("MLLLZ".equals(sig)) {
//			LOG.debug("sig: "+sig);
//		}
		SVGShape shape;
		SVGRect rect = path.createRectangle(eps);
		SVGLine line = null;
		if (rect == null) {
			line = createLineFromMLLLLOrMLCCLCC(path);
		} else {
			line = createLineFromRect(rect); 
		}
		shape = (line != null ? line : rect);
		return shape;
	}

	private static void replacePathsByShapes(List<SVGShape> shapeList, List<SVGPath> pathList) {
		if (shapeList.size() != pathList.size()){
			throw new RuntimeException("converted paths ("+shapeList.size()+") != old paths ("+pathList.size()+")");
		}
		for (int i = 0; i < pathList.size(); i++) {
			SVGPath path = pathList.get(i);
			SVGShape shape = shapeList.get(i);
			ParentNode parent = path.getParent();
			LOG.trace("Parent "+parent);
			if (parent != null) {
				LOG.trace("CONV "+shape.toXML());
				if (shape instanceof SVGPath) {
					// no need to replace as no conversion done
				} else {
					parent.replaceChild(path, shape);
				}
			}
		}
	}
	
	private void replaceEachPathWithShapesOrPaths(List<List<SVGShape>> shapeListList, List<SVGPath> pathList) {
		if (shapeListList.size() != pathList.size()){
			throw new RuntimeException("converted paths ("+shapeListList.size()+") != old paths ("+pathList.size()+")");
		}
		for (int i = 0; i < pathList.size(); i++) {
			SVGPath path = pathList.get(i);
			List<SVGShape> shapeList = shapeListList.get(i);
			ParentNode parent = path.getParent();
			LOG.trace("Parent " + parent);
			if (parent != null) {
				int position = parent.indexOf(path);
				for (SVGShape shape : shapeList) {
					LOG.trace("CONV " + shape.toXML());
					//if (shape instanceof SVGPath) {
						//No need to replace as no conversion done
					//} else {
					path.detach();
					parent.insertChild(shape, position++);
					//}
				}
			}
		}
	}

	/** 
	 * Many paths are drawn twice; if two or more paths are equal, remove the later one(s) if removeDuplicatePaths is set
	 */
	@Deprecated
	public void removeDuplicatePaths() {
		if (removeDuplicatePaths) {
			shapeListOut = SVGUtil.removeDuplicateShapes(shapeListOut);
		}
	}
	
	/** 
	 * Some paths have redundant move (M) commands that can be removed
	 * <p>
	 * E.g. M x1 y1 L x2 y2 M x2 y2 L x3 y3 will be converted to 
	 *      M x1 y1 L x2 y2 L x3 y3  
	 */
	@Deprecated
	public void removeRedundantMoveCommands() {
		if (removeRedundantMoveCommands) {
			for (SVGPath path : pathListIn) {
				removeRedundantMoveCommands(path, MOVE_EPS);
			}
		}
	}
	
	/** 
	 * Runs convertPathsToShapes(List<SVGPath> pathList) and splitPolylinesToLines(List<SVGShape> shapeList); use after setting true/false flags if required
	 */
	@Deprecated
	public void runAnalyses(List<SVGPath> pathList) {
		pathListIn = pathList;
		splitAtMoveCommands();
		removeRedundantMoveCommands();
		List<SVGShape> shapeList = convertPathsToShapes(pathListIn);
		splitPolylinesToLines(shapeList);
		removeDuplicatePaths();
	}
	
	public void setPathList(List<SVGPath> pathListIn) {
		this.pathListIn = pathListIn;
	}
	
	

	private static SVGPath removeRedundantMoveCommands(SVGPath path, double eps) {
		String d = path.getDString();
		if (d != null) {
			PathPrimitiveList newPrimitives = new PathPrimitiveList();
			PathPrimitiveList primitives = new SVGPathParser().parseDString(d);
			int primitiveCount = primitives.size();
			SVGPathPrimitive lastPrimitive = null;
			for (int i = 0; i < primitives.size(); i++) {
				SVGPathPrimitive currentPrimitive = primitives.get(i);
				boolean skip = false;
				if (currentPrimitive instanceof MovePrimitive) {
					if (i == primitives.size() - 1) { // final primitive
						skip = true;
					} else if (lastPrimitive != null) {
						// move is to end of last primitive
						Real2 lastLastCoord = lastPrimitive.getLastCoord();
						Real2 currentFirstCoord = currentPrimitive.getFirstCoord();
						skip = (lastLastCoord != null) && lastLastCoord.isEqualTo(currentFirstCoord, eps);
					}
					/*if (!skip && lastPrimitive != null) {
						SVGPathPrimitive nextPrimitive = primitives.get(i + 1);
						Real2 currentLastCoord = currentPrimitive.getLastCoord();
						Real2 nextFirstCoord = nextPrimitive.getFirstCoord();
						skip = (nextFirstCoord != null) && currentLastCoord.isEqualTo(nextFirstCoord, eps);
					}*/
				}
				if (!skip) {
					newPrimitives.add(currentPrimitive);
				} else {
					LOG.trace("skipped "+lastPrimitive+ "== "+currentPrimitive);
				}
				lastPrimitive = currentPrimitive;
			}
			return createNewPathIfModified(path, d, newPrimitives, primitiveCount);
		}
		return path;
	}
	
	private static SVGPath removeRedundantLineCommands(SVGPath path, double eps) {
		String d = path.getDString();
		if (d != null) {
			PathPrimitiveList newPrimitives = new PathPrimitiveList();
			PathPrimitiveList primitives = new SVGPathParser().parseDString(d);
			int primitiveCount = primitives.size();
			SVGPathPrimitive lastPrimitive = null;
			for (int i = 0; i < primitives.size(); i++) {
				SVGPathPrimitive currentPrimitive = primitives.get(i);
				boolean skip = false;
				if (currentPrimitive instanceof LinePrimitive) {
					if (lastPrimitive != null) {
						Real2 lastLastCoord = lastPrimitive.getLastCoord();
						Real2 currentFirstCoord = currentPrimitive.getFirstCoord();
						skip = (lastLastCoord != null) && lastLastCoord.isEqualTo(currentFirstCoord, eps);
					}
				}
				if (!skip) {
					newPrimitives.add(currentPrimitive);
				} else {
					LOG.trace("skipped "+lastPrimitive+ "== "+currentPrimitive);
				}
				lastPrimitive = currentPrimitive;
			}
			return createNewPathIfModified(path, d, newPrimitives, primitiveCount);
		}
		return path;
	}

	//TODO why not modify in place?
	private static SVGPath createNewPathIfModified(SVGPath path, String d,
			PathPrimitiveList newPrimitives, int primitiveCount) {
		int newPrimitiveCount = newPrimitives.size();
		if (newPrimitiveCount != primitiveCount) {
			LOG.trace("Deleted "+(primitiveCount - newPrimitiveCount)+" redundant primitives");
			String newD = SVGPath.constructDString(newPrimitives);
			SVGPath newPath = new SVGPath(newD);
			XMLUtil.copyAttributesFromTo(path, newPath);
			newPath.setDString(newD);
			//path.getParent().replaceChild(path, newPath);
			LOG.trace(">>>"+d+"\n>>>"+newD);
			return newPath;
		}
		return path;
	}
	
	/**
	 * Split paths into constituent paths if there are move commands other than at the start
	 * <p>
	 * @deprecated
	 * 
	 * @param paths
	 * @return list of paths with move commands only at the start
	 */
	@Deprecated
	public void splitAtMoveCommands() {
		if (splitAtMoveCommands) {
			splitAtMoveCommands(pathListIn);
		}
	}

	private static List<List<SVGPath>> splitAtMoveCommands(List<SVGPath> paths) {
		List<List<SVGPath>> results = new ArrayList<List<SVGPath>>();
		for (int i = 0; i < paths.size(); i++) {
			SVGPath path = paths.get(i);
			List<SVGPath> result = splitAtMoveCommands(path);
			results.add(result);
		}
		return results;
	}

	private static List<SVGPath> splitAtMoveCommands(SVGPath svgPath) {
		 ArrayList<SVGPath> splitPathList = new ArrayList<SVGPath>();
		 String d = svgPath.getDString();
		 List<String> newDStringList = splitAtMoveCommandsAndCreateNewDStrings(d);
		 if (newDStringList.size() == 1) {
			 splitPathList.add(svgPath);
		 } else {
			 double dz = 0;
			 for (String newDString : newDStringList) {
				 dz += 0.001;
				 SVGPath newPath = new SVGPath();
				 XMLUtil.copyAttributesFromTo(svgPath, newPath);
				 String zs = SVGUtil.getSVGXAttribute(svgPath, Z_COORDINATE);
				 if (zs != null && zs.trim().length() == 0) {
					 try {
						 double z = Double.parseDouble(zs);
						 SVGUtil.setSVGXAttribute(newPath, Z_COORDINATE, String.valueOf(z + dz));
					 } catch (Exception e) {
						 LOG.error("cannot find/parse z: "+e);
						 continue;
					 }
				 }
				 newPath.setDString(newDString);
				 splitPathList.add(newPath);
			 }
		 }
		 return splitPathList;
	}
	
	private static List<String> splitAtMoveCommandsAndCreateNewDStrings(String d) {
		LOG.trace(">p2sd>"+d);
		List<String> strings = new ArrayList<String>();
		int current = -1;
		StringBuilder sb = new StringBuilder(d);
		while (true) {
			int i = sb.indexOf(SVGPathPrimitive.MOVE_S, current + 1);
			if (i == -1) {
				strings.add(d.substring(Math.max(0, current)));
				break;
			} else if (current > -1) {
				strings.add(d.substring(current, i));
			}
			current = i;
		}
		return strings;
	}
	
	/** 
	 * Splits any polylines in shapeList into lines according to minLinesInPolyline ({@link setMinLinesInPolyline} and {@link getMinLinesInPolyline})
	 * 
	 * @param shapeList
	 * @return
	 */
	public List<SVGLine> splitPolylinesToLines(List<SVGShape> shapeList) {
		LOG.trace("minLines: "+minLinesInPolyline);
		List<SVGLine> totalSplitLineList = new ArrayList<SVGLine>();
		for (SVGShape shape : shapeList) {
			if (shape instanceof SVGPolyline) {
				SVGPoly polyline = (SVGPoly) shape;
				List<SVGLine> lines = polyline.createLineList();
				if (lines.size() < minLinesInPolyline) {
					annotateLinesAndAddToParentAndList(totalSplitLineList, polyline, lines);
				} else {
					LOG.trace("not split: "+lines.size());
				}
			}
		}
		return totalSplitLineList;
	}

	private void annotateLinesAndAddToParentAndList (
			List<SVGLine> totalSplitLineList, SVGPoly polyline, List<SVGLine> linesToAdd) {
		ParentNode parent = polyline.getParent();
		for (int i = 0; i < linesToAdd.size(); i++) {
			SVGLine line = linesToAdd.get(i);
			parent.appendChild(line);
			line.setId(line.getId()+"."+i);
			totalSplitLineList.add(line);
		}
		polyline.detach();
		LOG.trace("split: "+linesToAdd.size());
	}

	/** 
	* Written with help from http://stackoverflow.com/questions/4958161/determine-the-centre-center-of-a-circle-using-multiple-points
	* 
	* @param p1
	* @param p2
	* @param p3
	* @param eps TODO
	* @return circle
	*/
	public static SVGCircle findCircleFrom3Points(Real2 p1, Real2 p2, Real2 p3, Double eps) {
		SVGCircle circle = null;
		if (p1 != null && p2 != null && p3 != null) {
			Double d2 = p2.x * p2.x + p2.y * p2.y;
			Double bc = (p1.x * p1.x + p1.y * p1.y - d2) / 2;
			Double cd = (d2 - p3.x * p3.x - p3.y * p3.y) / 2;
			Double det = (p1.x - p2.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p2.y);
			if (Math.abs(det) > eps) {
				Real2 center = new Real2(
						(bc * (p2.y - p3.y) - cd * (p1.y - p2.y)) / det,
						((p1.x - p2.x) * cd - (p2.x - p3.x) * bc) / det);
				Double rad = center.getDistance(p1);
				circle = new SVGCircle(center, rad);
			}
		}
		return circle;
	}

	/** 
	* Written with help from http://stackoverflow.com/questions/4958161/determine-the-centre-center-of-a-circle-using-multiple-points
	* 
	* @param r2a the points
	* @param eps TODO
	* @return circle
	*/
	public static SVGCircle findCircleFromPoints(Real2Array r2a, double eps) {
		SVGCircle circle = null;
		if (r2a == null || r2a.size() < 3) {
			
		} else if (r2a.size() == 3) {
			circle = findCircleFrom3Points(r2a.get(0), r2a.get(1), r2a.get(2), eps);
		} else {
			RealArray x2y2Array = new RealArray();
			RealArray xArray = new RealArray();
			RealArray yArray = new RealArray();
			for (int i = 0; i < r2a.size(); i++) {
				Real2 point = r2a.get(i);
				double x = point.x;
				double y = point.y;
				x2y2Array.addElement(x * x + y * y);
				xArray.addElement(x);
				yArray.addElement(y);
			}
			Real2Range bbox =r2a.getRange2();
			// check if scatter in both directions
			if (bbox.getXRange().getRange() > eps && bbox.getYRange().getRange() > eps) {
				//Don't know the distribution and can't afford to find all triplets, so find the extreme points
				Real2 minXPoint = r2a.getPointWithMinimumX();
				Real2 maxXPoint = r2a.getPointWithMaximumX();
				Real2 minYPoint = r2a.getPointWithMinimumY();
				Real2 maxYPoint = r2a.getPointWithMaximumY();
			}
		}
		return circle;
	}


	/**
	 * Copies fill, opacity, stroke and stroke width and dash-array attributes to a list
	 * 
	 * @param input
	 * @param resultList
	 */
	public static void copyAttributes(SVGElement input, List<? extends SVGElement> resultList) {
		for (SVGElement result : resultList) {
			copyAttributes(input, result);
		}
	}

	/**
	 * Copies fill, opacity, stroke and stroke width and dash-array attributes
	 * 
	 * @param input
	 * @param result
	 */
	public static void copyAttributes(SVGElement input, SVGElement result) {
		input.setUseStyleAttribute(false);
		for (String attName : new String[]{
				StyleBundle.FILL, 
				StyleBundle.OPACITY, 
				StyleBundle.STROKE, 
				StyleBundle.STROKE_WIDTH, 
				StyleBundle.DASHARRAY, 
				}) {
			String val = input.getAttributeValue(attName);
			if (val != null) {
				result.addAttribute(new Attribute(attName, val));
				if (attName.equals(StyleBundle.DASHARRAY)) {
					LOG.trace("DASH "+input.toXML());
				}
			}
		}
		String zvalue = SVGUtil.getSVGXAttribute(input, Z_COORDINATE);
		if (zvalue != null) {
			SVGUtil.setSVGXAttribute(result, Z_COORDINATE, zvalue);
		}
	}
	
	/** 
	 * @param removeDuplicatePaths whether if two or more paths are equal, the later one(s) should be removed
	 */
	public void setRemoveDuplicatePaths(boolean removeDuplicatePaths) {
		this.removeDuplicatePaths = removeDuplicatePaths;
	}
	
	/** 
	 * Some paths have redundant move (M) commands that can be removed
	 * <p>
	 * E.g. M x1 y1 L x2 y2 M x2 y2 L x3 y3 will be converted to M x1 y1 L x2 y2 L x3 y3
	 *      
	 * @param whether this should be done
	 */
	public void setRemoveRedundantMoveCommands(boolean removeRedundantMoveCommands) {
		this.removeRedundantMoveCommands = removeRedundantMoveCommands;
	}
	
	/** 
	 * Some paths have redundant line (L) commands that can be removed
	 * <p>
	 * E.g. M x1 y1 L x2 y2 M x3 y3 L x3 y3 will be converted to M x1 y1 L x2 y2 M x3 y3
	 *      
	 * @param whether this should be done
	 */
	public void setRemoveRedundantLineCommands(boolean removeRedundantLineCommands) {
		this.removeRedundantLineCommands = removeRedundantLineCommands;
	}
	
	/**
	 * @param the minimum number of lines for polylines to be split into lines with {@link splitPolylinesToLines}
	 */
	public void setMinLinesInPolyline(Integer minLinesInPolyline) {
		this.minLinesInPolyline = minLinesInPolyline;
	}

	/**
	 * @param whether to split paths into constituent paths if there are move commands other than at the start
	 */
	public void setSplitAtMoveCommands(boolean splitAtMoveCommands) {
		this.splitAtMoveCommands = splitAtMoveCommands;
	}
	
	/** 
	 * @return whether if two or more paths are equal, the later one(s) will be removed
	 */
	public boolean isRemoveDuplicatePaths() {
		return removeDuplicatePaths;
	}
	
	/** 
	 * Some paths have redundant move (M) commands that can be removed
	 * <p>
	 * E.g. M x1 y1 L x2 y2 M x2 y2 L x3 y3 will be converted to M x1 y1 L x2 y2 L x3 y3
	 *      
	 * @return whether this will be done
	 */
	public boolean isRemoveRedundantMoveCommands() {
		return removeRedundantMoveCommands;
	}
	
	/** 
	 * Some paths have redundant line (L) commands that can be removed
	 * <p>
	 * E.g. M x1 y1 L x2 y2 M x3 y3 L x3 y3 will be converted to M x1 y1 L x2 y2 M x3 y3
	 *      
	 * @param whether this should be done
	 */
	public boolean isRemoveRedundantLineCommands() {
		return removeRedundantLineCommands;
	}
	
	/**
	 * @return the minimum number of lines for polylines to be split into lines with {@link splitPolylinesToLines}
	 */
	public Integer getMinLinesInPolyline() {
		return minLinesInPolyline;
	}
	
	/**
	 * @return whether to split paths into constituent paths if there are move commands other than at the start
	 */
	public boolean isSplitAtMoveCommands() {
		return splitAtMoveCommands;
	}
	
	/** 
	 * @return the number of decimal places for coordinates in output
	 */
	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}
	
	/**
	 * Converts paths to shapes where appropriate in an SVG element
	 * // FIXME - don't think we have to work on lists of Paths
	 * @param svgElement
	 */
	public List<SVGShape> convertPathsToShapes(SVGElement svgElement) {
		List<SVGPath> pathList = SVGPath.extractPaths(svgElement);
		List<List<SVGShape>> shapeListList = convertPathsToShapesAndSplitAtMoves(pathList);
		replaceEachPathWithShapesOrPaths(shapeListList, pathList);
		removeEmptyPaths(shapeListList);
		List<SVGShape> shapeListOut = new ArrayList<SVGShape>();
		for (List<SVGShape> shapeList : shapeListList) {
			shapeListOut.addAll(shapeList);
		}
		return shapeListOut;
	}

	private void removeEmptyPaths(List<List<SVGShape>> shapeListList) {
		for (List<SVGShape> shapeList : shapeListList) {
			Iterator<SVGShape> it = shapeList.iterator();
			while (it.hasNext()) {
				SVGShape shape = it.next();
				if ((shape instanceof SVGPath) && "".equals(((SVGPath) shape).getDString().replaceAll(" ", ""))) {
					shape.detach();
					it.remove();
				}
			}
		}
	}

	/**
	 * Get the results of processing
	 * 
	 * @deprecated
	 * @return list of shapes
	 */
	@Deprecated
	public List<SVGShape> getShapeListOut() {
		return shapeListOut;
	}

	/**
	 * Create line from path consisting of 3 or 4 lines creating the outline of a line
	 * 
	 * @deprecated
	 * @return
	 */
	@Deprecated
	public SVGShape createNarrowLine() {
		maxPathWidth = 1.0;
		if (svgPath == null) return null;
		SVGShape line = null;
		String signature = svgPath.getSignature();
		if (MLLL.equals(signature) || MLLLL.equals(signature)) {
			PathPrimitiveList primList = svgPath.ensurePrimitives();
			SVGLine line0 = primList.getLine(1);
			SVGLine line1 = primList.getLine(3);
			line = createNarrowLine(line0, line1);
		}
		return line;
	}

	private SVGLine createNarrowLine(SVGLine line0, SVGLine line1) {
		SVGLine line = null;
		if (line0.isParallelOrAntiParallelTo(line1, maxAngle)) {
			double dist = line0.calculateUnsignedDistanceBetweenLines(line1, maxAngle);
			if (dist < maxPathWidth) {
				Real2 end0Parallel = line0.getXY(0).getMidPoint(line1.getXY(0));
				Real2 end1Parallel = line0.getXY(1).getMidPoint(line1.getXY(1));
				Real2 end0AntiParallel = line0.getXY(0).getMidPoint(line1.getXY(1));
				Real2 end1AntiParallel = line0.getXY(1).getMidPoint(line1.getXY(0));
				SVGLine lineParallel = new SVGLine(end0Parallel, end1Parallel);
				SVGLine lineAntiParallel = new SVGLine(end0AntiParallel, end1AntiParallel);
				line = (lineParallel.getLength() > lineAntiParallel.getLength() ? lineParallel : lineAntiParallel);
				LOG.trace("line: "+line);
			}
		}
		return line;
	}
	
	/**
	 * TODO
	 * <p>
	 * @deprecated
	 * 
	 * @return TODO
	 */
	@Deprecated
	public SVGPath createNarrowQuadrant() {
		SVGPath newPath = null;
		String signature = svgPath.getSignature();
		if (MCLC.equals(signature)) {
			PathPrimitiveList primList = svgPath.ensurePrimitives();
			Arc quadrant0 = primList.getQuadrant(1, ANGLE_EPS);
			Arc quadrant2 = primList.getQuadrant(3, ANGLE_EPS);
		}
		return newPath;
	}

	/**
	 * Converts circles represented as polygons (closed paths) into SVG circles
	 * <p>
	 * TODO
	 * 
	 * @param polygon
	 * @return circle
	 */
	public SVGCircle convertToCircle(SVGPolygon polygon) {
		Real2Range bbox = polygon.getBoundingBox();
		SVGCircle circle = null;
		double eps = 10 * RECT_EPS;//Why not?
		if (Math.abs(bbox.getXRange().getRange() - bbox.getYRange().getRange()) < eps) {
			Real2 centre = bbox.getCentroid();
			RealArray radArray = new RealArray();
			for (Real2 point : polygon.getReal2Array()) {
				radArray.addElement(centre.getDistance(point));
			}
			circle = new SVGCircle();
			circle.copyAttributesFrom(polygon);
			circle.setRad(radArray.getMean());
			circle.setCXY(centre);
		}
		return circle;
	}
	
	/**
	 * Converts narrow rectangles into lines
	 * <p>
	 * Converts to line running along the longer of the width and the height, so long as it is below minRectThickness ({@link setMinRectThickness} and {@link getMinRectThickness})
	 *  
	 * @param rect
	 * @return line
	 */
	public SVGLine createLineFromRect(SVGRect rect) {
		SVGLine line1 = null;
		SVGLine line2 = null;
		if (rect != null) {
			Real2 origin = rect.getXY();
			double width = rect.getWidth();
			double height = rect.getHeight();
			if (width < maxRectThickness) {
				line1 = new SVGLine(origin.plus(new Real2(width / 2, 0.0)), origin.plus(new Real2(width / 2, height)));
				line1.setStrokeWidth(width);
			} 
			if (height < maxRectThickness) {
				line2 = new SVGLine(origin.plus(new Real2(0.0, height / 2)), origin.plus(new Real2(width, height / 2)));
				line2.setStrokeWidth(height);
			}
		}
		SVGLine line =  (line1 == null ? line2 : (line2 == null ? line1 : (line1.getLength() > line2.getLength() ? line1 : line2)));
		return line;
	}

	/**
	 * @return maximum thickness for lines created from rectangles
	 */
	public double getMaxRectThickness() {
		return maxRectThickness;
	}

	/**
	 * @param maxRectThickness maximum thickness for lines created from rectangles
	 */
	public void setMaxRectThickness(double maxRectThickness) {
		this.maxRectThickness = maxRectThickness;
	}
	
	public void setSplitPolyLines(boolean split) {
		this.splitPolylines = split;
	}

	public boolean isSplitPolyLines() {
		return splitPolylines;
	}

}

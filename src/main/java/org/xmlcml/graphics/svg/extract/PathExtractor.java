package org.xmlcml.graphics.svg.extract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.util.MultisetUtil;
import org.xmlcml.graphics.svg.GraphicsElement;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.SVGUtil;
import org.xmlcml.graphics.svg.cache.SVGCache;
import org.xmlcml.graphics.svg.linestuff.Path2ShapeConverter;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

/** annotates the SVGPaths in a SVGElement.
 * 
 * @author pm286
 *
 */
public class PathExtractor extends AbstractExtractor{

	private static final Logger LOG = Logger.getLogger(PathExtractor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	private List<SVGPath> pathList;
	private Multiset<String> sigSet;
	private Map<String, SVGPath> pathBySig;
	private Map<String, String> charBySig;
	private String pathBoxColor;
	private String resolvedOutlineCol = "red";

	private Real2Range positiveXBox;
	private List<SVGPath> originalPathList;
	/** paths after trimming (out of box, duplicates, etc.) */
	private List<SVGPath> nonNegativePathList;
	/** paths that can't be converted to text or shapes */
	private List<SVGPath> unconvertedPathList;
	private List<SVGPath> trimmedShadowedPathList;
	private List<SVGPath> currentPathList;
	private List<SVGPath> positiveBoxPathList;
	
	public void setPositiveXBox(Real2Range positiveXBox) {
		this.positiveXBox = positiveXBox;
		
	}

	/** may wish to pass controls in a DTO
	 * currently simply extracts paths without splitting, etc.
	 * @param svgElement
	 */
	public void extractPaths(GraphicsElement svgElement) {
		this.originalPathList = SVGPath.extractPaths(svgElement);
		SVGPath.addSignatures(originalPathList);
		positiveBoxPathList = new ArrayList<SVGPath>(originalPathList);
		SVGElement.removeElementsOutsideBox(positiveBoxPathList, positiveXBox);
		nonNegativePathList = SVGPath.removePathsWithNegativeY(positiveBoxPathList);
		trimmedShadowedPathList = SVGPath.removeShadowedPaths(nonNegativePathList);
		
		currentPathList = originalPathList;
		currentPathList = SVGPath.removeShadowedPaths(currentPathList);
		return;
	}

	public PathExtractor(SVGCache svgStore) {
		super(svgStore);
		setDefaults();
	}
	
	private void setDefaults() {
		pathBoxColor = "orange";
	}

	public GraphicsElement analyzePaths(List<SVGPath> pathList) {
		this.pathList = pathList;
		SVGG g = new SVGG();
		g.setClassName("paths");
		if (pathList != null) {
			annotatePathsAsGlyphsWithSignatures();
		}
		return g;
	}
	
	private void annotatePathsAsGlyphsWithSignatures() {
		SVGG g = new SVGG();
		createCharBySig();
		SVGG gg = annotatePaths();
		g.appendChild(gg);
		Iterable<Entry<String>> iterable = MultisetUtil.getEntriesSortedByCount(sigSet);
		List<Entry<String>> list = MultisetUtil.createStringEntryList(iterable);
		SVGG ggg = annotatePathsWithSignatures();
		g.appendChild(ggg);
	}
	
	private SVGG annotatePathsWithSignatures() {
		SVGG g = new SVGG();
		g.setClassName("annotateAsGlyphs");
		for (String sig : pathBySig.keySet()) {
			SVGPath path = pathBySig.get(sig);
			Real2 xy = path.getBoundingBox().getLLURCorners()[0];
			xy.plusEquals(new Real2(10., 10.));
			g.appendChild(path.copy());
			SVGText text = new SVGText(xy, sig);
			text.setFontSize(3.);
			text.setOpacity(0.5);
			g.appendChild(text);
		}
		return g;
	}
	
	private SVGG annotatePaths() {
		SVGG g = new SVGG();
		sigSet = HashMultiset.create();
		pathBySig = new HashMap<String, SVGPath>();
		for (SVGPath path : pathList) {
			path.setStrokeWidth(0.5);
			String sig = path.getSignature();
			sigSet.add(sig);
			if (!pathBySig.containsKey(sig)) {
				pathBySig.put(sig, path);
			}
			Real2Range box = path.getBoundingBox();
			String c = charBySig.get(sig);
			if (c != null && !c.equals("")) {
				Real2 xy = box.getLLURCorners()[0].plus(new Real2(-5, -5));
				SVGText text = new SVGText(xy, c);
				text.setFill(resolvedOutlineCol);
				text.setStrokeWidth(0.1);
				text.setFontSize(6.0);
				g.appendChild(text);
			}
			SVGRect rect = SVGRect.createFromReal2Range(box);
			rect.setFill(pathBoxColor);
			rect.setStrokeWidth(0.2);
			rect.setOpacity(0.3);
			g.appendChild(rect);
		}
		return g;
	}

	public SVGG createSVGAnnotation() {
		SVGG g = new SVGG();
		
		g.setClassName("pathAnnotation NYI");
		return g;
	}
	
	private Map<String, String> createCharBySig() {
		charBySig = new HashMap<String, String>();
		charBySig.put("MLLCLLLL", "1");
    charBySig.put("MCCLCCCLCLLLCLC", "2");
    charBySig.put("MCCCCCCCCZ", "8");
    charBySig.put("MLLCLLLLLC", "r");
    charBySig.put("MLLLLCLC", "7");
    charBySig.put("MLCCLLLLLCCLL", "h");
    charBySig.put("MLCCCCLCCCC", "c");
    charBySig.put("MCCCCCCLCCZ", "9");
    charBySig.put("MCCLLLLLLCCCCLCC", "?");
    charBySig.put("MCCCCLCCCLLCCCLCC", "?");
    charBySig.put("MCCCCLCCCCZ", "?");
    charBySig.put("MLLCC", "?");
    charBySig.put("MCCCCCLCCLZ", "?");
    charBySig.put("MCCLLLLLCCZ", "?");
    charBySig.put("MLLLLLLCCLCCL", "?");
    charBySig.put("MCCCCZ", "?");
    charBySig.put("MLLCLLLLLLLLLLLCC", "?");
    charBySig.put("MCLLLC", "?");
    charBySig.put("MCLLLCZ", "?");
    charBySig.put("MLLLLLLCLLCCL", "?");
    charBySig.put("MZ", "?");
    charBySig.put("MCLCCCLCCCLCCCLCC", "?");
    charBySig.put("MLCCCCLLLLLCCLLLCCLL", "?");
    charBySig.put("MLCLLLCL", "?");
    charBySig.put("MCLLLLLCZ", "?");
    charBySig.put("MLLCLCCLCCLCCCCCCCZ", "?");
    charBySig.put("MCCCCL", "?");
    charBySig.put("MLLLCCCCLLZ", "?");
		return charBySig;
		
	}

	public List<SVGPath> getCurrentPathList() {
		return currentPathList;
	}

	public Collection<? extends SVGPath> getOriginalPathList() {
		return originalPathList;
	}

	public SVGG debug(String outFilename) {
		SVGG g = new SVGG();
		debug(g, originalPathList, "black", "yellow", 0.3);
		debug(g, positiveBoxPathList, "black", "red", 0.3);
		debug(g, nonNegativePathList, "black", "green", 0.3);
		debug(g, currentPathList, "black", "blue", 0.3);
		debug(g, trimmedShadowedPathList, "black", "cyan", 0.3);
		
		writeDebug("paths",outFilename, g);
		return g;
	}

	private void debug(SVGG g, List<SVGPath> pathList, String stroke, String fill, double opacity) {
		for (SVGPath p : pathList) {
			SVGPath path = (SVGPath) p.copy();
			path.setStroke(stroke);
			path.setStrokeWidth(0.2);
			path.setFill(fill);
			path.setOpacity(opacity);
			path.addTitle(p.getSignature());
			g.appendChild(path);
		}
	}

	public Real2Range getBoundingBox() {
		boundingBox = SVGElement.createBoundingBox(originalPathList);
		return boundingBox;
	}



	
}

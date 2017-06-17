package org.xmlcml.graphics.svg.plot;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.util.MultisetUtil;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

/** annotates the SVGPaths in a SVGElement.
 * 
 * @author pm286
 *
 */
public class PathAnnotator {

	private static final Logger LOG = Logger.getLogger(PathAnnotator.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	private List<SVGPath> pathList;
	private Multiset<String> sigSet;
	private Map<String, SVGPath> pathBySig;
	private Map<String, String> charBySig;
	private String pathBoxColor;
	private String resolvedOutlineCol = "red";

	
	public PathAnnotator() {
		
	}
	
	public SVGElement analyzePaths(List<SVGPath> pathList) {
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
		PlotBox.LOG.debug("> "+list);
		SVGG ggg = annotatePathsWithSignatures();
//		File pathsSvgFile = plotBox.svgOutFile != null ? new File(plotBox.svgOutFile+"paths.svg") :
//			new File("target/paths/paths.svg");
//		PlotBox.LOG.debug("Writing SVG Paths: "+pathsSvgFile);
		g.appendChild(ggg);
//		SVGSVG.wrapAndWriteAsSVG(g, pathsSvgFile);
	}
	
	private SVGG annotatePathsWithSignatures() {
		SVGG g = new SVGG();
		g.setClassName("annotateAsGlyphs");
		for (String sig : pathBySig.keySet()) {
			SVGPath path = pathBySig.get(sig);
			Real2 xy = path.getBoundingBox().getCorners()[0];
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
				Real2 xy = box.getCorners()[0].plus(new Real2(-5, -5));
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
			PlotBox.LOG.debug("****************** ADD RECT ****************");
			g.appendChild(rect);
		}
		return g;
	}

	public SVGG getSVGElement() {
		// TODO Auto-generated method stub
		return null;
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



	
}

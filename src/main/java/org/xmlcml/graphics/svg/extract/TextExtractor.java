package org.xmlcml.graphics.svg.extract;

import java.io.File;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.plot.PlotBox;

/** extracts texts within graphic area.
 * 
 * @author pm286
 *
 */
public class TextExtractor extends AbstractExtractor {
	private static final char BLACK_VERTICAL_RECTANGLE = (char)0x25AE;
	private static final char WHITE_VERTICAL_RECTANGLE = (char)0x25AF;
	private static final char WHITE_SQUARE = (char)0x25A1;
	private static final Logger LOG = Logger.getLogger(TextExtractor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private List<SVGText> originalTextList;
	private List<SVGText> nonNegativeYTextList;
	private List<SVGText> nonNegativeNonEmptyTextList;
	private List<SVGText> currentTextList;
	
	public TextExtractor(PlotBox plotBox) {
		super(plotBox);
	}

	public void extractTexts(SVGElement svgElement) {
		originalTextList = SVGText.extractSelfAndDescendantTexts(svgElement);
		nonNegativeYTextList = SVGText.removeTextsWithNegativeY(this.originalTextList);
		nonNegativeNonEmptyTextList = SVGText.removeTextsWithEmptyContent(nonNegativeYTextList, plotBox.isRemoveWhitespace());
		this.currentTextList = nonNegativeNonEmptyTextList;
	}

	public List<SVGText> getTextList() {
		return currentTextList;
	}

	public SVGG debug(String outFilename) {
		SVGG g = new SVGG();
		// derived
		debug(g, originalTextList,"yellow",  "black", 0.3);
		debug(g, nonNegativeYTextList, "red", "black", 0.3);
		debug(g, nonNegativeNonEmptyTextList, "green", "black", 0.3);
		File outFile = new File(outFilename);
		SVGSVG.wrapAndWriteAsSVG(g, outFile);
		LOG.debug("wrote shapes: "+outFile.getAbsolutePath());
		return g;
	}

	private void debug(SVGG g, List<? extends SVGElement> elementList, String stroke, String fill, double opacity) {
		for (SVGElement e : elementList) {
			SVGText text = (SVGText) e.copy();
			text.setStyle(null);
			text.setStroke(stroke);
			text.setStrokeWidth(0.4);
			text.setFill(fill);
			text.setOpacity(opacity);
			String s = text.getValue();
			if (text.isRot90()) {
				Real2Range box = text.getBoundingBox();
				SVGRect box0 = SVGElement.createGraphicalBox(box, 0.0, 0.0);
				box0.setStrokeWidth(0.1);
				box0.setFill("cyan");
				box0.setOpacity(0.2);
				g.appendChild(box0);
			}
			if (s == null || s.equals("null")) {
				text.setText(String.valueOf(WHITE_SQUARE));
				text.setFill("cyan");
			} else if ("".equals(s)) {
				text.setText(String.valueOf(BLACK_VERTICAL_RECTANGLE));
				text.setFill("pink");
			} else if ("".equals(s.trim())) {
				text.setText(String.valueOf(BLACK_VERTICAL_RECTANGLE));
				text.setFill("cyan");
			} else {
				Real2Range box = text.getBoundingBox();
				SVGRect box0 = SVGElement.createGraphicalBox(box, 0.0, 0.0);
				box0.setStrokeWidth(0.1);
				box0.setFill("yellow");
				box0.setOpacity(0.2);
				g.appendChild(box0);
			}
			String title = s == null || "".equals(s.trim()) ? "empty" : s;
			text.addTitle(title);
			g.appendChild(text);
		}
	}
}

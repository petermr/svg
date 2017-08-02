package org.xmlcml.graphics.svg.extract;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.GraphicsElement;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.store.SVGStore;

/** extracts texts within graphic area.
 * 
 * @author pm286
 *
 */
public class TextExtractor extends AbstractExtractor {
	private static final char BLACK_VERTICAL_RECTANGLE = (char)0x25AE;
	private static final char WHITE_VERTICAL_RECTANGLE = (char)0x25AF;
	private static final char WHITE_SQUARE = (char)0x25A1;
	static final Logger LOG = Logger.getLogger(TextExtractor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private List<SVGText> originalTextList;
	private List<SVGText> nonNegativeYTextList;
	private List<SVGText> nonNegativeNonEmptyTextList;
	private List<SVGText> currentTextList;
	
	public TextExtractor(SVGStore svgStore) {
		super(svgStore);
	}

	public void extractTexts(GraphicsElement svgElement) {
		originalTextList = SVGText.extractSelfAndDescendantTexts(svgElement);
		nonNegativeYTextList = SVGText.removeTextsWithNegativeY(this.originalTextList);
		nonNegativeNonEmptyTextList = SVGText.removeTextsWithEmptyContent(nonNegativeYTextList, svgStore.isRemoveWhitespace());
//		this.currentTextList = nonNegativeNonEmptyTextList;
		this.currentTextList = originalTextList;
	}

	public List<SVGText> getTextList() {
		return currentTextList;
	}

	public SVGG debug(String outFilename) {
		SVGG g = new SVGG();
		// derived
		appendDebugToG(g, originalTextList,"yellow",  "black", 0.3, 10.0, "Helvetica");
		appendDebugToG(g, nonNegativeYTextList, "red", "black", 0.3, 12.0, "serif");
		appendDebugToG(g, nonNegativeNonEmptyTextList, "green", "black", 0.3, 14.0, "monospace");
		drawBox(g, "green", 2.0);

		writeDebug("texts",outFilename, g);
		return g;
	}

	private void appendDebugToG(SVGG g, List<? extends SVGElement> elementList, String stroke, String fill, double opacity, double fontSize, String fontFamily) {
		for (GraphicsElement e : elementList) {
			SVGText text = (SVGText) e.copy();
			text.setCSSStyleAndRemoveOldStyle(null);
			text.setStroke(stroke);
			text.setStrokeWidth(0.4);
			text.setFill(fill);
			text.setFontSize(fontSize);
			text.setFontFamily(fontFamily);
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
				addAnnotationRect(g, text);
			}
			String title = s == null || "".equals(s.trim()) ? "empty" : s;
			text.addTitle(title);
			g.appendChild(text);
		}
	}

	private void addAnnotationRect(SVGG g, SVGText text) {
		Real2Range box = text.getBoundingBox();
		SVGRect box0 = SVGElement.createGraphicalBox(box, 0.0, 0.0);
		box0.setStrokeWidth(0.1);
		box0.setFill("yellow");
		box0.setOpacity(0.2);
		g.appendChild(box0);
	}
	
	public Real2Range getBoundingBox() {
		boundingBox = SVGElement.createBoundingBox(originalTextList);
		return boundingBox;
	}

}

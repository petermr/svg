package org.xmlcml.graphics.svg.cache;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.GraphicsElement;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.normalize.TextDecorator;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.plot.AnnotatedAxis;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/** extracts texts within graphic area.
 * 
 * @author pm286
 *
 */
public class TextCache extends AbstractCache {
	private static final char BLACK_VERTICAL_RECTANGLE = (char)0x25AE;
	private static final char WHITE_VERTICAL_RECTANGLE = (char)0x25AF;
	private static final char WHITE_SQUARE = (char)0x25A1;
	static final Logger LOG = Logger.getLogger(TextCache.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private List<SVGText> horizontalTexts;
	private List<SVGText> verticalTexts;

	private List<SVGText> originalTextList;
	private List<SVGText> nonNegativeYTextList;
	private List<SVGText> nonNegativeNonEmptyTextList;
	private List<SVGText> currentTextList;
	
	public TextCache(ComponentCache svgCache) {
		super(svgCache);
	}

	public void extractTexts(GraphicsElement svgElement) {
		originalTextList = SVGText.extractSelfAndDescendantTexts(svgElement);
		nonNegativeYTextList = SVGText.removeTextsWithNegativeY(this.originalTextList);
		nonNegativeNonEmptyTextList = SVGText.removeTextsWithEmptyContent(nonNegativeYTextList, componentCache.isRemoveWhitespace());
		this.currentTextList = originalTextList;
	}

	public List<SVGText> getTextList() {
		return currentTextList;
	}
	
	public List<? extends SVGElement> getOrCreateElementList() {
		return getTextList();
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
	
	/** the bounding box of the actual text components
	 * The extent of the context (e.g. svgCache) may be larger
	 * @return the bounding box of the contained text
	 */
	public Real2Range getBoundingBox() {
		return getOrCreateBoundingBox(originalTextList);
	}

	public void createHorizontalAndVerticalTexts() {
		getOrCreateHorizontalTexts();
		getOrCreateVerticalTexts();
//		this.horizontalTexts = SVGText.findHorizontalOrRot90Texts(currentTextList, LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
//		this.verticalTexts = SVGText.findHorizontalOrRot90Texts(currentTextList, LineDirection.VERTICAL, AnnotatedAxis.EPS);
	}
	public List<SVGText> getOrCreateHorizontalTexts() {
		if (horizontalTexts == null) {
			horizontalTexts = SVGText.findHorizontalOrRot90Texts(currentTextList, LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		}
		return horizontalTexts;
	}

	public List<SVGText> getOrCreateVerticalTexts() {
		if (verticalTexts == null) {
			verticalTexts = SVGText.findHorizontalOrRot90Texts(currentTextList, LineDirection.VERTICAL, AnnotatedAxis.EPS);
		}
		return verticalTexts;
	}

	public Multiset<String> getHorizontalTextStyleMultiset() {
		return getTextStyleMultiset(horizontalTexts);
	}

	public Multiset<String> getVerticalTextStyles() {
		return getTextStyleMultiset(verticalTexts);
	}

	private Multiset<String> getTextStyleMultiset(List<SVGText> texts) {
		Multiset<String> styleSet = HashMultiset.create();
		for (SVGText text : texts) {
			String style = text.getStyle();
			style = style.replaceAll("clip-path\\:url\\(#clipPath\\d+\\);", "");
			styleSet.add(style);
		}
		return styleSet;
	}

	/** replaces long form os style by abbreviations.
	 * remove clip-paths
	 * Remove String values and attributes
	 * "font-family, Helvetica, font-weight, normal,font-size, px, font-style, #fff(fff), stroke, none, fill"
	 * 
	 * resultant string is of form:
	 * color (optional)
	 * ~ // serif (optional)
	 * ddd // font-size x 10 (mandatory)
	 * B // bold (optional
	 * I // italic (optional
	 * stroke (optional)
	 * 
	 * colors are flattened to hex hex hex
	 * color abbreviations (with some tolerances)
	 * . grey
	 * * black
	 * r g b
	 * 
	 * @return
	 */
	public Multiset<String> createAbbreviatedHorizontalTextStyleMultiset() {
		Multiset<String> styleSet = getHorizontalTextStyleMultiset();
		Multiset<String> abbreviatedStyleSet = HashMultiset.create();
		for (Multiset.Entry<String> entry : styleSet.entrySet()) {
			int count = entry.getCount();
			String style = entry.getElement();
			style = abbreviateStyle(style);
			abbreviatedStyleSet.add(style, count);
		}
		return abbreviatedStyleSet;
	}
	
	/** replaces long form of style by abbreviations.
	 * remove clip-paths
	 * Remove String values and attributes
	 * "font-family, Helvetica, font-weight, normal,font-size, px, font-style, #fff(fff), stroke, none, fill"
	 * 
	 * resultant string is of form:
	 * color (optional)
	 * ~ // serif (optional)
	 * ddd // font-size x 10 (mandatory)
	 * B // bold (optional
	 * I // italic (optional
	 * stroke (optional)
	 * 
	 * colors are flattened to hex hex hex
	 * color abbreviations (with some tolerances)
	 * . grey
	 * * black
	 * r g b
	 * 
	 * @return
	 */

	public static String abbreviateStyle(String style) {
		style = style.replaceAll("font-family:", "");
		style = style.replaceAll("TimesNewRoman;", "~");
		style = style.replaceAll("Helvetica;", "");
		style = style.replaceAll("font-weight:", "");
		style = style.replaceAll("normal;", "");
		style = style.replaceAll("bold;", "B");
		style = style.replaceAll("font-size:", "");
		style = style.replaceAll("(\\d+)\\.(\\d)\\d*", "$1$2");
		style = style.replaceAll("px;", "");
		style = style.replaceAll("font-style:", "");
		style = style.replaceAll("italic;", "I");
		style = style.replaceAll("#(.)(.)(.)(.)(.)(.);", "#$1$3$5;"); // compress rgb
		style = style.replaceAll("#000;", "*");
		style = style.replaceAll("#fff;", "");
		style = style.replaceAll("#[12][12][12];", "."); // grey
		style = style.replaceAll("#[012][012][cdef];", "b");
		style = style.replaceAll("#[012][cdef][012];", "g");
		style = style.replaceAll("#[cdef][012][012];", "r");
		style = style.replaceAll("stroke:", "");
		style = style.replaceAll("none;", "");
		style = style.replaceAll("fill:", "");
		style = style.replaceAll("clip-path\\:url\\(#clipPath\\d+\\);", "");
		return style;
	}

	public SVGG createCompactedTextsAndReplace() {
		TextDecorator textDecorator = new TextDecorator();
		SVGG g = textDecorator.compactTexts(originalTextList);
		currentTextList = SVGText.extractSelfAndDescendantTexts(g);
		componentCache.allElementList = null;
		componentCache.boundingBoxList = null;
		return g;
	}


}

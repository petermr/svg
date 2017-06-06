package org.xmlcml.graphics.svg.text;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.xml.XMLUtil;

/** holds a "paragraph".
 * 
 * Currently driven by <p> elements emitted by Tesseract. These in turn hold lines and words.
 * Still exploratory
 * 
 * @author pm286
 *
 */
public class SVGWord extends SVGG {

	
	private static final char EMDASH = (char)8211;
	private static final double DELTA_Y_TEXT = 0.3;
	private static final Logger LOG = Logger.getLogger(SVGWord.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public static final String CLASS = "word";
	private double interCharacterFactor = 0.1;
	
	public SVGWord() {
		super();
		this.setClassName(CLASS);
	}

	public SVGWord(SVGElement svgText) {
		this.appendChild(svgText.copy());
	}

	/** from Tesseract
	 * 
	 * @return
	 */
	public SVGText getSVGText() {
		return (SVGText) XMLUtil.getSingleElement(this, "*[local-name()='"+SVGText.TAG+"']");
	}
	
	/**
	 * gap between end of last word and start of this.
	 * 
	 * if either component is null, return zero
	 * 
	 * @param lastWord preceding word
	 * @return
	 */
	public double gapFollowing(SVGWord lastWord) {
		Real2Range lastBox = (lastWord == null) ? null : lastWord.getChildRectBoundingBox();
		Real2Range thisBox = this.getChildRectBoundingBox();
		return (lastBox == null || thisBox == null) ? 0.0 : thisBox.getXMin() - lastBox.getXMax();
	}

	/**
	 * gap between end of last word and start of this.
	 * 
	 * if either component is null, return zero
	 * 
	 * @param lastWord preceding word
	 * @return
	 */
	public double gapBefore(SVGText nextText) {
		if (nextText == null) return Double.NaN;
		Real2Range nextBox = nextText.getBoundingBox();
		Real2Range thisBox = this.getBoundingBox();
		return nextBox.getXMin() - thisBox.getXMax();
	}

	public Real2Range getBoundingBox() {
		SVGText text = this.getSVGText();
		return text == null ? null : text.getBoundingBox();
	}
	
	public Double getFontSize() {
		SVGElement text = this.getSVGText();
		return text == null ? null : text.getFontSize();
	}

	
	@Override
	public String toString() {
		return getSVGText() == null ? null : getSVGText().toString();
	}

	public boolean canAppend(SVGText text) {
		double horizontalGap = gapBefore(text);
		if (horizontalGap > interCharacterFactor * getFontSize()) {
			return false;
		}
		Real2 deltaXY = this.getXY().subtract(text.getXY()); 
		return Math.abs(deltaXY.getY()) < (DELTA_Y_TEXT * this.getFontSize());
	}

	public void append(SVGText newText) {
		SVGText svgText = this.getSVGText();
		if (svgText != null) {
//			Real2Range bbox = text.getBoundingBox().plusEquals(newText.getBoundingBox());
			String textValue = svgText.getText();
			svgText.getChild(0).detach();
			String newValue = newText.getText();
			svgText.appendChild(textValue + newValue);
			svgText.getBoundingBox();
		}
	}

	public String getStringValue() {
		SVGText text = getSVGText();
		return (text == null) ? null : text.getText();
	}
	
	public Real2 getXY() {
		SVGText text = getSVGText();
		return (text == null) ? null : text.getXY();
	}

	public Double getX() {
		Real2 xy = getXY();
		return xy == null ? null : new Double(xy.getX());
	}
	
	public Double getY() {
		Real2 xy = getXY();
		return xy == null ? null : new Double(xy.getY());
	}
	
	public String getSVGTextValue() {
		SVGElement text = this.getSVGText();
		return text == null ? null : text.getValue();
	}

	public void emdashToMinus() {
		String s = this.getStringValue();
		if (s != null && s.contains(String.valueOf(EMDASH))) {
			String s1 = s.replaceAll(String.valueOf(EMDASH), S_MINUS);
			this.getSVGText().setText(s1);
			if (!s.equals(s1)) {
				LOG.debug("EMDASH: "+s+" => "+s1);
			}
		}
	}

}

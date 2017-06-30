package org.xmlcml.graphics.svg.normalize;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.StyleAttribute;

import nu.xom.Attribute;

/**
 * wraps a SVGText so it can be built without accessing the XOM
 * 
 * @author pm286
 *
 */
public class TextDecorator extends AbstractDecorator {
	private static final Logger LOG = Logger.getLogger(TextDecorator.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private static final double X_EPS = 0.01;
	private static final double Y_EPS = 0.01;
	
	private double xeps;
	private double yeps;
	private RealArray xValues;
	private RealArray yValues;
	private List<SVGText> textList;
	private List<List<SVGText>> textListList;

	public TextDecorator() {
		setDefaults();
	}
	
	public TextDecorator(SVGText text0) {
		this();
		this.attributeComparer = new AttributeComparer(text0);
	}

	private void setDefaults() {
		this.xeps = X_EPS;
		this.yeps = Y_EPS;
		this.xValues = new RealArray();
		this.yValues = new RealArray();
	}

	public List<List<SVGText>> normalize(List<SVGText> texts) {
		textListList = new ArrayList<List<SVGText>>();
		if (texts != null && texts.size() > 0) {
			int ichar = 1;
			resetText0(texts.get(0));
			while (ichar < texts.size()) {
				SVGText text1 = texts.get(ichar);
				ichar++;
				attributeComparer.setElement1(text1);
				Set<String> attNames0Not1 = attributeComparer.getAttNames0Not1();
				Set<String> attNames1Not0 = attributeComparer.getAttNames1Not0();
				if (attNames0Not1.size() + attNames1Not0.size() != 0) {
					LOG.debug("attnames change "+attNames0Not1.size() + attNames1Not0);
					resetText0(text1);
					continue;
				}
				Set<Pair<Attribute, Attribute>> unequalAttValues = attributeComparer.getUnequalTextValues();
				if (unequalAttValues.size() != 0) {
					LOG.debug(unequalAttValues);
					resetText0(text1);
					continue;
				} else if (!this.hasEqualYCoord(textList.get(0), text1, yeps)) {
					LOG.debug("ycoord changed "+textList.get(0)+" // "+text1);
					resetText0(text1);
					continue;
				} else {
					LOG.trace("adding "+text1);
					textList.add(text1);
				}
			}
		}
		return textListList;
	}

	private void resetText0(SVGText text) {
		textList = new ArrayList<SVGText>();
		textListList.add(textList);
		textList.add(text);
		attributeComparer.setElement0(text);
	}

	public Double getY() {
		return textList == null || textList.size() == 0 ? null :  textList.get(0).getY();
	}

	public SVGText getNormalizedText() {
		SVGText normalizedText = null;
		if (textList != null && textList.size() > 0) {
			normalizedText = new SVGText(textList.get(0));
			attributeComparer = new AttributeComparer(new SVGText(textList.get(0)));
			for (int i = 1; i < textList.size(); i++) {
				SVGText text1 = textList.get(i);
			}
		}
		return normalizedText;
	}

	public void add(SVGText text) {
		textList.add(text);
	}

	public boolean hasEqualYCoord(SVGText text0, SVGText text1, double eps) {
		double y0 = text0.getY();
		double y1 = text1.getY();
		return Real.isEqual(y0, y1, eps);
	}

	public SVGG convertTexts2Array() {
		SVGG g = new SVGG();
		for (List<SVGText> textList : textListList) {
			RealArray xArray = new RealArray();
			RealArray wArray = new RealArray();
			StringBuilder sb = new StringBuilder();
			StringBuilder sbw = new StringBuilder();
			for (int i = 0; i < textList.size(); i++) {
				SVGText text = textList.get(i);
				xArray.addElement(text.getX());
				sb.append(text.getValue());
				wArray.addElement(text.getSVGXFontWidth());
			}
			SVGText arrayText = new SVGText(textList.get(0));
			arrayText.setX(xArray);
			arrayText.setSVGXFontWidth(wArray);
			arrayText.setText(sb.toString());
			StyleAttribute.createStyleAttribute(arrayText, true);
			g.appendChild(arrayText);
		}
		return g;
	}

	
}

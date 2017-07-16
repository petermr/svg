package org.xmlcml.graphics.svg.normalize;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.StyleAttributeFactory;
import org.xmlcml.graphics.svg.StyleAttributeFactory.AttributeStrategy;
import org.xmlcml.graphics.svg.util.ColorStore;
import org.xmlcml.graphics.svg.util.ColorStore.ColorizerType;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

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

	public static final boolean BOXES = true;
	private static final boolean NO_BOXES = false;

	private static final double X_EPS = 0.01;
	private static final double Y_EPS = 0.01;
	
	private double xeps;
	private double yeps;
	private RealArray xValues;
	private RealArray yValues;
	private List<SVGText> textList;
	private List<List<SVGText>> uncompactedTextListList;
//	private List<SVGText> compactedTextListList;
	private Real2Range textBoundingBox;
	private StyleAttributeFactory styleAttribute;
	private boolean isAddBoxes;

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

	public SVGG compact(List<SVGText> texts) {
		uncompactedTextListList = new ArrayList<List<SVGText>>();
		if (texts != null && texts.size() > 0) {
			addSingleCharTextToUncompactedList(texts.get(0));
			for (int ichar = 1; ichar < texts.size(); ichar++) {
				addCharacterToTextLists(texts.get(ichar));
			}
			for (SVGText text : texts) {
				text.detach();
			}
		}
		SVGG g = makeCompactedTextsAndAddToG();
		return g;

	}

	private void addCharacterToTextLists(SVGText text) {
		attributeComparer.setElement1(text);
		Set<String> attNames0Not1 = attributeComparer.getAttNames0Not1();
		Set<String> attNames1Not0 = attributeComparer.getAttNames1Not0();
		if (attNames0Not1.size() + attNames1Not0.size() != 0) {
			LOG.debug("attnames change "+attNames0Not1.size() + attNames1Not0);
			addSingleCharTextToUncompactedList(text);
		}
		Set<Pair<Attribute, Attribute>> unequalAttValues = attributeComparer.getUnequalTextValues();
		if (unequalAttValues.size() != 0) {
			LOG.debug(unequalAttValues);
			addSingleCharTextToUncompactedList(text);
		} else if (!this.hasEqualYCoord(textList.get(0), text, yeps)) {
			LOG.debug("ycoord changed "+textList.get(0)+" // "+text);
			addSingleCharTextToUncompactedList(text);
		} else {
			LOG.trace("adding "+text);
			textList.add(text);
		}
	}
	
	public SVGG decompact(List<SVGText> texts) {
		throw new RuntimeException("decompact NYI");
	}


	private void addSingleCharTextToUncompactedList(SVGText text) {
		textList = new ArrayList<SVGText>();
		uncompactedTextListList.add(textList);
		textList.add(text);
		attributeComparer.setElement0(text);
	}

	public Double getY() {
		return textList == null || textList.size() == 0 ? null :  textList.get(0).getY();
	}

	/** does this do anything?
	 * Not yet
	 * @return
	 */
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

	public SVGG makeCompactedTextsAndAddToG() {
		SVGG g = new SVGG();
		Multiset<String> styleSet = HashMultiset.create();
		for (List<SVGText> textList : uncompactedTextListList) {
			SVGText compactedText = createCompactText(textList);
			g.appendChild(compactedText);
			String style = compactedText.getStyle();
//			String style = styleAttribute.getAttributeValue();
			styleSet.add(style);
		}
		
		LOG.debug(styleSet+"; "+styleSet.entrySet().size());
		return g;
	}

//	private Color getNextAvailableColor(Map<String, Color> colorByStyle, String style, ColorStore colorizer) {
//		Color color = colorByStyle.get(style);
//		if (color == null) {
//			color = colorizer.getNextAvailableColor(colorByStyle.size());
//			colorByStyle.put(style, color);
//		}
//		return color;
//	}

	/** compacts a list of SVGTexts into a gingle SVGText.
	 * retains current boundingBox and styleAttribute.
	 * Does not test that y or styleAttribute is constant
	 * 
	 * @param textList
	 * @return
	 */
	private SVGText createCompactText(List<SVGText> textList) {
		RealArray xCoordinateArray = new RealArray();
		RealArray widthArray = new RealArray();
		StringBuilder textContentBuilder = new StringBuilder();
		textBoundingBox = new Real2Range();
		for (int i = 0; i < textList.size(); i++) {
			SVGText text = textList.get(i);
			textBoundingBox.plusEquals(text.getBoundingBox());
			xCoordinateArray.addElement(text.getX());
			textContentBuilder.append(text.getValue());
			widthArray.addElement(text.getSVGXFontWidth());
		}
		SVGText arrayText = new SVGText(textList.get(0));
		arrayText.setX(xCoordinateArray);
		arrayText.setSVGXFontWidth(widthArray);
		arrayText.setText(textContentBuilder.toString());
		StyleAttributeFactory.createAndAddOldStyleAttribute(arrayText);
		return arrayText;
	}

	public void setAddBoxes(boolean b) {
		this.isAddBoxes = b;
	}


	
}

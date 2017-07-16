package org.xmlcml.graphics.svg;

import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.graphics.svg.normalize.AttributeComparer;

import nu.xom.Attribute;

/** supports CSS-like style attribute list of name-value pairs
 * 
 * current strategy is to use a single style attribute rather than individual "old-style" fill=, stroke=, etc.
 * this makes it easier to keep track when new attributes are added.
 * 
 * thus
 *   SVGElement circle = new SVGCircle();
 *   circle.setFill("bar")
 *   will create <svg:circle style="fill:bar;" ...
 *   
 *   if style already uses fill, it will be updated
 *   
 *   circle.getFill()
 *   will use the 'style' attribute
 *   
 * 
 * @author pm286
 *
 */
public class StyleAttributeFactory {
	private static final Logger LOG = Logger.getLogger(StyleAttributeFactory.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public enum AttributeStrategy {
		KEEP,
		REMOVE,
		OVERWRITE,
		MERGE
	}
	private static final String STYLE = "style";
	private Map<String, String> styleMap;
	private String attributeValue;

	public StyleAttributeFactory() {
		styleMap = new HashMap<String, String>();
	}

	/** create StyleAttributeFactory from packed CSS name-values
	 * 
	 * @param cssValue
	 */
	public StyleAttributeFactory(String cssValue) {
		this();
		splitAndAddAttributeToMap(cssValue);
	}

	/**
	 * removes all old-style style attributes (fill="foo", etc) and creates or updates a
	 * single style attribute.
	 * This should only be used to clean up old style attributes. new code will always create
	 * style attributes
	 * 
	 * @param element
	 * @param strategy if OVERWRITE will overwrite values in style attribute with old style values
	 * @return new Style attribute 
	 */
	public static StyleAttributeFactory createUpdatedStyleAttribute(GraphicsElement element, AttributeStrategy strategy) {
		StyleAttributeFactory oldStyleAttributeFactory = element.createOldStyleAttributeFactory();
		StyleAttributeFactory existingStyleAttributeFactory = element.getExistingStyleAttributeFactory();
		StyleAttributeFactory newStyleAttributeFactory = null;
		if (strategy == null) {
			throw new RuntimeException("null strategy");
		} else if (AttributeStrategy.OVERWRITE == strategy) {
			element.removeOldStyleAttributes();
			newStyleAttributeFactory = oldStyleAttributeFactory;
		} else if (AttributeStrategy.KEEP == strategy) {
			newStyleAttributeFactory = existingStyleAttributeFactory;
		} else if (AttributeStrategy.MERGE == strategy) {
			newStyleAttributeFactory = oldStyleAttributeFactory.createMergedAttributeFactory(existingStyleAttributeFactory);
			element.removeOldStyleAttributes();
		} else {
			throw new RuntimeException("unknown "+strategy);
		}
		return newStyleAttributeFactory;
	}

//	public static void createAndAddUpdatedStyleAttribute(GraphicsElement element, AttributeStrategy update) {
//		StyleAttributeFactory styleAttribute = StyleAttributeFactory.createStyleAttributeFactoryFromOldStyle(element, update);
//		element.addAttribute(styleAttribute.createAttribute());
//	}
	
	public static void createAndAddOldStyleAttribute(GraphicsElement element) {
		StyleAttributeFactory styleAttributeFactory = element.createOldStyleAttributeFactory();
		addStyleAttribute(element, styleAttributeFactory);
	}

	
	/** adds old-style attribute to Style attribute
	 * 
	 * @param att
	 */
	public void addToMap(Attribute att) {
		if (styleMap.containsKey(att.getLocalName())) {
			throw new RuntimeException("Duplicate attribute: "+att);
		}
		styleMap.put(att.getLocalName(), att.getValue());
	}
	
	/** returns CSS-like string sorted by attribute names.
	 * 
	 * @return
	 */
	public String getAttributeValue() {
		if (attributeValue == null) {
			List<String> attNames = Arrays.asList(styleMap.keySet().toArray(new String[0]));
			Collections.sort(attNames);
			StringBuilder sb = new StringBuilder();
			for (String attName : attNames) {
				String units = getUnits(attName);
				sb.append(attName+":"+GraphicsElement.addUnits(styleMap.get(attName), units)+";");
			}
			attributeValue = sb.toString();
		}
		return attributeValue;
	}

	/** merges 2 SAFs.
	 * 
	 * @param styleAttributeFactory to merge with this
	 * @return merged SAF. 'this' is NOT Affected
	 */
	public StyleAttributeFactory createMergedAttributeFactory(StyleAttributeFactory styleAttributeFactory) {
		StyleAttributeFactory newAttributeFactory = new StyleAttributeFactory();
		newAttributeFactory.putEntries(this.styleMap);
		if (styleAttributeFactory != null) newAttributeFactory.putEntries(styleAttributeFactory.styleMap);
		return newAttributeFactory;
	}


	// ===================================
	
	/** adds or replaces STYLE attribute by contents of factory.
	 * 
	 * if SAF is null or empty will DELETE existing style attribute.
	 * 
	 * @param element STYLE attribute will be changed
	 * @param styleAttributeFactory attributeFactory to replace with
	 */
	private static void addStyleAttribute(GraphicsElement element, StyleAttributeFactory styleAttributeFactory) {
		String value = styleAttributeFactory.getAttributeValue();
		if (("").equals(value)) value = null;
		element.addAttribute(new Attribute(STYLE, styleAttributeFactory.getAttributeValue()));
	}

	/** certain attributes (currently only font-size) sometimes require units
	 * 
	 * @param attName
	 * @return units or empty string
	 */
	private String getUnits(String attName) {
		String units = attName.equals(StyleBundle.FONT_SIZE) ? GraphicsElement.PX : "";
		return units;
	}
	
	private Attribute createAttribute() {
		Attribute attribute = new Attribute(STYLE, getAttributeValue());
		return attribute;
	}

	private void putEntries(Map<String, String> styleMap) {
		for (Map.Entry<String, String> entry : styleMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (value == null && this.styleMap.get(key) != null) {
				this.styleMap.remove(key);
			} else {
				this.styleMap.put(key, value);
			}
		}
	}

	private void updateMap(Attribute styleAtt) {
		if (styleAtt != null) {
			String attValue = styleAtt.getValue();
			splitAndAddAttributeToMap(attValue);
		}
	}

	private void splitAndAddAttributeToMap(String attValue) {
		String[] values = attValue == null ? new String[] {} : attValue.split("\\s*;\\s*");
		for (String value : values) {
			if (value.trim().length() == 0) continue;
			String[] splits = value.split("\\s*:\\s*");
			if (splits.length != 2) {
				throw new RuntimeException("bad style attribute "+value+"; in "+attValue);
			}
			String styleName = splits[0];
			String styleValue = splits[1];
			if (!AttributeComparer.STYLE_SET.contains(styleName)) {
				LOG.warn("Unknown style name ignored: "+styleName);
			} else {
				styleMap.put(styleName, styleValue);
			}
		}
	}

	public String getAttributeValue(String attName) {
		return styleMap.get(attName);
	}

	public Map<String, String> getStyleMap() {
		return styleMap;
	}

}

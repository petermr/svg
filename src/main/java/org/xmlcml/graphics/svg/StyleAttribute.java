package org.xmlcml.graphics.svg;

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
 * @author pm286
 *
 */
public class StyleAttribute {
	private static final Logger LOG = Logger.getLogger(StyleAttribute.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public enum Preserve {
		KEEP,
		REMOVE
	}
	private static final String STYLE = "style";
	private Map<String, String> nameValueMap;

	public StyleAttribute() {
		nameValueMap = new HashMap<String, String>();
	}
	
	public static StyleAttribute createStyleAttribute(SVGElement element, Preserve preserve) {
		StyleAttribute styleAttribute = new StyleAttribute();
		for (int i = element.getAttributeCount() - 1; i >= 0; i--) {
			Attribute attribute = element.getAttribute(i);
			if (AttributeComparer.STYLE_SET.contains(attribute.getLocalName())) {
				styleAttribute.add(attribute);
				if (Preserve.REMOVE == preserve) {
					attribute.detach();
				}
			}
		}
		if (Preserve.REMOVE == preserve) {
			element.addAttribute(new Attribute(STYLE, styleAttribute.getStringValue()));
		}
		return styleAttribute;
	}
	
	public void add(Attribute att) {
		if (nameValueMap.containsKey(att.getLocalName())) {
			throw new RuntimeException("Duplicate attribute: "+att);
		}
		nameValueMap.put(att.getLocalName(), att.getValue());
	}
	
	public String getStringValue() {
		List<String> attNames = Arrays.asList(nameValueMap.keySet().toArray(new String[0]));
		Collections.sort(attNames);
		StringBuilder sb = new StringBuilder();
		for (String attName : attNames) {
			String units = attName.equals("font-size") ? "px" : "";
			sb.append(attName+":"+nameValueMap.get(attName)+units+";");
		}
		return sb.toString();
	}
	
	
}

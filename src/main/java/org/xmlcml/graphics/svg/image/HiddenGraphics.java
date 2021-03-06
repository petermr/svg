/**
 *    Copyright 2011 Peter Murray-Rust et. al.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.xmlcml.graphics.svg.image;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class HiddenGraphics {

	private final static Logger LOG = Logger.getLogger(HiddenGraphics.class);
	
	private static final String PNG = "png";
	private Dimension dimension;
	private BufferedImage img;
	private Graphics2D g;
	private String type;
	private Color backgroundColor;
	
	public HiddenGraphics() {
		setDefaults();
	}
	private void setDefaults() {
		type = PNG;
		setDefaultDimension();
		setBackgroundColor(Color.WHITE);
	}
	private void setDefaultDimension() {
		setDimension(new Dimension(400, 400));
	}
	private void setBackgroundColor(Color color) {
		backgroundColor = color;
	}
	public void setDimension(Dimension d) {
		dimension = d;
	}

	public Graphics2D createGraphics() {
		return createGraphics(null);
	}

	public Graphics2D createGraphics(SVGElement element) {
		if (element != null) {
			Real2Range boundingBox = element.getBoundingBox();
			setDimension(boundingBox.getDimension());
		} else {
			setDefaultDimension();
		}
		// there may be ultra thin images for lines, etc.
		img = new BufferedImage(Math.max(1, dimension.width), Math.max(1, dimension.height), BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		g.setBackground(backgroundColor);
		g.clearRect(0, 0, dimension.width, dimension.height);
		return g;
	}
	
	public void setOutputType(String type) {
		this.type = type;
	}
	
	public void write(String mimeType, File file) throws IOException {
		SVGImage.writeBufferedImage(img, mimeType, file);
	}
	
	public BufferedImage createImageTranslatedToOrigin(SVGElement element) {
		SVGElement elementCopy = SVGElement.readAndCreateSVG(element);
		return createImage(elementCopy);
	}
		
	public BufferedImage createImage(SVGElement element) {
		Graphics2D g2D = createGraphics(element);
		LOG.trace(element.toXML());
		element.draw(g2D);
		return img;
	}
	
}

package org.xmlcml.graphics.svg.cache;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.GraphicsElement;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGImage;

/** annotates the SVGImages in a SVGElement.
 * 
 * @author pm286
 *
 */
public class ImageCache extends AbstractCache{

	private static final Logger LOG = Logger.getLogger(ImageCache.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	private List<SVGImage> imageList;
	private String imageBoxColor;

	public List<SVGImage> getOrCreateImageList() {
		if (imageList == null) {
			imageList = SVGImage.extractSelfAndDescendantImages(componentCache.svgElement);
		}
		return imageList;
	}

	public ImageCache(ComponentCache svgStore) {
		super(svgStore);
		setDefaults();
	}
	
	public List<? extends SVGElement> getOrCreateElementList() {
		return getOrCreateImageList();
	}
	
	private void setDefaults() {
		imageBoxColor = "pink";
	}

	public GraphicsElement analyzeImages(List<SVGImage> imageList) {
		this.imageList = imageList;
		SVGG g = new SVGG();
		g.setClassName("images");
		if (imageList != null) {
//			annotateImagesAsGlyphsWithSignatures();
		}
		return g;
	}
	
	

	public List<SVGImage> getImageList() {
		return imageList;
	}

	public SVGG debug(String outFilename) {
		SVGG g = new SVGG();
		debug(g, imageList, "blue", "pink", 0.3);
		
		writeDebug("images",outFilename, g);
		return g;
	}

	private void debug(SVGG g, List<SVGImage> imageList, String stroke, String fill, double opacity) {
		for (SVGImage img : imageList) {
			SVGImage image = (SVGImage) img.copy();
			image.setStroke(stroke);
			image.setStrokeWidth(0.2);
			image.setFill(fill);
			image.setOpacity(opacity);
			image.addTitle(image.getSignature());
			g.appendChild(image);
		}
	}

	/** the bounding box of the actual image components
	 * The extent of the context (e.g. svgCache) may be larger
	 * @return the bounding box of the contained image
	 */
	public Real2Range getBoundingBox() {
		return getOrCreateBoundingBox(imageList);
	}



	
}

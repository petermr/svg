package org.xmlcml.graphics.svg.extract;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGImage;
import org.xmlcml.graphics.svg.store.SVGStore;

/** annotates the SVGImages in a SVGElement.
 * 
 * @author pm286
 *
 */
public class ImageExtractor extends AbstractExtractor{

	private static final Logger LOG = Logger.getLogger(ImageExtractor.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	private List<SVGImage> imageList;
	private String imageBoxColor;

	public void extractImages(SVGElement svgElement) {
		this.imageList = SVGImage.extractSelfAndDescendantImages(svgElement);
	}

	public ImageExtractor(SVGStore svgStore) {
		super(svgStore);
		setDefaults();
	}
	
	private void setDefaults() {
		imageBoxColor = "pink";
	}

	public SVGElement analyzeImages(List<SVGImage> imageList) {
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

	public Real2Range getBoundingBox() {
		boundingBox = SVGElement.createBoundingBox(imageList);
		return boundingBox;
	}



	
}

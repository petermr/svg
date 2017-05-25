package org.xmlcml.graphics.svg.plot;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.text.SVGPhrase;
import org.xmlcml.graphics.svg.text.SVGWord;

/** a box to hold text on an Annotated axis.
 * 
 * at least 2 use cases
 *  * axial title
 *  * axial scale values
 *  
 * @author pm286
 *
 */
public class AxisTextBox extends AxialBox {
	private static final Logger LOG = Logger.getLogger(AxisTextBox.class);

	static {
		LOG.setLevel(Level.DEBUG);
	}

	List<SVGText> textList;
	private List<SVGText> horizontalTexts;
	private List<SVGText> rot90Texts;

	private SVGPhrase scalesPhrase;
	private RealArray tickNumberValues; // the actual numbers in the scale
	private RealArray tickNumberScreenCoords; // the best estimate of the numbers positions
	
//	private AxialBox axialBox;
	private SVGPhrase horizontalPhrase;
	private SVGPhrase rot90Phrase;

	protected AxisTextBox() {
		super();
	}
	
	protected AxisTextBox(AnnotatedAxis axis) {
		super(axis);
	}
	
	void setTexts(List<SVGText> horTexts, List<SVGText> rot90Txts) {
		this.horizontalTexts = extractIntersectingTexts(new ArrayList<SVGText>(horTexts));
		LOG.debug("hor texts: "+horizontalTexts.size() /*+"; " + horizontalTexts*/);
		this.rot90Texts = extractIntersectingTexts(new ArrayList<SVGText>(rot90Txts));
		LOG.debug("ver texts: "+rot90Texts.size());
		extractText();
	}



	void extractText() {
		// not a good idea as it slices through words
//		horizontalTexts = SVGText.removeStringsCompletelyOutsideRange(horizontalTexts, axis.getRange());
		horizontalPhrase = SVGPhrase.createPhraseFromCharacters(horizontalTexts);
		if (horizontalPhrase != null) {
			horizontalPhrase = horizontalPhrase.removeWordsCompletelyOutsideRange(axis.getRange());
		}
		LOG.debug("HOR phrase: "+horizontalPhrase+"; "+horizontalTexts.size());
		rot90Phrase = SVGPhrase.createPhraseFromCharacters(rot90Texts);
		LOG.debug("VERT phrase: "+rot90Phrase+"; "+rot90Texts.size());
	}

	private void extractHorizontalAxisScalesAndCoords() {
//		scalesPhrase = SVGPhrase.createPhraseFromCharacters(horizontalTexts);
		scalesPhrase = horizontalPhrase;
		if (scalesPhrase != null) {
			LOG.debug("HOR scalesPhrase: "+scalesPhrase);
			bbox = scalesPhrase.getBoundingBox();
			setTickNumberUserCoords(scalesPhrase.getNumericValues());
			List<SVGWord> wordList = scalesPhrase.getOrCreateWordList();
			tickNumberScreenCoords = new RealArray();
			for (SVGWord word : wordList) {
				tickNumberScreenCoords.addElement(word.getXY().getX());
			}
			LOG.debug("xCoords: "+tickNumberScreenCoords);
			LOG.debug("x diff: "+tickNumberScreenCoords.calculateDifferences().format(decimalPlaces()));
		}
	}

	public RealArray getTickNumberUserCoords() {
		return tickNumberValues;
	}

	public void setTickNumberUserCoords(RealArray tickNumberUserCoords) {
		this.tickNumberValues = tickNumberUserCoords;
	}

	public RealArray getTickNumberScreenCoords() {
		return tickNumberScreenCoords;
	}

	public SVGPhrase getScalesPhrase() {
		return scalesPhrase;
	}

	public void setTickNumberValues(RealArray tickNumberValues) {
		this.tickNumberValues = tickNumberValues;
	}

	public void setTickNumberScreenCoords(RealArray tickNumberScreenCoords) {
		this.tickNumberScreenCoords = tickNumberScreenCoords;
	}

	public RealArray getTickNumberValues() {
		return tickNumberValues;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("tickNumberUserCoords: "+tickNumberValues+"\n");
		sb.append("tickNumberScreenCoords: "+tickNumberScreenCoords+"\n");
		return sb.toString();
	}
	
	private void createVerticalNumberUserAndScreenCoords(List<SVGWord> wordList) {
		double[] values = new double[wordList.size()];
		tickNumberScreenCoords = new RealArray();
		LOG.debug("wordList "+wordList);
		bbox = new Real2Range();
		for (int i = 0; i < wordList.size(); i++) {
			SVGWord word0 = wordList.get(i);
			bbox = bbox.plus(word0.getBoundingBox());
			tickNumberScreenCoords.addElement(word0.getXY().getY());
			String ss = word0.getStringValue();
			LOG.debug("ss "+ss);
			values[i] = (ss == null) ? Double.NaN : new Double(ss);
		}
		tickNumberValues = new RealArray(values);
	}

	SVGPhrase extractScaleValueList() {
		this.scalesPhrase = null;
		if (axis.isHorizontal()) {
			extractHorizontalAxisScalesAndCoords();
		} else {
			processVerticalAxis();
		}
		return this.scalesPhrase;
	}

	private void processVerticalAxis() {
		if (rot90Texts != null && rot90Texts.size() > 0) {
			processVerticalAxisRotatedChars();
		} else {
			processWordLadderScales();
		}
	}

	private void processWordLadderScales() {
		if (horizontalTexts == null || horizontalTexts.size() == 0) return;
		LOG.debug("VERTICAL AXIS; Hor (ladder) texts "+horizontalTexts.size());
		List<SVGWord> wordList = createWordListFromHorizontalTextsWithJoinsIfNecessary();
		wordList = removeWordsNotInVerticalRange(axis.getRange(), wordList);
		createVerticalNumberUserAndScreenCoords(wordList);
		LOG.debug("vertical tickNumberUserCoords:"+tickNumberValues);
	}

	private void processVerticalAxisRotatedChars() {
		LOG.debug("VERTICAL rotated characters, Not Yet Written: "+rot90Texts.size());
	}

	private List<SVGWord> removeWordsNotInVerticalRange(RealRange range, List<SVGWord> wordList) {
		List<SVGWord> wordList1 = new ArrayList<SVGWord>();
		for (SVGWord word : wordList) {
			Real2Range bbox = word.getBoundingBox();
			if (range.intersectsWith(bbox.getYRange())) {
				wordList1.add(word);
			}
		}
		LOG.debug("Made wordList "+wordList1);
		return wordList1;
	}

	private List<SVGWord> createWordListFromHorizontalTextsWithJoinsIfNecessary() {
		List<SVGWord> wordList = new ArrayList<SVGWord>();
		if (horizontalTexts.size() > 0) {
			SVGWord word = new SVGWord(horizontalTexts.get(0)); // ?? why
			for (int i = 1; i < horizontalTexts.size(); i++) {
				SVGText text = horizontalTexts.get(i);
				if (false) {
				} else if (word.canAppend(text)) {
					word.append(text);
				} else {
					word = new SVGWord(horizontalTexts.get(i));
					wordList.add(word);
				}
			}
		}
		return wordList;
	}
	
//	private List<SVGText> getOrCreateTextList() {
//		if (this.textList == null) {
//			textList = new ArrayList<SVGText>();
//			List<SVGText> textListAll = axis.getPlotBox().getTextList();
//			for (SVGText text : textListAll) {
//				if (text.isIncludedBy(captureBox)) {
//					textList.add(text);
//				}
//			}
//		}
//		return textList;
//	}

	/** get all lines intersecting with this.boundingBox.
	 * 
	 * @param lines
	 * @return
	 */
	private List<SVGText> extractIntersectingTexts(List<SVGText> texts) {
		List<SVGText> textList = new ArrayList<SVGText>();
		LOG.debug("******* bbox "+captureBox+"; "+texts.size());
		for (SVGText text : texts) {
			Real2Range textBBox = text.getBoundingBox();
			Real2Range inter = textBBox.intersectionWith(this.captureBox);
			LOG.trace(textBBox+"; inter: "+inter);
			if (inter!= null && inter.isValid()) {
				LOG.trace("inter1: "+inter);
				text.format(decimalPlaces());
				textList.add(text);
			}
		}
		LOG.trace("CAPTURED: "+textList.size());
		return textList;
	}
	
	public SVGElement createSVGElement() {
		SVGG g = (SVGG) super.createSVGElement();
		g.setClassName("axisTextBox");
		for (SVGElement element : containedGraphicalElements) {
			g.appendChild(element.copy());
		}
		return g;
	}



}

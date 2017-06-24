package org.xmlcml.graphics.svg.plot;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.Util;
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
public class AxisScaleBox extends AxialBox {
	private static final Logger LOG = Logger.getLogger(AxisScaleBox.class);

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

	protected AxisScaleBox() {
		super();
	}
	
	protected AxisScaleBox(AnnotatedAxis axis) {
		super(axis);
	}
	
	void setTexts(List<SVGText> horTexts, List<SVGText> rot90Txts) {
		this.horizontalTexts = extractIntersectingTexts(new ArrayList<SVGText>(horTexts));
		LOG.debug(axis.getLineDirection()+" HOR texts: "+horizontalTexts.size() /*+"; " + horizontalTexts*/);
		this.rot90Texts = extractIntersectingTexts(new ArrayList<SVGText>(rot90Txts));
		LOG.debug(axis.getLineDirection()+" ROT90 texts: "+rot90Texts.size());
		extractText();
	}



	void extractText() {
		LOG.debug("axial scales");
		if (axis.isHorizontal()) {
			// not a good idea as it slices through words
			horizontalPhrase = SVGPhrase.createPhraseFromCharacters(horizontalTexts);
			LOG.debug("HOR phrase: "+horizontalPhrase+"; "+horizontalTexts.size());
			if (horizontalPhrase != null) {
				horizontalPhrase = horizontalPhrase.removeWordsCompletelyOutsideRange(axis.getRange());
			}
			if(horizontalPhrase != null) {
				horizontalPhrase = horizontalPhrase.getWordsWithLowestYValue(0);
				horizontalPhrase = horizontalPhrase.normalizeMinus();
				LOG.debug("HOR phrase Y: "+horizontalPhrase+"; "+horizontalPhrase.getOrCreateWordList().size());
			}
			
		} else {
			horizontalPhrase = SVGPhrase.createPhraseFromCharacters(horizontalTexts);
			LOG.debug("Word Ladder?: "+horizontalPhrase+"; "+horizontalTexts.size());
			if (horizontalPhrase != null) {
				horizontalPhrase = removeVerticalWordsCompletelyOutsideRange(horizontalPhrase, axis.getRange());
				horizontalPhrase = horizontalPhrase.normalizeMinus();
				LOG.debug("Word Ladder??: "+horizontalPhrase+"; "+horizontalPhrase.getOrCreateWordList().size());
			}
			rot90Phrase = SVGPhrase.createPhraseFromCharacters(rot90Texts, true);
			LOG.debug("ROT90 phrase: "+rot90Phrase+"; "+rot90Texts.size());
			if (rot90Phrase != null) {
				SVGPhrase rot90HighestPhrase = rot90Phrase.getWordsWithHighestXValue(0);
				LOG.info("**************"+rot90HighestPhrase+"***************");
			} else {
				LOG.info("NO rot90 text");
			}
			LOG.trace("finished vert");
		}
	}

	
	private SVGPhrase removeVerticalWordsCompletelyOutsideRange(SVGPhrase horizontalPhrase, RealRange range) {
		List<SVGWord> wordList = horizontalPhrase.getOrCreateWordList();
		SVGPhrase filteredPhrase = new SVGPhrase();
		for (SVGWord word : wordList) {
			Real2Range wordBox = word.getBoundingBox();
			RealRange wordYRange = wordBox.getYRange();
			if (wordYRange.intersectsWith(range)) {
				filteredPhrase.addTrailingWord(word);
			}
		}
		return filteredPhrase;
	}

	private void extractHorizontalAxisScalesAndCoords() {
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

	public RealArray getTickValueScreenCoords() {
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
			ss = SVGWord.replaceNonStandardChars(ss, Util.S_MINUS, SVGWord.NON_STANDARD_MINUS);
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
			processVerticalAxisScaleValues();
		}
		return this.scalesPhrase;
	}

	private void processVerticalAxisScaleValues() {
		String rot90Value = null;
		if (rot90Texts != null && rot90Texts.size() > 0) {
			rot90Value = String.valueOf(rot90Texts.get(0).getText());
			LOG.debug("first rot90 scale value: "+rot90Value);
			if (!"null".equals(String.valueOf(rot90Value)) && !rot90Value.trim().equals("")) {
				LOG.debug("processing rot90 text");
				processVerticalAxisRot90Chars();
				processVerticalAxisWordLadderScales();
			}
		} else {
			processVerticalAxisWordLadderScales();
		}
	}

	private void processVerticalAxisWordLadderScales() {
		// if no rot90 we use wordladder
		if (rot90Phrase == null || rot90Phrase.getOrCreateWordList().size() == 0) {
			LOG.debug("VERTICAL AXIS; Hor (ladder) texts "+horizontalTexts.size());
			List<SVGWord> wordList = createWordListFromHorizontalTextsWithJoinsIfNecessary();
			LOG.debug("VERT words0: "+wordList);
			wordList = removeWordsNotInVerticalRange(axis.getRange(), wordList);
			LOG.debug("VERT words1: "+wordList);
			createVerticalNumberUserAndScreenCoords(wordList);
			LOG.debug("vertical tickNumberUserCoords from HOR wordladder:"+tickNumberValues);
		}
	}

	private void processVerticalAxisRot90Chars() {
		List<SVGWord> wordList = rot90Phrase.getOrCreateWordList();
		createVerticalNumberUserAndScreenCoords(wordList);
		LOG.debug("vertical tickNumberUserCoords from rot90:"+tickNumberValues);
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
			LOG.debug("HORTEXTS0 " + horizontalTexts);
			SVGWord word = new SVGWord(horizontalTexts.get(0)); // ?? why
			wordList.add(word);
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
			LOG.debug("HORTEXTS1 " + wordList);
		}
		return wordList;
	}
	
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

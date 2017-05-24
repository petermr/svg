package org.xmlcml.graphics.svg.plot;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
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
	private List<SVGText> verticalTexts;

	private SVGPhrase scalesPhrase;
	private RealArray tickNumberValues; // the actual numbers in the scale
	private RealArray tickNumberScreenCoords; // the best estimate of the numbers positions
	
	private AxialBox axialBox;
	private SVGPhrase horizontalPhrase;
	private SVGPhrase verticalPhrase;

	protected AxisTextBox() {
		super();
	}
	
	protected AxisTextBox(AnnotatedAxis axis) {
		this();
		if (axis == null) {
			throw new RuntimeException("null axis");
		}
		this.axis = axis;
	}
	
	void setTexts(List<SVGText> horizontalTexts, List<SVGText> verticalTexts) {
		this.horizontalTexts = extractIntersectingTexts(new ArrayList<SVGText>(horizontalTexts));
		LOG.debug("hor texts: "+horizontalTexts.size()+"; " + horizontalTexts);
		this.verticalTexts = extractIntersectingTexts(new ArrayList<SVGText>(verticalTexts));
		LOG.debug("ver texts: "+verticalTexts.size());
	}


	void extractText() {
		horizontalPhrase = SVGPhrase.createPhraseFromCharacters(horizontalTexts);
		verticalPhrase = SVGPhrase.createPhraseFromCharacters(verticalTexts);
	}

	private void extractHorizontalScalesAndCoords() {
		scalesPhrase = SVGPhrase.createPhraseFromCharacters(textList);
		if (scalesPhrase != null) {
			LOG.debug("HOR scalesPhrase: "+scalesPhrase);
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
		for (int i = 0; i < wordList.size(); i++) {
			SVGWord word0 = wordList.get(i);
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
			extractHorizontalScalesAndCoords();
		} else {
			processVerticalAxis();
		}
		return this.scalesPhrase;
	}

	private void processVerticalAxis() {
		if (textList == null || textList.size() == 0) return;
		List<SVGWord> wordList = createVerticalWordListLadder();
		createVerticalNumberUserAndScreenCoords(wordList);
		LOG.debug("vertical tickNumberUserCoords:"+tickNumberValues);
	}

	private List<SVGWord> createVerticalWordListLadder() {
		List<SVGWord> wordList = new ArrayList<SVGWord>();
		SVGWord word = new SVGWord(textList.get(0)); // ?? why
		wordList.add(word);
		for (int i = 1; i < textList.size(); i++) {
			SVGText text = textList.get(i);
			if (word.canAppend(text)) {
				word.append(text);
			} else {
				word = new SVGWord(textList.get(i));
				wordList.add(word);
			}
		}
		return wordList;
	}
	
	private List<SVGText> getOrCreateTextList() {
		if (this.textList == null) {
			textList = new ArrayList<SVGText>();
			List<SVGText> textListAll = axis.getPlotBox().getTextList();
			for (SVGText text : textListAll) {
				if (text.isIncludedBy(captureBox)) {
					textList.add(text);
				}
			}
		}
		return textList;
	}

	/** get all lines intersecting with this.boundingBox.
	 * 
	 * @param lines
	 * @return
	 */
	private List<SVGText> extractIntersectingTexts(List<SVGText> texts) {
		List<SVGText> textList = new ArrayList<SVGText>();
		LOG.debug("******* bbox "+captureBox);
		for (SVGText text : textList) {
			Real2Range textBBox = text.getBoundingBox();
			Real2Range inter = textBBox.intersectionWith(this.captureBox);
			LOG.trace(textBBox+"; inter: "+inter);
			if (inter!= null && inter.isValid()) {
				LOG.trace("inter1: "+inter);
				text.format(decimalPlaces());
				textList.add(text);
			}
		}
		return textList;
	}
	


}

package org.xmlcml.graphics.svg.text.phrase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.IntArray;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.euclid.IntRangeArray;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.Util;
import org.xmlcml.graphics.html.HtmlDiv;
import org.xmlcml.graphics.html.HtmlElement;
import org.xmlcml.graphics.html.HtmlLi;
import org.xmlcml.graphics.html.HtmlP;
import org.xmlcml.graphics.html.HtmlUl;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.text.horizontal.LineChunk;
import org.xmlcml.xml.XMLUtil;

import nu.xom.Element;

/** holds a list of PhraseLists 
 * renamed from PhraseListList
 * 
 * This is often a paragraph but can be any set of phrase lists.
 * 
 * @author pm286
 */
public class TextChunk extends SVGG implements Iterable<PhraseChunk> {
	private static final double PARA_SPACING_FACTOR = 1.2;
	public static final Logger LOG = Logger.getLogger(TextChunk.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public static final Double SUPERSCRIPT_Y_RATIO = 0.5;
	public static final Double SUBSCRIPT_Y_RATIO = 0.75;
//	private static final Double SUPERSCRIPT_FONT_RATIO = 1.05;

	public final static String TAG = "textChunk";
	private static final int EPS = 5;

	private List<PhraseChunk> childPhraseListList;
	private List<PhraseNew> phrases;
	private RealArray ySpacings; 
	private double paraSpacingTrigger;

	public TextChunk() {
		super();
		this.setClassName(TAG);
	}
	
	public TextChunk(TextChunk phraseListList) {
		this();
		getOrCreateChildPhraseListList();
		childPhraseListList.addAll(phraseListList.getOrCreateChildPhraseListList());
	}

	public TextChunk(List<PhraseChunk> phraseLists) {
		this();
		getOrCreateChildPhraseListList();
		for (PhraseChunk phraseList : phraseLists) {
			this.add(phraseList);
		}
	}

	public Iterator<PhraseChunk> iterator() {
		getOrCreateChildPhraseListList();
		return childPhraseListList.iterator();
	}

	public List<PhraseChunk> getOrCreateChildPhraseListList() {
		if (childPhraseListList == null) {
			List<Element> phraseChildren = XMLUtil.getQueryElements(this, "*[local-name()='"+SVGG.TAG+"' and @class='"+PhraseChunk.TAG+"']");
			childPhraseListList = new ArrayList<PhraseChunk>();
			for (Element child : phraseChildren) {
				PhraseChunk phraseList = (PhraseChunk)child;
				childPhraseListList.add(phraseList);
			}
		}
		return childPhraseListList;
	}

	public String getStringValue() {
		getOrCreateChildPhraseListList();
		StringBuilder sb = new StringBuilder();
		for (PhraseChunk phraseList : childPhraseListList) {
			sb.append(""+phraseList.getStringValue()+"//");
		}
		this.setStringValueAttribute(sb.toString());
		return sb.toString();
	}

	public void add(PhraseChunk phraseList) {
		this.appendChild(new PhraseChunk(phraseList));
		childPhraseListList = null;
		getOrCreateChildPhraseListList();
	}

	public PhraseChunk get(int i) {
		getOrCreateChildPhraseListList();
		return (i < 0 || i >= childPhraseListList.size()) ? null : childPhraseListList.get(i);
	}
	
	protected List<? extends LineChunk> getChildChunks() {
		getOrCreateChildPhraseListList();
		return childPhraseListList;
	}


	public List<IntArray> getLeftMarginsList() {
		getOrCreateChildPhraseListList();
		List<IntArray> leftMarginsList = new ArrayList<IntArray>();
		for (PhraseChunk phraseList : childPhraseListList) {
			IntArray leftMargins = phraseList.getLeftMargins();
			leftMarginsList.add(leftMargins);
		}
		return leftMarginsList;
	}
	
	/** assumes the largest index in phraseList is main body of table.
	 * 
	 * @return
	 */
	public int getMaxColumns() {
		getOrCreateChildPhraseListList();
		int maxColumns = 0;
		for (PhraseChunk phraseList : childPhraseListList) {
			maxColumns = Math.max(maxColumns, phraseList.size());
		}
		return maxColumns;
	}

	public IntRangeArray getBestColumnRanges() {
		getOrCreateChildPhraseListList();
		int maxColumns = getMaxColumns();
		IntRangeArray columnRanges = new IntRangeArray();
		for (int i = 0; i < maxColumns; i++) {
			columnRanges.set(i, (IntRange)null);
		}
		for (PhraseChunk phraseList : childPhraseListList) {
			if (phraseList.size() == maxColumns) {
				for (int i = 0; i < phraseList.size(); i++) {
					PhraseNew phrase = phraseList.get(i);
					IntRange range = phrase.getIntRange();
					IntRange oldRange = columnRanges.get(i);
					range = (oldRange == null) ? range : range.plus(oldRange);
					columnRanges.set(i, range);
				}
			}
		}
		return columnRanges;
	}
	
	public IntRangeArray getBestWhitespaceRanges() {
		getOrCreateChildPhraseListList();
		int maxColumns = getMaxColumns();
		IntRangeArray bestColumnRanges = getBestColumnRanges();
		IntRangeArray bestWhitespaces = new IntRangeArray();
		if (maxColumns > 0) {
			bestWhitespaces.add(new IntRange(bestColumnRanges.get(0).getMin() - EPS, bestColumnRanges.get(0).getMax() - EPS));
			for (int i = 1; i < maxColumns; i++) {
				IntRange whitespace = new IntRange(bestColumnRanges.get(i - 1).getMax(), bestColumnRanges.get(i).getMax());
				bestWhitespaces.add(whitespace);
			}
		}
		return bestWhitespaces;
	}
	
	/** find rightmostWhitespace range which includes start of phrase.
	 * 
	 */
	public int getRightmostEnclosingWhitespace(List<IntRange> bestWhitespaces, PhraseNew phrase) {
		for (int i = bestWhitespaces.size() - 1; i >= 0; i--) {
			IntRange range = bestWhitespaces.get(i);
			int phraseX = (int)(double) phrase.getStartX();
			if (range.contains(phraseX)) {
				return i;
			}
		}
		return -1;
	}

	public int size() {
		getOrCreateChildPhraseListList();
		return childPhraseListList.size();
	}

	public Real2Range getBoundingBox() {
		getOrCreateChildPhraseListList();
		Real2Range bbox = null;
		if (childPhraseListList.size() > 0) {
			bbox = childPhraseListList.get(0).getBoundingBox();
			for (int i = 1; i < childPhraseListList.size(); i++) {
				bbox = bbox.plus(childPhraseListList.get(i).getBoundingBox());
			}
		}
		return bbox;
	}

	public void rotateAll(Real2 centreOfRotation, Angle angle) {
		getOrCreateChildPhraseListList();
		for (PhraseChunk phraseList : childPhraseListList) {
			phraseList.rotateAll(centreOfRotation, angle);
			LOG.trace("PL: "+phraseList.toXML());
		}
		updatePhraseListList();
	}
	
	public void updatePhraseListList() {
		for (int i = 0; i < childPhraseListList.size(); i++) {
			this.replaceChild(this.getChildElements().get(i), childPhraseListList.get(i));
		}
	}

	public Real2 getXY() {
		return this.getBoundingBox().getLLURCorners()[0];
	}

	public boolean remove(PhraseChunk phraseList) {
		boolean remove = false;
		if (childPhraseListList != null && phraseList != null) {
			remove = childPhraseListList.remove(phraseList);
		}
		return remove;
	}
	
	public boolean replace(PhraseChunk oldPhraseList, PhraseChunk newPhraseList) {
		boolean replace = false;
		if (childPhraseListList != null) {
			int idx = this.childPhraseListList.indexOf(oldPhraseList);
			if (idx != -1) {
				replace = this.childPhraseListList.set(idx, newPhraseList) != null;
			}
		}
		return replace;
	}



	/**
	 * analyses neighbouring PhraseLists to see if the font sizes and Y-coordinates
	 * are consistent with sub or superscripts. If so, merges the lines 
	 * phraseList.mergeByXCoord(lastPhraseList)
	 * and removes the sub/super line
	 * lines. The merged phraseList contains all the characters and coordinates in 
	 * phraseLists with sub/superscript boolean flags.
	 * 
	 * getStringValue() represents the sub and superscripts by TeX notation (_{foo} and ^{bar})
	 * but the actual content retains coordinates and can be output to HTML
	 * 
	 * The ratios for the y-values and font sizes are hardcoded but will be settable later.
	 */
	public void applySubAndSuperscripts() {
		Double lastY = null;
		Double lastFontSize = null;
		Double deltaY = null;
		PhraseChunk lastPhraseList = null;
		List<PhraseChunk> removeList = new ArrayList<PhraseChunk>();
		for (PhraseChunk phraseList : this) {
			Double fontSize = Util.format(phraseList.getFontSize(), 1);
			Double y = Util.format(phraseList.getXY().getY(), 1);
			if (lastY != null) {
				double fontRatio = fontSize / lastFontSize;
				deltaY = y - lastY;
				if (deltaY > 0 && deltaY < fontSize * SUPERSCRIPT_Y_RATIO && fontRatio >= 1.0) {
					LOG.trace("SUPER "+lastPhraseList.getStringValue()+" => "+phraseList.getStringValue());
					lastPhraseList.setSuperscript(true);
					phraseList.mergeByXCoord(lastPhraseList);
					removeList.add(lastPhraseList);
				} else if (deltaY > 0 && deltaY < lastFontSize * SUBSCRIPT_Y_RATIO && fontRatio <= 1.0) {
					LOG.trace("SUB "+phraseList.getStringValue()+" => "+lastPhraseList.getStringValue());
					phraseList.setSubscript(true);
					lastPhraseList.mergeByXCoord(phraseList);
					removeList.add(phraseList);
				}
			}
			lastPhraseList = phraseList;
			lastFontSize = fontSize;
			lastY = y;
		}
		for (PhraseChunk phraseList : removeList) {
			remove(phraseList);
		}
	}

	public List<PhraseNew> getOrCreatePhrases() {
		if (phrases == null) {
			phrases = new ArrayList<PhraseNew>();
			for (PhraseChunk phraseList : this) {
				for (PhraseNew phrase : phraseList) {
					phrases.add(phrase);
				}
			}
		}
		return phrases;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (PhraseChunk phraseList : this) {
			sb.append(phraseList.toString()+"\n");
		}
		return sb.toString();
	}
	
	public HtmlElement toHtml() {
		HtmlElement div = new HtmlDiv();
		createParaSpacingTrigger();
		PhraseChunk lastPhraseList = null;
		HtmlP p = new HtmlP();
		div.appendChild(p);
		for (int i = 0; i < this.size(); i++) {
			PhraseChunk phraseList = this.get(i);
			if (lastPhraseList != null) {
				boolean newPara = triggerNewPara(lastPhraseList, phraseList);
				if (newPara) {
					p = new HtmlP();
					div.appendChild(p);
				} else {
					p.appendChild(" ");
				}
			}
			XMLUtil.transferChildren((Element)phraseList.toHtml().copy(), p);
			lastPhraseList = phraseList;
		}
		return div;
	}

	private boolean triggerNewPara(PhraseChunk lastPhraseList, PhraseChunk phraseList) {
		boolean newPara = false;
		String lastString = lastPhraseList.getStringValue();
		if (lastString.length() > 0) {
			char lastEnd = lastString.charAt(lastString.length() - 1);
			double deltaY = phraseList.getY() - lastPhraseList.getY();
			double deltaX = phraseList.getX() - lastPhraseList.getX();
			// just do paras on separation at present
			if (deltaY > paraSpacingTrigger) {
				newPara = true;
			}
		}
		return newPara;
	}

	private void createParaSpacingTrigger() {
		paraSpacingTrigger = Double.MAX_VALUE;
		RealArray spacings = this.getOrCreateYSpacings();
		if (spacings.size() > 0) {
			double maxYSpacing = spacings.getMax();
			double minYSpacing = spacings.getMin();
			if (maxYSpacing / minYSpacing > PARA_SPACING_FACTOR) {
				paraSpacingTrigger = (maxYSpacing + minYSpacing) / 2.;
			} else {
				paraSpacingTrigger = minYSpacing * PARA_SPACING_FACTOR;
			}
		}
	}

	private RealArray getOrCreateYSpacings() {
		if (ySpacings == null) {
			ySpacings = new RealArray();
			for (int i = 1; i < this.size(); i++) {
				double y = Real.normalize(this.get(i).getY() - this.get(i - 1).getY(), 2);
				ySpacings.addElement(y);
			}
		}
		LOG.trace(ySpacings);
		return ySpacings;
	}

	public HtmlElement toHtmlUL() {
		HtmlUl ul = new HtmlUl();
		for (PhraseChunk phraseList : this) {
			HtmlLi li = new HtmlLi();
			li.appendChild(phraseList.toHtml());
			ul.appendChild(li);
		}
		return ul;
	}

	public HtmlUl getPhraseListUl() {
		HtmlUl ul = new HtmlUl();
		for (PhraseChunk phraseList : this) {
			HtmlLi li = new HtmlLi();
			ul.appendChild(li);
			li.appendChild(phraseList.toHtml().copy());
		}
		return ul;
	}

	public String getCSSStyle() {
		String pllStyle = null;
		for (PhraseChunk phraseList : this) {
			String plStyle = phraseList.getCSSStyle();
			if (pllStyle == null) {
				plStyle = pllStyle;
			} else if (pllStyle.equals(plStyle)) {
				// OK
			} else {
				pllStyle = MIXED_STYLE;
			}
		}
		return pllStyle;
	}


}

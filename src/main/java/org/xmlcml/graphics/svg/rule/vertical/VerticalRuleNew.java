package org.xmlcml.graphics.svg.rule.vertical;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.rule.RuleNew;

public class VerticalRuleNew extends RuleNew {

	private static final Logger LOG = Logger.getLogger(VerticalRuleNew.class);

	static {
		LOG.setLevel(Level.DEBUG);
	}

	/** allowed misalignment for "same X"*/
	public static final double X_TOLERANCE = 2.0;
	
	public VerticalRuleNew(SVGLine line) {
		super(line);
	}
	
	/** requires sorted lines.
	 * 
	 * @param lines
	 * @return
	 */
	public static List<VerticalRuleNew> createSortedRulersFromSVGList(List<SVGLine> lines) {
		List<VerticalRuleNew> rulerList = new ArrayList<VerticalRuleNew>();
		for (int i = 0; i < lines.size(); i++) {
			SVGLine line = lines.get(i);
			if (line.isVertical(epsilon)) {
				VerticalRuleNew ruler = new VerticalRuleNew(line);
				rulerList.add(ruler);
			}
		}
		Collections.sort(rulerList, new VerticalRulerComparator());
		return rulerList;
	}
	
	public IntRange getIntRange() {
		return new IntRange(getBoundingBox().getYRange());
	}
	
}
class VerticalRulerComparator implements Comparator<VerticalRuleNew> {

	public int compare(VerticalRuleNew vr1, VerticalRuleNew vr2) {
		if (vr1 == null || vr2 == null || vr1.getIntRange() == null || vr2.getIntRange() == null) {
			return 0;
		}
		if (vr1.getX() < vr2.getX()) return -1;
		if (vr1.getX() > vr2.getX()) return 1;
		return vr1.getIntRange().getMin() - vr2.getIntRange().getMin();
	}

}

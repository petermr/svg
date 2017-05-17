package org.xmlcml.graphics.svg.linestuff;

import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.IntRange;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGLineList;

/** lines parallel to axes
 * 
 * @author pm286
 *
 */
public class AxialLineList extends SVGLineList {
	private static final Logger LOG = Logger.getLogger(AxialLineList.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private LineDirection direction;
	private double eps = SVGLine.EPS;

	public AxialLineList(LineDirection direction) {
		this.direction = direction;
	}
	
	public void sort() {
		Comparator<SVGLine> comparator = 
			LineDirection.HORIZONTAL.equals(direction) ? new HorizontalLineComparator() : new VerticalLineComparator();
		Collections.sort(lineList, comparator);
	}
	
	public boolean add(SVGLine line) {
		ensureLines();
		if (LineDirection.HORIZONTAL.equals(direction) && line.isHorizontal(eps)) {
			return lineList.add(line);
		} else if (LineDirection.VERTICAL.equals(direction) && line.isVertical(eps)) {
			return lineList.add(line);
		} else {
			return false;
		}
	}
	
	public double getEps() {
		return eps;
	}

	public void setEps(double eps) {
		this.eps = eps;
	}


	
}
class HorizontalLineComparator implements Comparator<SVGLine>{

	public int compare(SVGLine l1, SVGLine l2) {
		if (l1 == null || l2 == null) return -1;
		return (int)(l1.getXY(0).getY() - l2.getXY(0).getY());
	}
	
}
class VerticalLineComparator implements Comparator<SVGLine>{

	public int compare(SVGLine l1, SVGLine l2) {
		if (l1 == null || l2 == null) return -1;
		return (int)(l1.getXY(0).getX() - l2.getXY(0).getX());
	}
	
}


package org.xmlcml.graphics.svg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SVGLineList extends SVGG implements Iterable<SVGLine> {
	
	private static Logger LOG = Logger.getLogger(SVGLineList.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	protected ArrayList<SVGLine> lineList;

	public SVGLineList() {
		super();
	}
	
	public SVGLineList(List<SVGLine> lines) {
		this.lineList = new ArrayList<SVGLine>(lines);
	}

	/** adds all SVGLines in collection to new SVGLineList
	 * 
	 * @param elements List which potentially contains SVGLine elements
	 * @return empty list if no lines
	 */
	public static SVGLineList createLineList(List<SVGElement> elements) {
		SVGLineList lineList = new SVGLineList();
		for (GraphicsElement element : elements) {
			if (element instanceof SVGLine) {
				lineList.add((SVGLine) element);
			}
		}
		return lineList;
	}

	public List<SVGLine> getLineList() {
		ensureLines();
		return lineList;
	}

	public Iterator<SVGLine> iterator() {
		ensureLines();
		return lineList.iterator();
	}

	protected void ensureLines() {
		if (lineList == null) {
			lineList = new ArrayList<SVGLine>();
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		ensureLines();
		sb.append(lineList.toString());
		return sb.toString();
	}

	public SVGLine get(int i) {
		ensureLines();
		return (i < 0 || i >= lineList.size()) ? null : lineList.get(i);
	}

	public int size() {
		ensureLines();
		return lineList.size();
	}

	public SVGShape remove(int i) {
		ensureLines();
		if (get(i) != null) {
			return lineList.remove(i);
		}
		return null;
	}
	
	public boolean add(SVGLine line) {
		ensureLines();
		return lineList.add(line);
	}

}

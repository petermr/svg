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

package org.xmlcml.graphics.svg;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Util;

import nu.xom.Element;
import nu.xom.Node;

/** draws a straight line.
 * 
 * @author pm286
 *
 */
public class SVGRect extends SVGShape {

	private static final Logger LOG = Logger.getLogger(SVGRect.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public final static String ALL_RECT_XPATH = ".//svg:rect";

	private static final String HEIGHT = "height";
	private static final String WIDTH = "width";
	private static final String Y = "y";
	private static final String X = "x";
	final public static String TAG ="rect";

	/** constructor
	 */
	public SVGRect() {
		super(TAG);
		init();
	}
	
	/** constructor
	 */
	public SVGRect(SVGElement element) {
        super(element);
	}
	
	/** constructor
	 */
	public SVGRect(Element element) {
        super((SVGElement) element);
	}
	
	protected void init() {
		super.setDefaultStyle();
		setDefaultStyle(this);
	}
	public static void setDefaultStyle(SVGElement rect) {
		rect.setStroke("black");
		rect.setStrokeWidth(1.0);
		rect.setFill("none");
	}
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGRect(this);
    }

	/** constructor.
	 * 
	 * @param x1
	 * @param x2
	 */
	public SVGRect(double x, double y, double w, double h) {
		this();
		setX(x);
		setY(y);
		setWidth(w);
		setHeight(h);
	}

	/** create from bounding box
	 * 
	 * @param r2r
	 * @return null if r2r is null
	 */
	public static SVGRect createFromReal2Range(Real2Range r2r) {
		SVGRect rect = null;
		if (r2r != null) {
			Real2[] corners = r2r.getCorners();
			if (corners != null && corners.length == 2) {
				rect = new SVGRect(corners[0], corners[1]);
			}
		}
		return rect;
	}
	
	/** create from edges
	 * 
	 * @param xRange
	 * @param yRange
	 * @return null if r2r is null
	 */
	public static SVGRect createFromRealRanges(RealRange xRange, RealRange yRange) {
		SVGRect rect = null;
		if (xRange != null && yRange != null) {
			rect = new SVGRect(new Real2(xRange.getMin(), yRange.getMin()), new Real2(xRange.getMax(), yRange.getMax()));
		}
		return rect;
	}
	
	/** constructor.
	 * 
	 * @param x1 "lower left"
	 * @param x2 "upper right"
	 */
	public SVGRect(Real2 x1, Real2 x2) {
		this(x1.getX(), x1.getY(), x2.getX() - x1.getX(), x2.getY() - x1.getY());
	}
//  <g style="stroke-width:0.2;">
//  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="black" style="stroke-width:0.36;"/>
//  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="white" style="stroke-width:0.12;"/>
//</g>
	
	@Deprecated //"use createFromReal2Range which deals with nulls"
	public SVGRect(Real2Range bbox) {
		this(bbox.getXMin(), bbox.getYMin(), bbox.getXRange().getRange(), bbox.getYRange().getRange());
	}
	
	protected void drawElement(Graphics2D g2d) {
		saveGraphicsSettingsAndApplyTransform(g2d);
		ensureCumulativeTransform();
		double x1 = this.getDouble(X);
		double y1 = this.getDouble(Y);
		Real2 xy1 = new Real2(x1, y1);
		xy1 = transform(xy1, cumulativeTransform);
		double w = this.getDouble(WIDTH);
		double h = this.getDouble(HEIGHT);
		Real2 xy2 = new Real2(x1+w, y1+h);
		xy2 = transform(xy2, cumulativeTransform);
		
		Rectangle2D rect = new Rectangle2D.Double(xy1.x, xy1.y, xy2.x-xy1.x, xy2.y-xy1.y);
		fill(g2d, rect);
		draw(g2d, rect);
		restoreGraphicsSettingsAndTransform(g2d);
	}


	// this only works for 0 += PI/2, +- PI
	public void applyTransform(Transform2 t2) {
		//assume scale and translation only
		Real2 xy = getXY();
		xy.transformBy(t2);
		this.setXY(xy);
		double h = getHeight();
		double w = getWidth();
		Angle a = t2.getAngleOfRotation();
		Real2 xxyy = new Real2(xy.getX()+getWidth(), xy.getY()+getHeight());
		xxyy.transformBy(t2);
		if (a.isEqualTo(new Angle(Math.PI / 2.), 0.00001) ||
				a.isEqualTo(new Angle(-Math.PI / 2.0), 0.00001)) {
			setHeight(w);
			setWidth(h);
		}
	}
	
    /** round to decimal places.
     * 
     * @param places
     * @return this
     */
    public void format(int places) {
    	Real2 xy = getXY();
    	xy.format(places);
    	setXY(xy);
    	setHeight(Util.format(getHeight(), places));
    	setWidth(Util.format(getWidth(), places));
    	forceGetBoundingBox();
    	boundingBox = boundingBox.format(places);
    }
	
	/** extent of rect
	 * 
	 * @return
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			forceGetBoundingBox();
		}
		return boundingBox;
	}

	private void forceGetBoundingBox() {
		boundingBox = new Real2Range();
		Real2 origin = getXY();
		boundingBox.add(origin);
		boundingBox.add(origin.plus(new Real2(getWidth(), getHeight())));
	}
	
	/** get tag.
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	public void setBounds(Real2Range r2r) {
		if (r2r != null) {
			RealRange xr = r2r.getXRange();
			RealRange yr = r2r.getYRange();
			this.setXY(new Real2(xr.getMin(), yr.getMin()));
			this.setWidth(xr.getRange());
			this.setHeight(yr.getRange());
		}
	}
	
	/** makes a new list composed of the rects in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGRect> extractRects(List<SVGElement> elements) {
		List<SVGRect> rectList = new ArrayList<SVGRect>();
		for (SVGElement element : elements) {
			if (element instanceof SVGRect) {
				rectList.add((SVGRect) element);
			}
		}
		return rectList;
	}
	
	@Override
	public String getGeometricHash() {
		return getAttributeValue(X)+" "+getAttributeValue(Y)+" "+getAttributeValue(WIDTH)+" "+getAttributeValue(HEIGHT);
	}

	public static List<SVGRect> extractSelfAndDescendantRects(SVGElement svgElem) {
		return SVGRect.extractRects(SVGUtil.getQuerySVGElements(svgElem, ALL_RECT_XPATH));
	}

	public boolean isEqual(SVGRect otherRect, double delta) {
		Real2[] corners = this.getBoundingBox().getCorners();
		Real2[] otherCorners = otherRect.getBoundingBox().getCorners();
		return corners[0].getDistance(otherCorners[0]) < delta && corners[1].getDistance(otherCorners[1]) < delta;
	}
	
	@Override
	public String toString() {
		if (boundingBox == null) {
			boundingBox = getBoundingBox();
		}
		return boundingBox.toString();
	}

	@Override
	protected boolean isGeometricallyEqualTo(SVGShape shape, double epsilon) {
		if (shape != null && shape instanceof SVGRect) {
			return this.isEqual((SVGRect) shape, epsilon);
		}
		return false;
	}

	public Real2 getXY() {
		return new Real2(getX(), getY());
	}
}

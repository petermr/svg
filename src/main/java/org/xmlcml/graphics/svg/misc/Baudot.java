package org.xmlcml.graphics.svg.misc;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;

public class Baudot {

	private static Code[] CODES = {
			new Code("Null", "00000"),
			new Code("CR",   "00010"),
			new Code("LF",   "01000"),
			new Code(" ",   "00100"),
			new Code("Q",    "11101"),
			new Code("W",    "11001"),
			new Code("E",    "10000"),
			new Code("R",    "01010"),
			new Code("T",    "00001"),
			new Code("Y",    "10101"),
			new Code("U",    "00111"),
			new Code("I",    "00110"),
			new Code("O",    "11000"),
			new Code("P",    "10110"),
			new Code("1",    "11101"),
			new Code("2",    "11001"),
			new Code("3",    "10000"),
			new Code("4",    "01010"),
			new Code("5",    "00001"),
			new Code("6",    "10101"),
			new Code("7",    "00111"),
			new Code("8",    "00110"),
			new Code("9",    "11000"),
			new Code("0",    "10110"),
			new Code("A",    "00011"),
			new Code("S",    "00101"),
			new Code("D",    "01001"),
			new Code("F",    "01101"),
			new Code("G",    "11010"),
			new Code("H",    "10100"),
			new Code("J",    "01011"),
			new Code("K",    "01111"),
			new Code("L",    "10010"),
			new Code("Z",    "10001"),
			new Code("X",    "10111"),
			new Code("C",    "01110"),
			new Code("V",    "11110"),
			new Code("B",    "11001"),
			new Code("A",    "11101"),
			new Code("N",    "01100"),
			new Code("M",    "11100"),
			new Code("SHIFT","11011"),
			new Code("ERASE","11111"),
	};
	
	public final static List<Code> CODE_LIST = Arrays.asList(CODES);

	double deltax = 30.0;
	double deltay = deltax;

	public Baudot() {
		
	}
	
	public Code getCode(String ch) {
		for (Code code : CODE_LIST) {
			if (code.character.equals(ch)) {
				return code;
			}
		}
		return null;
	}

	public SVGG getSVGElement(List<String> chars) {
		SVGG gg = new SVGG();
		double y = 0 + deltay*0.2;
		for (String ch : chars) {
			Code code = this.getCode(ch);
			gg.appendChild(code.getSVG(deltax, y));
			y += deltay;
		}
		// surrounding rect
		double holesWidth = 5*deltax;
		double namesWidth = 2.5*deltax;
		double tapeWidth = holesWidth + namesWidth;
		double tapeBottom = y+deltay;
		Real2 tapeBox = new Real2(tapeWidth, tapeBottom);
		SVGRect rect = new SVGRect(new Real2(0, 0), tapeBox);
		String rectStyle = "fill:none;stroke:black;stroke-width:1.0;";
		rect.setCSSStyle(rectStyle);
		gg.appendChild(rect);
		
		double standTopY = tapeBottom + deltay;
		double standWidth = holesWidth + namesWidth + holesWidth;
		double standHeight = tapeWidth;
		double standBottom = standTopY + standHeight;
		SVGRect stand = new SVGRect(new Real2(0, standTopY), new Real2(standWidth, standBottom));
		stand.setCSSStyle(rectStyle);
		gg.appendChild(stand);
		double slotWidth = 20.; // this is the thickness of the acrylic;
		double standSlotX = standWidth / 2. - tapeWidth / 2.;
		double standSlotY = standTopY + standHeight / 2. - slotWidth / 2.;
		SVGRect standSlot = new SVGRect(new Real2(standSlotX, standSlotY), new Real2(standSlotX + tapeWidth, standSlotY + slotWidth));
		standSlot.setCSSStyle(rectStyle);
		gg.appendChild(standSlot);
		return gg;
	}
	
	public static void main(String[] args) {
		Baudot baudot = new Baudot();
		System.out.println(baudot.getCode("C"));
		SVGG g = baudot.getCode("C").getSVG(10., 20.);
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/baudot/c.svg"));
		g = baudot.getCode("ERASE").getSVG(15., 30.);
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/baudot/erase.svg"));
		String[] ss = {"T", "I", "M", " ", "C", "A", "T", "H", "E", "R", "I", "N", "E", " ", "2", "0", "1", "7"};
		g = baudot.getSVGElement(Arrays.asList(ss));
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/baudot/cat.svg"));
		ss =  new String[]{"J", "U", "D", "I", "T", "H", " ", "P", "E", "T", "E", "R", " ", "2", "0", "1", "7"};
		g = baudot.getSVGElement(Arrays.asList(ss));
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/baudot/jp.svg"));
	}
	
	
}
class Code {
	String holes;
	String character;
	double x;
	double deltax;

	public Code(String character, String holes) {
		this.character = character;
		this.holes = holes;
	}
	
	public SVGG getSVG(double deltax, double y) {
		this.deltax = deltax;
		SVGG g = new SVGG();
		System.out.println(">>"+holes);
		x = 0.0 + 0.2*deltax;
		for (int i = 0; i < 5; i++) {
			char ch = holes.charAt(i);
			if (ch == '1') {
				g.appendChild(drawCircle(y, deltax/3.));
			}
			x += deltax;
			if (i == 2) {
				x -= deltax/6.;
				g.appendChild(drawCircle(y, deltax/5.));
				x += 5.*deltax/6.;
			}
		}
		x += deltax*0.3;
		if (character.equals("I")) x += deltax*0.2;
		SVGText t = new SVGText(new Real2(x+(deltax*0.3), y+deltax*0.85), character);
		t.setCSSStyle("font-family:helvetica;font-size:"+(deltax*0.85)+";fill:black;stroke:none;");
		g.appendChild(t);
		return g;
	}
	
	public SVGCircle drawCircle(double y, double r) {
		SVGCircle circle = new SVGCircle(new Real2(x + deltax/2, y+deltax/2), r);
		circle.setCSSStyle("fill:none;stroke:black;stroke-width:1.5;");
		return circle;
	}
	
	public String toString() {
		return character+":"+holes;
	}
}

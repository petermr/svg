package org.xmlcml.graphics.svg.text.line;

import java.util.List;

import org.xmlcml.graphics.svg.rule.horizontal.LineChunk;

/** a lists of LineChunks, normally within a TextLine.
 * <p>
 * Normally the list will be Phrase, Blank, Phrase, Blank... Phrase but 
 * this may evolve.
 * </p>
 * 
 * @author pm286
 *
 */
public class TabbedTextLine {

	private List<LineChunk> lineChunkList;
}

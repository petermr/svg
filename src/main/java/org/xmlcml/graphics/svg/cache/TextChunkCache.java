package org.xmlcml.graphics.svg.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.cache.AbstractCache;
import org.xmlcml.graphics.svg.cache.ComponentCache;
import org.xmlcml.graphics.svg.cache.TextCache;
import org.xmlcml.graphics.svg.text.phrase.TextChunk;

/** creates textChunks 
 * uses TextCache as raw input and systematically builds PhraseList and TextChunks
 * NOT FINISHED
 * 
 * @author pm286
 *
 */
public class TextChunkCache extends AbstractCache {
	static final Logger LOG = Logger.getLogger(TextChunkCache.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private List<SVGText> rawTextList;
	private List<TextChunk> textChunkList;
	private TextCache siblingTextCache;

	private TextChunkCache() {
	}
	
	public TextChunkCache(ComponentCache containingComponentCache) {
		super(containingComponentCache);
	}

	public List<? extends SVGElement> getOrCreateElementList() {
		return getOrCreateTextChunkList();
	}
	
	public List<SVGText> getOrCreateRawTextList() {
		if (textChunkList == null) {
			 rawTextList = siblingTextCache == null ? null : siblingTextCache.getTextList();
			if (textChunkList == null) {
				textChunkList = new ArrayList<TextChunk>();
			}
		}
		return rawTextList;
	}

	public List<TextChunk> getOrCreateTextChunkList() {
		if (textChunkList == null) {
			getOrCreateRawTextList();
			throw new RuntimeException("TextChunks NYI");
		}
		return textChunkList;
	}

	@Override
	public String toString() {
		getOrCreateTextChunkList();
		String s = ""
			+ "rawText size: "+getOrCreateRawTextList().size()
			+ "textChunks: "+textChunkList.size()+"; "
			;
		return s;

	}

	@Override
	public void clearAll() {
		superClearAll();
		textChunkList = null;
	}

}

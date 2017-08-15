package org.xmlcml.graphics.svg.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.euclid.util.CSVUtil;
import org.xmlcml.euclid.util.MultisetUtil;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.cache.ComponentCache.Feature;
import org.xmlcml.graphics.svg.util.ColorStore;
import org.xmlcml.graphics.svg.util.ColorStore.ColorizerType;

import com.google.common.collect.Multiset;

/** tests the detection of graphics components (rects, lines, etc.) and
 * maybe makes decisions on processing.
 * 
 * @author pm286
 *
 */
public class ComponentCacheTest {
	private static final String TARGET_TABLE_TYPES_DIR = "target/table/types";
	private static final Logger LOG = Logger.getLogger(ComponentCacheTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	private static final String FILE = "file";

	@Test
	public void testSummarizeGraphicsComponents() throws IOException {
		List<List<String>> bodyList = new ArrayList<List<String>>();
		List<String> headers = new ArrayList<String>();
		headers.add(FILE);
		List<Feature> features = Arrays.asList(new Feature[] {
			Feature.HORIZONTAL_TEXT_COUNT,
			Feature.HORIZONTAL_TEXT_STYLE_COUNT,
			Feature.VERTICAL_TEXT_COUNT,
			Feature.VERTICAL_TEXT_STYLE_COUNT,
			
			Feature.LINE_COUNT,
			Feature.RECT_COUNT,
			Feature.PATH_COUNT,
			Feature.CIRCLE_COUNT,
			Feature.ELLIPSE_COUNT,
			Feature.POLYGONS_COUNT,
			Feature.POLYLINE_COUNT,
			Feature.SHAPE_COUNT,
		}
		);
		headers.addAll(Feature.getAbbreviations(features));
		for (File typesDir : Fixtures.TABLE_TYPES) {
			File[] svgFiles = typesDir.listFiles();
			if (svgFiles == null) continue;
			for (File svgFile : svgFiles) {
				if (svgFile.toString().endsWith(".svg")) {
					List<String> row = new ArrayList<String>();
					String filename = svgFile.getName();
					LOG.debug(filename);
					row.add(filename);
					SVGElement svgElement = SVGElement.readAndCreateSVG(svgFile);
					ComponentCache cache = new ComponentCache();
					cache.readGraphicsComponents(svgElement);
					List<String> featureValues = cache.getFeatureValues(features);
					row.addAll(featureValues);
					bodyList.add(row);
				}
			}
		}
		File csvFile = new File(TARGET_TABLE_TYPES_DIR, "graphics.csv");
		CSVUtil.writeCSV(csvFile.toString(), headers, bodyList);
	}
	
	@Test
	public void testSummarizeTextStyles() throws IOException {
		List<List<String>> bodyList = new ArrayList<List<String>>();
		int maxStyles = 5;
		List<String> headers = new ArrayList<String>();
		headers.add(FILE);
		for (int i = 0; i < maxStyles; i++) {
			headers.add("style"+i);
			headers.add("count"+i);
		}
		for (File typeDir : Fixtures.TABLE_TYPES) {
			String typeName = typeDir.getName();
			File outParent = new File(TARGET_TABLE_TYPES_DIR, typeName+"/");
			File[] svgFiles = typeDir.listFiles();			
			for (File svgFile : svgFiles) {
				String filename = svgFile.toString();
				if (filename.endsWith(".svg")) {
					List<String> row = new ArrayList<String>();
					String baseName = FilenameUtils.getBaseName(filename);
					LOG.debug(baseName);
					row.add(filename);
					SVGElement svgElement = SVGElement.readAndCreateSVG(svgFile);
					ComponentCache cache = new ComponentCache();
					cache.readGraphicsComponents(svgElement);
					Multiset<String> styleSet = cache.getOrCreateTextCache().createAbbreviatedHorizontalTextStyleMultiset();
					List<Multiset.Entry<String>> entryList = MultisetUtil.createStringListSortedByCount(styleSet);
					int entryCount = entryList.size();
					int filled = Math.min(entryCount, maxStyles);
					int empty = Math.max(0, maxStyles - entryCount);
					for (int i = 0; i < filled; i++) {
						Multiset.Entry<String> entry = entryList.get(i);
						row.add(entry.getElement());
						row.add(String.valueOf(entry.getCount()));
					}
					for (int i = 0; i < empty; i++) {
						row.add("");
						row.add("");
					}
					bodyList.add(row);
					File outfile = new File(outParent, baseName+".svg");
					SVGSVG.wrapAndWriteAsSVG(cache.getOrCreateConvertedSVGElement(), outfile);
				}
			}
		}
		File csvFile = new File(TARGET_TABLE_TYPES_DIR, "fonts.csv");
		CSVUtil.writeCSV(csvFile.toString(), headers, bodyList);
	}
	
	@Test
	public void testLinesAndRects() {
		List<List<String>> bodyList = new ArrayList<List<String>>();
		List<String> headers = new ArrayList<String>();
		headers.add(FILE);
		List<Feature> features = Arrays.asList(new Feature[] {
			Feature.LONG_HORIZONTAL_RULE_COUNT,
			Feature.SHORT_HORIZONTAL_RULE_COUNT,
			Feature.TOP_HORIZONTAL_RULE_COUNT,
			Feature.BOTTOM_HORIZONTAL_RULE_COUNT,
			Feature.LONG_HORIZONTAL_RULE_THICKNESS_COUNT,
			Feature.HORIZONTAL_PANEL_COUNT,
		}
		);
		headers.addAll(Feature.getAbbreviations(features));
		headers.add(FILE);
		for (File dir : Fixtures.TABLE_TYPES) {
			File[] svgFiles = dir.listFiles();
			for (File svgFile : svgFiles) {
				if (svgFile.toString().endsWith(".svg")) {
					List<String> row = new ArrayList<String>();
					String filename = svgFile.getName();
					LOG.debug(filename);
					row.add(filename);
					SVGElement svgElement = SVGElement.readAndCreateSVG(svgFile);
					ComponentCache cache = new ComponentCache();
					cache.readGraphicsComponents(svgElement);
					List<String> featureValues = cache.getFeatureValues(features);
					row.addAll(featureValues);
					bodyList.add(row);
				}
			}
		}
		File csvFile = new File(TARGET_TABLE_TYPES_DIR, "lines.csv");
		CSVUtil.writeCSV(csvFile.toString(), headers, bodyList);
		
	}
	
	@Test
	public void testPaintTextStyles() throws IOException {
		String color[] = {
				"red",
				"green",
				"blue",
				"cyan",
				"magenta",
				"yellow",
				"pink",
				"gray",
				"purple",
			};
		List<List<String>> bodyList = new ArrayList<List<String>>();
		ColorStore colorStore = ColorStore.createColorizer(ColorizerType.CONTRAST);
		for (File typeDir : Fixtures.TABLE_TYPES) {
			String typeName = typeDir.getName();
			File outParent = new File(TARGET_TABLE_TYPES_DIR, typeName+"/");
			File[] svgFiles = typeDir.listFiles();			
			for (File svgFile : svgFiles) {
				String filename = svgFile.toString();
				if (filename.endsWith(".svg")) {
					List<String> row = new ArrayList<String>();
					String baseName = FilenameUtils.getBaseName(filename);
					LOG.debug(baseName);
					row.add(filename);
					ComponentCache cache = new ComponentCache();
					cache.readGraphicsComponents(svgFile);
					cache.getOrCreateTextCache().createCompactedTextsAndReplace();
					TextCache textCache = cache.getOrCreateTextCache();
					SVGG g = new SVGG();
					g.appendChild(textCache.createColoredTextStyles(color));
					g.appendChild(cache.getOrCreateLineCache().createColoredHorizontalLineStyles(color));
					File outfile = new File(outParent, baseName+".svg");
					SVGSVG.wrapAndWriteAsSVG(g, outfile);
				}
				continue;
			}
		}
	}
	

}

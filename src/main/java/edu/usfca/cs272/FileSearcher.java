package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.usfca.cs272.InvertedIndex.ResultMetadata;
import opennlp.tools.stemmer.Stemmer;


/**
 * Uses a search query to search the inverted index structure and ranks the
 * results.
 */
public class FileSearcher implements FileSearchInterface {

	/**
	 * Maps queries as they are written in the query file to lists of ResultMetadata
	 * objects
	 */
	private final TreeMap<String, ArrayList<InvertedIndex.ResultMetadata>> results;

	/**
	 * Inverted index object on which the search is to be performed
	 */
	private final InvertedIndex index;
	
	/**
	 * Used to convert query text to standardized stems
	 */
	private final Stemmer stemmer;

	/** Logger used for this class. */
	private static final Logger log = LogManager.getRootLogger();
	
	/**
	 * Constructor
	 * 
	 * @param index the inverted index object on which the search is to be performed
	 */
	public FileSearcher(InvertedIndex index) {
		this.results = new TreeMap<>();
		this.index = index;
		this.stemmer = FileStemmer.getDefaultStemmer();
		log.debug("FileSearcher initialized");
	}

	/**
	 * 
	 * @param line    single line of search queries
	 * @param partial indicates whether we are doing a partial search
	 */
	@Override
	public void search(String line, boolean partial) {
		
		TreeSet<String> queries = FileStemmer.uniqueStems(line, stemmer);

		if (queries.isEmpty()) {
			return;
		}

		String joined = String.join(" ", queries);

		if (results.containsKey(joined)) {
			return;
		}
		
		results.put(joined, index.search(queries, partial));
		
	}

	/**
	 * Converts this.results into a form writable by JSONWRITER, then calls
	 * JSONWRITER to write it.
	 * 
	 * @param path file locations will be written to
	 * @throws IOException may be triggered by JSONWRITER
	 */
	@Override
	public void writeResults(Path path) throws IOException {

		var writeableResults = new TreeMap<String, ArrayList<TreeMap<String, String>>>();

		for (String query : results.keySet()) {
			// Convert metadata objects to format that can be printed by JSONWRITER
			ArrayList<TreeMap<String, String>> metadataAsString = new ArrayList<TreeMap<String, String>>();
			for (InvertedIndex.ResultMetadata result : results.get(query)) {
				TreeMap<String, String> resultMap = new TreeMap<String, String>();
				resultMap.put("where", "\"" + result.getLocation() + "\"");
				resultMap.put("count", String.valueOf(result.getMatchCount()));
				resultMap.put("score", String.valueOf(df.format(result.getScore())));
				metadataAsString.add(resultMap);
			}
			writeableResults.put(query, metadataAsString);
		}
		JsonWriter.writeObjectArrayObject(writeableResults, path);
	}

	/**
	 * Returns true if the given query line has been searched before and is present
	 * in the results map.
	 * 
	 * @param line the line of search queries
	 * @return true if the given query line has been searched before and is present
	 *         in the results map.
	 */
	@Override
	public boolean hasQuery(String line) {
		
		TreeSet<String> stemmed = FileStemmer.uniqueStems(line, stemmer);
		
		if (stemmed.isEmpty()) {
			return false;
		}
		
		String joined = String.join(" ", stemmed);
		
		return results.containsKey(joined);
	}


	/**
	 * Returns list of ResultMetadata objects if the passed query line is in
	 * results, or an empty list if it's not in results.
	 * 
	 * @param line line of search queries
	 * @return Returns list of ResultMetadata objects if the passed query line is in
	 *         results, or an empty list if it's not in results.
	 */
	@Override
	public List<ResultMetadata> getResults(String line) {
		
		TreeSet<String> stemmed = FileStemmer.uniqueStems(line, stemmer);

		if (stemmed.isEmpty()) {
			return Collections.unmodifiableList(new ArrayList<ResultMetadata>());
		}

		String joined = String.join(" ", stemmed);
		
		if (results.containsKey(joined)) {
			return Collections.unmodifiableList(results.get(joined));
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Returns the size of results
	 * @return size of results
	 */
	@Override
	public int size() {
		return results.size();
	}

}
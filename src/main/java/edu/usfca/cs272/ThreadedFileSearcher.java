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
public class ThreadedFileSearcher implements FileSearchInterface {

	/**
	 * Maps queries as they are written in the query file to lists of ResultMetadata
	 * objects
	 */
	private final TreeMap<String, ArrayList<InvertedIndex.ResultMetadata>> results;

	/**
	 * Inverted index object on which the search is to be performed
	 */
	private final ThreadSafeIndex index;

	/**
	 * Used to convert query text to standardized stems
	 */
	private final Stemmer stemmer;

	/** Manages threads for this class. */
	private final WorkQueue workQueue;

	/** Logger used for this class. */
	private static final Logger log = LogManager.getRootLogger();

	/**
	 * Constructor
	 * 
	 * @param index     the inverted index object on which the search is to be
	 *                  performed
	 * @param workQueue manages threads for this class
	 */
	public ThreadedFileSearcher(ThreadSafeIndex index, WorkQueue workQueue) {
		this.results = new TreeMap<>();
		this.index = index;
		this.stemmer = FileStemmer.getDefaultStemmer();
		this.workQueue = workQueue;
		log.debug("ThreadedFileSearcher initialized");
	}

	/**
	 * Iterates through a file (path) and calls search method on each line.
	 * 
	 * @param path    the file containing the search queries
	 * @param partial indicates whether or not a partial seach is being performed
	 *                perform searches
	 * @throws IOException may be thrown by BufferedReader
	 */
	@Override
	public void search(Path path, boolean partial) throws IOException {
		try {
			FileSearchInterface.super.search(path, partial);
		} finally {
			workQueue.finish();
		}
	}

	/**
	 * Conducts a search given a single query line
	 * 
	 * @param line    single line of search queries
	 * @param partial indicates whether we are doing a partial search
	 */
	@Override
	public void search(String line, boolean partial) {
		Runnable task = new Search(line, partial);
		workQueue.execute(task);
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
		synchronized (results) {
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
		}
		JsonWriter.writeObjectArrayObject(writeableResults, path);
	}

	/**
	 * Returns true if the given query line has been searched before and is present
	 * in the results map.
	 * 
	 * @param queries the line of search terms
	 * @return true if the given query line has been searched before and is present
	 *         in the results map.
	 */
	@Override
	public boolean hasQuery(String queries) {

		TreeSet<String> stemmed = FileStemmer.uniqueStems(queries, stemmer);

		if (stemmed.isEmpty()) {
			return false;
		}

		String joined = String.join(" ", stemmed);

		synchronized (results) {
			return results.containsKey(joined);
		}
	}

	/**
	 * Returns list of ResultMetadata objects if the passed query line is in
	 * results, or an empty list if it's not in results.
	 * 
	 * @param queries line of search terms
	 * @return Returns list of ResultMetadata objects if the passed query line is in
	 *         results, or an empty list if it's not in results.
	 */
	@Override
	public List<ResultMetadata> getResults(String queries) {

		TreeSet<String> stemmed = FileStemmer.uniqueStems(queries, stemmer);

		if (stemmed.isEmpty()) {
			return Collections.unmodifiableList(new ArrayList<ResultMetadata>());
		}

		String joined = String.join(" ", stemmed);
		
		synchronized (results) {
			if (results.containsKey(joined)) {
				return Collections.unmodifiableList(results.get(joined));
			}
		}
		
		return Collections.emptyList();
	}

	/**
	 * Returns the size of results
	 * 
	 * @return size of results
	 */
	@Override
	public int size() {
		synchronized (results) {
			return results.size();
		}
	}

	/**
	 * Represents the search method as a Runnable
	 */
	private class Search implements Runnable {

		/** single line of search queries **/
		private final String line;

		/** indicates whether we are doing a partial search **/
		private final boolean partial;

		/**
		 * @param line    String of queries
		 * @param partial indicates the type of search
		 */
		public Search(String line, boolean partial) {
			this.line = line;
			this.partial = partial;
		}

		@Override
		public void run() {
			TreeSet<String> queries = FileStemmer.uniqueStems(line);

			if (queries.isEmpty()) {
				return;
			}

			String joined = String.join(" ", queries);

			synchronized (results) {
				if (results.containsKey(joined)) {
					return;
				}

				// to prevent repeat searches
				results.put(joined, null);
			}

			var local = index.search(queries, partial);

			synchronized (results) {
				results.put(joined, local);
			}
		}

	}
}
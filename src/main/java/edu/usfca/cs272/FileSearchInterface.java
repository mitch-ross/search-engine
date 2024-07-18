package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;

import edu.usfca.cs272.InvertedIndex.ResultMetadata;

/**
 * Uses a search query to search the inverted index structure and ranks the
 * results.
 */
public interface FileSearchInterface {
	
	/**
	 * Format that is applied to the score attribute in the results structure
	 */
	public static final DecimalFormat df = new DecimalFormat("#0.00000000");
	
	/**
	 * Iterates through a file (path) and calls search method on each line.
	 * 
	 * @param path    the file containing the search queries
	 * @param partial indicates whether or not a partial seach is being performed
	 * @throws IOException thrown by BufferedReader
	 */
	public default void search(Path path, boolean partial) throws IOException {
		try (BufferedReader buf = Files.newBufferedReader(path, UTF_8)) {
			String line;
			while ((line = buf.readLine()) != null) {
				search(line, partial);
			}
		}
	}
	
	/**
	 * Conducts a search on a single line of query text
	 * @param line    single line of search queries
	 * @param partial indicates whether we are doing a partial search
	 */
	public void search(String line, boolean partial);
	
	/**
	 * Converts this.results into a form writable by JSONWRITER, then calls
	 * JSONWRITER to write it.
	 * 
	 * @param path file locations will be written to
	 * @throws IOException thrown by JSONWriter
	 */
	public void writeResults(Path path) throws IOException;
	
	/**
	 * Returns true if the given query line has been searched before and is present
	 * in the results map.
	 * 
	 * @param line the line of search queries
	 * @return true if the given query line has been searched before and is present
	 *         in the results map.
	 */
	public boolean hasQuery(String line);
	
	/**
	 * Returns list of ResultMetadata objects if the passed query line is in
	 * results, or an empty list if it's not in results.
	 * 
	 * @param line line of search queries
	 * @return Returns list of ResultMetadata objects if the passed query line is in
	 *         results, or an empty list if it's not in results.
	 */
	public List<ResultMetadata> getResults(String line);
	
	/**
	 * Returns the size of results
	 * @return size of results
	 */
	public int size();
	
}

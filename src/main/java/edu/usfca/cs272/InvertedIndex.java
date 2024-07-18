package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Contains data structures used by Driver
 */
public class InvertedIndex {

	/**
	 * Stores stem counts of files processed during program run.
	 */
	private final TreeMap<String, Integer> counts;

	/**
	 * Inverted index {stem, {location, [indexes]}}
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invIndex;

	/**
	 * Constructor
	 */
	public InvertedIndex() {
		counts = new TreeMap<String, Integer>();
		invIndex = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
	}

	/**
	 * Allows other classes to put a key-value pair into inv index.
	 * 
	 * @param stem     that may or may not be in inv index
	 * @param location the location the stem is located
	 * @param position index the word was found at in the specified location
	 */
	public void add(String stem, String location, int position) {
		invIndex.putIfAbsent(stem, new TreeMap<>());
		invIndex.get(stem).putIfAbsent(location, new TreeSet<>());
		boolean modified = invIndex.get(stem).get(location).add(position);
		if (modified) {
			counts.put(location, counts.getOrDefault(location, 0) + 1);
		}
	}
	
	/**
	 * Copies the contents of another inverted index into this one
	 * @param other the other inverted index
	 */
	public void addAll(InvertedIndex other) {
		for (var outerEntry : other.invIndex.entrySet()) {
			// Get other word & locations
			String otherWord = outerEntry.getKey();
			var otherLocations = outerEntry.getValue();
			// See if the other word is in this index
			var thisLocations = this.invIndex.get(otherWord);
			// If other word isn't in this index, simply put the whole entry
			if (thisLocations == null) {
				this.invIndex.put(otherWord, otherLocations);
			}
			// Otherwise loop, reusing treesets of integers when possible 
			else {
				for (var locationEntry : otherLocations.entrySet()) {
					// Get other location & positions
					String otherLocation = locationEntry.getKey();
					var otherPositions = locationEntry.getValue();
					// Compare those to what's present in this index
					var thisPositions = thisLocations.get(otherLocation);
					// If other location isn't in this index, simply put the whole entry
					if (thisPositions == null) {
						thisLocations.put(otherLocation, otherPositions);
					}
					else {
						thisPositions.addAll(otherPositions);
					}
				}
			}
		}
		
		for (var countEntry : other.counts.entrySet()) {
			// Get other location & count
			String otherLocation = countEntry.getKey();
			int otherCount = countEntry.getValue();
			// See if the other location is in this index
			var thisCount = this.counts.get(otherLocation);
			// If other location isn't in this index, simply put the whole entry
			if (thisCount == null) {
				this.counts.put(otherLocation, otherCount);
			}
			// Otherwise, do an increase
			else {
				this.counts.put(otherLocation, otherCount + thisCount);
			}
		}
	}

	@Override
	public String toString() {
		return "Inverted Index:\n" + invIndex.toString() + "\nCounts:\n" + counts.toString();
	}
	
	/**
	 * Returns an unmodifiable set containing location names found in counts.
	 * 
	 * @return an unmodifiable set containing location names found in counts.
	 */
	public Set<String> getLocations() {
		return Collections.unmodifiableSet(counts.keySet());
	}

	/**
	 * Returns an unmodifiable alphabetically ascending set of stems found in
	 * invIndex
	 * 
	 * @return an unmodifiable alphabetically ascending set of stems found in
	 *         invIndex
	 */
	public SortedSet<String> getStems() {
		return Collections.unmodifiableSortedSet(invIndex.navigableKeySet());
	}

	/**
	 * Returns locations a stem can be found in.
	 * 
	 * @param stem the stem we want the locations of
	 * @return a map of the locations a specified stem can be found in the index
	 */
	public Set<String> getStemLocations(String stem) {
		if (!hasStem(stem)) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(invIndex.get(stem).keySet());
	}

	/**
	 * Returns a set of the indexes in which a stem can be found at a location.
	 * 
	 * @param stem     the stem we want the indexes of
	 * @param location stem indexes are in regards to this file
	 * @return a set of the indexes in which a stem can be found at a location
	 */
	public Set<Integer> getStemPositionsIn(String stem, String location) {
		if (!stemHasLocation(stem, location)) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(invIndex.get(stem).get(location));
	}

	/**
	 * Determines whether a location is present in the index
	 * 
	 * @param location we are checking the presence of this
	 * @return true if location is in index, false otherwise
	 */
	public boolean hasLocation(String location) {
		return counts.containsKey(location);
	}

	/**
	 * Determines whether a stem is in the index at all
	 * 
	 * @param stem stem we are checking the presence of
	 * @return true if stem is present in the index, false otherwise
	 */
	public boolean hasStem(String stem) {
		return invIndex.containsKey(stem);
	}

	/**
	 * Determines whether a particular stem occurs at a particular location
	 * 
	 * @param stem     we want to know if this occurs at the location
	 * @param location location being searched
	 * 
	 * @return true is stem occurs at location, false otherwise
	 */
	public boolean stemHasLocation(String stem, String location) {
		return invIndex.containsKey(stem) && invIndex.get(stem).containsKey(location);
	}

	/**
	 * Returns true if a given stem is at a given position in a given location.
	 * 
	 * @param stem     stem in question
	 * @param location check if stem is at this location
	 * @param position check if stem is at this position in the given location
	 * @return true if a given stem is at a given position in a given location.
	 */
	public boolean stemAtPosition(String stem, String location, int position) {
		return stemHasLocation(stem, location) && invIndex.get(stem).get(location).contains(position);
	}

	/**
	 * Returns the number of locations found in the index.
	 * 
	 * @return number of locations found in the index
	 */
	public int countsSize() {
		return counts.size();
	}

	/**
	 * Returns the word count of a particular location
	 * 
	 * @param location the location we want the word count from
	 * @return the word count of the given location
	 */
	public int countOf(String location) {
		return counts.getOrDefault(location, 0);
	}

	/**
	 * Essentially a unique stems count.
	 * 
	 * @return the size of the inverted index
	 */
	public int indexSize() {
		return invIndex.size();
	}

	/**
	 * Returns the number of locations that a stem from the inverted index exists
	 * in.
	 * 
	 * @param stem the stem in question
	 * @return number of locations that a stem from the inverted index exists in,
	 *         integer.
	 */
	public int numLocationsAtStem(String stem) {
		if (hasStem(stem)) {
			return invIndex.get(stem).size();
		}
		return 0;
	}

	/**
	 * Returns the number of times a particular stem occurs at a particular
	 * location.
	 * 
	 * @param stem     we want to know how many times this stem occurs at the given
	 *                 location
	 * @param location we want to count how may times the given stem occurs in this
	 *                 location
	 * @return number of times a particular stem occurs at a particular location
	 */
	public int numStemAtLocation(String stem, String location) {
		if (stemHasLocation(stem, location)) {
			return invIndex.get(stem).get(location).size();
		}
		return 0;
	}

	/**
	 * Returns the number of positions at a location for a given stem
	 * 
	 * @param stem     stem of interest
	 * @param location location of interest.
	 * @return the number of positions at a location for a given stem
	 */
	public int numPositionsAtLocationForStem(String stem, String location) {
		return getStemPositionsIn(stem, location).size();
	}

	/**
	 * Writes the counts to the specified path in json format
	 * 
	 * @param path the file we want to write to
	 * @throws IOException thrown if there is an issue with given path
	 */
	public void writeCounts(Path path) throws IOException {
		JsonWriter.writeObject(Collections.unmodifiableMap(counts), path);
	}

	/**
	 * Writes the inverted index to the specified path in json format
	 * 
	 * @param path the file we want to write to
	 * @throws IOException thrown if there is an issue with given path
	 */
	public void writeInvIndex(Path path) throws IOException {
		JsonWriter.writeObjectObjetcArrary(Collections.unmodifiableMap(invIndex), path);
	}

	/**
	 * Performs a search on a single line of query, may be partial or exact.
	 * 
	 * @param query   set of query stems
	 * @param partial indicates whether to perform a partial search
	 * @return a list of result metadata objects
	 */
	public ArrayList<ResultMetadata> search(Set<String> query, boolean partial) {
		return partial ? partialSearch(query) : exactSearch(query);
	}

	/**
	 * Performs an exact search on a single query line, represented as a set of
	 * stems.
	 * 
	 * @param query set of stems used for search
	 * @return an ArrayList containing ResultMetadata objects
	 */
	public ArrayList<ResultMetadata> exactSearch(Set<String> query) {
				
		HashMap<String, ResultMetadata> matchCounts = new HashMap<>();
		ArrayList<ResultMetadata> results = new ArrayList<>();

		for (String stem : query) {
			// if stem is in index
			if (invIndex.containsKey(stem)) {
				// for every location that stem is found in
				addStemResults(stem, matchCounts, results);
			}
		}

		// Sort/rank results
		Collections.sort(results);
		return results;
	}

	/**
	 * Performs a partial search on a single query line, represented as a set of
	 * stems.
	 * 
	 * @param query set of stems used for search
	 * @return an ArrayList containing ResultMetadata objects
	 */
	public ArrayList<ResultMetadata> partialSearch(Set<String> query) {

		HashMap<String, ResultMetadata> matchCounts = new HashMap<>();
		ArrayList<ResultMetadata> results = new ArrayList<>();

		// Get locations of each stem
		for (String stem : query) {

			for (String similarStem : invIndex.tailMap(stem).keySet()) {

				if (!similarStem.startsWith(stem)) {
					break;
				}

				addStemResults(similarStem, matchCounts, results);

			}
		}
		// Sort/rank results
		Collections.sort(results);
		return results;
	}

	/**
	 * Helper function for exact and partial sort
	 * 
	 * @param stem        the stem being searched for
	 * @param matchCounts maps a word to the number of times it is found in a
	 *                    particular location in the index
	 * @param results     list of ResultMetadata objects, uses information from
	 *                    matchCounts to update
	 */
	private void addStemResults(String stem, HashMap<String, ResultMetadata> matchCounts,
			ArrayList<ResultMetadata> results) {
		for (String location : invIndex.get(stem).keySet()) {
			int matches = invIndex.get(stem).get(location).size();
			ResultMetadata result = matchCounts.get(location);
			// if location isn't already in matchCounts
			if (result == null) {
				result = new ResultMetadata(location);
				matchCounts.put(location, result);
				results.add(result);
			}
			result.increaseMatchCount(matches);
		}
	}

	/**
	 * Represents a file that contains stems from a search query. Used to rank
	 * results.
	 */
	public class ResultMetadata implements Comparable<ResultMetadata> {

		/** The normalized file path. */
		private final String location;

		/** The file's matching word count. */
		private long matchCount;

		/** The file's "score", which is matching stems / total stems. */
		private double score;

		/**
		 * Constructor
		 * 
		 * @param location the location which contains matching stems
		 */
		public ResultMetadata(String location) {
			this.location = location;
			this.matchCount = 0;
			this.score = 0;
		}

		@Override
		public int compareTo(ResultMetadata other) {
			int compared = Double.compare(other.score, this.score);

			if (compared == 0) {
				compared = Long.compare(counts.get(other.location), counts.get(this.location));

				if (compared == 0) {
					compared = String.CASE_INSENSITIVE_ORDER.compare(this.location, other.location);
				}
			}

			return compared;
		}

		/**
		 * @return location
		 */
		public String getLocation() {
			return location;
		}

		/**
		 * @return location's match count
		 */
		public long getMatchCount() {
			return matchCount;
		}

		/**
		 * @return location's score (matches / total words)
		 */
		public double getScore() {
			return score;
		}

		/**
		 * Increases the match count and updates the score accordingly
		 * 
		 * @param add the amount that is to be added to matchCount
		 */
		private void increaseMatchCount(int add) {
			matchCount += add;
			score = matchCount / counts.get(location).doubleValue();
		}
	}
}
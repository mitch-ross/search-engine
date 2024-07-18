package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;

/**
 * Contains data structures used by Driver
 */
public class ThreadSafeIndex extends InvertedIndex {

	/** The lock used to protect concurrent access to the index. */
	private final MultiReaderLock lock;

	/**
	 * Constructor
	 */
	public ThreadSafeIndex() {
		super();
		lock = new MultiReaderLock();
	}

	/**
	 * Allows other classes to put a key-value pair into inv index.
	 * 
	 * @param stem     that may or may not be in inv index
	 * @param location the location the stem is located
	 * @param position index the word was found at in the specified location
	 */
	@Override
	public void add(String stem, String location, int position) {
		lock.writeLock().lock();
		try {
			super.add(stem, location, position);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex other) {
		lock.writeLock().lock();
		try {
			super.addAll(other);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			return super.toString();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable set containing location names found in counts.
	 * 
	 * @return an unmodifiable set containing location names found in counts.
	 */
	@Override
	public Set<String> getLocations() {
		lock.readLock().lock();
		try {
			return super.getLocations();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns an unmodifiable alphabetically ascending set of stems found in
	 * invIndex
	 * 
	 * @return an unmodifiable alphabetically ascending set of stems found in
	 *         invIndex
	 */
	@Override
	public SortedSet<String> getStems() {
		lock.readLock().lock();
		try {
			return super.getStems();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns locations a stem can be found in.
	 * 
	 * @param stem the stem we want the locations of
	 * @return a map of the locations a specified stem can be found in the index
	 */
	@Override
	public Set<String> getStemLocations(String stem) {
		lock.readLock().lock();
		try {
			return super.getStemLocations(stem);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns a set of the indexes in which a stem can be found at a location.
	 * 
	 * @param stem     the stem we want the indexes of
	 * @param location stem indexes are in regards to this file
	 * @return a set of the indexes in which a stem can be found at a location
	 */
	@Override
	public Set<Integer> getStemPositionsIn(String stem, String location) {
		lock.readLock().lock();
		try {
			return super.getStemPositionsIn(stem, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Determines whether a location is present in the index
	 * 
	 * @param location we are checking the presence of this
	 * @return true if location is in index, false otherwise
	 */
	@Override
	public boolean hasLocation(String location) {
		lock.readLock().lock();
		try {
			return super.hasLocation(location);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Determines whether a stem is in the index at all
	 * 
	 * @param stem stem we are checking the presence of
	 * @return true if stem is present in the index, false otherwise
	 */
	@Override
	public boolean hasStem(String stem) {
		lock.readLock().lock();
		try {
			return super.hasStem(stem);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Determines whether a particular stem occurs at a particular location
	 * 
	 * @param stem     we want to know if this occurs at the location
	 * @param location location being searched
	 * 
	 * @return true is stem occurs at location, false otherwise
	 */
	@Override
	public boolean stemHasLocation(String stem, String location) {
		lock.readLock().lock();
		try {
			return super.stemHasLocation(stem, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns true if a given stem is at a given position in a given location.
	 * 
	 * @param stem     stem in question
	 * @param location check if stem is at this location
	 * @param position check if stem is at this position in the given location
	 * @return true if a given stem is at a given position in a given location.
	 */
	@Override
	public boolean stemAtPosition(String stem, String location, int position) {
		lock.readLock().lock();
		try {
			return super.stemAtPosition(stem, location, position);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns the number of locations found in the index.
	 * 
	 * @return number of locations found in the index
	 */
	@Override
	public int countsSize() {
		lock.readLock().lock();
		try {
			return super.countsSize();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns the word count of a particular location
	 * 
	 * @param location the location we want the word count from
	 * @return the word count of the given location
	 */
	@Override
	public int countOf(String location) {
		lock.readLock().lock();
		try {
			return super.countOf(location);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Essentially a unique stems count.
	 * 
	 * @return the size of the inverted index
	 */
	@Override
	public int indexSize() {
		lock.readLock().lock();
		try {
			return super.indexSize();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns the number of locations that a stem from the inverted index exists
	 * in.
	 * 
	 * @param stem the stem in question
	 * @return number of locations that a stem from the inverted index exists in,
	 *         integer.
	 */
	@Override
	public int numLocationsAtStem(String stem) {
		lock.readLock().lock();
		try {
			return super.numLocationsAtStem(stem);
		} finally {
			lock.readLock().unlock();
		}
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
	@Override
	public int numStemAtLocation(String stem, String location) {
		lock.readLock().lock();
		try {
			return super.numStemAtLocation(stem, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns the number of positions at a location for a given stem
	 * 
	 * @param stem     stem of interest
	 * @param location location of interest.
	 * @return the number of positions at a location for a given stem
	 */
	@Override
	public int numPositionsAtLocationForStem(String stem, String location) {
		lock.readLock().lock();
		try {
			return super.numPositionsAtLocationForStem(stem, location);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Writes the counts to the specified path in json format
	 * 
	 * @param path the file we want to write to
	 * @throws IOException thrown if there is an issue with given path
	 */
	@Override
	public void writeCounts(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeCounts(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Writes the inverted index to the specified path in json format
	 * 
	 * @param path the file we want to write to
	 * @throws IOException thrown if there is an issue with given path
	 */
	@Override
	public void writeInvIndex(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeInvIndex(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Performs an exact search on a single query line, represented as a set of
	 * stems.
	 * 
	 * @param query set of stems used for search
	 * @return an ArrayList containing ResultMetadata objects
	 */
	@Override
	public ArrayList<InvertedIndex.ResultMetadata> exactSearch(Set<String> query) {
		lock.readLock().lock();
		try {
			return super.exactSearch(query);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Performs a partial search on a single query line, represented as a set of
	 * stems.
	 * 
	 * @param query set of stems used for search
	 * @return an ArrayList containing ResultMetadata objects
	 */
	@Override
	public ArrayList<InvertedIndex.ResultMetadata> partialSearch(Set<String> query) {
		lock.readLock().lock();
		try {
			return super.partialSearch(query);
		} finally {
			lock.readLock().unlock();
		}
	}
}
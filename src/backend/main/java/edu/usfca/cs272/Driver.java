package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Mitch Ross
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */

public class Driver {

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();

		// Initialize data structures
		ArgumentParser parser = new ArgumentParser(args);

		InvertedIndex index;
		FileSearchInterface results;

		WorkQueue workQueue = null;
		ThreadSafeIndex safe = null;

		if (parser.hasFlag("-threads") || parser.hasFlag("-html")) {
			int threads = parser.getInteger("-threads", 5);
			if (threads < 1) {
				threads = 5;
			}

			workQueue = new WorkQueue(threads);
			safe = new ThreadSafeIndex();
			index = safe;
			results = new ThreadedFileSearcher(safe, workQueue);
		}

		else {
			index = new InvertedIndex();
			results = new FileSearcher(index);
		}

		// Reads
		if (parser.hasFlag("-text")) {
			try {
				buildIndex(parser.getPath("-text"), index, workQueue);
			} catch (IOException | NullPointerException e) {
				System.err.println("Error: invalid file");
			}
		}

		if (parser.hasFlag("-html")) {
			WebCrawler crawler = new WebCrawler(parser.getUrl("-html"), (ThreadSafeIndex) index, workQueue);
			crawler.crawl(parser.getInteger("-crawl", 1));
		}

		if (parser.hasFlag("-query")) {
			try {
				results.search(parser.getPath("-query"), parser.hasFlag("-partial"));
			} catch (IOException | NullPointerException e) {
				System.err.println("Error: invalid query file");
			}
		}

		// Close workQueue
		if (workQueue != null) {
			workQueue.shutdown();
		}

		// Writes

		if (parser.hasFlag("-counts")) {
			try {
				index.writeCounts(parser.getPath("-counts", Path.of("counts.json")));
			} catch (IOException e) {
				System.err.println("IO error, but this shouldn't happen if the above code works.");
			}
		}

		if (parser.hasFlag("-index")) {
			try {
				index.writeInvIndex(parser.getPath("-index", Path.of("index.json")));
			} catch (IOException e) {
				System.err.println("IO error, but this shouldn't happen if the above code works.");
			}
		}

		if (parser.hasFlag("-results")) {
			Path resultsFile = parser.getPath("-results", Path.of("results.json"));
			try {
				results.writeResults(resultsFile);
			} catch (IOException e) {
				System.err.println("Error: invalid filename for results.");
			}
		}

		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}

	/**
	 * Builds the index data structure with a path
	 * 
	 * @param path      file or directory that the index will be made from
	 * @param index     the index that will be built
	 * @param workQueue manages threads if multithreading
	 * @return inverted index with search results
	 * @throws IOException thrown by InvertedIndexBuilder and ThreadedIndexBuilder
	 */
	private static InvertedIndex buildIndex(Path path, InvertedIndex index, WorkQueue workQueue) throws IOException {
		if (workQueue != null) {
			ThreadedIndexBuilder.build(path, index, workQueue);
			return index;
		} else {
			InvertedIndexBuilder.build(path, index);
			return index;
		}
	}
}

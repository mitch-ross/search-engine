package edu.usfca.cs272;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Builds data structures found in DataStructs
 */
public class ThreadedIndexBuilder extends InvertedIndexBuilder {

	/** Logger used for this class. */
	private static final Logger log = LogManager.getRootLogger();
	
	/**
	 * Builds ThreadSafeIndex.
	 * 
	 * @param path  either the file or directory that will be processed
	 * @param index the ThreadSafeIndex data structure
	 * @param workQueue the number of threads that will be used
	 * @throws IOException if there as issue with the given path
	 */
	public static void build(Path path, InvertedIndex index, WorkQueue workQueue) throws IOException {
		log.debug("Building index from path with threads");
		if (Files.isDirectory(path)) {
			processDirectory(path, index, workQueue);
		} else {
			Runnable task = new ProcessFile(path, index);
			workQueue.execute(task);
		}
		workQueue.finish();
	}

	/**
	 * Iterates through a directory looking for files to call processFile on.
	 * 
	 * @param directory the path of a directory
	 * @param index     the inverted index data strcuture
	 * @param workQueue     manages worker threads
	 * @throws IOException may be thrown when creating DirectoryStream
	 */
	public static void processDirectory(Path directory, InvertedIndex index, WorkQueue workQueue) throws IOException {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
			for (Path path : directoryStream) {
				if (Files.isDirectory(path)) {
					processDirectory(path, index, workQueue);
				} else if (isText(path)) {
					Runnable task = new ProcessFile(path, index);
					workQueue.execute(task);
				}
			}
		}
	}

	/**
	 * Gets added to work queue to process files
	 */
	private static class ProcessFile implements Runnable {

		/** file the file to be processed */
		private final Path file;

		/** index the ThreadSafeIndex data struct */
		private final InvertedIndex index;

		/**
		 * @param file  file to be processed
		 * @param index where file gets documented
		 */
		public ProcessFile(Path file, InvertedIndex index) {
			this.file = file;
			this.index = index;
		}

		@Override
		public void run() {
			InvertedIndex local = new InvertedIndex();
			try {
				InvertedIndexBuilder.processFile(file, local);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			index.addAll(local);
		}

	}

}
package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import opennlp.tools.stemmer.Stemmer;

/**
 * Builds data structures found in DataStructs
 */
public class InvertedIndexBuilder {

	/**
	 * Builds InvertedIndex from a file path.
	 * 
	 * @param path  either the file or directory that will be processed
	 * @param index the InvertedIndex data structure
	 * @throws IOException if there as issue with the given path
	 */
	public static void build(Path path, InvertedIndex index) throws IOException {
		if (Files.isDirectory(path)) {
			processDirectory(path, index);
		} else {
			processFile(path, index);
		}
	}

	/**
	 * Iterates through a directory looking for files to call processFile on.
	 * 
	 * @param directory the path of a directory
	 * @param data      the inverted index data strcuture
	 * @throws IOException may be thrown when creating DirectoryStream
	 */
	public static void processDirectory(Path directory, InvertedIndex data) throws IOException {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory)) {
			for (Path path : ds) {
				if (Files.isDirectory(path)) {
					processDirectory(path, data);
				} else if (isText(path)) {
					processFile(path, data);
				}
			}
		}
	}

	/**
	 * Adds data from a single file to the inverted index.
	 * 
	 * @param file  the file to be processed
	 * @param index the InvertedIndex data struct
	 * @throws IOException if there is an issue with the path
	 */
	public static void processFile(Path file, InvertedIndex index) throws IOException {
		try (BufferedReader buf = Files.newBufferedReader(file, UTF_8)) {
			String location = file.toString();
			Stemmer stemmer = FileStemmer.getDefaultStemmer();
			String line;
			int fileTextIndex = 1;
			while ((line = buf.readLine()) != null) {
				String[] parsed = FileStemmer.parse(line);
				for (int i = 0; i < parsed.length; i++) {
					String stemmed = stemmer.stem(parsed[i]).toString();
					if (stemmed.length() == 0) {
						continue;
					}
					index.add(stemmed, location, fileTextIndex);
					fileTextIndex++;
				}
			}
		}
	}
	
	/**
	 * Determines whether a particular path ends with the .txt or .text extension.
	 * Any capitalization will pass.
	 * 
	 * @param path is checked for the proper extension
	 * @return true if .txt/.text file, false otherwise
	 */
	public static boolean isText(Path path) {
		String lower = path.toString().toLowerCase();
		return lower.endsWith(".txt") || lower.endsWith(".text");
	}

}
package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class JsonWriter {

	/**
	 * Indents the writer by the specified amount. Does nothing if the indentation
	 * level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {
		writer.append("[");
		if (!elements.isEmpty()) {
			Iterator<? extends Number> it = elements.iterator();
			writer.append("\n");
			writeIndent(it.next().toString(), writer, indent + 1);
			while (it.hasNext()) {
				writer.append(",");
				writer.append("\n");
				writeIndent(it.next().toString(), writer, indent + 1);
			}
		}
		writer.append("\n");
		writeIndent("]", writer, indent);
		writer.flush();
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 * @throws IOException see writeArray()
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) throws IOException {
		StringWriter writer = new StringWriter();
		writeArray(elements, writer, 0);
		return writer.toString();
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent)
			throws IOException {
		writer.append("{");
		if (!elements.isEmpty()) {
			Iterator<? extends Entry<String, ? extends Number>> it = elements.entrySet().iterator();
			// first element
			Entry<String, ? extends Number> entry = it.next();
			writer.append("\n");
			writeEntryObject(entry, writer, indent);
			// remaining elements
			while (it.hasNext()) {
				entry = it.next();
				writer.append(",");
				writer.append("\n");
				writeEntryObject(entry, writer, indent);
			}
		}
		writer.append("\n");
		writeIndent("}\n", writer, indent);
		writer.flush();
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 * @throws IOException see writeObject
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) throws IOException {
		StringWriter writer = new StringWriter();
		writeObject(elements, writer, 0);
		return writer.toString();
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.append("{");
		if (!elements.isEmpty()) {
			var it = elements.entrySet().iterator();
			Entry<String, ? extends Collection<? extends Number>> entry = it.next();
			writer.append("\n");
			writeEntryArray(entry, writer, indent);
			while (it.hasNext()) {
				entry = it.next();
				writer.append(",");
				writer.append("\n");
				writeEntryArray(entry, writer, indent);
			}
		}
		writer.append("\n");
		writeIndent("}", writer, indent);
		writer.flush();
	}

	/**
	 * Writes an entry that maps a String to a Collection of Numbers.
	 * 
	 * @param entry  like from a map
	 * @param writer writes
	 * @param indent desired indent
	 * @throws IOException may be thrown by the writer
	 */
	public static void writeEntryArray(Entry<String, ? extends Collection<? extends Number>> entry, Writer writer,
			int indent) throws IOException {
		writeQuote(entry.getKey(), writer, indent + 1);
		writer.append(": ");
		writeArray(entry.getValue(), writer, indent + 1);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 * @throws IOException see writeObjectArrays
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements)
			throws IOException {
		StringWriter writer = new StringWriter();
		writeObjectArrays(elements, writer, 0);
		return writer.toString();
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.append("[");
		if (!elements.isEmpty()) {
			Iterator<? extends Map<String, ? extends Number>> it = elements.iterator();
			writeObject(it.next(), writer, indent + 1);
			while (it.hasNext()) {
				writer.append(",");
				writeObject(it.next(), writer, indent + 1);
			}
		}
		writer.append("\n");
		writeIndent("]\n", writer, indent);
		writer.flush();
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 * @throws IOException see writeArrayObjects()
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements)
			throws IOException {
		StringWriter writer = new StringWriter();
		writeArrayObjects(elements, writer, 0);
		return writer.toString();

	}

	/**
	 * Writes an object containing objects which contain arrays, in the format of
	 * the inverted index.
	 * 
	 * @param objObjArr the treemap which contains a treemap which contains a
	 *                  TreeSet
	 * @param path      the path to which the invIndex will be written
	 * @throws IOException if a path is not accessible by BufferedWriter
	 */
	public static void writeObjectObjetcArrary(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> objObjArr, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectObjectArrary(objObjArr, writer, 0);
		}
	}

	/**
	 * Writes an object containing objects which contain arrays, in the format of
	 * the inverted index.
	 * 
	 * @param objObjArr the object that is being written
	 * @param writer    writes
	 * @param indent    the desired indent
	 * @throws IOException if there is an issue with the writer
	 */
	public static void writeObjectObjectArrary(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> objObjArr, Writer writer,
			int indent) throws IOException {
		writer.append("{");
		if (!objObjArr.isEmpty()) {
			var it = objObjArr.entrySet().iterator();
			var entry = it.next();
			writer.append("\n");
			writeEntryObjectArrary(entry, writer, indent);
			while (it.hasNext()) {
				entry = it.next();
				writer.append(",");
				writer.append("\n");
				writeEntryObjectArrary(entry, writer, indent);
			}
		}
		writer.append("\n}");
		writer.flush();
	}

	/**
	 * Writes an entry which maps a String to a Map which maps Strings to
	 * Collections of numbers.
	 * 
	 * @param entry  like from a map
	 * @param writer writes
	 * @param indent desired indent
	 * @throws IOException may be thrown by the writer
	 */
	public static void writeEntryObjectArrary(
			Entry<String, ? extends Map<String, ? extends Collection<? extends Number>>> entry, Writer writer,
			int indent) throws IOException {
		writeQuote(entry.getKey(), writer, indent + 1);
		writer.append(": ");
		writeObjectArrays(entry.getValue(), writer, indent + 1);
	}

	/**
	 * Returns a String JSON representation of an object of objects of arrays.
	 * 
	 * @param objObjArr the object of objects of arrays
	 * @return String JSON representation of an object of objects of arrays
	 * @throws IOException if default out file results in a hard drive error
	 */
	public static String writeObjectObjectArray(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> objObjArr) throws IOException {
		StringWriter writer = new StringWriter();
		writeObjectObjectArrary(objObjArr, writer, 0);
		return writer.toString();
	}
	
	/**
	 * Writes an object containing arrays which contain objects NOTE: All
	 * non-Collection objects nested in the given object are Strings!
	 * 
	 * @param objArrObj an object containing arrays which contain objects
	 * @param path      the file being written to
	 * @throws IOException may be thrown by buffered writer
	 */
	public static void writeObjectArrayObject(Map<String, ? extends List<? extends Map<String, String>>> objArrObj, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrayObject(objArrObj, writer, 0);
		}
	}

	/**
	 * Writes an object containing arrays which contain objects NOTE: All
	 * non-Collection objects nested in the given object are Strings!
	 * 
	 * @param objArrObj an object containing arrays which contain objects
	 * @param writer    BufferedWriter
	 * @param indent    desired indent
	 * @throws IOException my be thrown by BufferedWriter
	 */
	public static void writeObjectArrayObject(Map<String, ? extends List<? extends Map<String, String>>> objArrObj,
			BufferedWriter writer, int indent) throws IOException {
		writer.append("{");
		if (!objArrObj.isEmpty()) {
			Iterator<? extends Entry<String, ? extends List<? extends Map<String, String>>>> it = objArrObj.entrySet().iterator();
			Entry<String, ? extends List<? extends Map<String, String>>> entry = it.next();
			writer.append("\n");
			writeEntryArrayObjectStrings(entry, writer, indent + 1);
			while (it.hasNext()) {
				entry = it.next();
				writer.append(",");
				writer.append("\n");
				writeEntryArrayObjectStrings(entry, writer, indent + 1);
			}
		}
		writer.append("\n}");
		writer.flush();
	}
	
	/**
	 * Writes an entry which maps a String to an ArrayList of TreeMaps which map
	 * Strings to Strings.
	 * 
	 * @param entry  like from a map
	 * @param writer writes
	 * @param indent desired indent
	 * @throws IOException may be thrown by the writer
	 */
	public static void writeEntryArrayObjectStrings(Entry<String, ? extends List<? extends Map<String, String>>> entry,
			BufferedWriter writer, int indent) throws IOException {
		writeQuote(entry.getKey(), writer, indent);
		writer.append(": ");
		writeArrayObjectStrings(entry.getValue(), writer, indent);
	}

	/**
	 * Writes an array of objects. NOTE: All non-Collection objects nested in the
	 * given object are Strings!
	 * 
	 * @param elements an array of objects
	 * @param writer   BufferedWriter
	 * @param indent   desired indent
	 * @throws IOException may be thrown by BufferedWriter
	 */
	public static void writeArrayObjectStrings(Collection<? extends Map<String, String>> elements, Writer writer,
			int indent) throws IOException {
		writer.append("[");
		if (!elements.isEmpty()) {
			writer.append("\n");
			writeIndent(writer, indent + 1);
			Iterator<? extends Map<String, String>> it = elements.iterator();
			writeObjectStrings(it.next(), writer, indent + 1);
			while (it.hasNext()) {
				writer.append(",\n");
				writeIndent(writer, indent + 1);
				writeObjectStrings(it.next(), writer, indent + 1);
			}
		}
		writer.append("\n");
		writeIndent("]", writer, indent);
		writer.flush();
	}

	/**
	 * Writes an object. NOTE: All non-Collection objects nested in the given object
	 * are Strings!
	 * 
	 * @param elements a string-to-string map
	 * @param writer   BufferedWriter
	 * @param indent   desired indent
	 * @throws IOException may be thrown by writer
	 */
	public static void writeObjectStrings(Map<String, String> elements, Writer writer, int indent) throws IOException {
		writer.append("{");
		if (!elements.isEmpty()) {
			Iterator<Entry<String, String>> it = elements.entrySet().iterator();
			// first element
			Entry<String, String> entry = it.next();
			writer.append("\n");
			writeEntryObject(entry, writer, indent);
			// remaining elements
			while (it.hasNext()) {
				entry = it.next();
				writer.append(",");
				writer.append("\n");
				writeEntryObject(entry, writer, indent);
			}
		}
		writer.append("\n");
		writeIndent("}", writer, indent);
		writer.flush();
	}

	/**
	 * Writes a simple entry with no internal nesting.
	 * 
	 * @param <K>    key type
	 * @param <V>    value type
	 * @param entry  like from a map
	 * @param writer writes
	 * @param indent desired indent
	 * @throws IOException may be thrown by the writer
	 */
	public static <K, V> void writeEntryObject(Entry<K, V> entry, Writer writer, int indent) throws IOException {
		writeQuote(entry.getKey().toString(), writer, indent + 1);
		writer.append(": ");
		writer.append(entry.getValue().toString());
	}
}
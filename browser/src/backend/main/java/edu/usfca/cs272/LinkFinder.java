package edu.usfca.cs272;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds HTTP(S) URLs from the anchor tags within HTML code.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Spring 2023
 */
public class LinkFinder {

	/**
	 * Returns a list of all the valid HTTP(S) URLs found in the HREF attribute of
	 * the anchor tags in the provided HTML. The URLs will be converted to absolute
	 * using the base URL and normalized (removing fragments and encoding special
	 * characters as necessary).
	 *
	 * Any URLs that are unable to be properly parsed (throwing an
	 * {@link MalformedURLException}) or that do not have the HTTP/S protocol will
	 * not be included.
	 * 
	 * Considering creating a version of this function that has a limit on how many
	 * links are added to the collection before returning. The build is more than
	 * fast enough so this is probably unnecessary.
	 *
	 * @param base the base URL used to convert relative URLs to absolute3
	 * @param html the raw HTML associated with the base URL
	 * @param urls the data structure to store found HTTP(S) URLs
	 *
	 * @see Pattern#compile(String)
	 * @see Matcher#find()
	 * @see Matcher#group(int)
	 * @see #normalize(URL)
	 * @see #isHttp(URL)
	 */
	public static void findUrls(URL base, String html, Collection<URL> urls) {
		Pattern preLinkPattern = Pattern.compile("(?i)<\\s*a[^><]+href\\s*=\\s*\\\"");
		Matcher preLinkMatcher = preLinkPattern.matcher(html);

		Pattern postLinkPattern = Pattern.compile("\\\"");
		Matcher postLinkMatcher = postLinkPattern.matcher(html);

		while (preLinkMatcher.find()) {
			int linkStart = preLinkMatcher.end();
			postLinkMatcher.find(linkStart);

			String url = html.substring(linkStart, postLinkMatcher.start());
			url = url.split("#")[0]; // maybe normalize instead

			// Check if proper http format
			try {
				if (!isHttp(new URL(url))) {
					continue;
				}
			} catch (MalformedURLException e) {
			}

			// add base if necessary & add to collection
			URL realUrl;
			try {
				realUrl = new URL(url);
				urls.add(realUrl);
			} catch (MalformedURLException e) {
				try {
					// base required?
					realUrl = new URL(base, url);
					urls.add(realUrl);
				} catch (MalformedURLException e1) {
					continue;
				}
			}
		}
	}

	/**
	 * Returns a list of all the valid HTTP(S) URLs found in the HREF attribute of
	 * the anchor tags in the provided HTML.
	 *
	 * @param base the base URL used to convert relative URLs to absolute3
	 * @param html the raw HTML associated with the base URL
	 * @return list of all valid HTTP(S) URLs in the order they were found
	 *
	 */
	public static ArrayList<URL> listUrls(URL base, String html) {
		ArrayList<URL> urls = new ArrayList<URL>();
		findUrls(base, html, urls);
		return urls;
	}

	/**
	 * Returns a set of all the unique valid HTTP(S) URLs found in the HREF
	 * attribute of the anchor tags in the provided HTML.
	 *
	 * @param base the base URL used to convert relative URLs to absolute3
	 * @param html the raw HTML associated with the base URL
	 * @return list of all valid HTTP(S) URLs in the order they were found
	 *
	 */
	public static HashSet<URL> uniqueUrls(URL base, String html) {
		HashSet<URL> urls = new HashSet<URL>();
		findUrls(base, html, urls);
		return urls;
	}

	/**
	 * Removes the fragment component of a URL (if present), and properly encodes
	 * the query string (if necessary).
	 *
	 * @param url the URL to normalize
	 * @return normalized URL
	 * @throws URISyntaxException    if unable to craft new URI
	 * @throws MalformedURLException if unable to craft new URL
	 */
	public static URL normalize(URL url) throws MalformedURLException, URISyntaxException {
		return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
				url.getQuery(), null).toURL();
	}

	/**
	 * Determines whether the URL provided uses the HTTP or HTTPS protocol.
	 *
	 * @param url the URL to check
	 * @return true if the URL uses the HTTP or HTTPS protocol
	 */
	public static boolean isHttp(URL url) {
		return url.getProtocol().matches("(?i)https?");
	}
}
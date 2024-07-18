package edu.usfca.cs272;

import java.net.URL;
import java.util.ArrayList;

import opennlp.tools.stemmer.Stemmer;

/**
 * 
 */
public class WebCrawler {

	/**
	 * URL crawl starts from.
	 */
	private final URL seed;

	/**
	 * Index that is built as web pages are crawled.
	 */
	private final ThreadSafeIndex index;

	/**
	 * Manages the threads that processes individual web pages
	 */
	private final WorkQueue workQueue;

	/**
	 * Ensures that the same web pages aren't crawled more than once.
	 */
	private final ArrayList<URL> processed;

	/**
	 * Sets the limit for the number of web pages that will be crawled.
	 */
	private int crawls;

	/**
	 * Constructor
	 * 
	 * @param url       the url the crawl will start from
	 * @param index     the index that will be built during the crawl
	 * @param workQueue manages threads that processes individual web pages
	 */
	public WebCrawler(URL url, ThreadSafeIndex index, WorkQueue workQueue) {
		seed = url;
		this.index = index;
		this.workQueue = workQueue;
		processed = new ArrayList<URL>();
		crawls = 0;
	}

	/**
	 * Initiates the crawl, shouldn't be used more than once on one site.
	 * 
	 * @param crawls the maximum number of web pages that will be processed during
	 *               this crawl.
	 */
	public void crawl(int crawls) {

		this.crawls = crawls - 1;

		processed.add(seed);
		Runnable task = new ProcessWebpage(seed);
		workQueue.execute(task);

		while (workQueue.isActive()) {
			// just wait
		}
		
		workQueue.finish();
	}

	/**
	 * 
	 */
	private class ProcessWebpage implements Runnable {

		/** file the url to be processed */
		private final URL url;

		/**
		 * @param url url to be processed
		 */
		public ProcessWebpage(URL url) {
			this.url = url;
		}

		@Override
		public void run() {

			// create local index
			ThreadSafeIndex local = new ThreadSafeIndex();

			// get html & remove comments & block elements
			String html = HtmlFetcher.fetch(url, 3);
			if (html == null) {
				return;
			}
			html = HtmlCleaner.stripBlockElements(html);

			// get links
			ArrayList<URL> links = LinkFinder.listUrls(url, html);
			for (URL link : links) {
				synchronized (processed) {
					if (!processed.contains(link) && crawls > 0) {
						processed.add(link);
						Runnable task = new ProcessWebpage(link);
						workQueue.execute(task);
						crawls--;
					}
				} // maybe move this duplicate code into its own function???
			}

			// clean & process for index
			String cleaned = HtmlCleaner.stripHtml(html);
			// String location = LinkFinder.normalize(url).toString();
			String location = url.toString().split("#")[0]; // maybe switch out with normalize
			Stemmer stemmer = FileStemmer.getDefaultStemmer();
			int htmlTextPosition = 1;
			String[] lines = cleaned.split("\n");

			for (String line : lines) {
				String[] parsed = FileStemmer.parse(line);
				for (int i = 0; i < parsed.length; i++) {
					String stemmed = stemmer.stem(parsed[i]).toString();
					if (stemmed.length() == 0) {
						continue;
					}
					local.add(stemmed, location, htmlTextPosition);
					htmlTextPosition++;
				}
			}
			synchronized (index) {
				index.addAll(local);
			}
		}
	}
}

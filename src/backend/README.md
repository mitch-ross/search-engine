Overview:
This guide provides instructions on how to use the edu.usfca.cs272.Driver program. The program is designed to build and search an inverted index from provided text files or URLs. It supports multithreading for enhanced performance and includes various command-line options for customization.

Command-Line Arguments:
The program accepts several command-line arguments in flag/value pairs. The primary flags and their purposes are listed below:

-text <path>: Specifies the path to the text file or directory to be indexed.
-html <url>: Specifies the URL to be crawled and indexed.
-query <path>: Specifies the path to the query file.
-index <path>: Specifies the path where the inverted index should be written.
-counts <path>: Specifies the path where the word counts should be written.
-results <path>: Specifies the path where the search results should be written.
-partial: Indicates that partial search should be performed.
-threads <number>: Specifies the number of threads to be used for multithreading.
-crawl <number>: Specifies the depth of crawling for the provided URL.

---------------- Steps to Use the Program ----------------

Prepare Command-Line Arguments: Prepare the command-line arguments based on your requirements. For example, to index text files and perform a search, you might use:
java edu.usfca.cs272.Driver -text /path/to/text/files -query /path/to/query/file -index /path/to/index.json -results /path/to/results.json

Run the Program: Execute the program with the prepared command-line arguments:
java edu.usfca.cs272.Driver <arguments>

Initialization: The program starts by initializing necessary data structures based on the provided arguments.

Building the Index:
If the -text flag is provided, the program reads the specified text files or directory and builds the inverted index.
If the -html flag is provided, the program starts a web crawler to index the specified URL, up to the specified crawl depth.

Performing Searches:
If the -query flag is provided, the program reads the specified query file and performs the search on the built index.
The -partial flag can be used to indicate partial matching in the search.

Writing Outputs:
If the -index flag is provided, the program writes the built inverted index to the specified path.
If the -counts flag is provided, the program writes the word counts to the specified path.
If the -results flag is provided, the program writes the search results to the specified path.

Multithreading:
If the -threads flag is provided or the -html flag is used, the program initializes a thread-safe index and a work queue with the specified number of threads for multithreading.
The ThreadSafeIndex and WorkQueue classes manage concurrent access and task execution.

Shutting Down:
After completing the tasks, the program shuts down the work queue if multithreading was used.

Elapsed Time:
The program calculates and prints the elapsed time for the entire operation.

---------------- Example Usage ----------------

Indexing Text Files and Searching:
To index text files located in /data/text and perform searches based on queries in /data/queries/query.txt, writing the index and results to JSON files:
java edu.usfca.cs272.Driver -text /data/text -query /data/queries/query.txt -index /output/index.json -results /output/results.json

Crawling a Website and Searching
To crawl a website with the URL http://example.com, perform searches based on queries in /data/queries/query.txt, using 10 threads for multithreading:
java edu.usfca.cs272.Driver -html http://example.com -query /data/queries/query.txt -index /output/index.json -results /output/results.json -threads 10 -crawl 3

Error Handling:
The program includes basic error handling for invalid file paths and URLs. If an error occurs during file reading or writing, or if the provided arguments are invalid, the program prints an error message to the standard error stream.

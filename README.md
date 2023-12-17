# google-algorithm

## Introduction

This project is a simplified recreation of Google's algorithm that manages all the websites it serves. This project is merely a proof of concept, so many shortcuts were taken to save money and resources. No hardware such as physical hardisks and servers were used, so a digital recreation of Hard disks were used instead stored as a HardDisk class. Additionally, only necessary features of a browser and searchEngine were implemented utilizing the respective interfaces.

## Project Structure

To understand this algorithm, I must explain the vital classes variables in its implementation:

#### `HardDisk`

This is a java class that simulates a physical hard disk. It implements a treeMap and not a hashTable to be able to store the hypothetical billions of entries in webpages and words if this were actually being used by google. The map keys are Longs and the values are InfoFiles. The keys represent the index of the first block of storage on the hard disk that a web page or word is stored at. The smallest block of memory on a hard disk is 512 bytes, so each index position represents and increment of at most 512 bytes. For example, website a takes up 1111 bytes and website b takes up 500 bytes. website a's InfoFile would be stored at index 0 because it is the first website on the hard disk, and website b would be stored at index 2 because website a uses up 2 full blocks and partially uses the 3rd block of 512 bytes.

#### `InfoFile`

This class is used to store all the data associated with each page and each word referenced on all the pages. The InfoFiles are the objects that are stored on the HardDisk objects. This class can be broken down into its its attributes:

data - Contains a string which is equal to either the url or the word that this InfoFile corresponds to.

influence - A Long equal to the final influence after the page-rank algorithm is done running

influenceTemp - A long equal to the temporary influence of the file during one iteration of the page-rank algorithm

indices - An array of index positions of where that InfoFile's data is referenced on other webpages. For example, webpages a and b are stored at index position 0 and 7 on the simulated hard disk. If b references a, then a's InfoFile.indices would contain a 0.

Final influence - Final influence after designated number of iterations of the ranking algorithm.

#### `wordDisk && pageDisk`

These variables are HardDisk objects that represent a harddisk for storing every word seen on every website and one for every url seen.

#### `urlToIndex && wordToIndex`

These variables are both maps whose keys are either the word or url as a String and whose values are the index positions of that url or word in its respective harddisk. There is one key difference between these two variables. urlToIndex implements a treeMap to enable it to handle the large volume of urls on the internet and wordToIndex implements a hashMap because there are far less words and sotring the hashMap in memory is not an issue.

## Algorithm Analysis

The complete algorithm can be broken down into 3 distinct sub-algorithms

#### `1. Collection`

The algorithm is provided a list of urls to begin with, and it initializes a Queue with these urls. Then, it preforms a breadth first search, indexing each page and every word on the page as it goes. The algorithm also has checks in place to ensure each url is only indexed once and each word is only indexed once per page. Indexing words and urls is simply the process of adding the word or url into the hardisk and adding the data with its corresponding index into urlToIndex or wordToIndex.

#### `2. Ranking`

This is a simplified version of google's page-rank algorithm. The algorithm works by assigning each page an initial influence of 1, and making each page give its entire influence proportionally to each webpage it references. This process is repeated for a number of iterations until the influences of each page converge to their final numbers. 

#### `3. Searching`

This portion of the algorithm will actually return the results to your search inquiry with the 10 pages that match your inquiry and have the highest influences. The 10 best search results are found by storing all of the search results into a priority queue that stores the lowest influence match at the root. By limiting the size of the queue to 10, the algorithm can compare any new matches to the root of the priorityQueue and do nothing if the match is less than the root. Otherwise, the algorithm will poll the root and offer the new match in its place, and the PriorityQueue comparator will place the new match in its correct ranking. 

## Usage

To work properly, the jsoup-1.8.3.jar file must be added as a dependency in the configurations. 

To see a demo of the algorithm, run the search file. For the purposes of the demo, a word disk and page disk file have already been provided. These files only contain the numbers 1 through 90 as words, and searching multiple numbers will return only multiples of those numbers. For example, searching 2 and 3 should return multiples of 2 and 3. However, it will only return the 10 results with the highest influence. If no matches exist, a blank array will be returned.

## Acknowledgments

This project was created for CSC220 at the University of Miami taught by Victor Milenkovic. Dr. Milenkovic created the framework and also provided guidance throughout the project. 

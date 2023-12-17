# google-algorithm

## Introduction

This project is a simplified recreation of Google's algorithm that manages all the websites it serves. This project is merely a proof of concept, so many shortcuts were taken to save money and resources. No hardware such as physical hardisks and servers were used, so a digital recreation of Hard disks were used instead stored as a HardDisk class. Additionally, only necessary features of a browser and searchEngine were implemented utilizing the respective interfaces.

## Project Structure

To understand this algorithm, I must explain the vital classes variables in its implementation:

### HardDisk

This is a java class that simulates a physical hard disk. It implements a treeMap and not a hashTable to be able to store the hypothetical billions of entries in webpages and words if this were actually being used by google. The map keys are Longs and the values are InfoFiles. The keys represent the index of the first block of storage on the hard disk that a web page or word is stored at. The smallest block of memory on a hard disk is 512 bytes, so each index position represents and increment of at most 512 bytes. For example, website a takes up 1111 bytes and website b takes up 500 bytes. website a's InfoFile would be stored at index 0 because it is the first website on the hard disk, and website b would be stored at index 2 because website a uses up 2 full blocks and partially uses the 3rd block of 512 bytes.

### InfoFile

This class is used to store all the data associated with each page and each word referenced on all the pages. This class can be broken down into its its attributes:

data - Contains a string which is equal to either the url or the word that this InfoFile corresponds to.

influence - A Long equal to the final influence after the page-rank algorithm is done running

influenceTemp - A long equal to the temporary influence of the file during one iteration of the page-rank algorithm

indices - An array of index positions of where that InfoFile's data is referenced on other webpages. For example, webpages a and b are stored at index position 0 and 7 on the simulated hard disk. If b references a, then a's InfoFile.indices would contain a 0.

Final influence - Final influence after designated number of iterations of the ranking algorithm.

### wordDisc && pageDisc

These variables are HardDisk objects that represent a harddisk for storing every word seen on every website and one for every url seen.

## Algorithm Analysis

## Usage

## Acknowledgments

import java.util.*;

public class NotGPT implements SearchEngine{
    HardDisk pageDisk = new HardDisk();
    HardDisk wordDisk = new HardDisk();
    Map<String, Long> urlToIndex = new TreeMap<>();
    Map<String, Long> wordToIndex = new HashMap<>();

    public class Vote implements Comparable<Vote> {

        public Long index;
        public double vote;

        public Vote (Long index, double vote) {
            this.index = index;
            this.vote = vote;
        }

        @Override
        /**
         * Used to sort the votes first by the index then by the influence.
         */
        public int compareTo(Vote o) {
            Long otherIndex = o.index;
            if (index != otherIndex) {
                return index.compareTo(otherIndex);
            }

            Double otherInfluence = pageDisk.get(o.index).influence;
            Double thisInfluence = pageDisk.get(index).influence;
            return thisInfluence.compareTo(otherInfluence);
        }
    }

    /**
     * Takes url string, gets the index of where it should be stored on pageDisk, then adds it at that position on pageDisk
     * Additionally, it adds the url and index it is stored at to the map urlToIndex
     * @param url url of the given website
     * @return the index at which it was stored at
     */
    public Long indexPage(String url) {
        Long index = pageDisk.newFile();
        urlToIndex.put(url, index);
        InfoFile file = new InfoFile(url);
        pageDisk.put(index, file);
        System.out.println("indexing page " + index + " " + file);
        return index;
    }

    /**
     * Takes word from page and gets index of where it should be stored on wordDisk
     * It also adds the word and index to wordToIndex
     * @param word word to be indexed
     * @return the index where it was stored on wordDisk
     */
    public Long indexWord(String word) {
        Long index = wordDisk.newFile();
        wordToIndex.put(word, index);
        InfoFile file = new InfoFile(word);
        wordDisk.put(index, file);
        System.out.println("indexing word " + index + " " + file);
        return index;
    }

    /**
     * Collect info from all web pages reachable from URLs in startingURLs.
     * @param browser implementation of SearchEngine
     * @param startingURLs list of startingURLs provided by the users
     */
    public void collect (Browser browser, List<String> startingURLs) {
        System.out.println("starting pages " + startingURLs);

        // Queue of page indices
        ArrayDeque<Long> pageIndices = new ArrayDeque<>();
        // Add each starting url to the queue if it is the first time it has occurred
        for (String url : startingURLs) {
            if(!urlToIndex.containsKey(url)) {
                Long index = indexPage(url);
                pageIndices.offer(index);
            }
        }

        // While the queue isn't empty
        while (!pageIndices.isEmpty()) {

            // Dequeue the index and get the corresponding url
            System.out.println("queue " + pageIndices);
            Long pageIndex = pageIndices.poll();
            InfoFile urlInfo = pageDisk.get(pageIndex);
            System.out.println("dequeued " + urlInfo);
            String url = urlInfo.data;

            // If the url was successfully loaded
            if(browser.loadPage(url)) {
                //Add the url to seen urls and get the list of urls referenced on the page
                Set<String> seenUrl = new TreeSet<>();
                List<String> urls = browser.getURLs();

                System.out.println("urls " + urls);

                Long urlIndex = urlToIndex.get(url);    //pageIndex of current loaded url

                // add each of these urls to the queue if it hasn't been seen yet
                for (String link : urls) {
                    if(!urlToIndex.containsKey(link)) {
                        pageIndices.offer(indexPage(link));
                    }
                    if(!seenUrl.contains(link)) {
                        seenUrl.add(link);
                        pageDisk.get(urlIndex).indices.add(urlToIndex.get(link));
                    }
                }
                System.out.println("updated page file " + pageDisk.get(urlIndex));

                //ditto the process for words on an individual page
                Set<String> seenWord = new TreeSet<>();
                List<String> words = browser.getWords();
                System.out.println("words " + words);

                for (String word : words) {
                    if(!wordToIndex.containsKey(word)) {
                        indexWord(word);
                    }
                    if (!seenWord.contains(word)) {
                        Long index = wordToIndex.get(word);
                        wordDisk.get(index).indices.add(urlIndex);
                        seenWord.add(word);
                        System.out.println("updated word file " + wordDisk.get(index));
                    }
                }
            }
        }
    }

    /**
     * Another method for assigning influence, much slower.
     * @param defaultInfluence value to prevent urls from disappearing
     */
    public void rankSlow (double defaultInfluence) {
        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            InfoFile file = entry.getValue();
            double influencePerIndex = (file.influence / file.indices.size());
            for (long index : file.indices) {
                InfoFile fileToAdd = pageDisk.get(index);
                fileToAdd.influenceTemp += influencePerIndex;
            }
        }

        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            InfoFile file = entry.getValue();
            file.influence = file.influenceTemp + defaultInfluence;
            file.influenceTemp = 0.0;
        }
    }

    /**
     * Ranks each page based on its influence determined by redistributing each page's
     * influence proportionally through a # of iterations
     * @param defaultInfluence value to prevent urls from disappearing
     */
    public void rankFast (double defaultInfluence) {

        // Create the list of votes for each page
        List<Vote> voteList = new ArrayList<>();
        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            InfoFile file = entry.getValue();
            for (long index : file.indices) {
                double influencePerIndex = (file.influence / file.indices.size());
                Vote newVote = new Vote(index, influencePerIndex);
                voteList.add(newVote);
            }
        }


        Collections.sort(voteList);

        ListIterator<Vote> voteIterator = voteList.listIterator();
        Vote vote = voteIterator.hasNext() ? voteIterator.next() : null;

        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            long index = entry.getKey();
            InfoFile file = entry.getValue();

            while(vote != null && vote.index == index) {
                file.influenceTemp += vote.vote;
                vote = voteIterator.hasNext() ? voteIterator.next() : null;
            }

            // Sets the InfoFile's influence after this iteration
            file.influence = file.influenceTemp + defaultInfluence;

            // reset the influenceTemp for the next iteration
            file.influenceTemp = 0.0;
        }
    }


    /**
     * Assign a priority to each web page using the PageRank algorithm.
     * @param fast boolean to determine whether to use rankFast or slow
     */
    public void rank (boolean fast) {
        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            InfoFile file = entry.getValue();
            file.influence = 1.0;
            file.influenceTemp = 0.0;
        }
        int empty = 0;
        for (Map.Entry<Long, InfoFile> entry : pageDisk.entrySet()) {
            InfoFile file = entry.getValue();
            if(file.indices.isEmpty()) {
                empty++;
            }
        }
        double defaultInfluence = (1.0 * empty) / pageDisk.size();

        if (fast) {
            for (int i = 0; i < 20; i++) {
                rankFast(defaultInfluence);
            }
        } else{
            for (int i = 0; i < 20; i++) {
                rankSlow(defaultInfluence);
            }
        }


    }



    /**
     * Search for up to numResults pages containing all searchWords
     * @param searchWords user inputted words to search
     * @param numResults number of results you want to find. Typically 10
     * @return array of results in order of decreasing importance (influence)
     */
    public String[] search (List<String> searchWords, int numResults) {

        // remove any words that have not been indexed from searchWords
        for (int i = 0; i < searchWords.size(); i ++) {
            String word = searchWords.get(i);
            if (wordToIndex.get(word) == null) {
                searchWords.remove(word);
            }
        }

        // If searchWords ends up empty, return an empty array.
        if (searchWords.isEmpty()) {
            return new String[0];
        }

        // Matching pages with the least popular page on the top of the
        // queue.
        PageComparator pageComparator = new PageComparator();
        PriorityQueue<Long> bestPageIndices = new PriorityQueue<>(pageComparator);

        // Iterator into list of page indices for each key word.
        Iterator<Long>[] wordPageIndexIterators = (Iterator<Long>[]) new Iterator[searchWords.size()];

        // Current page index in each list, just ``behind'' the iterator
        long[] currentPageIndex;

        // Set the listIterator to the indices array in its InfoFile for each word in searchWords that has been indexed
        for (int i = 0; i < wordPageIndexIterators.length; i++) {
            long wordIndex = wordToIndex.get(searchWords.get(i));
            List<Long> wordIndices = wordDisk.get(wordIndex).indices;
            wordPageIndexIterators[i] = wordIndices.listIterator();
        }

        currentPageIndex = new long[wordPageIndexIterators.length];

        // Move each iterator through the lists by moving the smaller indices to match the largest until one of the lists does not have a next.
        while (getNextPageIndices(currentPageIndex, wordPageIndexIterators)) {
            // If every word appears on the same page, add it to bestPageIndices
            if (allEqual(currentPageIndex)) {
                long index = currentPageIndex[0];
                System.out.println(pageDisk.get(index).data);
                if (bestPageIndices.size() < numResults) {
                    bestPageIndices.offer(index);
                } else {
                    InfoFile file = pageDisk.get(bestPageIndices.peek());
                    InfoFile newFile = pageDisk.get(index);
                    if (file.influence < newFile.influence) {
                        bestPageIndices.poll();
                        bestPageIndices.offer(index);
                    }
                }
            }
        }

        String[] results = new String[bestPageIndices.size()];

        Deque<Long> resultsStack = new ArrayDeque<>();

        while(!bestPageIndices.isEmpty()) {
            resultsStack.push(bestPageIndices.poll());
        }

        int i = 0;
        while(!resultsStack.isEmpty()) {
            InfoFile file = pageDisk.get(resultsStack.pop());
            results[i++] = file.data;
        }
        return results;
    }

    /**
     * Check if all elements in an array of long are equal.
     * @param array array of numbers
     * @return true if all are equal, false otherwise
     */
    private boolean allEqual(long[] array) {
        long cmp = array[0];
        for (int i = 1; i < array.length; i++) {
            if (cmp != array[i]) {
                return false;
            }
        }
        return true;
    }

    /** Get the largest element of an array of long.
     @param array an array of numbers
     @return largest element
     */
    private long getLargest(long[] array) {
        long max = array[0];
        for (int i = 1; i < array.length; i ++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /** If all the elements of currentPageIndex are equal,
     set each one to the next() of its Iterator,
     but if any Iterator hasNext() is false, just return false.

     Otherwise, do that for every element not equal to the largest element.

     Return true.

     @param currentPageIndex array of current page indices
     @param wordPageIndexIterators array of iterators with next page indices
     @return true if all page indices are updated, false otherwise
     */
    private boolean getNextPageIndices (long[] currentPageIndex,
                                        Iterator<Long>[] wordPageIndexIterators) {
        if (allEqual(currentPageIndex)) {
            for (int i = 0; i < currentPageIndex.length; i++) {
                Iterator<Long> iter = wordPageIndexIterators[i];

                if (!iter.hasNext()) {
                    return false;
                }
                currentPageIndex[i] = iter.next();
            }
            return true;
        }

        long max = getLargest(currentPageIndex);
        for (int i = 0; i < currentPageIndex.length; i++) {
            if (currentPageIndex[i] != max) {
                Iterator<Long> iter = wordPageIndexIterators[i];

                if (!iter.hasNext()) {
                    return false;
                }
                currentPageIndex[i] = iter.next();
            }
        }
        return true;
    }

    public class PageComparator implements Comparator<Long> {

        @Override
        public int compare(Long pageIndex1, Long pageIndex2) {
            double influence1 = pageDisk.get(pageIndex1).influence;
            double influence2 = pageDisk.get(pageIndex2).influence;
            return Double.compare(influence1, influence2);
        }
    }
}
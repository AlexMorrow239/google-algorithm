import java.util.ArrayList;
import java.util.List;

//import javax.xml.stream.events.StartDocument;

public class Collect {
  public static void main(String[] args) {
    Browser browser = new BetterBrowser();
    SearchEngine notGPT = new NotGPT();

    List<String> startingURLs = new ArrayList<>();
    startingURLs.add("http://www.cs.miami.edu/home/vjm/csc220/google/mary.html");
    // startingURLs.add("http://www.cs.miami.edu/home/vjm/csc220/google2/1.html");
      
    List<String> temp = new ArrayList<>();
    //noinspection ForLoopReplaceableByForEach
    for (int i = 0; i < startingURLs.size(); i++) {
      temp.add(BetterBrowser.reversePathURL(startingURLs.get(i)));
    }
    startingURLs = temp;

    notGPT.collect(browser, startingURLs);

    NotGPT g = (NotGPT) notGPT;
    System.out.println("map from URL to page index");
    System.out.println(g.urlToIndex);
    System.out.println("map from page index to page file");
    System.out.println(g.pageDisk);
    System.out.println("map from word to word index");
    System.out.println(g.wordToIndex);
    System.out.println("map from word index to word file");
    System.out.println(g.wordDisk);

    g.pageDisk.write("pagedisk.txt");
    g.wordDisk.write("worddisk.txt");
  }
}

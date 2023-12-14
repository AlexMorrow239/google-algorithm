import java.util.ArrayList;
import java.util.List;

/** This class represents the information stored in a file to record a
 * word or web page. */
public class InfoFile {
    public final String data; // URL or word
    public double influence = 0;
    public double influenceTemp = 0;
    List<Long> indices = new ArrayList<Long>(); // page indices

    public InfoFile (String data) {
	this.data = data;
    }

    public String toString () {
	return data + indices + influence;
    }
}


    

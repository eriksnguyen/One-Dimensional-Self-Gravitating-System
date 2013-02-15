package dataAnalysis;

import java.io.File;
import java.io.IOException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 *
 * @author Erik
 */
public class AnalysisMain {

    public static void main(String[] args) throws IOException, InvalidFormatException{
        String s = "C:\\Users\\Erik\\Documents\\School\\Research\\Dr. Miller\\New Set of Runs\\";
        File[] allFiles = new File(s).listFiles();
        
        for(File f: allFiles)
            Partition.virial(f.toString()+"\\");
    }
}

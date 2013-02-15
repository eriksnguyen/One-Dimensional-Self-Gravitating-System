/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataExtraction;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author Erik
 */
public class ExtractionFilter implements FileFilter{

    String[] okExtensions = new String[]{"xls","xlsx"};
    @Override
    public boolean accept(File pathname) {
        String s = pathname.toString().toLowerCase();
        for(String ext: okExtensions){
            if(s.endsWith(ext)) {
                return false;
            }
        }
        return true;
    }
    
}

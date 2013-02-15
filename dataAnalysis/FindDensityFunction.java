/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author Erik
 */
public class FindDensityFunction {

    
    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(new File("C:\\Users\\Erik\\Desktop\\data.dat"));
        int size = 0;
        while(sc.hasNextLine()){
            size++;
            sc.nextLine();
        }
        
        sc = new Scanner(new File("C:\\Users\\Erik\\Desktop\\data.dat"));
        Histogram[] h = new Histogram[size];
        int index = 0;
        while(sc.hasNextLine()){
            String[] temp = sc.nextLine().trim().split("\\s+");
            double[] values = new double[temp.length];
            for(int i = 0; i < temp.length; i++)
                values[i] = Double.parseDouble(temp[i]);
            h[index++] = new Histogram(values,0.3);
        }
        
        new AverageDensityFunctions(h);
        
        
    }
}

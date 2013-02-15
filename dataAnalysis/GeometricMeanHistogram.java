/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author Erik
 */
public class GeometricMeanHistogram {

    public static void main(String[] args) throws FileNotFoundException {
        String s = "C:\\Users\\Erik\\Documents\\School\\Research\\Dr. Miller\\"
                + "Extended Time Cold IC\\Period Dependency\\Fiducial\\3000 Particles 700 T\\";
        defineUnevenBins(20, readin(s, 700));
        defineDensity("C:\\Users\\Erik\\Desktop\\info.dat");
    }

    static void defineDensity(String file) throws FileNotFoundException{
        Scanner sc = new Scanner(new File(file));
        double[] density = new double[partitions.length-1];
        
        int numFiles = 0;
        while(sc.hasNextLine()){
            String[] temp = sc.nextLine().trim().split("\\s+");
            double[] pos = new double[temp.length];
            for(int i = 0; i < temp.length; i++)
                pos[i] = Double.parseDouble(temp[i]);
            
            Arrays.sort(pos);
            
            double right = partitions[1];
            int index = 0;
            int[] count = new int[density.length];
            for(int i = 0; i < pos.length; i++){
                if(pos[i] <= right){
                    count[index]++;
                }
                else{
                    count[++index]++;
                    right = partitions[index + 1];
                }
            }
            
            for(int i = 0; i < count.length; i++){
                density[i] += count[i]/(partitions[i+1]- partitions[i])/pos.length;
            }
            
            numFiles ++;
        }
        
        for(int i = 0; i < density.length; i++){
            double x = Math.sqrt(Math.abs(partitions[i])*Math.abs(partitions[i+1]));
            if(partitions[i] < 0 && partitions[i+1] < 0)
                x = -x;
            
            System.out.println(x + "\t" + density[i]/numFiles);
        }
    }
    /*
     * Reads in a desired line
     */
    static double[] readin(String loc, double t) throws FileNotFoundException {
        System.out.println("Reading in...");
        //Readin desired line
        String[] temp;
        try (Scanner sc = new Scanner(new File(loc + "positions.dat"))) {
            int lineNum = (int) (t * 10);
            while (lineNum-- > 0) {
                sc.nextLine();
            }
            temp = sc.nextLine().trim().split("\\s+");
        }

        //Turn into double array and sort
        double[] ret = new double[temp.length];
        for (int i = 0; i < temp.length; i++) {
            ret[i] = Double.parseDouble(temp[i]);
        }
        Arrays.sort(ret);

        System.out.println("Finished");
        return ret;
    }

    static double[] partitions;
    static void defineUnevenBins(int binSize, double[] positions) {
        double[] left = new double[positions.length/binSize],
                right = new double[positions.length/binSize];
        
        int count = 0, index = 0;
        for (int i = 0; i < positions.length; i++) {
            if (++count == 1) {
                left[index] = positions[i];
            } else if (count == 20) {
                right[index++] = positions[i];
                count = 0;
            }
        }
        
        partitions = new double[left.length + 1];
        //Empirically determined bin boundaries
        partitions[0] = -4.4723773;
        partitions[partitions.length - 1] = 4.2191730000001;
        
        for(int i = 1; i < left.length; i++){
            partitions[i] = (left[i]+right[i])/2;//Average the walls to make bin
        }
    }
}

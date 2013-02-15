package dataAnalysis;

import java.util.Arrays;

public class Histogram {

    public final double width;//Bin width
    public final int[] count;//number of particles in a given bin
    public final double[] binLoc, percent, density;//middle coordinate of bin
    public final int size;//number of data points binned
    public final int leftBarrier, rightBarrier;
    public Histogram(double[] data, double binWidth) {
        Arrays.sort(data);
        width = binWidth;
        size = data.length;

        //Absolute bounds that the histogram will fall under
        leftBarrier = (int) Math.floor(data[0] / width);
        rightBarrier = (int) Math.ceil(data[data.length - 1] / width);

        //Makes the arrays with the proper number of bins
        int arySize = rightBarrier - leftBarrier;
        count = new int[arySize];
        binLoc = new double[arySize];
        percent = new double[arySize];
        density = new double[arySize];

        bin(data, (leftBarrier + 1) * width);
    }

    /*
     * Essentially creates the histogram. @Pre-requisite: data must be sorted
     * @Variable: firstBin denotes the right side of the first bin
     */
    private void bin(double[] data, double firstBin) {

        //Count based histogram
        int index = 0;
        double bin = firstBin;
        for (double d : data) {
            if (d > bin) {
                index++;//go to next bin
                bin += width;
            }
            count[index]++;
        }

        double firstBinLoc = firstBin - width / 2;        
        for (int i = 0; i < count.length; i++) {
            //Find the middle of every bin and store it's coordinate
            binLoc[i] = firstBinLoc + i * width;
            
            //Percentage based histogram and density curve
            density[i] = (percent[i] = count[i] / (double) size) / width;
        }
    }
    
    @Override
    public String toString(){
        String ret = "";
        for(int i = 0; i < density.length; i++)
            ret+= binLoc[i] + "\t"+density[i]+"\n";
        return ret;
    }
}

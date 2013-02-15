/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAnalysis;

import java.util.Arrays;

/**
 *
 * @author Erik
 */
public class AverageDensityFunctions {
    
    public AverageDensityFunctions(Histogram...vars){
        int furthestL = Integer.MAX_VALUE, furthestR = Integer.MIN_VALUE;
        for(Histogram h: vars){
            if(furthestL > h.leftBarrier)
                furthestL = h.leftBarrier;
            if(furthestR < h.rightBarrier)
                furthestR = h.rightBarrier;
        }
        
        double[][] values = new double[vars.length][furthestR-furthestL];
        
        for(int i = 0; i < vars.length; i++){
            int begin = vars[i].leftBarrier - furthestL;
            for(int j = begin; j < vars[i].rightBarrier - furthestL; j++)
                values[i][j] = vars[i].density[j-begin];
        }
       
        double[] xLoc = new double[furthestR - furthestL];
        double start = furthestL*vars[0].width + vars[0].width/2;
        for(int i = 0; i < xLoc.length; i++)
            xLoc[i] = start + vars[0].width*i;
        doStats(xLoc, values);
    }
    
    private void doStats(double[] abscissa, double[][] valueSet){
        for(int i = 0; i < valueSet[0].length; i++){
            int numSet = valueSet.length;
            double average = 0;
            for(int j = 0; j < numSet; j++)
                average += valueSet[j][i];
            average/= numSet;
            double variance = 0;
            for(int j = 0; j < numSet; j++)
                variance += Math.pow(average - valueSet[j][i],2);
            variance /= numSet - 1;
            double stdDeviation = Math.sqrt(variance);
            
            System.out.println(abscissa[i] + "\t" + average + "\t"+stdDeviation);
        }
    }
}

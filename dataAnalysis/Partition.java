package dataAnalysis;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Erik
 */
public class Partition {

    private static double percent;//holds area percentage
    private static String rootDir;//root directory of files
    //Indeces of particles of respective regions
    private static ArrayList<Integer> coreBlue, haloGreen, haloRed;
    private static Font f;//Cell Font
    private static Workbook workbook;//workbook for output
    private static CellStyle style;

    /*
     * Finds the average energies of regions over time
     */
    public static void average(String dir, int p) throws IOException, InvalidFormatException {

        percent = p / 100.;//true percentage
        rootDir = dir;
        coreBlue = new ArrayList();
        haloGreen = new ArrayList();
        haloRed = new ArrayList();

        //Setup workbook
        workbook = new XSSFWorkbook();
        
        //Create font and style
        f = workbook.createFont();
        f.setFontName("Calibri");
        f.setFontHeightInPoints((short) 11);
                
        style = workbook.createCellStyle();
        style.setFont(f);
        
        System.out.println(dir + " :");
        
        //Figures out which particles belong under which fields
        makePartition();

        System.out.println("Partition formed. Calculating averages...");
        
        //Writes out the averages
        findAvg(workbook.createSheet("Average Energies " + p + " %"));
        
        System.out.println("Completed");
    }


    private static void findAvg(Sheet s) throws IOException {
        try (Scanner e = new Scanner(new File(rootDir + "energies.dat"))) {
            String[] temp;
            Row r; 
            Cell c;
            for (int row = 0; e.hasNextLine(); row++) {
                //Readin next line
                temp = e.nextLine().trim().split("\\s+");

                double core = 0.0, green = 0.0, red = 0.0;
                
                for(int index : coreBlue)//Sum coreBlue energies
                    core += Double.parseDouble(temp[index]);
                for(int index : haloGreen)//Sum G corner energies
                    green += Double.parseDouble(temp[index]);
                for(int index : haloRed)//Sum R corner Energies
                    red += Double.parseDouble(temp[index]);
                
                //Average the energies
                core /= coreBlue.size();
                green /= haloGreen.size();
                red /= haloRed.size();
                
                r = s.createRow(row);//Make the row
                
                //Print out coreBlue, green and red
                c = r.createCell(0);
                c.setCellValue(core);
                c.setCellStyle(style);
                
                c = r.createCell(1);
                c.setCellValue(green);
                c.setCellStyle(style);
                
                c = r.createCell(2);
                c.setCellValue(red);
                c.setCellStyle(style);
                
            }

            //Write out workbook
            workbook.write(new FileOutputStream(rootDir + "Energy.xlsx"));
        }
    }

    private static double maxX, maxV;

    private static void makePartition() throws FileNotFoundException {

        String[] pos, vel;
        try (Scanner x = new Scanner(new File(rootDir + "positions.dat"))) {
            pos = x.nextLine().trim().split("\\s+");
        }
        try (Scanner v = new Scanner(new File(rootDir + "velocities.dat"))) {
            vel = v.nextLine().trim().split("\\s+");
        }

        double[] xAry = new double[pos.length],
                vAry = new double[vel.length];
        for (int i = 0; i < xAry.length; i++) {
            xAry[i] = Double.parseDouble(pos[i]);
            vAry[i] = Double.parseDouble(vel[i]);
        }

        maxX = 0;
        maxV = 0;
        
        findRange(xAry, vAry);//sets range

        //sets boundaries
        double alpha = maxX * Math.sqrt(percent),
                beta = maxV * Math.sqrt(percent),
                _alpha = maxX * (1 - Math.sqrt(percent)),
                _beta = maxV * (1 - Math.sqrt(percent));

        for (int i = 0; i < xAry.length; i++) {

            if (!(inRange(xAry[i], _alpha) || inRange(vAry[i], _beta))) {
                if (xAry[i] * vAry[i] > 0) {
                    haloRed.add(i);
                } else {
                    haloGreen.add(i);
                }
            } else if (inRange(xAry[i], alpha) && inRange(vAry[i], beta)) {
                coreBlue.add(i);
            }
        }
    }

    /*
     * @Pre-requisite: range > 0; Returns true if the double falls within
     * [-range,range].
     */
    private static boolean inRange(double d, double range) {
        return (-range < d) && (d < range);
    }

    /*
     * Assuming that the particles form a rectangle in mu space, this finds the
     * dimensions
     */
    private static void findRange(double[] x, double[] v) {
        for (int i = 0; i < x.length; i++) {
            maxX = Math.max(maxX, Math.abs(x[i]));
            maxV = Math.max(maxV, Math.abs(v[i]));
        }
    }

    /*
     * 
     */
    public static void virial(String fileDir) throws IOException, InvalidFormatException {
        workbook = WorkbookFactory.create(new FileInputStream(fileDir + "Energy.xlsx"));

        f = workbook.createFont();
        f.setFontName("Calibri");
        f.setFontHeightInPoints((short) 11);

        //Setup style
        style = workbook.createCellStyle();
        style.setFont(f);
        
        System.out.println("Virial Ratios being calculated...");
        
        Sheet vR = workbook.createSheet("Virial Ratio");

        try (Scanner eTotal = new Scanner(new File(fileDir + "energies.dat"))) {
            try (Scanner vel = new Scanner(new File(fileDir + "velocities.dat"))) {
                Row r;
                Cell c;
                String[] energy, velocity;
                for (int rowIndex = 0; eTotal.hasNextLine(); rowIndex++) {
                    energy = eTotal.nextLine().trim().split("\\s+");
                    velocity = vel.nextLine().trim().split("\\s+");

                    r = vR.createRow(rowIndex);
                    c = r.createCell(0);
                    c.setCellStyle(style);

                    double K = 0.0, E = 0.0;
                    for (int i = 0; i < velocity.length; i++) {
                        K += Math.pow(Double.parseDouble(velocity[i]), 2);
                        E += Double.parseDouble(energy[i]);
                    }
                    K /= 2000;

                    //Remember that Usys = 0.5 * sum(Uparticle)
                    double U = (E - K)/2;

                    c.setCellValue(2 * K / U);
                }
            }
        }

        workbook.write(new FileOutputStream(fileDir + "Energy.xlsx"));
        
        System.out.println("Virial Ratios finished");
    }
}
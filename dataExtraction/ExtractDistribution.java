/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataExtraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Erik
 */
public class ExtractDistribution {

    File[] allDir;
    Workbook workbook;
    CellStyle style;
    int time;

    public ExtractDistribution(String dir, int t) throws IOException {
        allDir = new File(dir).listFiles(new ExtractionFilter());

        time = t;

        workbook = new XSSFWorkbook();

        Font f = workbook.createFont();
        f.setFontName("Calibri");
        f.setFontHeightInPoints((short) 11);

        style = workbook.createCellStyle();
        style.setFont(f);

        writeSheets(workbook.createSheet("Positions"),
                workbook.createSheet("Velocities"),
                workbook.createSheet("Energies"));

        workbook.write(new FileOutputStream(dir + time + " tc.xlsx"));
    }

    private void writeSheets(Sheet s1, Sheet s2, Sheet s3) throws FileNotFoundException {
        int row = 0;
        for (File f : allDir) {
            write(s1, row, findData(f.toString() + "\\positions.dat"));
            write(s2, row, findData(f.toString() + "\\velocities.dat"));
            write(s3, row, findData(f.toString() + "\\energies.dat"));
            row++;
        }
    }

    private void write(Sheet s, int row, String[] data){
        Row r = s.createRow(row);
        Cell c;
        for(int i = 0; i < data.length; i++){
            c = r.createCell(i);
            c.setCellValue(Double.parseDouble(data[i]));
            c.setCellStyle(style);
        }
    }
    private String[] findData(String fileLoc) throws FileNotFoundException {
        try (Scanner sc = new Scanner(new File(fileLoc))) {
            for (int i = 0; i < time; i++) {
                sc.nextLine();
            }
            return sc.nextLine().trim().split("\\s+");
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String s = "C:\\Users\\Erik\\Desktop\\SimData\\Virial Ratio 2.5\\";
        int[] ary = {0};
        for(int i: ary) {
            new ExtractDistribution(s, i);
        }
    }
}

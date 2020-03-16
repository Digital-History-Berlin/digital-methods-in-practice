/*
 * The Tracer project is a result of Marco Büchler's PhD in computer science about
 * 'Computational Aspects of the Historical Text Re-use and Knowledge Transfer'.
 * Tracer includes a six level architecture consisting of
 *      - Preprocessing (eu.etrap.tracer.preprocessing)
 *      - Training (eu.etrap.tracer.featuring)
 *      - Selection (eu.etrap.tracer.selection)
 *      - Linking (eu.etrap.tracer.linking)
 *      - Scoring (eu.etrap.tracer.scoring)
 *      - Postprocessing (eu.etrap.tracer.postprocessing).
 *
 * Tracer is provided both by open source and open access for non-commercial use.
 *
 * If you have any questions, please, send me an e-mail to mbuechler@etrap.eu.
 *
 * Marco Büchler
 * eTRAP Research Group 
 * Institute for Computer Science
 * Georg-August-Universiy Göttingen
 * Papendiek 16
 * 37073 Göttingen, Germany
 * web: http://www.etrap.eu
 *
 * This project has begun at the Leipzig e-Humanities Research Group at Leipzig
 * University, Germany is now continued as part of the eTRAP research group 
 * (http://www.etrap.eu).
 *
 */


package eu.etrap.tracer.phd.cleaning.evaluation.bible;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

/**
 *
 * @author mbuechler
 */
public class BibleEvaluatorMain {

    private static Vector<String> objFiles2Scan = null;
    private static String strFileExtension = null;
    private static String strThreshold = null;
    private static HashMap<String, HashMap<String, double[]>> objMatrix = null;
    private static String aryBiblesAbbr[] = new String[]{"", "ASV", "BBE", "DBY", "KJV", "WEB", "WBS", "YLT"};
    private static String aryIndex[] = new String[49 + 1];
    private static double noVerses = 28632.0;

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException if file is not found
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strDataDirectory = args[0]; //"/home/mbuechler/Dissertation/Results"; //args[0];
        strFileExtension = "score";
        strThreshold = "Threshold=0.6";


        execute(strDataDirectory);

        int size = objFiles2Scan.size();

        System.out.println("Number of score files=" + size);

        objMatrix = new HashMap<String, HashMap<String, double[]>>();

        for (int i = 0; i < size; i++) {
            processFile(objFiles2Scan.get(i));
        }

        System.out.println("\n\nProcessing finished.\nEvaluated data sets=" + objMatrix.size());

        System.out.println("\nProcessing output files ...");
        outputPrecision();
        outputRecall();
        outputFMeasure();

        outputTextReuseCompression();
        outputTextReuseCompressionModified();

        outputNoisyChannelEvaluation();
        outputNoisyChannelEvaluationModified();
    }

    protected static void outputNoisyChannelEvaluation() throws IOException {
        BufferedWriter objWriter = new BufferedWriter(new FileWriter("/tmp/Bible.NoisyChannelEvaluation.tex"));

        double aryOutput[][] = new double[49 + 1][12 + 1];

        Iterator<String> objIter = objMatrix.keySet().iterator();
        double max = 0;
        while (objIter.hasNext()) {
            String strKey = objIter.next();

            String strSplit[] = strKey.split("_");
            int idx_i = Integer.parseInt(strSplit[1]);
            int idx_j = Integer.parseInt(strSplit[2]);
            int column = (idx_i - 1) * 4 + idx_j;

            HashMap<String, double[]> objData = objMatrix.get(strKey);
            Iterator<String> objIter2 = objData.keySet().iterator();

            while (objIter2.hasNext()) {
                String strKey2 = objIter2.next();
                int row = getColumn(strKey2);

                if (row != -1) {
                    double aryData[] = objData.get(strKey2);
                    aryOutput[row][column] = 10 * Math.log10(aryData[0] / Math.max(1, 0));
                    max = Math.max(max, aryOutput[row][column]);
                }
            }
        }

        for (int i = 1; i <= 49; i++) {
            String aryText[] = aryIndex[i].split(" vs. ");

            if (!aryText[0].trim().equals(aryText[1].trim())) {
                String strLine = "\\footnotesize " + aryIndex[i] + " &";

                for (int j = 1; j <= 12; j++) {
                    strLine += "  \\footnotesize " + outputColorNSE(aryOutput[i][j], Math.max(max, 10 * Math.log10(noVerses))) + " & ";
                }

                strLine = strLine.substring(0, strLine.length() - 3);
                strLine += "\\\\ \\hline";

                objWriter.write(strLine.trim() + "\n");
            }
        }

        objWriter.flush();
        objWriter.close();
    }

    protected static void outputNoisyChannelEvaluationModified() throws IOException {
        BufferedWriter objWriter = new BufferedWriter(new FileWriter("/tmp/Bible.NoisyChannelEvaluationModified.tex"));

        double aryOutput[][] = new double[49 + 1][12 + 1];

        Iterator<String> objIter = objMatrix.keySet().iterator();

        while (objIter.hasNext()) {
            String strKey = objIter.next();

            String strSplit[] = strKey.split("_");
            int idx_i = Integer.parseInt(strSplit[1]);
            int idx_j = Integer.parseInt(strSplit[2]);
            int column = (idx_i - 1) * 4 + idx_j;

            HashMap<String, double[]> objData = objMatrix.get(strKey);
            Iterator<String> objIter2 = objData.keySet().iterator();

            while (objIter2.hasNext()) {
                String strKey2 = objIter2.next();
                int row = getColumn(strKey2);

                if (row != -1) {
                    double aryData[] = objData.get(strKey2);
                    aryOutput[row][column] = 10 * Math.log10(aryData[1] / Math.max(1, 0));

                }
            }
        }

        for (int i = 1; i <= 49; i++) {
            String aryText[] = aryIndex[i].split(" vs. ");

            if (!aryText[0].trim().equals(aryText[1].trim())) {
                String strLine = "\\footnotesize " + aryIndex[i] + " &";

                for (int j = 1; j <= 12; j++) {
                    strLine += "  \\footnotesize " + outputColorNSE(aryOutput[i][j], 10 * Math.log10(noVerses)) + " & ";
                }

                strLine = strLine.substring(0, strLine.length() - 3);
                strLine += "\\\\ \\hline";

                objWriter.write(strLine.trim() + "\n");
            }
        }

        objWriter.flush();
        objWriter.close();
    }

    private static String outputColorNSE(double dblValue, double max) {
        Locale.setDefault(Locale.US);
        DecimalFormat df = new DecimalFormat("00.0");
        DecimalFormat df2 = new DecimalFormat("0.0000000");

        String strOutput = "\\cellcolor[gray]{" + df2.format((1 - dblValue / max))
                + "} \\color{";

        if (dblValue / max <= 0.5) {
            strOutput += "black";
        } else {
            strOutput += "white";
        }

        strOutput += "}{" + df.format(dblValue) + "}";

        return strOutput;
    }

    protected static void outputTextReuseCompression() throws IOException {
        BufferedWriter objWriter = new BufferedWriter(new FileWriter("/tmp/Bible.TextReuseCompression.tex"));

        double aryOutput[][] = new double[49 + 1][12 + 1];

        Iterator<String> objIter = objMatrix.keySet().iterator();

        double max = 0.0;
        while (objIter.hasNext()) {
            String strKey = objIter.next();

            String strSplit[] = strKey.split("_");
            int idx_i = Integer.parseInt(strSplit[1]);
            int idx_j = Integer.parseInt(strSplit[2]);
            int column = (idx_i - 1) * 4 + idx_j;

            HashMap<String, double[]> objData = objMatrix.get(strKey);
            Iterator<String> objIter2 = objData.keySet().iterator();

            while (objIter2.hasNext()) {
                String strKey2 = objIter2.next();
                int row = getColumn(strKey2);

                if (row != -1) {
                    double aryData[] = objData.get(strKey2);
                    aryOutput[row][column] = aryData[2] / (noVerses * noVerses - noVerses);
                    max = Math.max(max, aryOutput[row][column]);
                }
            }
        }

        for (int i = 1; i <= 49; i++) {
            String aryText[] = aryIndex[i].split(" vs. ");

            if (!aryText[0].trim().equals(aryText[1].trim())) {
                String strLine = "\\footnotesize " + aryIndex[i] + " &";

                for (int j = 1; j <= 12; j++) {
                    strLine += "  \\footnotesize " + outputColor(aryOutput[i][j], max) + " & ";
                }

                strLine = strLine.substring(0, strLine.length() - 3);
                strLine += "\\\\ \\hline";

                objWriter.write(strLine.trim() + "\n");
            }
        }

        objWriter.flush();
        objWriter.close();
    }

    private static String outputColor(double dblValue, double max) {
        Locale.setDefault(Locale.US);
        DecimalFormat df = new DecimalFormat("0.00");

        String strOutput = "\\cellcolor[gray]{" + (1 - 0.99 * dblValue / max)
                + "} \\color{";

        if (0.99 * dblValue / max <= 0.5) {
            strOutput += "black";
        } else {
            strOutput += "white";
        }

        strOutput += "}{" + df.format(-1 * Math.log10(dblValue)) + "}";

        return strOutput;
    }

    protected static void outputTextReuseCompressionModified() throws IOException {
        BufferedWriter objWriter = new BufferedWriter(new FileWriter("/tmp/Bible.TextReuseCompressionModified.tex"));

        double aryOutput[][] = new double[49 + 1][12 + 1];

        Iterator<String> objIter = objMatrix.keySet().iterator();

        while (objIter.hasNext()) {
            String strKey = objIter.next();

            String strSplit[] = strKey.split("_");
            int idx_i = Integer.parseInt(strSplit[1]);
            int idx_j = Integer.parseInt(strSplit[2]);
            int column = (idx_i - 1) * 4 + idx_j;

            HashMap<String, double[]> objData = objMatrix.get(strKey);
            Iterator<String> objIter2 = objData.keySet().iterator();

            while (objIter2.hasNext()) {
                String strKey2 = objIter2.next();
                int row = getColumn(strKey2);

                if (row != -1) {
                    double aryData[] = objData.get(strKey2);
                    aryOutput[row][column] = aryData[3] / noVerses;

                }
            }
        }

        for (int i = 1; i <= 49; i++) {
            String aryText[] = aryIndex[i].split(" vs. ");

            if (!aryText[0].trim().equals(aryText[1].trim())) {
                String strLine = "\\footnotesize " + aryIndex[i] + " &";

                for (int j = 1; j <= 12; j++) {
                    strLine += "  \\footnotesize " + outputColor(aryOutput[i][j]) + " & ";
                }

                strLine = strLine.substring(0, strLine.length() - 3);
                strLine += "\\\\ \\hline";

                objWriter.write(strLine.trim() + "\n");
            }
        }

        objWriter.flush();
        objWriter.close();
    }

    protected static void outputRecall() throws IOException {
        BufferedWriter objWriter = new BufferedWriter(new FileWriter("/tmp/Bible.Recall.tex"));

        double aryOutput[][] = new double[49 + 1][12 + 1];

        Iterator<String> objIter = objMatrix.keySet().iterator();

        while (objIter.hasNext()) {
            String strKey = objIter.next();

            String strSplit[] = strKey.split("_");
            int idx_i = Integer.parseInt(strSplit[1]);
            int idx_j = Integer.parseInt(strSplit[2]);
            int column = (idx_i - 1) * 4 + idx_j;

            HashMap<String, double[]> objData = objMatrix.get(strKey);
            Iterator<String> objIter2 = objData.keySet().iterator();

            while (objIter2.hasNext()) {
                String strKey2 = objIter2.next();
                int row = getColumn(strKey2);

                if (row != -1) {
                    double aryData[] = objData.get(strKey2);
                    aryOutput[row][column] = aryData[1] / noVerses;

                }
            }
        }

        for (int i = 1; i <= 49; i++) {
            String aryText[] = aryIndex[i].split(" vs. ");

            if (!aryText[0].trim().equals(aryText[1].trim())) {
                String strLine = "\\footnotesize " + aryIndex[i] + " &";

                for (int j = 1; j <= 12; j++) {
                    strLine += "  \\footnotesize " + outputColor(aryOutput[i][j]) + " & ";
                }

                strLine = strLine.substring(0, strLine.length() - 3);
                strLine += "\\\\ \\hline";

                objWriter.write(strLine.trim() + "\n");
            }
        }

        objWriter.flush();
        objWriter.close();
    }

        protected static void outputFMeasure() throws IOException {
        BufferedWriter objWriter = new BufferedWriter(new FileWriter("/tmp/Bible.FMeasure.tex"));

        double aryOutput[][] = new double[49 + 1][12 + 1];

        Iterator<String> objIter = objMatrix.keySet().iterator();

        while (objIter.hasNext()) {
            String strKey = objIter.next();

            String strSplit[] = strKey.split("_");
            int idx_i = Integer.parseInt(strSplit[1]);
            int idx_j = Integer.parseInt(strSplit[2]);
            int column = (idx_i - 1) * 4 + idx_j;

            HashMap<String, double[]> objData = objMatrix.get(strKey);
            Iterator<String> objIter2 = objData.keySet().iterator();

            while (objIter2.hasNext()) {
                String strKey2 = objIter2.next();
                int row = getColumn(strKey2);

                if (row != -1) {
                    double aryData[] = objData.get(strKey2);
                    double dblP = aryData[1] / aryData[0];
                    double dblR = aryData[1] / noVerses;
                    double dblF = 2* dblP*dblR /(dblP+dblR);
                    aryOutput[row][column] = dblF;

                }
            }
        }

        for (int i = 1; i <= 49; i++) {
            String aryText[] = aryIndex[i].split(" vs. ");

            if (!aryText[0].trim().equals(aryText[1].trim())) {
                String strLine = "\\footnotesize " + aryIndex[i] + " &";

                for (int j = 1; j <= 12; j++) {
                    strLine += "  \\footnotesize " + outputColor(aryOutput[i][j]) + " & ";
                }

                strLine = strLine.substring(0, strLine.length() - 3);
                strLine += "\\\\ \\hline";

                objWriter.write(strLine.trim() + "\n");
            }
        }

        objWriter.flush();
        objWriter.close();
    }
    
    protected static void outputPrecision() throws IOException {
        BufferedWriter objWriter = new BufferedWriter(new FileWriter("/tmp/Bible.Precision.tex"));

        double aryOutput[][] = new double[49 + 1][12 + 1];

        Iterator<String> objIter = objMatrix.keySet().iterator();

        while (objIter.hasNext()) {
            String strKey = objIter.next();

            String strSplit[] = strKey.split("_");
            int idx_i = Integer.parseInt(strSplit[1]);
            int idx_j = Integer.parseInt(strSplit[2]);
            int column = (idx_i - 1) * 4 + idx_j;

            HashMap<String, double[]> objData = objMatrix.get(strKey);
            Iterator<String> objIter2 = objData.keySet().iterator();

            while (objIter2.hasNext()) {
                String strKey2 = objIter2.next();
                int row = getColumn(strKey2);

                if (row != -1) {
                    double aryData[] = objData.get(strKey2);
                    aryOutput[row][column] = aryData[1] / aryData[0];

                }
            }
        }

        for (int i = 1; i <= 49; i++) {
            String aryText[] = aryIndex[i].split(" vs. ");

            if (!aryText[0].trim().equals(aryText[1].trim())) {
                String strLine = "\\footnotesize " + aryIndex[i] + " &";

                for (int j = 1; j <= 12; j++) {
                    strLine += "  \\footnotesize " + outputColor(aryOutput[i][j]) + " & ";
                }

                strLine = strLine.substring(0, strLine.length() - 3);
                strLine += "\\\\ \\hline";

                objWriter.write(strLine.trim() + "\n");
            }
        }

        objWriter.flush();
        objWriter.close();
    }

    private static String outputColor(double dblValue) {

        DecimalFormat df = new DecimalFormat("0.00");

        String strOutput = "\\cellcolor[gray]{" + (1 - dblValue)
                + "} \\color{";

        if (dblValue <= 0.5) {
            strOutput += "black";
        } else {
            strOutput += "white";
        }

        strOutput += "}{" + df.format(dblValue) + "}";

        return strOutput;
    }

    private static int getColumn(String strKey) {
        int row = 0;
        String strSplit[] = strKey.split("\t");
        int intBible1 = Integer.parseInt(strSplit[0]);
        int intBible2 = Integer.parseInt(strSplit[1]);

        row = (intBible1 - 1) * 7 + intBible2;

        aryIndex[row] = aryBiblesAbbr[intBible1] + " vs. " + aryBiblesAbbr[intBible2];

        return row;
    }

    protected static void processFile(String strFileName) throws FileNotFoundException, IOException {
        String strSplit[] = strFileName.split("/");
        int index1 = strSplit[9].indexOf("WLP:") + 4;
        int index2 = strSplit[9].indexOf("_redwo");

        String strMatrixID = getMatrixID(strFileName);
        System.out.println("Processing file: " + strMatrixID + "\t"
                + strSplit[9].substring(index1, index2) + "\t" + strSplit[10]);

        BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
        String strLine = null;

        HashMap<String, double[]> objData = new HashMap<String, double[]>();

        int counter = 0;
        while ((strLine = objReader.readLine()) != null) {
            counter++;
            String strSplit2[] = strLine.split("\t");
            int intVerse1 = Integer.parseInt(strSplit2[0]) % 1000000;
            int intVerse2 = Integer.parseInt(strSplit2[1]) % 1000000;
            int intBible1 = Integer.parseInt(strSplit2[0]) / 1000000;
            int intBible2 = Integer.parseInt(strSplit2[1]) / 1000000;
            String strKey = intBible1 + "\t" + intBible2;

            double[] aryData = objData.get(strKey);

            if (aryData == null) {
                aryData = new double[4];
            }

            aryData[0]++;
            aryData[2] += Double.parseDouble(strSplit2[3]);

            if (intVerse1 == intVerse2) {
                aryData[1]++;
                aryData[3] += Double.parseDouble(strSplit2[3]);
            }

            objData.put(strKey, aryData);

            /*for( int i=0; i<aryData.length; i++ ){
            System.out.print( aryData[i] + "\t" );
            }   
            System.out.println( "" );*/
        }

        System.out.println("size=" + counter);
        System.out.println("# bibles=" + objData.size());

        objMatrix.put(strMatrixID, objData);

        objReader.close();
    }

    private static String getMatrixID(String strFileName) {
        int ma_index_i = 0;
        int ma_index_j = 0;

        if (strFileName.contains("lem=false") && strFileName.contains("syn=false")
                && strFileName.contains("ssim=false")) {
            ma_index_i = 1;
        }

        if (strFileName.contains("ssim=true")) {
            ma_index_i = 2;
        }

        if (strFileName.contains("lem=true") && strFileName.contains("syn=false")) {
            ma_index_i = 3;
        }

        if (strFileName.contains("lem=true") && strFileName.contains("syn=true")) {
            ma_index_i = 4;
        }


        if (strFileName.contains("01-01-01-00-00-WordBasedTrainingImpl")) {
            ma_index_j = 3;
        }

        if (strFileName.contains("01-02-01-01-01-BiGramShinglingTrainingImpl")) {
            ma_index_j = 2;
        }

        if (strFileName.contains("01-02-01-01-02-TriGramShinglingTrainingImpl")) {
            ma_index_j = 1;
        }

        return "S_" + ma_index_j + "_" + ma_index_i;
    }

    public static Vector<String> execute(String strDataDirectory) {
        objFiles2Scan = new Vector<String>();
        scanDirectory(new File[]{new File(strDataDirectory)});
        return objFiles2Scan;
    }

    private static void scanDirectory(File objFiles[]) {
        for (int i = 0; i < objFiles.length; i++) {
            File objDirectory = new File(objFiles[i].getAbsolutePath());
            File objTMPFiles[] = objDirectory.listFiles();

            for (int j = 0; j < objTMPFiles.length; j++) {
                if (objTMPFiles[j].isDirectory()) {
                    scanDirectory(new File[]{objTMPFiles[j].getAbsoluteFile()});
                } else {
                    if (objTMPFiles[j].getAbsolutePath().endsWith(strFileExtension)
                            && objTMPFiles[j].getAbsolutePath().contains(strThreshold)) {
                        objFiles2Scan.add(objTMPFiles[j].getAbsolutePath());
                    }
                }
            }
        }
    }
}
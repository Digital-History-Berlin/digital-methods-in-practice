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



package eu.etrap.tracer.noisychannel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 *
 * @author mbuechler
 */
public class NoisyChannelEvaluationGnuplotPreparationMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String strHomeDir = args[0];//"/home/mbuechler/Dissertation/RandomnessStructure/";//args[0];
        String strFilePrefix = args[1];//"DEU";//args[1];
        String strFileSuffix = args[2];//"co-oc.nse";//args[2];

        int intMinRange = -100;
        int intMaxRange = 10000000;

        double aryData[][] = null;
        int dim1 = 0;
        int dim2 = 0;
        String aryKeys[] = null;

        if (strFilePrefix.equals("DEU")) {
            aryKeys = new String[]{"DEU-1", "DEU-3", "DEU-10", "DEU-30",
                "DEU-100", "DEU-300", "DEU-1k", "DEU-3k", "DEU-10k", "DEU-30k", "DEU-100k", "DEU-300k",
                "DEU-1M", "DEU-3M", "DEU-10M", "DEU-30M", "DEU-100M", "DEU-250M"};
        }

        if (strFilePrefix.equals("ENG")) {
            aryKeys = new String[]{"ENG-1", "ENG-3", "ENG-10", "ENG-30",
                "ENG-100", "ENG-300", "ENG-1k", "ENG-3k", "ENG-10k", "ENG-30k", "ENG-100k", "ENG-300k",
                "ENG-1M", "ENG-3M", "ENG-10M", "ENG-30M", "ENG-100M", "ENG-300M", "ENG-488M"};
        }
        
        int size = Math.abs(intMinRange) + intMaxRange + 1;

        dim1 = aryKeys.length;
        dim2 = size;
        aryData = new double[dim1][dim2];


        String strFiles[] = new File(strHomeDir).list();
        for (int i = 0; i < strFiles.length; i++) {
            if (strFiles[i].startsWith(strFilePrefix) && strFiles[i].endsWith(strFileSuffix)) {
                System.out.println("\n\nProcessing " + strHomeDir + strFiles[i]);

                String strKey = strFiles[i].replace("." + strFileSuffix, "");
                System.out.println("Key is " + strKey);

                int index = -1;


                for (int k = 0; k < aryKeys.length; k++) {
                    if (aryKeys[k].equals(strKey)) {
                        index = k;
                    }
                }

                System.out.println("Key is " + strKey + " out of " + aryKeys.length + " datasets. Index is " + index);

                try {
                    String strCoocFileName = strHomeDir + strFiles[i];

                    BufferedReader objReader = new BufferedReader(new FileReader(strCoocFileName));
                    String strLine = null;

                    while ((strLine = objReader.readLine()) != null) {
                        String strSplit[] = strLine.split("\t");
                        int intSigClass = Integer.parseInt(strSplit[0]);
                        double dblSNR = Double.parseDouble(strSplit[3]);

                        if (intSigClass >= intMinRange && intSigClass <= intMaxRange) {
                            aryData[index][intSigClass + Math.abs(intMinRange)] = dblSNR;
                        }
                    }

                    objReader.close();


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        for (int k = 0; k < dim1; k++) {
            int counter = 0;
            for (int j = 0; j < dim2; j++) {
                if (aryData[k][j] != 0) {
                    counter++;
                }
            }
            System.out.println(k + "\t" + counter);
        }

        try {
            String strResultsDir = strHomeDir + "results-" + strFilePrefix + "-" + strFileSuffix + "/";
            new File(strResultsDir).mkdirs();

            processWriteTable(aryData, strResultsDir + strFileSuffix.replace(".nse", "") + ".matrix.data", dim1, dim2, aryKeys, intMinRange, strFileSuffix);
            processStatistics(aryData, strResultsDir + strFileSuffix.replace(".nse", "") + ".statistics.data", dim1, dim2, aryKeys, intMinRange, strFileSuffix);
            process3DPlot(aryData, strResultsDir + strFileSuffix.replace(".nse", "") + ".3d.data", dim1, dim2, aryKeys, intMinRange, strFileSuffix);

            String strThetaDistFileName = strResultsDir + "ThetaDistribution/";
            new File(strThetaDistFileName).mkdirs();
            processThetaDistribution(aryData, strThetaDistFileName, dim1, dim2, aryKeys, intMinRange, strFileSuffix.replace(".nse", ""));

            String strCorpusSizeDistFileName = strResultsDir + "CorpusSizeDistribution/";
            new File(strCorpusSizeDistFileName).mkdirs();
            processCorpusSizeDist(aryData, strCorpusSizeDistFileName, dim1, dim2, aryKeys, intMinRange, strFileSuffix.replace(".nse", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void processWriteTable(double aryData[][], String strOutFile, int dim1, int dim2, String aryKeys[], int intMinRange, String strFileSuffix) throws IOException {
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutFile));
        Locale.setDefault(Locale.US);
        DecimalFormat df = new DecimalFormat("0.00000");

        String strLine = "xxx\t";
        for (int i = 0; i < aryKeys.length; i++) {
            strLine += aryKeys[i] + "\t";
        }

        strLine = strLine.trim() + "\n";
        objWriter.write(strLine);

        for (int i = 0; i < dim2; i++) {
            strLine = (i - Math.abs(intMinRange)) + "\t";
            for (int j = 0; j < dim1; j++) {
                strLine += df.format(aryData[j][i]) + "\t";
            }
            objWriter.write(strLine.trim() + "\n");
        }

        objWriter.flush();
        objWriter.close();
    }

    protected static void processThetaDistribution(double aryData[][], String strOutFile, int dim1, int dim2, String aryKeys[], int intMinRange, String strFileSuffix) throws IOException {
        int intThetas[] = new int[]{0, 4, 7, 8, 11, 15, 20, 25, 30, 35, 40, 45, 50, 75, 100, 300, 1000, 3000};
        for (int i = 0; i < intThetas.length; i++) {
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutFile + strFileSuffix + ".theta-"
                    + new DecimalFormat("0000").format(intThetas[i])));

            Locale.setDefault(Locale.US);
            DecimalFormat df = new DecimalFormat("0.00000");

            for (int j = 0; j < dim1; j++) {
                objWriter.write(formatKey(aryKeys[j]) + "\t"
                        + j + "\t" + df.format(aryData[j][intThetas[i] + Math.abs(intMinRange)]) + "\n");
            }

            objWriter.flush();
            objWriter.close();
        }
    }

    protected static void processCorpusSizeDist(double aryData[][], String strOutFile, int dim1, int dim2, String aryKeys[], int intMinRange, String strFileSuffix) throws IOException {

        for (int i = 0; i < aryKeys.length; i++) {
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutFile + aryKeys[i]));
            
            Locale.setDefault(Locale.US);
            DecimalFormat df = new DecimalFormat("0.00000");

            for (int j = 0; j < dim2; j++) {
                if( aryData[i][j] != 0 ){
                objWriter.write((j - Math.abs(intMinRange)) + "\t" + aryData[i][j] + "\n");
                }
            }

            objWriter.flush();
            objWriter.close();
        }
    }

    protected static void processStatistics(double aryData[][], String strOutFile, int dim1, int dim2, String aryKeys[], int intMinRange, String strFileSuffix) throws IOException {
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutFile));
        for (int i = 0; i < aryKeys.length; i++) {
            
            Locale.setDefault(Locale.US);
            DecimalFormat df = new DecimalFormat("0.00000");

            double min = 10000000;
            double max = -1 * min;
            int intMin = 0;
            int intMax = 0;

            for (int j = 0; j < dim2; j++) {
                if (aryData[i][j] <= min) {
                    intMin = j - Math.abs(intMinRange);
                    min = aryData[i][j];
                }

                if (aryData[i][j] >= max) {
                    intMax = j - Math.abs(intMinRange);
                    max = aryData[i][j];
                }
            }
            
            objWriter.write(formatKey(aryKeys[i]) + "\t" + i + "\t"
                    + min + "\t" + intMin + "\t" + max + "\t" + intMax + "\n");

        }

        objWriter.flush();
        objWriter.close();

    }

    protected static void process3DPlot(double aryData[][], String strOutFile, int dim1, int dim2, String aryKeys[], int intMinRange, String strFileSuffix) throws IOException {
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutFile));
        for (int i = 0; i < aryKeys.length; i++) {
            
            Locale.setDefault(Locale.US);
            DecimalFormat df = new DecimalFormat("0.00000");

            for (int j = 0; j < dim2; j++) {
                objWriter.write(formatKey(aryKeys[i]) + "\t"
                        + (j - Math.abs(intMinRange)) + "\t" + df.format(aryData[i][j]) + "\n");
            }
            objWriter.write("\n");
        }

        objWriter.flush();
        objWriter.close();

    }

    protected static int formatKey(String strKey) {

        String strSubString = strKey.substring(4);
        int length = strSubString.length();
        String strNumber = strSubString.trim();

        int number = 1;
        
        if ( strNumber.trim().endsWith("k") ) {
            number *= 1000;
            strNumber= strSubString.substring(0, length - 1);
        }

        if ( strNumber.trim().endsWith("M") ) {
            strNumber= strSubString.substring(0, length - 1);
            number *= 1000000;
        }
       
        number *= Integer.parseInt(strNumber);

        return number;
    }
}

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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Locale;

/**
 *
 * @author mbuechler
 */
public class PRCalculatorMain {

    private static HashMap<String, double[]> objData = null;
    private static HashMap<String, double[]> objDataMax = null;
    private static HashMap<String, double[]> objDataScores = null;
    private static HashMap<String, double[]> objRanCorData = null;
    private static HashMap<String, double[]> objRanCorDataMax = null;
    private static HashMap<String, double[]> objRanCorDataScores = null;
    private static String aryBiblesAbbr[] = new String[]{"", "ASV", "BBE", "DBY", "KJV", "WEB", "WBS", "YLT"};

    /**
     * @param args the command line arguments
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws IOException {
        String strFileName = args[0]; //"/home/mbuechler/Dissertation/Results/data/corpora/Bible/TRACER_DATA/01:02-WLP:lem=true_syn=true_ssim=false_redwo=false:ngram=5:LLR=true_toLC=true_rDia=true_w2wl=false:wlt=5/01-02-01-01-02-TriGramShinglingTrainingImpl/02-02-01-01-01-LocalMaxFeatureFrequencySelectorImpl:FeatDens=0.8/01:01-01-01-01-MultiVersionsOfBible:01-01-01-02-MultiVersionsOfBible/02-02-01-01-01-02-SelectedFeatureResemblanceSimilarityImpl:Threshold=0.6/MultiVersionsOfBible-MultiVersionsOfBible.score";

        String strSplit[] = strFileName.split("/");
        int index1 = strSplit[9].indexOf("WLP:") + 4;
        int index2 = strSplit[9].indexOf("_redwo");

        readData(strFileName, true);
        readData(strFileName.replace("/Bible/", "/RanCorBible/").replace("MultiVersionsOfBible", "MultiVersionsOfBible.ncrb"), false);

        Iterator<String> objIter = objData.keySet().iterator();

        while (objIter.hasNext()) {
            String strKey = objIter.next();
            double aryData[] = objData.get(strKey);
            double aryData2[] = objDataMax.get(strKey);
            double aryData3[] = objRanCorData.get(strKey);
            double aryData4[] = objRanCorDataMax.get(strKey);
            double aryData5[] = objDataScores.get(strKey);

            String strSplit2[] = strKey.split("\t");

            String strOutFile = "eval-output/" + aryBiblesAbbr[Integer.parseInt(strSplit2[0])] + "_" + aryBiblesAbbr[Integer.parseInt(strSplit2[1])] + "-"
                    + strSplit[10] + "-" + strSplit[9].substring(index1, index2) + ".csv";

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutFile));
            System.out.println(aryBiblesAbbr[Integer.parseInt(strSplit2[0])] + "_" + aryBiblesAbbr[Integer.parseInt(strSplit2[1])] + "-"
                    + strSplit[10] + "-" + strSplit[9].substring(index1, index2));

            Locale.setDefault(Locale.US);
            DecimalFormat df = new DecimalFormat("0.00000");
            DecimalFormat df2 = new DecimalFormat("0.000000000");


            for (int i = 0; i < aryData.length; i++) {
                double p = aryData[i] / aryData2[i];
                double r = aryData[i] / 28632.0;
                double f = 2*p*r/(p+r);

                objWriter.write(((double) i / (double) (aryData.length - 1)) + "\t"
                        + aryData[i] + "\t" + aryData2[i] + "\t"
                        + aryData3[i] + "\t" + aryData4[i]
                        + "\t" + df.format(p) + "\t" + df.format(r) + "\t"
                        + df2.format(aryData5[i] / (28632.0 * 28632.0 - 28632.0)) + "\t" + df.format(10 * Math.log10(aryData2[i] / Math.max(1, aryData4[i]))) 
                        + "\t" + f + "\n");
            }

            objWriter.flush();
            objWriter.close();
        }

    }

    private static void readData(String strFileName, boolean isNatural) {
        try {
            BufferedReader objReader = new BufferedReader(new FileReader(strFileName));

            String strLine = null;

            if (isNatural) {
                objData = new HashMap<String, double[]>();
                objDataMax = new HashMap<String, double[]>();
                objDataScores = new HashMap<String, double[]>();
            } else {
                objRanCorData = new HashMap<String, double[]>();
                objRanCorDataMax = new HashMap<String, double[]>();
                objRanCorDataScores = new HashMap<String, double[]>();
            }

            int counter = 0;
            while ((strLine = objReader.readLine()) != null) {
                counter++;
                String strSplit2[] = strLine.split("\t");
                int intVerse1 = Integer.parseInt(strSplit2[0]) % 1000000;
                int intVerse2 = Integer.parseInt(strSplit2[1]) % 1000000;
                int intBible1 = Integer.parseInt(strSplit2[0]) / 1000000;
                int intBible2 = Integer.parseInt(strSplit2[1]) / 1000000;
                String strKey = intBible1 + "\t" + intBible2;

                double[] aryData = null;
                double[] aryData2 = null;
                double[] aryData3 = null;

                if (isNatural) {
                    aryData = objData.get(strKey);
                    aryData2 = objDataMax.get(strKey);
                    aryData3 = objDataScores.get(strKey);
                } else {
                    aryData = objRanCorData.get(strKey);
                    aryData2 = objRanCorDataMax.get(strKey);
                    aryData3 = objRanCorDataScores.get(strKey);
                }


                if (aryData == null) {
                    aryData = new double[1001];
                    aryData2 = new double[1001];
                    aryData3 = new double[1001];
                }

                process(aryData2, Double.parseDouble(strSplit2[3]));
                process2(aryData3, Double.parseDouble(strSplit2[3]));

                if (intVerse1 == intVerse2) {
                    process(aryData, Double.parseDouble(strSplit2[3]));
                }

                if (isNatural) {
                    objData.put(strKey, aryData);
                    objDataMax.put(strKey, aryData2);
                    objDataScores.put(strKey, aryData3);
                } else {
                    objRanCorData.put(strKey, aryData);
                    objRanCorDataMax.put(strKey, aryData2);
                    objRanCorDataScores.put(strKey, aryData3);
                }
            }

            System.out.println("size=" + counter);

            objReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void process(double[] aryData, double score) {

        for (int i = 0; i < aryData.length; i++) {
            if (score >= (double) i / (double) (aryData.length - 1)) {
                aryData[i]++;
            } else {
                break;
            }
        }
    }

    private static void process2(double[] aryData, double score) {

        for (int i = 0; i < aryData.length; i++) {
            if (score >= (double) i / (double) (aryData.length - 1)) {
                aryData[i] += score;
            } else {
                break;
            }
        }
    }
}

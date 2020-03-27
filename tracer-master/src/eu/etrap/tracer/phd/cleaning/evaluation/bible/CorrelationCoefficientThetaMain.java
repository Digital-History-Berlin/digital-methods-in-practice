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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author mbuechler
 */
public class CorrelationCoefficientThetaMain {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException if file is not found
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strFile1 = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/data/KJV_WBS-01-02-01-01-02-TriGramShinglingTrainingImpl-lem=true_syn=true_ssim=false.csv";
        strFile1 = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/data/BBE_YLT-01-02-01-01-02-TriGramShinglingTrainingImpl-lem=true_syn=true_ssim=false.csv";

        String strFolderName = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/data/";
        strFolderName = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/MotivationCorrelationRundTRC/";

        File objFiles[] = new File(strFolderName).listFiles();
        ArrayList<Double> dataAll1 = new ArrayList<Double>();
        ArrayList<Double> dataAll2 = new ArrayList<Double>();


        for (int i = 0; i < objFiles.length; i++) {
            String fileName[] = objFiles[i].getName().split("-");
            String strBibleVersions[] = fileName[0].split("_");

            if (//!strBibleVersions[0].trim().equals(strBibleVersions[1].trim()) &&
                   objFiles[i].getName().contains("trigram.out.cum")) {
                System.out.print(objFiles[i] + "\t");
                ArrayList<Double> data1 = getData(objFiles[i].getAbsolutePath(), 1);
                ArrayList<Double> data2 = getData(objFiles[i].getAbsolutePath(), 2);

                dataAll1.addAll(data1);
                dataAll2.addAll(data2);

                double cor = cov(data1, average(data1), data2, average(data2)) / stdv(data1, average(data1)) / stdv(data2, average(data2));
                System.out.println("cor=" + cor);
            }
        }

        double cor = cov(dataAll1, average(dataAll1), dataAll2, average(dataAll2)) / stdv(dataAll1, average(dataAll1)) / stdv(dataAll2, average(dataAll2));
        System.out.println("cor=" + cor);
    }

    private static double average(ArrayList<Double> data) {
        double avg = 0;

        int size = data.size();

        for (int i = 0; i < size; i++) {
            avg += data.get(i);
        }

        return avg / (double) data.size();
    }

    private static double cov(ArrayList<Double> data1, double avg1, ArrayList<Double> data2, double avg2) {
        double cov = 0;
        int size = data1.size();

        for (int i = 0; i < size; i++) {
            cov += (data1.get(i) - avg1) * (data2.get(i) - avg2);
        }

        cov /= (double) data1.size();

        return cov;
    }

    private static double stdv(ArrayList<Double> data, double avg) {
        double stdv = 0;
        int size = data.size();

        for (int i = 0; i < size; i++) {
            stdv += Math.pow((data.get(i) - avg), 2);
        }

        stdv /= (double) data.size();

        stdv = Math.sqrt(stdv);

        return stdv;
    }

    private static ArrayList<Double> getData(String strFileName, int intColumnIndex) throws FileNotFoundException, IOException {
        ArrayList<Double> objData = new ArrayList<Double>();

        BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            if (Double.parseDouble(strSplit[0]) >= 0.5 && Double.parseDouble(strSplit[0]) <= 1.0) {
                objData.add(Double.parseDouble(strSplit[intColumnIndex]));
                System.out.println( strLine );
            }
        }

        objReader.close();

        return objData;
    }
}

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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author mbuechler
 */
public class CorrelationCoefficientMain {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException if file is not found
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String strFile1 = "/home/mbuechler/Dissertation/Arbeit-2012-12-18/chapters/05-Results/table-includes/Bible.Recall.tex";
        String strFile2 = "/home/mbuechler/Dissertation/Arbeit-2012-12-18/chapters/05-Results/table-includes/Bible.TextReuseCompressionModified.tex";

        strFile1 = "/home/mbuechler/Dissertation/Arbeit-2012-12-18/chapters/05-Results/table-includes/Bible.Precision.tex";
        //strFile2 = "/home/mbuechler/Dissertation/Arbeit-2012-12-18/chapters/05-Results/table-includes/Bible.NoisyChannelEvaluation.tex";
        
        ArrayList<Double> data1 = getData(strFile1);
        ArrayList<Double> data2 = getData(strFile2);
       
        /*ArrayList<Double> data3 = getData( "/home/mbuechler/Dissertation/Arbeit-2012-12-18/chapters/05-Results/table-includes/Bible.Recall.tex" );
        
        System.out.println( data1.size() + "\t" + data3.size() );
        
        ArrayList<Double> data4 = new ArrayList<Double>();
        for( int i=0; i<data1.size(); i++ ){
            System.out.println( i + "\t" +  data1.get(i) + "\t" + data3.get(i) );
            double dblP = data1.get(i);
            double dblR = data3.get(i);
            double dblF = 2*dblP*dblR/(dblP+dblR);
            data4.add(i, dblF);
        }
        data1=data4;*/
        
        System.out.println(average(data1));
        System.out.println(average(data2));
        System.out.println(stdv(data1, average(data1)));
        System.out.println(stdv(data2, average(data2)));
        System.out.println(cov(data1, average(data1), data2, average(data2)));

        double cor = cov(data1, average(data1), data2, average(data2)) / stdv(data1, average(data1)) / stdv(data2, average(data2));
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

    private static ArrayList<Double> getData(String strFileName) throws FileNotFoundException, IOException {
        ArrayList<Double> objData = new ArrayList<Double>();

        BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            strLine = strLine.replace("\\footnotesize", "");
            strLine = strLine.replace(" & ", "\t");
            strLine = strLine.replace(" \\hline", "");

            String strSplit[] = strLine.split("\t");

            for (int i = 9; i <= 12; i++) {
                int index = strSplit[i].lastIndexOf("{") + 1;
                int index2 = strSplit[i].lastIndexOf("}");

                objData.add(Double.parseDouble(strSplit[i].substring(index, index2).replace(",", ".")));
            }
        }

        objReader.close();

        return objData;
    }

    private static ArrayList<Double> getData2(String strFileName) throws FileNotFoundException, IOException {
        ArrayList<Double> objData = new ArrayList<Double>();

        BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            strLine = strLine.replace("\\footnotesize", "");
            strLine = strLine.replace(" & ", "\t");
            strLine = strLine.replace(" \\hline", "");

            String strSplit[] = strLine.split("\t");

            for (int i = 9; i <=12; i++) {
                int index = strSplit[i].lastIndexOf("{") + 1;
                int index2 = strSplit[i].lastIndexOf("}");

                objData.add(Math.pow(10,-1*Double.parseDouble(strSplit[i].substring(index, index2).replace(",", "."))));
            }
        }

        objReader.close();

        return objData;
    }
}

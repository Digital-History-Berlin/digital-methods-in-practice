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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
// java -Xmx1g -cp traces.jar eu.etrap.tracer.phd.cleaning.evaluation.bible.ScoringDistMain
/**
 *
 * @author mbuechler
 */
public class ScoringDistMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int size = 100;
        long ary[][] = new long[size + 1][2];

        String strFile = args[0];
        System.out.println("Processing " + strFile);

        //HashMap<String, int[][]> objData = new HashMap<String, int[][]>();

        try {
            BufferedReader objReader = new BufferedReader(new FileReader(strFile));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                //System.out.println(strLine);
                String strSplit[] = strLine.split("\t");
                int key1 = Integer.parseInt(strSplit[0]) / 1000000;
                int key2 = Integer.parseInt(strSplit[1]) / 1000000;

                /*int ary[][] = objData.get(key1 + "\t" + key2);
                if (ary == null) {
                    ary = new int[size + 1][2];
                }*/

                int verse1 = Integer.parseInt(strSplit[0]) % 1000000;
                int verse2 = Integer.parseInt(strSplit[1]) % 1000000;
                int index = (int) Math.ceil(Math.round(Double.parseDouble(strSplit[3]) * 100));

                //System.out.println(key1 + "\t" + key2 + "\t" + strSplit[3] + "\t" + index);

                ary[index][0]++;

                if (verse1 == verse2) {
                    ary[index][1]++;
                }

                //objData.put(key1 + "\t" + key2, ary);

            }
            objReader.close();

            /*Iterator<String> objIter = objData.keySet().iterator();

            while (objIter.hasNext()) {

                String strKey = objIter.next();
                int ary[][] = objData.get(strKey);*/
                
                for (int i = 0; i <= 100; i++) {
                    System.out.println( ((double) i / (double) 100) + "\t"
                            + ary[i][0] + "\t" + ary[i][1]);
                }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

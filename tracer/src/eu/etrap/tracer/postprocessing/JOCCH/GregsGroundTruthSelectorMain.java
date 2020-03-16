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


package eu.etrap.tracer.postprocessing.JOCCH;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author mbuechler
 */
public class GregsGroundTruthSelectorMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {

            // READ corpus file
            String strCorpusFileName = args[0];
            BufferedReader objReader = new BufferedReader(new FileReader(strCorpusFileName));
            HashMap<Integer, String> mapRUID2ReuseUnit = new HashMap<Integer, String>();
            HashMap<Integer, String> mapRUID2Author = new HashMap<Integer, String>();


            String strLine = null;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");

                // 1) RUID --> SENTENCE
                mapRUID2ReuseUnit.put(new Integer(strSplit[0]), strSplit[1]);

                // 2) AUTHOR --> RUID
                String strWork = "";

                if( strSplit.length ==4){
                   strWork = strSplit[3];
                }


                mapRUID2Author.put(new Integer(strSplit[0]), strWork.replace(" (Greek). Machine readable text", ""));
            }

            objReader.close();

            System.out.println(mapRUID2ReuseUnit.size());
            System.out.println(mapRUID2Author.size());


            // Compute string based bigrams
            String strFileName = args[1];
            HashMap<Integer, String> objWordID2Word = new HashMap<Integer, String>();
            objReader = new BufferedReader(new FileReader(strFileName));

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                objWordID2Word.put(new Integer(strSplit[0].trim()), strSplit[1].trim());
            }
            objReader.close();

            strFileName = args[2];
            HashMap<Integer, String> objFeat2String = new HashMap<Integer, String>();
            objReader = new BufferedReader(new FileReader(strFileName));

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                Integer objFeatID = new Integer(strSplit[0]);
                String strWordKeys[] = strSplit[1].trim().split(" ");

                String strStringFeature = "";
                for (int i = 0; i < strWordKeys.length; i++) {
                    strStringFeature += objWordID2Word.get(new Integer(strWordKeys[i])) + " ";
                }
                strStringFeature = strStringFeature.trim();

                objFeat2String.put(new Integer(strSplit[0].trim()), strStringFeature);
            }
            objReader.close();



            // RUID --> feat id
            strFileName = args[3];//"/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/JCDL/2011-11-08-JCDL-Data/PerseusGreek.sel";
            HashMap<Integer, HashSet<String>> objRUID2Signature = new HashMap<Integer, HashSet<String>>();
            objReader = new BufferedReader(new FileReader(strFileName));

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intRUID = Integer.parseInt(strSplit[1]);
                int intSELFEATID = Integer.parseInt(strSplit[0]);

                HashSet<String> objSignature = new HashSet<String>();

                if (objRUID2Signature.containsKey(intRUID)) {
                    objSignature = objRUID2Signature.get(intRUID);
                }
                String strStringFeature = objFeat2String.get(intSELFEATID);

                if (strStringFeature == null) {
                    System.out.println("ERROR for " + strLine);
                    System.exit(0);
                }

                objSignature.add(strStringFeature);
                objRUID2Signature.put(intRUID, objSignature);

            }
            objReader.close();


            // READ RE-USE GRAPH
            String strReuseGraphFileName = args[4]; //"/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/JCDL/2011-11-08-JCDL-Data/PerseusGreek-PerseusGreek.score";
            objReader = new BufferedReader(new FileReader(strReuseGraphFileName));
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strReuseGraphFileName + ".enriched"));

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");

                int intRUID1 = new Integer(strSplit[0].trim());
                String strSourceDocName = mapRUID2Author.get(intRUID1);
                String strSourceDocSentenceText = mapRUID2ReuseUnit.get(intRUID1);
                double dblMatchDocScore = new Double(strSplit[2].trim());
                int intRUID2 = new Integer(strSplit[1].trim());
                String strMatchDocName = mapRUID2Author.get(intRUID2);
                String strMatchDocSentenceText = mapRUID2ReuseUnit.get(intRUID2);

                if ((intRUID1 / 10000000) != (intRUID2 / 10000000)) {

                    HashSet<String> objOverlap = getOverlap(objRUID2Signature.get(intRUID1), objRUID2Signature.get(intRUID2));

                    objWriter.write(strSourceDocName + "\t" + intRUID1 + "\t"
                            + strSourceDocSentenceText + "\t" + dblMatchDocScore + "\t"
                            + strMatchDocName + "\t" + intRUID2 + "\t"
                            + strMatchDocSentenceText + "\t"
                            + objRUID2Signature.get(intRUID1) + "\t"
                            + objRUID2Signature.get(intRUID2) + "\t"
                            + objOverlap + "\t"
                            + objRUID2Signature.get(intRUID1).size() + "\t"
                            + objRUID2Signature.get(intRUID2).size() + "\t"
                            + objOverlap.size() + "\t"
                            + "\n");

                }
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HashSet<String> getOverlap(HashSet<String> objRUID1, HashSet<String> objRUID2) {
        HashSet<String> objOverlap = new HashSet<String>();

        Iterator<String> objIter = objRUID1.iterator();

        while (objIter.hasNext()) {
            String strKey = objIter.next();
            if (objRUID2.contains(strKey)) {
                objOverlap.add(strKey);
            }
        }

        return objOverlap;
    }
}

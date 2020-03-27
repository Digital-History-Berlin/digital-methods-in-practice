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



package eu.etrap.tracer.phd.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author mbuechler
 */
public class CooccTrainFileTestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        HashSet objINvertListFromDisc = loadInvertedList();
        System.out.println("loaded data= " + objINvertListFromDisc.size());

        HashMap<String, String> objWord2ID = loadFeats();
        System.out.println("loaded data= " + objWord2ID.size());

        HashMap<String, String> objFmap = loadFMAP();
        System.out.println("loaded data= " + objFmap.size());

        try {
            HashSet<String> objInvertedLIst = new HashSet<String>();
            String strCorpusName = "/home/mbuechler/Development/Traces/data/corpora/example/TRACER_DATA/01:02-WLP:lem=false_syn=false_ssim=false_redwo=false:ngram=4:LLR=true_toLC=false_rDia=false_w2wl=false:wlt=0/example.txt.prep";
            BufferedReader objReader = new BufferedReader(new FileReader(strCorpusName));
            String strLine = null;

            int counter = 0;
            while ((strLine = objReader.readLine()) != null) {
                String strSID = strLine.split("\t")[0];
                strLine = strLine.split("\t")[1].trim();

                int intNumberOfWords = strLine.split(" ").length;

                counter += intNumberOfWords * (intNumberOfWords - 1);

                String strSplit[] = strLine.split(" ");
                for (int i = 0; i < intNumberOfWords; i++) {
                    for (int j = 0; j < intNumberOfWords; j++) {
                        if (i != j) {

                            // holen der WordIDs
                            String strWordID1 = objWord2ID.get( strSplit[i] );
                            String strWordID2 = objWord2ID.get( strSplit[j] );

                            // holen der fid
                            String strFID = objFmap.get( strWordID1 + " " + strWordID2 + " " +  (j - i) );

                            // vergleich, ob daten bereits geladen sind
                            String strKey = strSID + "\t" + strFID + "\t" + (j - i);
                            objInvertedLIst.add(strKey);

                            if( !objINvertListFromDisc.contains( strKey ) ){
                                System.out.println( "" );
                                System.out.println( strSID + "\t" + strSplit[i] + " " + strSplit[j] + " " +  (j - i) );
                                System.out.println( strWordID1 + " " + strWordID2 + " " +  (j - i) );
                                System.out.println( "" );
                            }
                        }
                    }
                }

                //if( objInvertedLIst.size() != intNumberOfWords * (intNumberOfWords - 1) ){
                //System.out.println(strLine.split("\t")[0].trim() + "\t" +
                /*    System.out.println( strSID + "\t" +
                intNumberOfWords + "\t" + intNumberOfWords * (intNumberOfWords - 1) +
                "\t" + objInvertedLIst.size());
                }
                
                objInvertedLIst.clear();*/
            }

            objReader.close();

            System.out.println("counter=" + counter);
            System.out.println("objInvertedLIst.size()=" + objInvertedLIst.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashSet<String> loadInvertedList() {
        HashSet<String> objInvertedLIst = new HashSet<String>();
        try {
            String strCorpusName = "/home/mbuechler/Development/Traces/data/corpora/example/TRACER_DATA/01:02-WLP:lem=false_syn=false_ssim=false_redwo=false:ngram=4:LLR=true_toLC=false_rDia=false_w2wl=false:wlt=0/01-01-02-00-SemanticCooccurrenceTrainingImpl/example.txt.train";
            BufferedReader objReader = new BufferedReader(new FileReader(strCorpusName));
            String strLine = null;

            while ((strLine = objReader.readLine()) != null) {
                strLine = strLine.trim();
                objInvertedLIst.add(strLine);
            }

            objReader.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return objInvertedLIst;
    }

    public static HashMap<String, String> loadFeats() {
        HashMap<String, String> objInvertedLIst = new HashMap<String, String>();
        try {
            String strCorpusName = "/home/mbuechler/Development/Traces/data/corpora/example/TRACER_DATA/01:02-WLP:lem=false_syn=false_ssim=false_redwo=false:ngram=4:LLR=true_toLC=false_rDia=false_w2wl=false:wlt=0/01-01-02-00-SemanticCooccurrenceTrainingImpl/example.txt.feats";
            BufferedReader objReader = new BufferedReader(new FileReader(strCorpusName));
            String strLine = null;

            while ((strLine = objReader.readLine()) != null) {
                strLine = strLine.trim();
                String strSplit[] = strLine.split( "\t" );
                objInvertedLIst.put( strSplit[1], strSplit[0] );
            }

            objReader.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return objInvertedLIst;
    }


        public static HashMap<String, String> loadFMAP() {
        HashMap<String, String> objInvertedLIst = new HashMap<String, String>();
        try {
            String strCorpusName = "/home/mbuechler/Development/Traces/data/corpora/example/TRACER_DATA/01:02-WLP:lem=false_syn=false_ssim=false_redwo=false:ngram=4:LLR=true_toLC=false_rDia=false_w2wl=false:wlt=0/01-01-02-00-SemanticCooccurrenceTrainingImpl/example.txt.fmap";
            BufferedReader objReader = new BufferedReader(new FileReader(strCorpusName));
            String strLine = null;

            while ((strLine = objReader.readLine()) != null) {
                strLine = strLine.trim();
                String strSplit[] = strLine.split( "\t" );
                objInvertedLIst.put( strSplit[1] + " " + strSplit[2], strSplit[0] );
            }

            objReader.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return objInvertedLIst;
    }
}

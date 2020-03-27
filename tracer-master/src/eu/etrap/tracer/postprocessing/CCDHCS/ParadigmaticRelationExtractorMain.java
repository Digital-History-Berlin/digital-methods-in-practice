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



package eu.etrap.tracer.postprocessing.CCDHCS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mbuechler
 */
public class ParadigmaticRelationExtractorMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Read MultiVersionBible
        String strFileName = "/home/mbuechler/Development/Traces/data/corpora/Bible/MultiVersionsOfBible.preps";
        BufferedReader objReader;

        TreeMap<Integer, TreeMap<Integer, String>> objData = new TreeMap<Integer, TreeMap<Integer, String>>();

        try {

            HashSet<String> objFunctionWords = new HashSet<String>();
            BufferedReader obR = new BufferedReader(new FileReader("/home/mbuechler/Development/Traces/data/corpora/Bible/bible.sw"));

            String line = null;
            while ((line = obR.readLine()) != null) {
                objFunctionWords.add(line.trim());
            }
            obR.close();


            objReader = new BufferedReader(new FileReader(strFileName));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intRUID = Integer.parseInt(strSplit[0]);
                int intBibleVersion = intRUID / 1000000;
                int intRUIDInBibleVersion = intRUID % 1000000;

                // TODO: Split into 7 versions
                // Store id --> verse
                TreeMap<Integer, String> objBible = objData.get(intBibleVersion);

                if (objBible == null) {
                    objBible = new TreeMap<Integer, String>();
                }

                objBible.put(intRUIDInBibleVersion, strLine);
                objData.put(intBibleVersion, objBible);
            }

            objReader.close();

            /*Iterator<Integer> objIter = objData.keySet().iterator();
            while( objIter.hasNext() ){
            Integer objBibleNumber = objIter.next();
            System.out.println( objBibleNumber + "\t" + objData.get(objBibleNumber).size() );
            }*/


            int intFirst = objData.firstKey();
            int intLast = objData.lastKey();

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strFileName + ".results"));

            for (int i = intFirst; i <= intLast; i++) {
                for (int j = i + 1; j <= intLast; j++) {
                    TreeMap<Integer, String> objData1 = objData.get(i);
                    TreeMap<Integer, String> objData2 = objData.get(j);
                    int intSize = objData1.size();

                    // run from id 1 to 28K
                    for (int k = 1; k <= intSize; k++) {
                        String strSentence1 = objData1.get(k);
                        String strSentence2 = objData2.get(k);

                        String strWords1[] = strSentence1.split("\t")[1].split(" ");
                        String strWords2[] = strSentence2.split("\t")[1].split(" ");

                        String strWordMax[] = strWords1;
                        String strWordMin[] = strWords2;

                        if (strWordMin.length > strWordMax.length) {
                            strWordMax = strWords2;
                            strWordMin = strWords1;
                        }

                        //System.out.println("k=" + k);
                        //System.out.println("Satz1: " + strSentence1);
                        //System.out.println("Satz2: " + strSentence2);

                        // extract words that have left and right neighbor as same
                        for (int h = 1; h < strWordMin.length - 1; h++) {
                            String strLeftWord = strWordMin[h - 1];
                            String strRightWord = strWordMin[h + 1];
                            /*System.out.println("min: " + strLeftWord
                            + "\t" + strRightWord);*/

                            for (int l = 0; l < strWordMax.length - 2; l++) {

                                /*if (k == 14) {
                                System.out.println("\t-->" + strLeftWord.trim() + "\t" + strWordMax[l].trim() + "\t" + l);
                                System.out.println("\t-->" + strRightWord.trim() + "\t" + strWordMax[l + 2].trim() + "\t" + (l + 2));
                                }*/

                                if (strLeftWord.trim().equals(strWordMax[l].trim())
                                        && strRightWord.trim().equals(strWordMax[l + 2].trim())) {

                                    // and that are not the same
                                    // store  word word id --> bv1 bv2
                                    if (!strWordMin[h].trim().equals(strWordMax[l + 1].trim()) && Math.abs(l + 1 - h) <= 2
                                            && !objFunctionWords.contains(strWordMin[h].trim())
                                            && ! objFunctionWords.contains(strWordMax[l + 1].trim())) {
                                        objWriter.write(strWordMin[h].trim() + "\t" + strWordMax[l + 1].trim() + "\t"
                                                + strSentence1.split("\t")[3] + "\t" + strSentence2.split("\t")[3] + "\n");
                                        /*System.out.println("FOUND: " + strWordMin[h].trim() + "\t" + strWordMax[l + 1].trim() +
                                        "\t" + h + "\t" +(l+1));*/
                                    }
                                    // System.out.println("BREAK");
                                    break;
                                }
                            }
                        }

                        // and that are not the same
                        // store  word word id --> bv1 bv2
                    }

                }
            }
            objWriter.flush();
            objWriter.close();
        } catch (Exception ex) {
            Logger.getLogger(ParadigmaticRelationExtractorMain.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

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

import bak.pcj.map.ObjectKeyIntMap;
import bak.pcj.map.ObjectKeyIntOpenHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author mbuechler
 */
public class TextDecontaminationMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            ObjectKeyIntMap objAuthors = loadAuthorsFile();

            // READ corpus file
            String strCorpusFileName = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/PerseusGreek.txt";
            BufferedReader objReader = new BufferedReader(new FileReader(strCorpusFileName));
            HashMap<Integer, String> mapRUID2ReuseUnit = new HashMap<Integer, String>();
            HashMap<Integer, String> mapRUID2Author = new HashMap<Integer, String>();
            TreeMap<String, SortedSet<Integer>> mapWork2RUID = new TreeMap<String, SortedSet<Integer>>();


            String strLine = null;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");

                // 1) RUID --> SENTENCE
                mapRUID2ReuseUnit.put(new Integer(strSplit[0]), strSplit[1]);

                // 2) AUTHOR --> RUID
                mapRUID2Author.put(new Integer(strSplit[0]), strSplit[3].split(": ")[0]);

                // 3) WORK --> RUID
                SortedSet<Integer> objRUIDs = new TreeSet<Integer>();
                if (mapWork2RUID.containsKey(strSplit[3].trim())) {
                    objRUIDs = mapWork2RUID.get(strSplit[3].trim());
                }
                objRUIDs.add(new Integer(strSplit[0]));
                mapWork2RUID.put(strSplit[3].trim(), objRUIDs);

            }

            objReader.close();

            System.out.println(mapRUID2ReuseUnit.size());
            System.out.println(mapWork2RUID.size());



            // READ RE-USE GRAPH
            String strReuseGraphFileName = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/PerseusGreek-PerseusGreekBiGramLem.score";
            objReader = new BufferedReader(new FileReader(strReuseGraphFileName));
            HashMap<Integer, SortedSet<Integer>> mapRUI2RUIDs = new HashMap<Integer, SortedSet<Integer>>();

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");

                int intRUID1 = new Integer(strSplit[0].trim());
                int intRUID2 = new Integer(strSplit[1].trim());

                String strAuthor1 = mapRUID2Author.get(intRUID1);
                String strAuthor2 = mapRUID2Author.get(intRUID2);

                int intDate1 = objAuthors.get(strAuthor1);
                int intDate2 = objAuthors.get(strAuthor2);

                if (intDate1 >= intDate2) {

                    SortedSet<Integer> objRUIDs = new TreeSet<Integer>();
                    if (mapRUI2RUIDs.containsKey(new Integer(strSplit[0].trim()))) {
                        objRUIDs = mapRUI2RUIDs.get(new Integer(strSplit[0].trim()));
                    }
                    objRUIDs.add(new Integer(strSplit[1]));
                    mapRUI2RUIDs.put(new Integer(strSplit[0].trim()), objRUIDs);
                }
            }

            objReader.close();

            System.out.println("\n" + mapRUID2ReuseUnit.size());
            System.out.println(mapWork2RUID.size());
            System.out.println(mapRUI2RUIDs.size());


            // Baseline
            // Plutarch vs. Plutarch
            System.out.println("BASELINE");
            String strWork1 = "Plutarch: Apophthegmata Laconica";
            String strWork2 = "Plutarch: Quaestiones Romanae";
            printBaseLine(strWork1, strWork2, mapWork2RUID, mapRUID2ReuseUnit);


            // Plato vs. Plutarch
            strWork1 = "Plutarch: Apophthegmata Laconica";
            strWork2 = "Plato: Tim. --- Hippias Major, Hippias Minor, Ion, Menexenus, Cleitophon, Timaeus, Critias, Minos, Epinomis (Greek). Machine readable text";
            printBaseLine(strWork1, strWork2, mapWork2RUID, mapRUID2ReuseUnit);



            SortedMap<String, SortedSet<Integer>> objPlutarchData = mapWork2RUID.subMap("Plutarch: ", "Plutarch: ZZZZZ");
            Object objData[] = objPlutarchData.keySet().toArray();

            double dblScore = 0;
            double dblCounter = 0;
            for (int i = 0; i < objData.length; i++) {
                for (int j = 0; j < objData.length; j++) {
                    strWork1 = (String) objData[i];
                    strWork2 = (String) objData[j];

                    int intMin = 700;
                    if (i != j && Math.abs(mapWork2RUID.get(strWork1).size() - mapWork2RUID.get(strWork2).size()) <= 200
                            && Math.min(mapWork2RUID.get(strWork1).size(), mapWork2RUID.get(strWork2).size()) >= intMin) {
                        dblScore += printBaseLine(strWork1, strWork2, mapWork2RUID, mapRUID2ReuseUnit);
                        dblCounter++;
                    }
                }
            }

            System.out.println("res=" + dblScore / dblCounter);
            //System.exit(0);

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strReuseGraphFileName + ".tdc"));

            // iterate works
            Iterator<String> objIter = mapWork2RUID.keySet().iterator();

            while (objIter.hasNext()) {
                String strWork = objIter.next();
                SortedSet<Integer> objWord2RUIDs = mapWork2RUID.get(strWork);

                Iterator<Integer> objRUIDs = objWord2RUIDs.iterator();
                SortedSet<Integer> objNotContaminatedRUIDs = new TreeSet<Integer>();

                while (objRUIDs.hasNext()) {
                    Integer objRUIDCandidate = objRUIDs.next();

                    if (!mapRUI2RUIDs.containsKey(objRUIDCandidate)) {
                        objNotContaminatedRUIDs.add(objRUIDCandidate);
                    }
                }

                // compute word distribution of contaminated text
                HashMap<String, Integer> objContaminatedWordDist = computeWordDistribution(objWord2RUIDs, mapRUID2ReuseUnit);

                // compute word distribution of decontaminated text
                HashMap<String, Integer> objDeContaminatedWordDist = computeWordDistribution(objNotContaminatedRUIDs, mapRUID2ReuseUnit);

                double dblCosineMeasureWords = computeCosineDistance(objContaminatedWordDist, objDeContaminatedWordDist);



                // compute ngram distribution of contaminated text
                HashMap<String, Integer> objContaminatedNGramDist = computeBigramDistribution(objWord2RUIDs, mapRUID2ReuseUnit);

                // compute ngram distribution of decontaminated text
                HashMap<String, Integer> objDeContaminatedNgramDist = computeBigramDistribution(objNotContaminatedRUIDs, mapRUID2ReuseUnit);

                //if (strWork.contains("Plutarch") || strWork.contains("Homer") || strWork.contains("Plato")) {
                objWriter.write(strWork + "\t" + objWord2RUIDs.size() + "\t"
                        + objNotContaminatedRUIDs.size() + "\t" + (double) objNotContaminatedRUIDs.size() / (double) objWord2RUIDs.size()
                        + "\t" + dblCosineMeasureWords + "\t" + objContaminatedWordDist.size() + "\t" + objDeContaminatedWordDist.size() + "\t"
                        + (double) objDeContaminatedWordDist.size() / (double) objContaminatedWordDist.size()
                        + "\t" + objContaminatedNGramDist.size() + "\t" + objDeContaminatedNgramDist.size()
                        + "\n");
                //}




                // compute bigram distribution of contaminated text
                // compute bigram distribution of decontaminated text
            }

            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static double printBaseLine(String strWork1, String strWork2,
            TreeMap<String, SortedSet<Integer>> mapWork2RUID, HashMap<Integer, String> mapRUID2ReuseUnit) {

        HashMap<String, Integer> objWork1WordDist = computeWordDistribution(mapWork2RUID.get(strWork1), mapRUID2ReuseUnit);
        HashMap<String, Integer> objWork2WordDist = computeWordDistribution(mapWork2RUID.get(strWork2), mapRUID2ReuseUnit);

        double dblCosineMeasureWords = computeCosineDistance(objWork1WordDist, objWork2WordDist);

        System.out.println(strWork1.substring(0, Math.min(30, strWork1.length()) - 1) + "\t"
                + strWork2.substring(0, Math.min(30, strWork2.length()) - 1) + "\t"
                + mapWork2RUID.get(strWork1).size() + "\t"
                + mapWork2RUID.get(strWork2).size() + "\t" + (double) mapWork2RUID.get(strWork2).size() / (double) mapWork2RUID.size()
                + "\t" + dblCosineMeasureWords + "\t" + objWork1WordDist.size() + "\t" + objWork2WordDist.size());

        return dblCosineMeasureWords;
    }

    protected static HashMap<String, Integer> computeWordDistribution(SortedSet<Integer> objRUIDs, HashMap<Integer, String> mapRUID2ReuseUnit) {

        Iterator<Integer> objIter = objRUIDs.iterator();
        HashMap<String, Integer> objDist = new HashMap<String, Integer>();

        while (objIter.hasNext()) {
            Integer objRUID = objIter.next();

            String strReUseUnit = mapRUID2ReuseUnit.get(objRUID);

            String strWords[] = strReUseUnit.split(" ");

            for (int i = 0; i < strWords.length; i++) {
                Integer objFreq = 0;

                if (objDist.containsKey(strWords[i])) {
                    objFreq = objDist.get(strWords[i]);
                }

                objFreq++;
                objDist.put(strWords[i], objFreq);
            }
        }

        return objDist;
    }

    protected static HashMap<String, Integer> computeBigramDistribution(SortedSet<Integer> objRUIDs, HashMap<Integer, String> mapRUID2ReuseUnit) {

        Iterator<Integer> objIter = objRUIDs.iterator();
        HashMap<String, Integer> objDist = new HashMap<String, Integer>();

        while (objIter.hasNext()) {
            Integer objRUID = objIter.next();

            String strReUseUnit = mapRUID2ReuseUnit.get(objRUID);

            String strWords[] = strReUseUnit.split(" ");

            for (int i = 0; i < strWords.length - 1; i++) {
                Integer objFreq = 0;

                if (objDist.containsKey(strWords[i] + " " + strWords[i + 1])) {
                    objFreq = objDist.get(strWords[i] + " " + strWords[i + 1]);
                }

                objFreq++;
                objDist.put(strWords[i] + " " + strWords[i + 1], objFreq);
            }
        }

        return objDist;
    }

    protected static double computeCosineDistance(HashMap<String, Integer> objContaminatedWordDist, HashMap<String, Integer> objDeContaminatedWordDist) {
        double dblCosineMeasure = 0.0;

        // compute X norm
        double dblXNorm = 0.0;
        Iterator<String> objIterCont = objContaminatedWordDist.keySet().iterator();
        while (objIterCont.hasNext()) {
            String strFeature = objIterCont.next();
            double dblFreq = objContaminatedWordDist.get(strFeature);
            dblXNorm += dblFreq * dblFreq;
        }
        dblXNorm = Math.sqrt(dblXNorm);

        // compute Y norm
        double dblYNorm = 0.0;
        Iterator<String> objIterDeCont = objDeContaminatedWordDist.keySet().iterator();
        while (objIterDeCont.hasNext()) {
            String strFeature = objIterDeCont.next();
            double dblFreq = objDeContaminatedWordDist.get(strFeature);
            dblYNorm += dblFreq * dblFreq;
        }
        dblYNorm = Math.sqrt(dblYNorm);

        // compute XY norm
        double dblXYNorm = 0.0;
        Iterator<String> objIterXY = objDeContaminatedWordDist.keySet().iterator();
        while (objIterXY.hasNext()) {
            String strFeature = objIterXY.next();
            double dblContFreq = 0;

            if (objContaminatedWordDist.containsKey(strFeature)) {
                dblContFreq = objContaminatedWordDist.get(strFeature);
            }

            double dblDeContFreq = objDeContaminatedWordDist.get(strFeature);
            dblXYNorm += dblContFreq * dblDeContFreq;
        }
        dblCosineMeasure = dblXYNorm / dblXNorm / dblYNorm;

        return dblCosineMeasure;
    }

    private static ObjectKeyIntMap loadAuthorsFile() throws FileNotFoundException, IOException {
        ObjectKeyIntMap objAuthors = new ObjectKeyIntOpenHashMap();

        String strScoringFileName = "/home/mbuechler/JOCCH/PerseusAuthors-v2.csv";
        BufferedReader objReader = new BufferedReader(new FileReader(strScoringFileName));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            if (strSplit.length == 6) {
                System.out.println(strSplit[1] + "\t" + Integer.parseInt(strSplit[5]));
                objAuthors.put(strSplit[1].trim(), Integer.parseInt(strSplit[5]));
            } else {
                System.out.println("IGNORE: " + strLine);
            }
        }
        objReader.close();

        return objAuthors;
    }
}

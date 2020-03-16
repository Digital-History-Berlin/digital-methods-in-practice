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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author mbuechler
 */
public class ParadigmaticRelationEvaluatorMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String strFileName = "/home/mbuechler/Development/Traces/data/corpora/Bible/MultiVersionsOfBible.preps.results";
        BufferedReader objReader;
        HashMap<String, String> objWord2Lem = new HashMap<String, String>();
        HashMap<String, String> objWord2WordClass = new HashMap<String, String>();
        HashMap<String, String> objLem2Syn = new HashMap<String, String>();
        HashMap<String, Integer> objRelations = new HashMap<String, Integer>();

        int intAllData = 0;
        int intInflectedVariant = 0;
        int intSynonym = 0;
        int intBindestrich = 0;
        int intComposition = 0;
        int intSamePrefix = 0;
        int intSameSuffix = 0;
        int intArchaicInflectedVariant = 0;
        int intArchaicSynonym = 0;
        int intSimilarWrittenVariant = 0;
        int intSameWordClasse = 0;


        int intNotClassified = 0;

        try {
            BufferedWriter objTaggedWriter = new BufferedWriter(new FileWriter(strFileName + ".tagged"));
            BufferedWriter objUntaggedWriter = new BufferedWriter(new FileWriter(strFileName + ".untagged"));
            String in = "/home/mbuechler/Development/Traces/data/corpora/Bible/Bible.lemma";
            String strLine = null;
            objReader = new BufferedReader(new FileReader(in));
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                objWord2Lem.put(strSplit[0].trim(), strSplit[1].trim());
                objWord2WordClass.put(strSplit[0].trim(), strSplit[2].trim());
            }
            objReader.close();
            System.out.println("lem size=" + objWord2Lem.size());
            System.out.println("word class size=" + objWord2WordClass.size());


            in = "/home/mbuechler/Development/Traces/data/corpora/Bible/Bible.syns";
            objReader = new BufferedReader(new FileReader(in));
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                String strWord = strSplit[0].trim();
                String strSyn = strSplit[1].trim().split("_")[0];

                String strResult = strSyn;
                if (objLem2Syn.containsKey(strWord.trim())) {
                    strResult += "\t" + objLem2Syn.get(strWord.trim());
                }
                objLem2Syn.put(strWord + "\t" + strSyn, null);
                objLem2Syn.put(strSyn + "\t" + strWord, null);
            }
            objReader.close();
            System.out.println("syn size=" + objLem2Syn.size());



            objReader = new BufferedReader(new FileReader(strFileName));

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                String aryWords[] = new String[2];
                aryWords[0] = strSplit[0].trim();
                aryWords[1] = strSplit[1].trim();

                Arrays.sort(aryWords);

                String strKey = aryWords[0] + "\t" + aryWords[1];

                int freq = 0;
                if (objRelations.containsKey(strKey)) {
                    freq = objRelations.get(strKey);
                }
                freq++;

                objRelations.put(aryWords[0] + "\t" + aryWords[1], freq);
            }
            System.out.println("rels size=" + objRelations.size());


            objReader.close();

            Iterator<String> objIter = objRelations.keySet().iterator();
            while (objIter.hasNext()) {
                intAllData++;

                String strRel = objIter.next();
                String strSplit[] = strRel.split("\t");
                String strWord1 = strSplit[0].trim();
                String strWord2 = strSplit[1].trim();

                String strBaseWord1 = objWord2Lem.get(strWord1);
                String strBaseWord2 = objWord2Lem.get(strWord2);

                if (strBaseWord1 == null) {
                    strBaseWord1 = strWord1;
                }

                if (strBaseWord2 == null) {
                    strBaseWord2 = strWord2;
                }

                String strWordClassWord1 = objWord2WordClass.get(strWord1);
                String strWordClassWord2 = objWord2WordClass.get(strWord2);

                if (strWordClassWord1 == null) {
                    strWordClassWord1 = "UN";
                }

                if (strWordClassWord2 == null) {
                    strWordClassWord2 = "UN";
                }

                String strStatus = "";

                if (strBaseWord1.trim().equals(strBaseWord2.trim())) {
                    strStatus = "INFLECTED VARIANT";
                    intInflectedVariant++;
                }

                if (objLem2Syn.containsKey(strBaseWord1.trim() + "\t" + strBaseWord2.trim())) {
                    strStatus = "SYNONYMS";
                    intSynonym++;
                }

                if (objLem2Syn.containsKey(strWord1.trim() + "\t" + strWord2.trim())) {
                    strStatus = "SYNONYMS";
                    intSynonym++;
                }

                if (objLem2Syn.containsKey(strWord2.trim() + "\t" + strWord1.trim())) {
                    strStatus = "SYNONYMS";
                    intSynonym++;
                }

                if (strWord1.equals("wife")) {
                    System.out.println(strWord1 + "\t" + strWord2
                            + "\t" + strBaseWord1 + "\t" + strBaseWord2
                            + "\t" + strWordClassWord1 + "\t" + strWordClassWord2 + "\t" + strStatus);
                }

                if (strWord1.trim().replaceAll("-", "").equals(strWord2.trim().replaceAll("-", ""))
                        && !strWord1.equals(strWord2)) {
                    strStatus = "HYPHEN";
                    intBindestrich++;
                }


                if (strStatus.equals("") && (strWord1.contains("-") || strWord2.contains("-"))) {
                    String strWords1[] = strWord1.split("-");
                    String strWords2[] = strWord2.split("-");

                    if (strWords1[0].trim().equals(strWords2[0].trim())) {
                        intComposition++;
                        strStatus = "COMPOSITION";
                    }

                   if (strWords1[strWords1.length - 1].trim().equals(strWords2[strWords2.length - 1].trim())) {
                        intComposition++;
                        strStatus = "COMPOSITION";
                    }

                }

                if (strStatus.equals("") && strWord1.endsWith("eth")) {
                    strBaseWord1 = strWord1.substring(0, strWord1.length() - 3);

                    if (strBaseWord1.equals(strBaseWord2)) {
                        strStatus = "ARCHAIC INFLECTED VERSION";
                        intArchaicInflectedVariant++;
                    }

                    if (objLem2Syn.containsKey(strBaseWord1.trim() + "\t" + strBaseWord2.trim())) {
                        strStatus = "ARCHAIC SYNONYM";
                        intArchaicSynonym++;
                    }

                }


                if (strStatus.equals("") && strWord2.endsWith("eth")) {
                    strBaseWord2 = strWord2.substring(0, strWord2.length() - 3);

                    if (strBaseWord1.equals(strBaseWord2)) {
                        strStatus = "ARCHAIC INFLECTED VERSION";
                        intArchaicInflectedVariant++;
                    }

                    if (objLem2Syn.containsKey(strBaseWord1.trim() + "\t" + strBaseWord2.trim())) {
                        strStatus = "ARCHAIC SYNONYM";
                        intArchaicSynonym++;
                    }

                }






                if (strStatus.equals("") && strWord1.endsWith("est")) {
                    strBaseWord1 = strWord1.substring(0, strWord1.length() - 3);

                    if (strBaseWord1.equals(strBaseWord2)) {
                        strStatus = "ARCHAIC INFLECTED VERSION";
                        intArchaicInflectedVariant++;
                    }

                    if (objLem2Syn.containsKey(strBaseWord1.trim() + "\t" + strBaseWord2.trim())) {
                        strStatus = "ARCHAIC SYNONYM";
                        intArchaicSynonym++;
                    }

                }


                if (strStatus.equals("") && strWord2.endsWith("est")) {
                    strBaseWord2 = strWord2.substring(0, strWord2.length() - 3);

                    if (strBaseWord1.equals(strBaseWord2)) {
                        strStatus = "ARCHAIC INFLECTED VERSION";
                        intArchaicInflectedVariant++;
                    }

                    if (objLem2Syn.containsKey(strBaseWord1.trim() + "\t" + strBaseWord2.trim())) {
                        strStatus = "ARCHAIC SYNONYM";
                        intArchaicSynonym++;
                    }

                }






                if (strStatus.equals("") && strWord1.endsWith("th")) {
                    strBaseWord1 = strWord1.substring(0, strWord1.length() - 2);

                    if (strBaseWord1.equals(strBaseWord2)) {
                        strStatus = "ARCHAIC INFLECTED VERSION";
                        intArchaicInflectedVariant++;
                    }

                    if (objLem2Syn.containsKey(strBaseWord1.trim() + "\t" + strBaseWord2.trim())) {
                        strStatus = "ARCHAIC SYNONYM";
                        intArchaicSynonym++;
                    }
                }

                if (strStatus.equals("") && strWord2.endsWith("th")) {
                    strBaseWord2 = strWord2.substring(0, strWord2.length() - 2);

                    if (strBaseWord1.equals(strBaseWord2)) {
                        strStatus = "ARCHAIC INFLECTED VERSION";
                        intArchaicInflectedVariant++;
                    }

                    if (objLem2Syn.containsKey(strBaseWord1.trim() + "\t" + strBaseWord2.trim())) {
                        strStatus = "ARCHAIC SYNONYM";
                        intArchaicSynonym++;
                    }
                }

                if (strStatus.equals("") && strWord2.trim().startsWith(strWord1.trim())) {
                    strStatus = "PREFIX";
                    intSamePrefix++;
                }

                if (strStatus.equals("") && (strWord2.trim().endsWith(strWord1.trim()) || strWord1.trim().startsWith(strWord2.trim()))) {
                    strStatus = "SUFFIX";
                    intSameSuffix++;
                }

                double dice = 0;
                if (strStatus.equals("")) {
                    HashSet<String> objFeatWord1 = fingerprint(strWord1);
                    HashSet<String> objFeatWord2 = fingerprint(strWord2);
                    dice = dice(objFeatWord1, objFeatWord2);
                    if (dice >= 0.7) {
                        intSimilarWrittenVariant++;
                        strStatus = "SIMILAR WRITTEN VARIANT";
                    }

                    if (dice >= 0.6 && strWord1.length() >= 5 && strWord2.length() >= 5) {
                        intSimilarWrittenVariant++;
                        strStatus = "SIMILAR WRITTEN VARIANT";
                    }
                }

               /* if (strStatus.equals("")) {
                    String strSuffix = "ment";
                    if (strWord1.trim().endsWith(strSuffix) && strWord2.trim().endsWith(strSuffix)) {
                        intSameWordClasse++;
                        strStatus = "SAME WORD CLASS" + strSuffix;
                    }

                    strSuffix = "ness";
                    if (strWord1.trim().endsWith(strSuffix) && strWord2.trim().endsWith(strSuffix)) {
                        intSameWordClasse++;
                        strStatus = "SAME WORD CLASS"+ strSuffix;
                    }

                    strSuffix = "ion";
                    if (strWord1.trim().endsWith(strSuffix) && strWord2.trim().endsWith(strSuffix)) {
                        intSameWordClasse++;
                        strStatus = "SAME WORD CLASS"+ strSuffix;
                    }


                    strSuffix = "ed";
                    if (strWord1.trim().endsWith(strSuffix) && strWord2.trim().endsWith(strSuffix)) {
                        intSameWordClasse++;
                        strStatus = "SAME WORD CLASS"+ strSuffix;
                    }

                    strSuffix = "ly";
                    if (strWord1.trim().endsWith(strSuffix) && strWord2.trim().endsWith(strSuffix)) {
                        intSameWordClasse++;
                        strStatus = "SAME WORD CLASS"+ strSuffix;
                    }

                    strSuffix = "ing";
                    if (strWord1.trim().endsWith(strSuffix) && strWord2.trim().endsWith(strSuffix)) {
                        intSameWordClasse++;
                        strStatus = "SAME WORD CLASS"+ strSuffix;
                    }

                    strSuffix = "est";
                    if (strWord1.trim().endsWith(strSuffix) && strWord2.trim().endsWith(strSuffix)) {
                        intSameWordClasse++;
                        strStatus = "SAME WORD CLASS"+ strSuffix;
                    }

                    strSuffix = "eth";
                    if (strWord1.trim().endsWith(strSuffix) && strWord2.trim().endsWith(strSuffix)) {
                        intSameWordClasse++;
                        strStatus = "SAME WORD CLASS"+ strSuffix;
                    }
                }*/

                if (strStatus.equals("")) {
                    intNotClassified++;
                }

                if (strStatus.equals("")) {
                    objUntaggedWriter.write(strWord1 + "\t" + strWord2
                            + "\t" + strBaseWord1 + "\t" + strBaseWord2
                            + "\t" + strWordClassWord1 + "\t" + strWordClassWord2 + "\t" + dice + "\n");
                } else {
                    objTaggedWriter.write(strWord1 + "\t" + strWord2  + "\t" + strStatus + "\n");
                }
            }


            System.out.println("\n\n\nintAllData=" + intAllData);
            System.out.println("intInflectedVariant=" + intInflectedVariant);
            System.out.println("intSynonym=" + intSynonym);
            System.out.println("intBindestrich=" + intBindestrich);
            System.out.println("intComposition=" + intComposition);
            System.out.println("intSamePrefix=" + intSamePrefix);
            System.out.println("intSameSuffix=" + intSameSuffix);
            System.out.println("intArchaicInflectedVariant=" + intArchaicInflectedVariant);
            System.out.println("intArchaicSynonym=" + intArchaicSynonym);
            System.out.println("intSimilarWrittenVariant=" + intSimilarWrittenVariant);
            System.out.println("intSameWordClasse=" + intSameWordClasse);
            System.out.println("intNotClassified=" + intNotClassified);


            objTaggedWriter.flush();
            objTaggedWriter.close();
            objUntaggedWriter.flush();
            objUntaggedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HashSet<String> fingerprint(String strWord) {
        HashSet<String> objFeatures = new HashSet<String>();

        for (int i = 0; i < strWord.length() - 1; i++) {
            objFeatures.add(strWord.subSequence(i, i + 2).toString());
        }
        return objFeatures;
    }

    private static double dice(HashSet<String> objFeat1, HashSet<String> objFeats2) {
        double dice = 0;

        Iterator<String> objIter = objFeat1.iterator();
        while (objIter.hasNext()) {
            String strFeat = objIter.next();

            if (objFeats2.contains(strFeat)) {
                dice++;
            }
        }

        dice *= 2;
        dice /= (double) (objFeat1.size() + objFeats2.size());

        return dice;
    }
}

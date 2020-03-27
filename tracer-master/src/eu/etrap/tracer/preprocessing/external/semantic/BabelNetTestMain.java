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
package eu.etrap.tracer.preprocessing.external.semantic;

import eu.etrap.tracer.selection.WordClassWeightsInterface;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetIDRelation;
import it.uniroma1.lcl.babelnet.data.BabelPOS;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.babelnet.data.BabelSenseSource;
import it.uniroma1.lcl.jlt.util.Language;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

/**
 * Created on 26.07.2017 14:18:42 by mbuechler
 *
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class BabelNetTestMain {

    public static TreeSet<String> objSynonyms = null;
    public static TreeSet<String> objHypernyms = null;
    public static TreeSet<String> objHyponyms = null;
    public static TreeSet<String> objMeronyms = null;
    public static TreeSet<String> objHolynyms = null;
    public static TreeSet<String> objAntonyms = null;
    public static TreeSet<String> objCohyponyms = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        String strFileName = args[0];
        String strLanguage = args[1];
        //String strFileName = "/home/mbuechler/Development/tracer/data/corpora/jane-austen/tagged-texts.lemma-list";
        //String strFileName = "/home/mbuechler/Development/tracer/data/corpora/jane-austen/test.lemma-list";
        //String strLanguage = "EN";
        Language lang = null;
        
        if (strLanguage == null || strLanguage.trim().equals("")){
            strLanguage = "EN";
        }
        
        if (strLanguage.equals("EN")){
            lang = Language.EN;
        }
        
        if (strLanguage.equals("LA")){
            lang = Language.LA;
        }
        
        if (strLanguage.equals("DE")){
            lang = Language.DE;
        }

        objSynonyms = new TreeSet<String>();
        objHypernyms = new TreeSet<String>();
        objHyponyms = new TreeSet<String>();
        objMeronyms = new TreeSet<String>();
        objHolynyms = new TreeSet<String>();
        objAntonyms = new TreeSet<String>();
        objCohyponyms = new TreeSet<String>();

        BabelNet objBabelNet = BabelNet.getInstance();

        BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            if (  !strLine.trim().equals("")) {
                //String strWord = "car";
                String strSplit[] = strLine.split("\t");
                String strWord = strSplit[0].trim();
                String strPoS = strSplit[1].trim();
                //String strPoS = "n";

                BabelPOS babelTag = null;

                if ( strPoS.equals(Character.toString(WordClassWeightsInterface.TAG_NOUN)) ) {
                    babelTag = BabelPOS.NOUN;
                }

                if (strPoS.equals(Character.toString(WordClassWeightsInterface.TAG_VERB))) {
                    babelTag = BabelPOS.VERB;
                }

                if (strPoS.equals(Character.toString(WordClassWeightsInterface.TAG_ADJECTIVE))) {
                    babelTag = BabelPOS.ADJECTIVE;
                }

                if (strPoS.equals(Character.toString(WordClassWeightsInterface.TAG_ADVERB))) {
                    babelTag = BabelPOS.ADVERB;
                }

                if (strPoS.equals(Character.toString(WordClassWeightsInterface.TAG_ARTICLE))) {
                    babelTag = BabelPOS.ARTICLE;
                }

                if (strPoS.equals(Character.toString(WordClassWeightsInterface.TAG_CONJUNCTION))) {
                    babelTag = BabelPOS.CONJUNCTION;
                }

                if (strPoS.equals(Character.toString(WordClassWeightsInterface.TAG_INTERJECTION))) {
                    babelTag = BabelPOS.INTERJECTION;
                }

                if (strPoS.equals(Character.toString(WordClassWeightsInterface.TAG_PREPOSITION))) {
                    babelTag = BabelPOS.PREPOSITION;
                }

                if (strPoS.equals(Character.toString(WordClassWeightsInterface.TAG_PRONOUN))) {
                    babelTag = BabelPOS.PRONOUN;
                }

                System.out.println("\n\nPROCESSING WORD " + strWord + " with PoS " + babelTag + ". Used language: " + lang);

                List<BabelSynset> objSynsets = objBabelNet.getSynsets(strWord, lang, babelTag, BabelSenseSource.WN);
                ListIterator<BabelSynset> objIterator = objSynsets.listIterator();

                while (objIterator.hasNext()) {
                    BabelSynset objSynset = objIterator.next();

                    System.out.println("\tSynSet: " + objSynset + "\tCategory=" + objSynset.getCategories(lang) + "\tDomains" + objSynset.getDomains());
  
                    List<BabelSense> objSenses = objSynset.getSenses(lang, BabelSenseSource.WN);
                    ListIterator<BabelSense> objListIteratorSource = objSenses.listIterator();

                    ArrayList<String> objSynonymsList = new ArrayList<String>();

                    while (objListIteratorSource.hasNext()) {
                        BabelSense objSenseSource = objListIteratorSource.next();
                        objSynonymsList.add(objSenseSource.getLemma());
                    }

                    Object[] arySynonyms = objSynonymsList.toArray();

                    for (int i = 0; i < arySynonyms.length; i++) {
                        for (int j = 0; j < arySynonyms.length; j++) {
                            if (i != j) {
                                objSynonyms.add(arySynonyms[i] + "\t" + arySynonyms[j] + "\t" +
                                        objSynset.getSynsetSource() + "---" + objSynset.getSynsetSource());
                            }
                        }
                    }

                    ArrayList<String> objRelations = getSemanticRelations(objBabelNet, objSynset, lang, BabelPointer.ANY_HYPERNYM);
                    printList(objRelations, "HYPER");

                    objRelations = getSemanticRelations(objBabelNet, objSynset, lang, BabelPointer.ANY_HYPONYM);
                    printList(objRelations, "HYPO");

                    objRelations = getSemanticRelations(objBabelNet, objSynset, lang, BabelPointer.ANY_MERONYM);
                    printList(objRelations, "MERO");

                    objRelations = getSemanticRelations(objBabelNet, objSynset, lang, BabelPointer.ANY_HOLONYM);
                    printList(objRelations, "HOLO");

                    objRelations = getSemanticRelations(objBabelNet, objSynset, lang, BabelPointer.ANTONYM);
                    printList(objRelations, "ANTO");

                    objRelations = getCohyponyms(objBabelNet, objSynset, lang);
                    printList(objRelations, "COHYPO");

                //ArrayList<String> objRelations = getPath(objBabelNet, objSynset, lang);
                    //printList(objRelations, "PATH");
                }
            }
        }

        // write to file
        writeListonDisc(objSynonyms, strFileName, "SYNO");
        writeListonDisc(objHypernyms, strFileName, "HYPER");
        writeListonDisc(objHyponyms, strFileName, "HYPO");
        writeListonDisc(objMeronyms, strFileName, "MERO");
        writeListonDisc(objHolynyms, strFileName, "HOLO");
        writeListonDisc(objAntonyms, strFileName, "ANTO");
        writeListonDisc(objCohyponyms, strFileName, "COHYPO");

    }

    public static void writeListonDisc(TreeSet<String> objSet, String strInputFile, String strType) throws IOException {
        String strFileSuffix = "." + strType.trim().toLowerCase();

        String strOutputFile = strInputFile.replace(".lemma-list", strFileSuffix);

        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutputFile));

        Iterator<String> objIter = objSet.iterator();

        while (objIter.hasNext()) {
            objWriter.write(objIter.next().trim().replace("_", " ") + "\n");
        }

        objWriter.flush();
        objWriter.close();

    }

    public static void printList(ArrayList<String> objRelations, String strType) {
        if (objRelations != null) {
            Iterator<String> objIter = objRelations.iterator();
            while (objIter.hasNext()) {
                String strRelation = objIter.next();

                if (strType.equals("HYPER")) {
                    objHypernyms.add(strRelation);
                }

                if (strType.equals("HYPO")) {
                    objHyponyms.add(strRelation);
                }

                if (strType.equals("MERO")) {
                    objMeronyms.add(strRelation);
                }

                if (strType.equals("HOLO")) {
                    objHolynyms.add(strRelation);
                }

                if (strType.equals("ANTO")) {
                    objAntonyms.add(strRelation);
                }

                if (strType.equals("COHYPO")) {
                    objCohyponyms.add(strRelation);
                }
            }
        }
    }

    public static ArrayList<String> getSemanticRelations(BabelNet objBabelNet, BabelSynset objSynset, Language lang, BabelPointer relType) throws IOException {

        ArrayList<String> objSemanticRelations = new ArrayList<String>();

        String strPivotWord = objSynset.getMainSense(lang).getLemma();
        
        List<BabelSynsetIDRelation> objEdges = objSynset.getEdges(relType);
        Iterator<BabelSynsetIDRelation> objIter = objEdges.iterator();

        while (objIter.hasNext()) {
            BabelSynsetIDRelation objRelations = objIter.next();
            BabelSynset objBabelSynset = objBabelNet.getSynset(objRelations.getBabelSynsetIDTarget());

            List<BabelSense> objHypers = objBabelSynset.getSenses(lang);
            ListIterator<BabelSense> objListIteratorHypers = objHypers.listIterator();

            while (objListIteratorHypers.hasNext()) {
                BabelSense objHyperSense = objListIteratorHypers.next();

                objSemanticRelations.add(strPivotWord + "\t" + objHyperSense.getLemma() + "\t" + objSynset.getSynsetSource() 
                        + "---" + objBabelSynset.getSynsetSource());
            }
        }

        return objSemanticRelations;
    }

    public static ArrayList<String> getCohyponyms(BabelNet objBabelNet, BabelSynset objSynset, Language lang) throws IOException {
        ArrayList<String> objSemanticRelations = new ArrayList<String>();

        String strPivotWord = objSynset.getMainSense(lang).getLemma();
        BabelPointer objRelType = BabelPointer.ANY_HYPERNYM;

        List<BabelSynsetIDRelation> objEdges = objSynset.getEdges(objRelType);
        Iterator<BabelSynsetIDRelation> objIter = objEdges.iterator();

        while (objIter.hasNext()) {
            BabelSynsetIDRelation objRelations = objIter.next();
            BabelSynset objBabelSynset = objBabelNet.getSynset(objRelations.getBabelSynsetIDTarget());

            List<BabelSense> objHypers = objBabelSynset.getSenses(lang);
            ListIterator<BabelSense> objListIteratorHypers = objHypers.listIterator();

            // iterating over all hypernyms
            while (objListIteratorHypers.hasNext()) {
                BabelSense objHyperSense = objListIteratorHypers.next();

                List<BabelSynsetIDRelation> objCohyponyms = objBabelSynset.getEdges(BabelPointer.ANY_HYPONYM);
                Iterator<BabelSynsetIDRelation> objIterCohyponyms = objCohyponyms.iterator();

                while (objIterCohyponyms.hasNext()) {
                    BabelSynsetIDRelation objHyponymRelation = objIterCohyponyms.next();
                    BabelSynset objHypoSynset = objBabelNet.getSynset(objHyponymRelation.getBabelSynsetIDTarget());

                    List<BabelSense> objCohypoList = objHypoSynset.getSenses(lang);
                    ListIterator<BabelSense> objListIteratorHypo = objCohypoList.listIterator();

                    while (objListIteratorHypo.hasNext()) {
                        BabelSense objCohypoSense = objListIteratorHypo.next();
                        objSemanticRelations.add(strPivotWord + "\t" + objCohypoSense.getLemma() + "\t" + objHyperSense.getLemma());
                    }
                }
                //objSemanticRelations.add(strPivotWord + "\t" + objHyperSense.getLemma());
            }
        }

        return objSemanticRelations;
    }

    public static ArrayList<String> getPath(BabelNet objBabelNet, BabelSynset objSynset, Language lang) throws IOException {
        ArrayList<String> objSemanticRelations = new ArrayList<String>();

        String strPivotWord = objSynset.getMainSense(lang).getLemma();
        BabelPointer objRelType = BabelPointer.ANY_HYPERNYM;

        List<BabelSynsetIDRelation> objEdges = objSynset.getEdges(objRelType);
        Iterator<BabelSynsetIDRelation> objIter = objEdges.iterator();

        while (objIter.hasNext()) {
            BabelSynsetIDRelation objRelations = objIter.next();
            BabelSynset objBabelSynset = objBabelNet.getSynset(objRelations.getBabelSynsetIDTarget());

            List<BabelSense> objHypers = objBabelSynset.getSenses(lang);
            ListIterator<BabelSense> objListIteratorHypers = objHypers.listIterator();

            while (objListIteratorHypers.hasNext()) {
                BabelSense objHyperSense = objListIteratorHypers.next();
                String strPath = objHyperSense.getLemma().trim() + "\t" + strPivotWord;
                System.out.println("PATH=" + strPath);
                String strFinalPath = traversePath(objBabelNet, objBabelSynset, lang, BabelPointer.ANY_HYPERNYM, strPath);
                System.out.println("PATH=" + strFinalPath);
            }
        }

        return objSemanticRelations;
    }

    public static String traversePath(BabelNet objBabelNet, BabelSynset objSynset, Language lang, BabelPointer relType, String strPath) throws IOException {
        List<BabelSynsetIDRelation> objEdges = objSynset.getEdges(relType);
        Iterator<BabelSynsetIDRelation> objIter = objEdges.iterator();

        while (objIter.hasNext()) {
            BabelSynsetIDRelation objRelations = objIter.next();
            BabelSynset objBabelSynset = objBabelNet.getSynset(objRelations.getBabelSynsetIDTarget());

            List<BabelSense> objRelationsList = objBabelSynset.getSenses(lang);
            ListIterator<BabelSense> objListIterator = objRelationsList.listIterator();

            BabelSense objSense = objBabelSynset.getMainSense(lang);
            String strLemma = objSense.getLemma();
            String strPathValues[] = strPath.split("\t");

            if (strPathValues[0].equals(strLemma)) {
                return strPath;
            }
            System.out.println("\t" + strLemma + "(" + objSense.isKeyConcept() + ")" + "\t\t" + strPath);
            strPath = objSense.getLemma().trim() + "\t" + strPath;
            strPath = traversePath(objBabelNet, objBabelSynset, lang, relType, strPath);

        }
        return strPath;
    }
}

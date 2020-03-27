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

import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.BabelSynsetIDRelation;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import it.uniroma1.lcl.babelnet.data.BabelPOS;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.babelnet.data.BabelSenseSource;
import it.uniroma1.lcl.jlt.util.Language;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created on 26.07.2017 14:18:42 by mbuechler
 *
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class TestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InvalidBabelSynsetIDException {
        BabelNet bn = BabelNet.getInstance();
        String strWord = "abstraho";
        System.out.println("Word=" + strWord);
        List<BabelSynset> objSynsets = bn.getSynsets(strWord, Language.LA, BabelPOS.VERB);
        //BabelSynset by = bn.getSynset(new BabelSynsetID("bn:00086603v"));

        Iterator<BabelSynset> objIter = objSynsets.iterator();

        while (objIter.hasNext()) {
            BabelSynset by = objIter.next();
            System.out.println("name          = " + by.getMainSense(Language.LA));
            System.out.println("Size of edges = " + by.getSenses().isEmpty());

            List<BabelSynsetIDRelation> objList = by.getEdges();
            System.out.println(objList.toArray().length);

            for (BabelSynsetIDRelation edge : by.getEdges(BabelPointer.ANY_HYPERNYM)) {
                System.out.println(by.getId() + "\t" + by.getMainSense(Language.LA).getLemma() + " - " + edge + " - "
                        + edge.getPointer() + " - "
                        + edge.getBabelSynsetIDTarget().toBabelSynset().getMainSense(Language.LA) + "\t"
                        + edge.getBabelSynsetIDTarget().toBabelSynset().getSynsetSource() + "\t"
                        + edge.getBabelSynsetIDTarget().toBabelSynset().getSenseSources() + "\t"
                        + edge.getTarget());
            }

        }
        /*ArrayList<String> aryList = getPath(bn, by, Language.EN);

         int size = aryList.size();

         for (int i = 0; i < size; i++) {
         System.out.println(aryList.get(i));
         }*/
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
                System.out.println("Synset=" + objBabelSynset);
                String strPath = objHyperSense.getLemma().trim() + "\t" + strPivotWord;
                System.out.println("PATH_Start=" + strPath);
                String strFinalPath = traversePath(objBabelNet, objBabelSynset, lang, BabelPointer.ANY_HYPERNYM, strPath);
                System.out.println("PATH_End=" + strFinalPath);
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

            System.out.println("Synset=" + objBabelSynset);
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

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
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class MainConceptTestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        BabelNet objBabelNet = BabelNet.getInstance();

        String strWord = "puella";
        BabelPOS babelTag = BabelPOS.NOUN;

        System.out.println("PROCESSING WORD " + strWord);

        List<BabelSynset> objSynsets = objBabelNet.getSynsets(strWord, Language.LA);
        ListIterator<BabelSynset> objIterator = objSynsets.listIterator();
        
        while (objIterator.hasNext()) {
            BabelSynset objSynset = objIterator.next();

            System.out.println("\nSynSet: " + objSynset + "\tCategory=" + objSynset.getCategories(Language.LA) + "\tDomains" + objSynset.getDomains());
            System.out.println("Elements: " + objSynset.getPOS() + "\t" + objSynset.getSenses(Language.LA));
            System.out.println( "Further:" + objSynset.getSynsetSource() + "\t" 
                    + objSynset.getSynsetType() + "\t" + objSynset.getId() );

            List<BabelSense> objSenses = objSynset.getSenses(Language.LA);
            ListIterator<BabelSense> objListIteratorSource = objSenses.listIterator();
            
            BabelSense objSenseMain = objSynset.getMainSense(Language.LA);
            /*System.out.println( "Main sense: " + objSenseMain.getLemma() + "\t" + objSenseMain.getSimpleLemma() + "\t" +
                    objSenseMain.getGeoNamesURI() + "\t" + objSenseMain.getSensekey() + "\t" + objSenseMain.getFrequency() +
                    "\t" + objSenseMain.getSimpleLemma() + "\t" + objSenseMain.getYAGOURI() + "\t" + objSenseMain.getPosition());*/
            ArrayList<String> objSynonymsList = new ArrayList<String>();

            //ListIterator<BabelSense> objListIteratorTarget = objSenses.listIterator();
            while (objListIteratorSource.hasNext()) {
                BabelSense objSenseSource = objListIteratorSource.next();
                
                /*System.out.println( "Sense: " + objSenseSource.getLemma() + "\t" + objSenseSource.isKeyConcept() + "\t" + objSenseMain.getLemma() + "\t" + objSenseMain.getSimpleLemma() + "\t" +
                    objSenseMain.getGeoNamesURI() + "\t" + objSenseMain.getSensekey() + "\t" + objSenseMain.getFrequency() +
                    "\t" + objSenseMain.getSimpleLemma() + "\t" + objSenseMain.getYAGOURI() + "\t" + objSenseMain.getPosition());*/
                
            }

            Object[] arySynonyms = objSynonymsList.toArray();
            
            ArrayList<String> objRelations = getSemanticRelations(objBabelNet, objSynset, Language.LA, BabelPointer.ANY_HYPERNYM);
            printList(objRelations, "HYPER");
        }
    }
    
        public static void printList(ArrayList<String> objRelations, String strType) {
        if (objRelations != null) {
            Iterator<String> objIter = objRelations.iterator();
            while (objIter.hasNext()) {
                String strRelation = objIter.next();
                System.out.println(strType + "\t" + strRelation);
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

                objSemanticRelations.add(strPivotWord + "\t" + objHyperSense.getLemma()+ "\t" +objHyperSense.isKeyConcept() + 
                        "\t" + objBabelSynset.getMainSense(lang) + "\t" + objBabelSynset.getId());
            }
        }

        return objSemanticRelations;
    }
}

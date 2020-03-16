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


package eu.etrap.tracer.preprocessing.graph;

import eu.etrap.medusa.config.ConfigurationContainer;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created on 01.03.2011 09:25:05 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AsymmetricWordGraphHandlerImpl extends AbstractWordGraphHandler{

    public void reduceGraph() {
        weightDirectedAssociations();
        cleanGraph();
    }

    /**
     * Q: Why is this method necessary?
     * A: In Ancient Greek there is quite often more than just one baseform. In
     * most cases this is caused: by language evolution (smaller changes of the
     * baseforms) or that an inflected word can have a baseform as noun, verb, and
     * others at the same time. See e.g. http://www.perseus.tufts.edu/hopper/morph?l=%E1%BC%88%CE%BC%E1%BD%B0&amp;la=greek
     *
     * For this reason this method takes a directed graph and inherite the word
     * frequency to baseform. Finally, by an arg max it's decided for the baseform
     * with the highest score.
     *
     * HINT: This step is pragmatically. However, it is highly necessary. Otherwise
     * there is NO chance for a lemmatisation on the pre-processing level.
     */
    protected void weightDirectedAssociations(){

                // compute virtual base form frequencies
            Iterator<String> objIter = objInputWordAssociations.keySet().iterator();

            while( objIter.hasNext() ){
                String strWordForm = objIter.next();
                Long objWordFreq = objWordformFrequency.get( strWordForm );
                HashSet<String> objReplacementCandidates = objInputWordAssociations.get( strWordForm );

                Iterator<String> objReplacementCandidatesIter = objReplacementCandidates.iterator();

                while( objReplacementCandidatesIter.hasNext() ){
                    String strReplacementCandidates = objReplacementCandidatesIter.next();

                    long longFreqValue = 0;
                    if (objReplacementCandidateFreqs.containsKey(strReplacementCandidates)){
                        longFreqValue = objReplacementCandidateFreqs.get(strReplacementCandidates);
                    }else{
                         Long longFreq = objWordformFrequency.get(strReplacementCandidates);

                         if( longFreq == null ){
                            longFreqValue = 0;
                         }else{
                            longFreqValue = longFreq;
                         }
                    }

                    longFreqValue += objWordFreq;
   
                    objReplacementCandidateFreqs.put(strReplacementCandidates, longFreqValue);
                }
            }

            ConfigurationContainer.println( "Number of total replacement candidates is " + objReplacementCandidateFreqs.size() );
    }

    protected void cleanGraph(){
            Iterator<String> objWordFormIter = objInputWordAssociations.keySet().iterator();

            while( objWordFormIter.hasNext() ){
                String strWordForm = objWordFormIter.next();
                HashSet<String> objBaseforms = objInputWordAssociations.get( strWordForm );

                long longMaxBaseformFreq = 0;
                String strMostProbableBaseform = null;

                Iterator<String> objBaseformIter = objBaseforms.iterator();
                while( objBaseformIter.hasNext() ){
                    String strBaseForm = objBaseformIter.next();
                    long longBaseFormFreq = objReplacementCandidateFreqs.get(strBaseForm);

                    if( longBaseFormFreq > longMaxBaseformFreq ){
                        longMaxBaseformFreq = longBaseFormFreq;
                        strMostProbableBaseform = strBaseForm;
                    }
                }

                objCleanedDirectedGraph.put( strWordForm, strMostProbableBaseform );
            }

            ConfigurationContainer.println( "Total number of directed associations is " + objReplacementCandidateFreqs.size() );
    }
}

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created on 01.03.2011 09:25:26 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class SymmetricWordGraphHandlerImpl extends AsymmetricWordGraphHandlerImpl {

    @Override
    public void reduceGraph() {
        directed2IndirectedGraph();
        weightDirectedAssociations();
        cleanGraph();
    }

    protected void directed2IndirectedGraph() {
        Iterator<String> objIter = objInputWordAssociations.keySet().iterator();
        HashMap<String, HashSet<String>> objDirectedtWordAssociations = new HashMap<String, HashSet<String>>();

        while (objIter.hasNext()) {
            String strInvestigatedNode = objIter.next();
            HashSet<String> objCandList = objInputWordAssociations.get(strInvestigatedNode);

            // get word frequencies
            Long longInvestigatedWordFreq = this.objWordformFrequency.get(strInvestigatedNode);
            long longInvFreq = 0;

            if (longInvestigatedWordFreq != null) {
                longInvFreq = longInvestigatedWordFreq;
            }

            Iterator<String> objCandidateIter = objCandList.iterator();
            String strArgMaxCand = null;

            while (objCandidateIter.hasNext()) {
                String strCandidate = objCandidateIter.next();

                // get word frequencies
                Long longCandidateFreq = this.objWordformFrequency.get(strCandidate);
                long longCandFreq = 0;

                if (longCandidateFreq != null) {
                    longCandFreq = longCandidateFreq;
                }

                if (longCandFreq > longInvFreq) {
                    strArgMaxCand = strCandidate;
                    longInvFreq = longCandFreq;
                } else {
                    if ((longCandFreq == longInvFreq) && (ignoreWordFrequency || (longCandFreq > 0))
                            && strCandidate.hashCode() < strInvestigatedNode.hashCode()) {
                        strArgMaxCand = strCandidate;
                        longInvFreq = longCandFreq;
                    }

                }
            }

            if (!(strArgMaxCand == null)) {
                /*System.out.println("ACCEPTED: " + strInvestigatedNode + "\t"
                        + strArgMaxCand + "\t"
                        + objWordformFrequency.get(strInvestigatedNode) + "\t"
                        + longInvFreq);*/
                HashSet<String> objCandidate = new HashSet<String>();
                objCandidate.add(strArgMaxCand);

                objDirectedtWordAssociations.put(strInvestigatedNode, objCandidate);

                if (ignoreWordFrequency) {
                    if (!objWordformFrequency.containsKey(strInvestigatedNode)) {
                        objWordformFrequency.put(strInvestigatedNode, 0L);
                    }

                    if (!objWordformFrequency.containsKey( strArgMaxCand )) {
                        objWordformFrequency.put( strArgMaxCand, 0L);
                    }
                }
            }
        }

        objInputWordAssociations = objDirectedtWordAssociations;

        ConfigurationContainer.println("Number of directed associations is " + objInputWordAssociations.size());
    }
}

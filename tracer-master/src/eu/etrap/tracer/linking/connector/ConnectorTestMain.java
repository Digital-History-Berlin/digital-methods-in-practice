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



package eu.etrap.tracer.linking.connector;

import bak.pcj.IntIterator;
import eu.etrap.tracer.linking.connector.ram.Feature2RUIDRAMConnectorImpl;
import eu.etrap.tracer.linking.connector.ram.RUID2FeatureRAMConnectorImpl;

import bak.pcj.map.IntKeyIntMap;
import bak.pcj.map.IntKeyIntOpenHashMap;

/**
 *
 * @author mbuechler
 */
public class ConnectorTestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            String strSelectionFile = "/home/mbuechler/Development/Traces/data/corpora/example/TRACER_DATA/01:02-WLP:lem=false_syn=false_ssim=false_redwo=false:ngram=4:LLR=true_toLC=false_rDia=false_w2wl=false:wlt=0/01-02-01-01-01-BiGramShinglingTrainingImpl/01-01-01-05-01-GlobalLogLikelihoodRatioSelectorImpl:FeatDens=0.65/example.sel";

            Connector objRUID2FeatureConnector = new RUID2FeatureRAMConnectorImpl();
            objRUID2FeatureConnector.init();
            objRUID2FeatureConnector.prepareData(strSelectionFile);

            Connector objFeature2RUIDConnector = new Feature2RUIDRAMConnectorImpl();
            objFeature2RUIDConnector.init();
            objFeature2RUIDConnector.prepareData(strSelectionFile);

            int aryListOfAllRUIDs[] = objRUID2FeatureConnector.getAllIDs();
            System.out.println( "aryListOfAllRUIDs.size()=" +aryListOfAllRUIDs.length );

            for (int h = 0; h < aryListOfAllRUIDs.length; h++) {
                int intRUID = aryListOfAllRUIDs[h];

                int aryFeatures[] = objRUID2FeatureConnector.getData(intRUID);

                IntKeyIntMap objRUIDCandidates = new IntKeyIntOpenHashMap();

                for (int i = 0; i < aryFeatures.length; i++) {
                    int aryMatchingRUID[] = objFeature2RUIDConnector.getData(aryFeatures[i]);

                    for (int j = 0; j < aryMatchingRUID.length; j++) {
                        int intEdgeFrequency = 0;
                        if (aryMatchingRUID[j] != intRUID) {
                            if (objRUIDCandidates.containsKey(aryMatchingRUID[j])) {
                                intEdgeFrequency = objRUIDCandidates.get(aryMatchingRUID[j]);
                            }

                            intEdgeFrequency++;
                            objRUIDCandidates.put(aryMatchingRUID[j], intEdgeFrequency);
                        }
                    }
                }

                IntIterator objIter = objRUIDCandidates.keySet().iterator();
                while (objIter.hasNext()) {
                    int intCandidateRUID = objIter.next();
                    int intOverlap = objRUIDCandidates.get(intCandidateRUID);
                    System.out.println(intRUID + "\t" + intCandidateRUID + "\t" + intOverlap);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

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



package eu.etrap.tracer.linking;

import bak.pcj.IntIterator;
import bak.pcj.list.IntArrayList;
import bak.pcj.list.IntList;
import bak.pcj.map.IntKeyIntMap;
import bak.pcj.map.IntKeyIntOpenHashMap;
import bak.pcj.set.IntOpenHashSet;
import bak.pcj.set.IntSet;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.utils.FileManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;

/**
 * Created on 08.12.2010 13:11:14 by mbuechler
 *
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractInterCorpusLinking extends AbstractLinking {
    //TODO: Load Mapping der Features here
    
    protected int intWorkNumbering= 1000000;

    @Override
    public void init() throws ConfigurationException {
        super.init();
        strTaxonomyCode = "02";
        
        this.strRUID2FeatureFile = FileManager.getMultipleSelectionFileName();
        this.strFeature2RUIDFile = FileManager.getMultipleSelectionFileName();
    }

        @Override
    protected void initRUID2Feat() throws ConfigurationException {
        this.objRUID2FeatureConnector = eu.etrap.tracer.utils.ClassLoader.loadConnectorImpl(this.strRUID2FeatureImplementation);
        objRUID2FeatureConnector.init();
    }

    @Override
    protected void initFeat2RUID() throws ConfigurationException {
        this.objFeature2RUIDConnector = eu.etrap.tracer.utils.ClassLoader.loadConnectorImpl(this.strFeature2RUIDImplementation);
        this.objFeature2RUIDConnector.init();
    }
    
    @Override
    public void prepareData() throws LinkingException {
        ConfigurationContainer.println("\tPreparing RUID2Feature connector by implementation in " + strRUID2FeatureImplementation);
        objRUID2FeatureConnector.prepareData(strRUID2FeatureFile);
        this.arySelStats[Constants.NUMBER_OF_FINGERPRINTED_RUID] = objRUID2FeatureConnector.getAllIDs().length;
        ConfigurationContainer.println("\tDONE!\n");

        ConfigurationContainer.println("\tPreparing Feature2RUID connector by implementation in " + strFeature2RUIDImplementation);
        objFeature2RUIDConnector.prepareData(strFeature2RUIDFile);
        this.arySelStats[Constants.NUMBER_OF_FINGERPRINT_FEATURES] = objFeature2RUIDConnector.getAllIDs().length;
        ConfigurationContainer.println("\tDONE!\n");
    }

    @Override
    public void link() throws LinkingException {
        try {
            String strLinkingFileName = FileManager.getLinkingFileName();
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strLinkingFileName));
            IntSet objLinkedRUIDs = new IntOpenHashSet();
            IntSet objLinkedFeatures = new IntOpenHashSet();

            int aryListOfAllRUIDs[] = objRUID2FeatureConnector.getAllIDs();
            ConfigurationContainer.println("\tLinking process started for "
                    + aryListOfAllRUIDs.length
                    + " fingerprinted re-use units");

            long longBeginTime = System.currentTimeMillis();
            long longRUID2FeatTime = 0;
            long longFeat2RUIDTime = 0;

            for (int h = 0; h < aryListOfAllRUIDs.length; h++) {
                int intRUID = aryListOfAllRUIDs[h];

                longRUID2FeatTime -= System.currentTimeMillis();
                int aryFeatures[] = objRUID2FeatureConnector.getData(intRUID);
                longRUID2FeatTime += System.currentTimeMillis();

                Arrays.sort(aryFeatures);

                IntKeyIntMap objRUIDCandidates = new IntKeyIntOpenHashMap();

                for (int i = 0; i < aryFeatures.length; i++) {
                    longFeat2RUIDTime -= System.currentTimeMillis();
                    int aryMatchingRUID[] = objFeature2RUIDConnector.getData(aryFeatures[i]);
                    longFeat2RUIDTime += System.currentTimeMillis();

                    for (int j = 0; j < aryMatchingRUID.length; j++) {

                        if (isPreSelected(intRUID, aryMatchingRUID[j])
                                && !objRUIDCandidates.containsKey(aryMatchingRUID[j])) {
                            longRUID2FeatTime -= System.currentTimeMillis();
                            int aryFeaturesCandidate[] = objRUID2FeatureConnector.getData(aryMatchingRUID[j]);
                            longRUID2FeatTime += System.currentTimeMillis();
                            Arrays.sort(aryFeaturesCandidate);

                            int aryMinFeats[] = aryFeatures;
                            int aryMaxFeats[] = aryFeaturesCandidate;

                            if (aryMinFeats.length > aryMaxFeats.length) {
                                int aryTemp[] = aryMinFeats;
                                aryMinFeats = aryMaxFeats;
                                aryMaxFeats = aryTemp;
                            }

                            IntList objFeats = new IntArrayList(aryMaxFeats);

                            int intOverlap = 0;
                            for (int k = 0; k < aryMinFeats.length; k++) {
                                if (objFeats.remove(aryMinFeats[k])) {
                                    intOverlap++;
                                    objLinkedFeatures.add(aryFeatures[i]);
                                    objLinkedRUIDs.add(intRUID);
                                    objLinkedRUIDs.add(aryMatchingRUID[j]);
                                    arySelStats[Constants.NUMBER_OF_LINKED_LINKS]++;
                                }
                            }

                            objRUIDCandidates.put(aryMatchingRUID[j], intOverlap);
                        }
                    }
                }

                IntIterator objIter = objRUIDCandidates.keySet().iterator();
                while (objIter.hasNext()) {
                    int intCandidateRUID = objIter.next();
                    int intOverlap = objRUIDCandidates.get(intCandidateRUID);
                    
                    int workIDSource = intRUID / this.intWorkNumbering;
                    int workIDTarget = intCandidateRUID / this.intWorkNumbering;
                    
                    if (isSelected(intOverlap) && (workIDSource != workIDTarget) && (intOverlap>=this.intMinOverlap) ) {
                        objWriter.write(intRUID + "\t" + intCandidateRUID + "\t" + intOverlap + "\n");
                        objWriter.write(intCandidateRUID + "\t" + intRUID + "\t" + intOverlap + "\n");
                    }
                    arySelStats[Constants.NUMBER_OF_UNIQUE_LINKS]++;
                }

                if ((h + 1) % 1000 == 0) {
                    ConfigurationContainer.printR("\t\t" + (h + 1)
                            + " re-use units processed. " + (h + 1) * 100 / aryListOfAllRUIDs.length
                            + "% DONE!");
                }
            }
            ConfigurationContainer.printR("\t\t" + aryListOfAllRUIDs.length
                    + " re-use units processed. 100% DONE!\n");

            long longNormalisationFactor = 1000;
            long longEndTime = System.currentTimeMillis();
            long longTotalTime = (longEndTime - longBeginTime) / longNormalisationFactor;

            longRUID2FeatTime /= longNormalisationFactor;
            longFeat2RUIDTime /= longNormalisationFactor;

            long longLinkingTime = longTotalTime - longRUID2FeatTime - longFeat2RUIDTime;

            arySelStats[Constants.NUMBER_OF_LINKED_RUID] = objLinkedRUIDs.size();
            arySelStats[Constants.NUMBER_OF_LINKED_FEATURES] = objLinkedFeatures.size();

            arySelStats[Constants.LINKING_TIME_FOR_RUID2FEAT] = (int) longRUID2FeatTime;
            arySelStats[Constants.LINKING_TIME_FOR_FEAT2RUID] = (int) longFeat2RUIDTime;
            arySelStats[Constants.LINKING_LINK_TIME] = (int) longLinkingTime;

            objLinkedRUIDs.clear();
            objLinkedFeatures.clear();

            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            throw new LinkingException(e);
        }

    }
}

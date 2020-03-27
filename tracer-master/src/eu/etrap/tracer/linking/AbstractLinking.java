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
import bak.pcj.set.IntSet;
import bak.pcj.set.IntOpenHashSet;
import eu.etrap.medusa.config.ClassConfig;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.linking.connector.Connector;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 08.12.2010 13:10:29 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractLinking extends ClassConfig {

    protected int arySelStats[] = null;
    protected String strTaxonomyCode = null;
    protected Connector objRUID2FeatureConnector = null;
    protected Connector objFeature2RUIDConnector = null;
    protected String strRUID2FeatureFile = null;
    protected String strFeature2RUIDFile = null;
    protected String strRUID2FeatureImplementation = null;
    protected String strFeature2RUIDImplementation = null;
    protected int intMinOverlap = -1;

    public void init() throws ConfigurationException {
        this.config();
        this.initRUID2Feat();
        this.initFeat2RUID();
        arySelStats = new int[9];
        
        if (intMinOverlap < 0){
            intMinOverlap = 2;
        }
    }

    protected void initRUID2Feat() throws ConfigurationException {
    }

    protected void initFeat2RUID() throws ConfigurationException {
    }

    public void prepareData() throws LinkingException {
    }

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

                    if (isSelected(intOverlap)) {
                        objWriter.write(intRUID + "\t" + intCandidateRUID + "\t" + intOverlap + "\n");
                        objWriter.write( intCandidateRUID + "\t" + intRUID + "\t" + intOverlap + "\n");
                    }
                    arySelStats[Constants.NUMBER_OF_UNIQUE_LINKS]++;
                }

                /*IntKeyIntMap objRUIDCandidates = new IntKeyIntOpenHashMap();
                
                for (int i = 0; i < aryFeatures.length; i++) {
                longFeat2RUIDTime -= System.currentTimeMillis();
                int aryMatchingRUID[] = objFeature2RUIDConnector.getData(aryFeatures[i]);
                longFeat2RUIDTime += System.currentTimeMillis();
                
                for (int j = 0; j < aryMatchingRUID.length; j++) {
                int intEdgeFrequency = 0;
                if ( isPreSelected( aryMatchingRUID[j], intRUID ) ) {
                if (objRUIDCandidates.containsKey(aryMatchingRUID[j])) {
                intEdgeFrequency = objRUIDCandidates.get(aryMatchingRUID[j]);
                }
                
                intEdgeFrequency++;
                objRUIDCandidates.put(aryMatchingRUID[j], intEdgeFrequency);
                objLinkedFeatures.add(aryFeatures[i]);
                objLinkedRUIDs.add(intRUID);
                objLinkedRUIDs.add(aryMatchingRUID[j]);
                arySelStats[Constants.NUMBER_OF_LINKED_LINKS]++;
                }
                }
                }
                
                IntIterator objIter = objRUIDCandidates.keySet().iterator();
                while (objIter.hasNext()) {
                int intCandidateRUID = objIter.next();
                int intOverlap = objRUIDCandidates.get(intCandidateRUID);
                
                if( isSelected( intOverlap ) ){
                objWriter.write(intRUID + "\t" + intCandidateRUID + "\t" + intOverlap + "\n");
                }
                arySelStats[Constants.NUMBER_OF_UNIQUE_LINKS]++;
                }*/

                if ( (h+1) % 1000 == 0) {
                    ConfigurationContainer.printR("\t\t" + (h+1)
                            + " re-use units processed. " + (h+1) * 100 / aryListOfAllRUIDs.length
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

    protected boolean isPreSelected(int intRUID1, int intRUID2) {
        if (intRUID1 < intRUID2) {
            return true;
        }

        return false;
    }

    protected boolean isSelected(int intOverlap) {
        
        if ( intOverlap >= this.intMinOverlap ){
            return true;
        }
        
        return false;
    }

    public boolean isAlreadyExistent() throws ConfigurationException {
        String strLinkingFileName = FileManager.getLinkingFileName();
        File objLinkingFile = new File(strLinkingFileName);

        if (objLinkingFile.exists()) {
            return true;
        }

        return false;
    }

    public void writeLinkingStats() throws LinkingException {
        try {
            String strFileName = FileManager.getLinkingMetaFileName();
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strFileName));
            objWriter.write("NUMBER_OF_FINGERPRINTED_RUID\t" + this.arySelStats[Constants.NUMBER_OF_FINGERPRINTED_RUID] + "\n");
            objWriter.write("NUMBER_OF_FINGERPRINT_FEATURES\t" + this.arySelStats[Constants.NUMBER_OF_FINGERPRINT_FEATURES] + "\n");
            objWriter.write("NUMBER_OF_LINKED_RUID\t" + this.arySelStats[Constants.NUMBER_OF_LINKED_RUID] + "\n");
            objWriter.write("NUMBER_OF_LINKED_FEATURES\t" + this.arySelStats[Constants.NUMBER_OF_LINKED_FEATURES] + "\n");
            objWriter.write("NUMBER_OF_UNIQUE_LINKS\t" + this.arySelStats[Constants.NUMBER_OF_UNIQUE_LINKS] + "\n");
            objWriter.write("NUMBER_OF_LINKED_LINKS\t" + this.arySelStats[Constants.NUMBER_OF_LINKED_LINKS] + "\n");
            objWriter.write("LINKING_TIME_FOR_RUID2FEAT\t" + this.arySelStats[Constants.LINKING_TIME_FOR_RUID2FEAT] + "\n");
            objWriter.write("LINKING_TIME_FOR_FEAT2RUID\t" + this.arySelStats[Constants.LINKING_TIME_FOR_FEAT2RUID] + "\n");
            objWriter.write("LINKING_LINK_TIME\t" + this.arySelStats[Constants.LINKING_LINK_TIME] + "\n");
            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            throw new LinkingException(e);
        }
    }

    public String getFolderName() {
        String strSourceCorpusFileName = getFileName(this.strRUID2FeatureFile);
        String strInvestigationCorpusFileName = getFileName(this.strFeature2RUIDFile);
        return strSourceCorpusFileName + "-" + strInvestigationCorpusFileName;
    }

    protected String getFileName(String strFullFileName) {
        String aryPathElements[] = strFullFileName.split("/");
        String strFileName = aryPathElements[aryPathElements.length - 1];
        return strFileName.replace(".sel", "");
    }

    public String getFullFolderName() {
        String aryCodes[] = new String[2];
        aryCodes[0] = objRUID2FeatureConnector.getTaxonomyCode();
        aryCodes[1] = objFeature2RUIDConnector.getTaxonomyCode();
        Arrays.sort(aryCodes);

        String strSourceCorpusFileName = getFileName(this.strRUID2FeatureFile);
        String strInvestigationCorpusFileName = getFileName(this.strFeature2RUIDFile);

        String strResult = strTaxonomyCode + "-" + aryCodes[0]
                + "-" + strSourceCorpusFileName + "-" + aryCodes[1]
                + "-" + strInvestigationCorpusFileName;
        return strResult;
    }

    public String getTaxonomyCode() {
        String aryCodes[] = new String[2];
        aryCodes[0] = objRUID2FeatureConnector.getTaxonomyCode();
        aryCodes[1] = objFeature2RUIDConnector.getTaxonomyCode();
        Arrays.sort(aryCodes);

        return strTaxonomyCode + ":" + aryCodes[0] + ":" + aryCodes[1];
    }
}

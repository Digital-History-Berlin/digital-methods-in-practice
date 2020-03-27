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



package eu.etrap.tracer.featuring.semantic;

import bak.pcj.map.ObjectKeyIntMap;
import bak.pcj.map.ObjectKeyIntOpenHashMap;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import eu.etrap.tracer.featuring.Training;
import eu.etrap.tracer.featuring.TrainingException;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 08.12.2010 14:42:38 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class SemanticCooccurrenceTrainingImpl extends AbstractSemanticTraining implements Training {

    protected String strSortedInputFile = null;

    @Override
    public void init() throws ConfigurationException {
        super.init();

        strHierarchy = "01-01-02-00-00";

        sortOrder = new int[]{3, 0, 1, 2};
        sortTypes = new char[]{'i', 'i', 'i', 'I'};
        keyIndex = new int[]{0, 1};
    }

    @Override
    protected void doPreparation() {
        super.doPreparation();

        setGlobalProperty("MEMORY_ALLOCATOR_IMPL",
                "eu.etrap.medusa.config.DistanceBasedCooccurrenceMemoryAllocatorImpl");

        setGlobalProperty("EXPORTER_IMPL",
                "eu.etrap.medusa.export.SpectralCooccurrenceFlatFileExporterImpl");

        setGlobalProperty("PARSER_FILTER_IMPL",
                "eu.etrap.medusa.filter.sidx.IDXDistanceBasedSentenceCooccurrencesFilterImpl");


        this.setProperty("dblSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IDXDistanceBasedSentenceCooccurrencesFilterImpl");
        this.setProperty("intMinimumFrequency", "0", "eu.etrap.medusa.filter.sidx.IDXDistanceBasedSentenceCooccurrencesFilterImpl");
        this.setProperty("dblCutoffSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IDXDistanceBasedSentenceCooccurrencesFilterImpl");
        this.setProperty("intCutoffMinimumFrequency", "0", "eu.etrap.medusa.filter.sidx.IDXDistanceBasedSentenceCooccurrencesFilterImpl");

        this.setGlobalProperty("SIGNIFICANCE_IMPL", "eu.etrap.medusa.significance.FrequencySignificanceImpl");
        this.setGlobalProperty("CUTOFF_SIGNIFICANCE_IMPL", "eu.etrap.medusa.significance.FrequencySignificanceImpl");

        // in order to get integer values as significance measure
        this.setProperty("intAccuracy", "0", "eu.etrap.medusa.export.SpectralCooccurrenceFlatFileExporterImpl");
        this.setProperty("boolExportFrequency", "false", "eu.etrap.medusa.export.SpectralCooccurrenceFlatFileExporterImpl");
    }

    @Override
    public void doDedicatedTrain() throws TrainingException {
        // create co-occurrence inverted list
        super.doDedicatedTrain();

        countFeatureTokenStats(strTrainingFileName, 3);

        doCreateInvertedList();
    }

    @Override
    public void writeOutputFile() throws TrainingException {
        // Sort by feature frequency and first feature

        String strTempFile = ConfigurationContainer.createTempFile();
        ConfigurationContainer.println("\nSorting file " + strTrainingFileName
                + " in " + strTempFile);
        this.sort(strTrainingFileName, strTempFile);
        ConfigurationContainer.println("File is sorted.\n");

        strSortedInputFile = strTempFile;

        super.writeOutputFile();
    }

    @Override
    protected void writeFMAPFile() throws TrainingException {
        String strLine = null;
        int counter = 0;

        try {
            ConfigurationContainer.println("Writing fmap file  " + FileManager.getTrainingFMAPFileName()
                    + " by using temporary sort file in " + strSortedInputFile);

            BufferedReader objReader = new BufferedReader(new FileReader(strSortedInputFile));
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(FileManager.getTrainingFMAPFileName()));

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                String strKey = "";

                for (int i = 0; i < keyIndex.length; i++) {
                    strKey += strSplit[keyIndex[i]].trim() + " ";
                }

                strKey = strKey.trim();
                counter++;

                objWriter.write(counter + "\t" + strKey + "\t"
                        + strSplit[2].trim() + "\n");
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();

            ConfigurationContainer.println("fmap file written.\n");
        } catch (Exception e) {
            throw new TrainingException(e);
        }
    }

    //protected void writeTrainFile(String strFeatureMapFile, String strTempFile, String strOutFile) throws TrainingException {
    @Override
    protected void writeTrainFile() throws TrainingException {
        try {
            ConfigurationContainer.println("Writing train file in " + FileManager.getTrainingTrainFileName());

            HashMap<Integer, HashSet<Integer>> objMappingWord2SID =
                    new HashMap<Integer, HashSet<Integer>>();
            TreeSet<String> objInvertedList = new TreeSet<String>();


            BufferedReader objReader = new BufferedReader(new FileReader(strPositionalInvertedListFileName));

            // lies hier fid --> {sids}
            // lies hashset inverted list IL
            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");

                Integer objWordID = new Integer(strSplit[0].trim());
                Integer objSID = Integer.parseInt(strSplit[1].trim());

                HashSet<Integer> objLocations = null;

                if (objMappingWord2SID.containsKey(objWordID)) {
                    objLocations = objMappingWord2SID.get(objWordID);
                } else {
                    objLocations = new HashSet<Integer>();
                }

                objLocations.add(objSID);
                objMappingWord2SID.put(objWordID, objLocations);

                objInvertedList.add(strLine.trim());
            }

            objReader.close();

            // lies feature map file ein feature --> ID
            objReader = new BufferedReader(new FileReader(FileManager.getTrainingFMAPFileName()));

            ObjectKeyIntMap objFeature2IDMapping = new ObjectKeyIntOpenHashMap();

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");

                int intFeatureID = Integer.parseInt(strSplit[0].trim());
                String strFeature = strSplit[1].trim() + " " + strSplit[2].trim();


                objFeature2IDMapping.put(strFeature, intFeatureID);
            }

            objReader.close();


            // iteriere durch temp sorted liste
            objReader = new BufferedReader(new FileReader(strSortedInputFile));
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(FileManager.getTrainingTrainFileName()));

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");

                // nimm key und bereite das Ersetzen aus der fmap-Datei vor
                String strFeature = strSplit[0].trim() + " " + strSplit[1].trim()
                        + " " + strSplit[2].trim();
                int intFeatureID = objFeature2IDMapping.get(strFeature);

                // nimm das HashSet mit den wenigsten Elementen
                Integer objWord1 = Integer.parseInt(strSplit[0].trim());
                Integer objWord2 = Integer.parseInt(strSplit[1].trim());
                int intWordDist = Integer.parseInt(strSplit[2].trim());


                HashSet<Integer> objLocationsOfWord1 = objMappingWord2SID.get(objWord1);
                Iterator<Integer> objIterator = objLocationsOfWord1.iterator();

                while (objIterator.hasNext()) {
                    // ich brauche hier die folgenden Informationen:
                    // SID+POS des zweiten Wortes
                    Integer objSID = objIterator.next();
                    String strPartKey = objWord1 + "\t" + objSID + "\t";

                    SortedSet<String> objCandidates = objInvertedList.subSet(strPartKey, strPartKey + "\uFFFF");
                    Iterator<String> objIterator2 = objCandidates.iterator();

                    // this happens for example iff word1 and word2 are the same.
                    // then duplicated datasets occur
                    HashSet<String> objDistinctDataSets = new HashSet<String>();

                    while (objIterator2.hasNext()) {
                        String strWord1Occurrence = objIterator2.next();
                        String strPosition = strWord1Occurrence.split("\t")[2].trim();
                        int intPOS = Integer.parseInt(strPosition);

                        String strRequestedKey = objWord2 + "\t" + objSID
                                + "\t" + (intPOS + intWordDist);

                        if (objInvertedList.contains(strRequestedKey)) {
                            objDistinctDataSets.add(objSID + "\t" + intFeatureID
                                    + "\t" + intWordDist);
                        }
                    }

                    Iterator<String> objIter = objDistinctDataSets.iterator();

                    while (objIter.hasNext()) {
                        objWriter.write(objIter.next() + "\n");
                    }

                    objDistinctDataSets.clear();
                }
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();

            ConfigurationContainer.println("Train file written.");
        } catch (Exception e) {
            throw new TrainingException(e);
        }
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        new File(this.strSortedInputFile).delete();
    }
}

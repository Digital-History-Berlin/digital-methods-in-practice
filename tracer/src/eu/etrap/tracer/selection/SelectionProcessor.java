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


package eu.etrap.tracer.selection;

import bak.pcj.IntIterator;
import bak.pcj.map.IntKeyIntMap;
import bak.pcj.map.IntKeyIntOpenHashMap;
import bak.pcj.set.IntOpenHashSet;
import bak.pcj.set.IntSet;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.utils.ClassLoader;
import eu.etrap.tracer.meta.MetaInformationBean;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 04.04.2011 11:30:50 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class SelectionProcessor {

    String strSelectionImplementations[] = null;
    ArrayList<Selection> objSelections = null;
    int arySelStats[] = null;

    public void init(String strLevel3Implementations) throws ConfigurationException {
        strSelectionImplementations = strLevel3Implementations.split(",");
        Arrays.sort(strSelectionImplementations);

        objSelections = new ArrayList<Selection>();
        for (int i = 0; i < strSelectionImplementations.length; i++) {
            Selection objSelection = ClassLoader.loadSelectionImpl(strSelectionImplementations[i].trim());
            objSelection.init();

            if (!objSelection.isAlreadyExistent()) {
                objSelections.add(objSelection);
            }
        }

        ConfigurationContainer.println("\n" + objSelections.size() + " of " + strSelectionImplementations.length
                + " configured selection implementations need to be excecuted. "
                + (strSelectionImplementations.length - objSelections.size())
                + " selection strategies are already pre-computed.");
    }

    public boolean isOpenExistingTask() {
        if (this.objSelections.isEmpty()) {
            return false;
        }

        return true;
    }

    public String[] getUsedSelectionImplementations() {
        return strSelectionImplementations;
    }

    public String getUsedSelectionImplementationsAsString() {
        String strReturnString = "";

        String strImpls[] = this.getUsedSelectionImplementations();

        for (int i = 0; i < strImpls.length; i++) {
            strReturnString += strImpls[i] + ",";
        }

        strReturnString = strReturnString.substring(0, strReturnString.length() - 1);

        return strReturnString;
    }

    public void process(MetaInformationBean objMIBean) throws SelectionException, ConfigurationException {
        String strInFile = determineTrainingInFile(objMIBean);
        String strOutFile = FileManager.getMultipleSelectionFileName();

        if (!new File(strOutFile).exists()) {

            new File(new File(strOutFile).getParent()).mkdirs();

            ConfigurationContainer.println("Selection done from infile=" + strInFile + " to outdir="
                    + strOutFile);

            ConfigurationContainer.println("Weighting features for " + objSelections.size()
                    + " selection strategies.");

            int size = objSelections.size();
            int index = 0;

            while (objSelections.size() > 0) {
                index++;
                Selection objSelection = objSelections.get(0);
                ConfigurationContainer.println("[" + index + "/"
                        + size + "]: " + "Computing features weights for selection strategies "
                        + objSelection.getClass().getCanonicalName());
                objSelection.weightFeatures();
                objSelection.select();

                objSelection.writeSelectionStats();

                ConfigurationContainer.println("[" + index + "/"
                        + size + "]: " + "DONE.\n");

                objSelections.remove(0);
            }

        } else {
            ConfigurationContainer.println("OUTPUT directory outfile="
                    + strOutFile + " already exists");
        }
    }

    public void buildMultipleSelectionFile(MetaInformationBean objMIBean) throws ConfigurationException, SelectionException {
        try {
            ConfigurationContainer.print("\nCreating multiple selection for ... ");
            String strOutFile = FileManager.getMultipleSelectionFileName();
            if (!new File(strOutFile).exists()) {

                String strSmallestSelectionFileName = null;
                long longSmallestSelectionFileNameSize = 0;

                for (int i = 0; i < strSelectionImplementations.length; i++) {
                    String strSelectionOutFile = FileManager.getSelectionFileName(strSelectionImplementations[i]);
                    long longFileSize = new File(strSelectionOutFile).length();

                    if (longSmallestSelectionFileNameSize == 0) {
                        longSmallestSelectionFileNameSize = longFileSize;
                        strSmallestSelectionFileName = strSelectionOutFile;
                    }

                    long longMin = Math.min(longSmallestSelectionFileNameSize, longFileSize);
                    if (longSmallestSelectionFileNameSize > longMin) {
                        longSmallestSelectionFileNameSize = longFileSize;
                        strSmallestSelectionFileName = strSelectionOutFile;
                    }
                }

                BufferedReader objReader = new BufferedReader(new FileReader(strSmallestSelectionFileName));
                String strLine = null;

                HashSet<String> objSelectionData = new HashSet<String>();

                while ((strLine = objReader.readLine()) != null) {
                    objSelectionData.add(strLine.trim());
                }

                objReader.close();

                ConfigurationContainer.println("\n\t[1" + "/" + strSelectionImplementations.length
                        + "]: Still " + objSelectionData.size() + " selected features after processing " + strSmallestSelectionFileName);

                int index = 1;
                for (int i = 0; i < strSelectionImplementations.length; i++) {
                    String strSelectionOutFile = FileManager.getSelectionFileName(strSelectionImplementations[i]);
                    HashSet<String> objNewSelectionData = new HashSet<String>();

                    if (!strSmallestSelectionFileName.trim().equals(strSelectionOutFile)) {
                        objReader = new BufferedReader(new FileReader(strSelectionOutFile));

                        while ((strLine = objReader.readLine()) != null) {
                            if (objSelectionData.contains(strLine.trim())) {
                                objNewSelectionData.add(strLine.trim());
                            }
                        }
                        objReader.close();
                        objSelectionData.clear();
                        objSelectionData = objNewSelectionData;

                        index++;
                        ConfigurationContainer.println("\t[" + index + "/" + strSelectionImplementations.length
                                + "]: Still " + objSelectionData.size()
                                + " selected features after processing " + strSelectionOutFile);
                    }
                }

                ConfigurationContainer.print("Writing final selection file to " + strOutFile + " ...");
                BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutFile));

                Iterator<String> objIter = objSelectionData.iterator();
                while (objIter.hasNext()) {
                    String strSelectionDataSet = objIter.next();
                    objWriter.write(strSelectionDataSet + "\n");
                }

                objWriter.flush();
                objWriter.close();
                ConfigurationContainer.println("DONE!");
                ConfigurationContainer.print( "\tWriting meta information ... " );
                writeSelectionStats();
                ConfigurationContainer.println( "DONE!" );

            } else {
                ConfigurationContainer.println("ALREADY COMPUTED!!");
            }
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }

    public void writeSelectionStats() throws SelectionException {
        try {
            arySelStats = new int[11];
            IntSet objRUIDDistribution = new IntOpenHashSet();
            IntKeyIntMap objFeatureDistribion = new IntKeyIntOpenHashMap();

            // strSelectionFile eq. in file
            String strSelectionFile = FileManager.getMultipleSelectionFileName();
            String strSelectionMetaFileName = strSelectionFile.replace(".sel", ".meta");
            BufferedReader objReader = new BufferedReader(new FileReader(strSelectionFile));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intFeatureID = Integer.parseInt(strSplit[0]);
                int intRUID = Integer.parseInt(strSplit[1]);

                objRUIDDistribution.add(intRUID);

                int intFeatureFrequency = 0;
                if (objFeatureDistribion.containsKey(intFeatureID)) {
                    intFeatureFrequency = objFeatureDistribion.get(intFeatureID);
                }
                intFeatureFrequency++;
                objFeatureDistribion.put(intFeatureID, intFeatureFrequency);
            }

            objReader.close();

            arySelStats[Constants.NUMBER_OF_REUSE_UNITS] = objRUIDDistribution.size();

            IntIterator objIter = objFeatureDistribion.keySet().iterator();
            while (objIter.hasNext()) {
                int intFeatureID = objIter.next();
                int intFeatureFrequency = objFeatureDistribion.get(intFeatureID);
                countFeatureTokenStats(intFeatureFrequency);
            }

            String strFileName = FileManager.getTrainingFMAPFileName();
            objReader = new BufferedReader(new FileReader(strFileName));

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int FeatureID = Integer.parseInt(strSplit[0]);

                if (!objFeatureDistribion.containsKey(FeatureID)) {
                    arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_0]++;
                }
            }

            objReader.close();

            objRUIDDistribution.clear();
            objFeatureDistribion.clear();
            objRUIDDistribution = null;
            objFeatureDistribion = null;

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strSelectionMetaFileName));

            objWriter.write("NUMBER_OF_REUSE_UNITS\t" + arySelStats[Constants.NUMBER_OF_REUSE_UNITS] + "\n");
            objWriter.write("NUMBER_OF_REUSE_UNITS\t" + arySelStats[Constants.NUMBER_OF_REUSE_UNITS] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES\t" + arySelStats[Constants.NUMBER_OF_FEATURE_TYPES] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TOKENS\t" + arySelStats[Constants.NUMBER_OF_FEATURE_TOKENS] + "\n");
            objWriter.write("MAX_FEATURE_FREQUENCY\t" + arySelStats[Constants.MAX_FEATURE_FREQUENCY] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_1\t" + arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_1] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_2\t" + arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_2] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_3\t" + +arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_3] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_4\t" + arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_4] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_5\t" + +arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_5] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_LARGER_THAN_5\t" + arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_LARGER_THAN_5] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_0\t" + arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_0] + "\n");

            objWriter.flush();
            objWriter.close();

        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }

    protected void countFeatureTokenStats(int intFeatureFrequency) {
        this.arySelStats[Constants.NUMBER_OF_FEATURE_TYPES]++;
        this.arySelStats[Constants.NUMBER_OF_FEATURE_TOKENS] += intFeatureFrequency;
        this.arySelStats[Constants.MAX_FEATURE_FREQUENCY] =
                Math.max(arySelStats[Constants.MAX_FEATURE_FREQUENCY], intFeatureFrequency);

        switch (intFeatureFrequency) {
            case 1:
                arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_1]++;
                break;
            case 2:
                arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_2]++;
                break;
            case 3:
                arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_3]++;
                break;
            case 4:
                arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_4]++;
                break;
            case 5:
                arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_5]++;
                break;
            default:
                arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_LARGER_THAN_5]++;
        }
    }

    protected String determineTrainingInFile(MetaInformationBean objMIBean) {
        String strTrainingInFile = "";

        String strOrigCorpusDir = new File(objMIBean.strOrigKorpusFile).getParent();
        String strOrigCorpusFileName = new File(objMIBean.strOrigKorpusFile).getName();

        strTrainingInFile += strOrigCorpusDir + "/";

        String TRACES_HOME = FileManager.getTracesHome();
        strTrainingInFile += TRACES_HOME + "/";
        strTrainingInFile += objMIBean.strPreprocessingFolder + "/";
        strTrainingInFile += objMIBean.strTrainingFolder + "/";
        strTrainingInFile += strOrigCorpusFileName.replace(".txt", ".train");

        return strTrainingInFile;
    }
}

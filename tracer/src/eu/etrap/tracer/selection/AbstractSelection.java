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
import de.uni_leipzig.asv.filesort.FileSort;
import eu.etrap.medusa.config.ClassConfig;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 08.12.2010 13:55:05 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractSelection extends ClassConfig {

    protected int arySelStats[] = null;
    protected double dblFeatureDensity = 0;
    protected String strFeatureDistStoreID = null;
    protected String strTaxonomyCode = null;

    public void init() throws ConfigurationException {
        super.config();

        if (dblFeatureDensity <= 0) {
            throw new ConfigurationException("Feature density is set to 0 for the selection strategy within "
                    + this.getClass().getCanonicalName() + ". This does not make sense since all features would be removed.");
        }

        if (dblFeatureDensity > 1) {
            throw new ConfigurationException("Feature density is set to " + dblFeatureDensity + " for the selection strategy within "
                    + this.getClass().getCanonicalName() + ". Please do choose a feature density larger than 0 and not larger than 1.");
        }


        arySelStats = new int[11];
        strFeatureDistStoreID = "UNSET";
        strTaxonomyCode = "01-00-00-00-00";
    }

    public boolean isAlreadyExistent() throws ConfigurationException {
        String strSelectionFile = FileManager.getSelectionFileName(this.getClass().getName());
        if (new File(strSelectionFile).exists()) {
            return true;
        }

        return false;
    }

    public void writeSelectionStats() throws SelectionException {
        ConfigurationContainer.print( "\tWriting meta information ... " );
        try {
            IntSet objRUIDDistribution = new IntOpenHashSet();
            IntKeyIntMap objFeatureDistribion = new IntKeyIntOpenHashMap();

            // strSelectionFile eq. in file
            String strSelectionFile = FileManager.getSelectionFileName(this.getClass().getName());
            String strSelectionMetaFileName =  strSelectionFile.replace(".sel", ".meta");
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

            objWriter.write( "NUMBER_OF_REUSE_UNITS\t" + arySelStats[Constants.NUMBER_OF_REUSE_UNITS] + "\n");
            objWriter.write("NUMBER_OF_REUSE_UNITS\t" +  arySelStats[Constants.NUMBER_OF_REUSE_UNITS] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES\t" + arySelStats[Constants.NUMBER_OF_FEATURE_TYPES] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TOKENS\t" +  arySelStats[Constants.NUMBER_OF_FEATURE_TOKENS] + "\n");
            objWriter.write("MAX_FEATURE_FREQUENCY\t" +  arySelStats[Constants.MAX_FEATURE_FREQUENCY] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_1\t" +  arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_1] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_2\t" +  arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_2] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_3\t" +  + arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_3] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_4\t" +  arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_4] + "\n") ;
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_5\t" +  + arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_5] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_LARGER_THAN_5\t" +  arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_LARGER_THAN_5] + "\n");
            objWriter.write("NUMBER_OF_FEATURE_TYPES_WITH_FREQ_0\t" +  arySelStats[Constants.NUMBER_OF_FEATURE_TYPES_WITH_FREQ_0] + "\n");

            objWriter.flush();
            objWriter.close();
            ConfigurationContainer.println( "DONE!" );
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

    public void weightFeatures() throws SelectionException {
    }

    public void select() throws SelectionException {
    }

    public String getFolderName() {
        String strClassName = this.getClass().getName();
        int index = strClassName.lastIndexOf(".");
        String strResult = strClassName.substring(index + 1);
        return strResult + "_FeatDens=" + this.dblFeatureDensity;
    }

    public String getFullFolderName() {
        return this.strTaxonomyCode + "-" + getFolderName();
    }

    public String getTaxonomyCode() {
        return this.strTaxonomyCode;
    }

    protected void sortTrainingFile() throws SelectionException {
        try {
            ConfigurationContainer.print("\tSorting file " + FileManager.getTrainingTrainFileName() + " ... ");
            String strSortedFileName = FileManager.getTrainingSortedTrainFileName();

            if (!new File(strSortedFileName).exists()) {
                String strTrainFile = FileManager.getTrainingTrainFileName();

                int sortOrder[] = new int[]{1, 2, 0};
                char sortTypes[] = new char[]{'i', 'i', 'i'};
                FileSort sort = new FileSort("\t", sortOrder, sortTypes);
                sort.sort(strTrainFile, strSortedFileName);
                ConfigurationContainer.println("DONE.");
            } else {
                ConfigurationContainer.println("Already sorted.");
            }
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }
}

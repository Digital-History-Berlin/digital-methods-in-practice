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



package eu.etrap.tracer.scoring;

import bak.pcj.list.IntArrayList;
import bak.pcj.list.IntList;
import eu.etrap.medusa.config.ClassConfig;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.utils.FileManager;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Created on 28.05.2011 11:10:18 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractScoring extends ClassConfig {

    protected double aryScoreStats[] = null;
    protected String strTaxonomyCode = null;
    protected String strFingerpringFile = null;
    protected HashMap<Integer, ArrayList<Integer>> objFingerprints = null;
    protected double dblScoringThreshold = 0.0;
    protected DecimalFormat objFormat = null;

    public void init() throws ConfigurationException {
        super.config();
        strTaxonomyCode = "00-00-00-00-00-00";
        objFingerprints = new HashMap<Integer, ArrayList<Integer>>();

        aryScoreStats = new double[4];
        aryScoreStats[Constants.SCORING_THRESHOLD] = dblScoringThreshold;
        
        objFormat = new DecimalFormat("##0.0000000");
    }

    public void prepareData() throws ScoringException {
        ConfigurationContainer.println("\tPreparing data from " + strFingerpringFile + " ...");

        try {
            BufferedReader objReader =
                    new BufferedReader(new FileReader(this.strFingerpringFile));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intRUID = Integer.parseInt(strSplit[1].trim());
                int intFID = Integer.parseInt(strSplit[0].trim());

                ArrayList<Integer> objRUID2Fingerprint = new ArrayList<Integer>();

                if (objFingerprints.containsKey(intRUID)) {
                    objRUID2Fingerprint = objFingerprints.get(intRUID);
                }

                objRUID2Fingerprint.add(intFID);
                objFingerprints.put(intRUID, objRUID2Fingerprint);
            }

            objReader.close();

            Iterator<Integer> objIter = objFingerprints.keySet().iterator();
            while (objIter.hasNext()) {
                Integer objRUID = objIter.next();
                ArrayList<Integer> objRUID2Fingerprint = objFingerprints.get(objRUID);
                objRUID2Fingerprint = sort(objRUID2Fingerprint);
                objFingerprints.put(objRUID, objRUID2Fingerprint);
            }
        } catch (Exception e) {
            throw new ScoringException(e);
        }
    }

    public void score() throws ScoringException {
        ConfigurationContainer.println("\tScoring data ");
        long longStartTime = System.currentTimeMillis();
        String strLine = null;

        try {
            String strFileName = FileManager.getLinkingFileName();
            ConfigurationContainer.println("\t\tFROM " + strFileName + " ...");
            BufferedReader objReader = new BufferedReader(new FileReader(strFileName));

            String strScoringFileName = FileManager.getScoringFileName();
            ConfigurationContainer.println("\t\tTO " + strScoringFileName + " ...");
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strScoringFileName));

            //String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                aryScoreStats[Constants.SCORING_TOTAL_NUMBER_OF_LINKS]++;
                String strSplit[] = strLine.split("\t");
                Integer objRUID1 = new Integer(strSplit[0]);
                Integer objRUID2 = new Integer(strSplit[1]);

                double dblWeight = weight(objFingerprints.get(objRUID1), objFingerprints.get(objRUID2));
                double dblOverlap = overlap(objFingerprints.get(objRUID1), objFingerprints.get(objRUID2)).size();

                if (selectLink(dblWeight, dblOverlap)) {
                    aryScoreStats[Constants.SCORING_SELECTED_NUMBER_OF_LINKS]++;
                    dblWeight = this.formatScore(dblWeight);
                    //System.out.println( objRUID1 + "\t" + objRUID2 + "\t" + dblOverlap);
                    objWriter.write(objRUID1 + "\t" + objRUID2 + "\t" + (double) dblOverlap + "\t" + dblWeight + "\n");
                }

                if (aryScoreStats[Constants.SCORING_TOTAL_NUMBER_OF_LINKS] % 100000 == 0) {
                    ConfigurationContainer.printR("\t\t" + (long) aryScoreStats[Constants.SCORING_TOTAL_NUMBER_OF_LINKS]
                            + " re-use candidates processed. Selection by scoring is "
                            + objFormat.format( aryScoreStats[Constants.SCORING_SELECTED_NUMBER_OF_LINKS] * 100 / aryScoreStats[Constants.SCORING_TOTAL_NUMBER_OF_LINKS])
                            + "%.");
                }
            }

            ConfigurationContainer.println("");

            long longNormalisationFactor = 1000;
            long longEndTime = System.currentTimeMillis();
            long longTotalTime = (longEndTime - longStartTime) / longNormalisationFactor;
            aryScoreStats[Constants.SCORING_TIME] = longTotalTime;

            objReader.close();
            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            ConfigurationContainer.println("EXCEPTION WHILE PROCESSING LINE \"" + strLine + "\"");
            throw new ScoringException(e);
        }
    }

    protected boolean selectLink(double dblWeight, double dblOverlap) {

        if (dblWeight >= this.dblScoringThreshold && dblOverlap >= 0) {
            return true;
        }

        return false;
    }

    protected double weight(ArrayList<Integer> objFingerprint1, ArrayList<Integer> objFingerprint2) {
        // computer overlap
        return overlap(objFingerprint1, objFingerprint2).size();
    }

    protected IntList overlap(ArrayList<Integer> objFingerprint1, ArrayList<Integer> objFingerprint2) {

        ArrayList<Integer> objLargerFingerprint = null;
        ArrayList<Integer> objSmallerFingerprint = null;

        if (objFingerprint1.size() < objFingerprint2.size()) {
            objLargerFingerprint = new ArrayList<Integer>(objFingerprint2);
            objSmallerFingerprint = objFingerprint1;
        } else {
            objLargerFingerprint = new ArrayList<Integer>(objFingerprint1);
            objSmallerFingerprint = objFingerprint2;
        }

        // computer overlap
        IntList objOverlap = new IntArrayList(objSmallerFingerprint.size());

        IntList objFeats = new IntArrayList(objLargerFingerprint.size());
        for( int i=0; i<objLargerFingerprint.size(); i++ ){
            objFeats.add(objLargerFingerprint.get(i));
        }

        for (int k = 0; k < objSmallerFingerprint.size(); k++) {
            if (objFeats.remove(objSmallerFingerprint.get(k))) {
                objOverlap.add(objSmallerFingerprint.get(k));
            }
        }

        /*Iterator<Integer> objIter = objSmallerFingerprint.iterator();
        while (objIter.hasNext()) {
        Integer objFeature = objIter.next();
        
        if (objLargerFingerprint.contains(objFeature)) {
        objOverlap.add(objFeature);
        objLargerFingerprint.remove(objFeature);
        }
        }*/

        return objOverlap;
    }

    protected ArrayList<Integer> sort(ArrayList<Integer> objFingerprint) {
        Iterator<Integer> objIter = objFingerprint.iterator();
        int aryReturn[] = new int[objFingerprint.size()];

        int index = 0;
        while (objIter.hasNext()) {
            aryReturn[index] = objIter.next();
            index++;
        }

        Arrays.sort(aryReturn);
        ArrayList<Integer> objSortedFingerprint = new ArrayList<Integer>();
        for (int i = 0; i < aryReturn.length; i++) {
            objSortedFingerprint.add(aryReturn[i]);
        }

        return objSortedFingerprint;
    }

    public boolean isAlreadyExistent() throws ConfigurationException {

        String strLinkingFileName = FileManager.getScoringFileName();
        File objLinkingFile = new File(strLinkingFileName);

        if (objLinkingFile.exists()) {
            return true;
        }

        return false;
    }

    public void writeScoringStats() throws ScoringException {
        try {
            String strScoringMetaFileName = FileManager.getScoringMetaFileName();

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strScoringMetaFileName));

            objWriter.write("SCORING_THRESHOLD\t" + aryScoreStats[Constants.SCORING_THRESHOLD] + "\n");
            objWriter.write("SCORING_TOTAL_NUMBER_OF_LINKS\t" + (long) aryScoreStats[Constants.SCORING_TOTAL_NUMBER_OF_LINKS] + "\n");
            objWriter.write("SCORING_SELECTED_NUMBER_OF_LINKS\t" + (long) aryScoreStats[Constants.SCORING_SELECTED_NUMBER_OF_LINKS] + "\n");
            objWriter.write("SCORING_TIME\t" + (long) aryScoreStats[Constants.SCORING_TIME] + "\n");

            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            throw new ScoringException(e);
        }
    }

    public String getFolderName() {
        String strClassName = getClass().getCanonicalName();
        int index = strClassName.lastIndexOf(".");
        String strResult = strClassName.substring(index + 1);
        return strResult + "_Threshold=" + this.dblScoringThreshold;
    }

    public String getFullFolderName() {
        String strResult = getTaxonomyCode() + "-" + getFolderName();
        return strResult;
    }

    public String getTaxonomyCode() {
        return strTaxonomyCode;
    }

    protected double formatScore(double dblScore) {
        int intAccuracy = 10000;
        int intFormattedScore = (int) (dblScore * intAccuracy);
        return (double) intFormattedScore / (double) intAccuracy;
    }
}

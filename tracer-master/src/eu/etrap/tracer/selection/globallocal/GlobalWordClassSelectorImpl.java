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


package eu.etrap.tracer.selection.globallocal;

import eu.etrap.tracer.TracerException;
import eu.etrap.tracer.selection.WordClassWeightsInterface;
import bak.pcj.IntIterator;
import de.uni_leipzig.asv.filesort.FileSort;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import eu.etrap.tracer.preprocessing.Preprocessing;
import eu.etrap.tracer.preprocessing.WordGraphPreparer;
import eu.etrap.tracer.preprocessing.WordLevelPreprocessingImpl;
import eu.etrap.tracer.selection.Selection;
import eu.etrap.tracer.selection.SelectionException;
import eu.etrap.tracer.selection.globalglobal.AbstractGlobalSelection;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 27.12.2010 13:34:00 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class GlobalWordClassSelectorImpl extends AbstractGlobalSelection implements Selection, WordClassWeightsInterface {

    String strWordClassFileName = null;
    HashMap<String, String> objPOSTag = null;

    @Override
    public void init() throws ConfigurationException {
        super.init();
        objPOSTag = new HashMap<String, String>();
        strSortedOutFile = FileManager.getSelectionSortedWordClassFileName();
        strTaxonomyCode = "01-01-02-01-01";
    }

    @Override
    protected void doWeightFeatures() throws SelectionException {
        try {
            loadPOSTags();

            // weight words
            HashMap<String, Double> objWeightedWords = new HashMap<String, Double>();
            Iterator<String> objIter = this.objPOSTag.keySet().iterator();

            while (objIter.hasNext()) {
                String strWord = objIter.next();
                String strTags = objPOSTag.get(strWord);

                String aryTags[] = strTags.split("\t");
                double dblWordWeight = 0.0;

                for (int i = 0; i < aryTags.length; i++) {
                    char charTags = aryTags[i].trim().charAt(0);
                    double dblTagWeight = 1 / (double) aryTags.length * (double) getWordClassWeight(charTags);
                    dblWordWeight += dblTagWeight;
                }

                objWeightedWords.put(strWord.trim(), dblWordWeight);
            }

            // free no longer used hash map
            objPOSTag.clear();
            objPOSTag = null;

            ConfigurationContainer.println("\tWeighting features by word class tags ...");

            HashMap<Integer, String> objFeatsMapping = loadFeatsFile();
            HashMap<Integer, String> objFMAPMapping = loadFMAPFile();

            String strFileName = FileManager.getSelectionWordClassFileName();

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strFileName));
            IntIterator objFeatIter = this.objFeatureDistribution.keySet().iterator();

            int intScaleFactor = 1000;

            while (objFeatIter.hasNext()) {
                int intFeatID = objFeatIter.next();

                if (intFeatID > 0) {
                    String strFeatureKey = objFMAPMapping.get(intFeatID);
                    String aryFeatureKey[] = strFeatureKey.split(" ");

                    double dblTotalFeatureWeight = 0.0;

                    for (int i = 0; i < aryFeatureKey.length; i++) {
                        String strWord = objFeatsMapping.get(new Integer(aryFeatureKey[i].trim()));
                        Double oblWeight = objWeightedWords.get(strWord);

                        if (oblWeight != null) {
                            dblTotalFeatureWeight += oblWeight;
                        }
                    }

                    int intScaledFactor = (int) (dblTotalFeatureWeight * (double) intScaleFactor);

                    objWriter.write(intFeatID + "\t" + intScaledFactor + "\n");
                }
            }

            objWriter.flush();
            objWriter.close();

            int sortOrder[] = new int[]{1, 0};
            char sortTypes[] = new char[]{'i', 'I'};
            FileSort sort = new FileSort("\t", sortOrder, sortTypes);
            sort.sort(strFileName, strSortedOutFile);

        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }

    protected HashMap<Integer, String> loadFMAPFile() throws FileNotFoundException, IOException, ConfigurationException {
        return loadFile(FileManager.getTrainingFMAPFileName());
    }

    protected HashMap<Integer, String> loadFeatsFile() throws FileNotFoundException, IOException, ConfigurationException {
        return loadFile(FileManager.getTrainingFeatsFileName());
    }

    protected HashMap<Integer, String> loadFile(String strFileName) throws FileNotFoundException, IOException {
        HashMap<Integer, String> objMap = new HashMap<Integer, String>();

        BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            objMap.put(new Integer(strSplit[0].trim()), strSplit[1].trim());
        }

        objReader.close();

        return objMap;
    }

    protected int getWordClassWeight(Character objWordClass) {

        int intMorphCode = 0;

        switch (objWordClass) {
            case 'n':
                intMorphCode = WordClassWeightsInterface.NOUN;
                break;
            case 'v':
                intMorphCode = WordClassWeightsInterface.VERB;
                break;
            case 't':
                intMorphCode = WordClassWeightsInterface.PARTICIPLE;
                break;
            case 'a':
                intMorphCode = WordClassWeightsInterface.ADJECTIVE;
                break;
            case 'd':
                intMorphCode = WordClassWeightsInterface.ADVERB;
                break;
            case 'p':
                intMorphCode = WordClassWeightsInterface.PRONOUN;
                break;
            case 'm':
                intMorphCode = WordClassWeightsInterface.NUMERAL;
                break;
            case 'i':
                intMorphCode = WordClassWeightsInterface.INTERJECTION;
                break;
            case 'l':
                intMorphCode = WordClassWeightsInterface.ARTICLE;
                break;
            case 'g':
                intMorphCode = WordClassWeightsInterface.PARTICLE;
                break;
            case 'c':
                intMorphCode = WordClassWeightsInterface.CONJUNCTION;
                break;
            case 'r':
                intMorphCode = WordClassWeightsInterface.PREPOSITION;
                break;
            case 'e':
                intMorphCode = WordClassWeightsInterface.EXCLAMATION;
                break;
            case 'u':
                intMorphCode = WordClassWeightsInterface.PUNCTUATION;
                break;
            case '-':
                intMorphCode = WordClassWeightsInterface.DEFAULT;
                break;
            default:
                intMorphCode = WordClassWeightsInterface.DEFAULT;
        }

        return intMorphCode;
    }

    protected void loadPOSTags() throws FileNotFoundException, IOException, ConfigurationException, TracerException {
        ConfigurationContainer.println("\tLoad pos tags for words from file " + strWordClassFileName);

        WordGraphPreparer objPrep = new WordGraphPreparer();
        objPrep.init();
        objPrep.prepare();
        objPrep = null;

        Preprocessing objPreprocess = new WordLevelPreprocessingImpl();
        objPreprocess.init();

        BufferedReader objReader = new BufferedReader(new FileReader(strWordClassFileName));

        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");

            String strWord = objPreprocess.execute( strSplit[0].trim() );
            String strMorphCode = strSplit[2].trim();
            Character strWordClass = strMorphCode.charAt(0);

            String strResult = "";
            if (objPOSTag.containsKey(strWord)) {
                strResult = objPOSTag.get(strWord);
            }
            strResult += "\t" + strWordClass.toString();

            objPOSTag.put(strWord, strResult.trim());
        }

        objReader.close();
        ConfigurationContainer.println("\t" + objPOSTag.size()
                + " unique words loaded.");
    }

    @Override
    protected void buildByteArray() throws SelectionException {
        super.buildByteArray();

        try {
            BufferedReader objReader = new BufferedReader(new FileReader( FileManager.getSelectionSortedWordClassFileName() ));
            String strLine = null;

            while (intNumberOfAlreadySelectedTokens < this.intNumberOfSelectedTokens) {
                strLine = objReader.readLine();
                int intFeatID = Integer.parseInt(strLine.split("\t")[0].trim());

                int intFeatFreq = objFeatureDistribution.get(intFeatID);
                if (intFeatFreq > 0) {
                    setSelectedFeature(intFeatID);
                    intNumberOfAlreadySelectedTokens += intFeatFreq;
                }
            }

            objReader.close();
        } catch (Exception e) {
            throw new SelectionException(e);
        }
    }
}

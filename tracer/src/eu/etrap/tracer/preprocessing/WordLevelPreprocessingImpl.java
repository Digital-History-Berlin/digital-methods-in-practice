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


package eu.etrap.tracer.preprocessing;

import eu.etrap.tracer.Constants;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 07.12.2010 16:57:22 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class WordLevelPreprocessingImpl extends AbstractPreprocessing implements Preprocessing {

    protected boolean boolLemmatisation = false;
    protected boolean boolReplaceSynonyms = false;
    protected boolean boolReplaceStringSimilarWords = false;
    protected boolean boolReplaceWordByWordLength = false;
    protected boolean boolReplaceByReducedString = false;
    protected int intMinWordLengthThreshold = 0;
    protected int intNGramSize = 0;
    protected boolean weigthByLogLikelihoodRatio = false;
    protected HashMap<String, String> objWord2Lemma = null;
    protected HashMap<String, String> objWord2Synonym = null;
    protected HashMap<String, String> objWord2StringSimilarWord = null;
    protected HashMap<String, String> objWord2ReducedString = null;

    @Override
    public void init() throws ConfigurationException {
        super.init();

        if (intNGramSize < 1) {
            intNGramSize = 4;
        }

        objWord2Lemma = new HashMap<String, String>();
        objWord2Synonym = new HashMap<String, String>();
        objWord2StringSimilarWord = new HashMap<String, String>();
        objWord2ReducedString = new HashMap<String, String>();

        if (boolLemmatisation) {
            String strWord2LemFile = FileManager.getCleanedBaseformFileName();

            if (!new File(strWord2LemFile).exists()) {
                throw new ConfigurationException("Word to lemma file not found! File name: " + strWord2LemFile);
            }

            ConfigurationContainer.print("Starting to load mapping from word to lemma from "
                    + strWord2LemFile + " ...");
            try {
                this.loadMappingFile(strWord2LemFile, this.objWord2Lemma);
            } catch (Exception e) {
                throw new ConfigurationException(e);
            }

            ConfigurationContainer.println(" FINISHED. Loaded " + objWord2Lemma.size() + " datasets.");
        }

        if (boolReplaceSynonyms) {
            String strMappingFile = FileManager.getCleanedSynonymWordFileName();

            if (!new File(strMappingFile).exists()) {
                throw new ConfigurationException("Word to synonym file not found! File name: " + strMappingFile);
            }

            ConfigurationContainer.print("Starting to load mapping from word to synonym from "
                    + strMappingFile + " ...");
            try {
                this.loadMappingFile(strMappingFile, objWord2Synonym);
            } catch (Exception e) {
                throw new ConfigurationException(e);
            }

            ConfigurationContainer.println(" FINISHED. Loaded " + objWord2Synonym.size() + " datasets.");
        }

        if (boolReplaceStringSimilarWords) {
            String strMappingFile = FileManager.getCleanedStringSimilarWordFileName();

            if (!new File(strMappingFile).exists()) {
                throw new ConfigurationException("Word to string similar word file not found! File name: " + strMappingFile);
            }

            ConfigurationContainer.print("Starting to load mapping from word to string similar word from "
                    + strMappingFile + " ...");
            try {
                this.loadMappingFile(strMappingFile, objWord2StringSimilarWord);
            } catch (Exception e) {
                throw new ConfigurationException(e);
            }

            ConfigurationContainer.println(" FINISHED. Loaded " + objWord2StringSimilarWord.size() + " datasets.");
        }

        if (this.boolReplaceByReducedString) {
            String strMappingFile = FileManager.getCleanedLengthReducedWordsFileName(intNGramSize, weigthByLogLikelihoodRatio);

            if (!new File(strMappingFile).exists()) {
                throw new ConfigurationException("Word to length reduced word file not found! File name: " + strMappingFile);
            }

            ConfigurationContainer.print("Starting to load mapping from word to length reduced word from "
                    + strMappingFile + " ...");
            try {
                this.loadMappingFile(strMappingFile, objWord2ReducedString);
            } catch (Exception e) {
                throw new ConfigurationException(e);
            }

            ConfigurationContainer.println(" FINISHED. Loaded " + objWord2ReducedString.size() + " datasets.");
        }
    }

    protected void loadMappingFile(String strGraphFileName, HashMap<String, String> objWordGraphEdges) throws FileNotFoundException, IOException {
        BufferedReader objReader = new BufferedReader(new FileReader(strGraphFileName));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            objWordGraphEdges.put(strSplit[0].trim(), strSplit[1].trim());
        }

        objReader.close();

    }

    public String execute(String strLine) {
        // the result string is build in objBuffer
        StringBuilder objBuffer = new StringBuilder();

        ArrayList<int[]> objWhitespaces = getWhitespacePositions(strLine);
        Iterator<int[]> objIter = objWhitespaces.iterator();
        int intStart = 0;
        int intEnd = 0;

        while (objIter.hasNext()) {
            intEnd = ((int[]) objIter.next())[0];

            if (intEnd - intStart > 0) {
                String strOrigWord = strLine.substring(intStart, intEnd).trim();
                String strWord = strOrigWord;
                this.countStats[Constants.ALL_WORDS]++;

                // lemmatisierung
                if (boolLemmatisation) {
                    String strTmpWord = processLemmatisation(strWord);

                    if (!strWord.equals(strTmpWord)) {
                        countStats[Constants.LEMMATISATION_INDEX]++;
                    }

                    strWord = strTmpWord;
                }


                // synonyms
                if (boolReplaceSynonyms) {
                    String strTmpWord = processSynonyms(strWord);

                    if (!strWord.equals(strTmpWord)) {
                        countStats[Constants.SYNONYM_INDEX]++;
                    }

                    strWord = strTmpWord;
                }

                // replace string similar words
                if (boolReplaceStringSimilarWords) {
                    String strTmpWord = processReplaceStringSimilarWords(strWord);

                    if (!strWord.equals(strTmpWord)) {
                        countStats[Constants.STRING_SIMILARITY_INDEX]++;
                    }

                    strWord = strTmpWord;
                }

                // replace reduced strings
                if (boolReplaceByReducedString) {
                    String strTmpWord = processReplaceReducedString(strWord);

                    if (!strWord.equals(strTmpWord)) {
                        countStats[Constants.LENGTH_REDUCED_WORDs_INDEX]++;
                    }

                    strWord = strTmpWord;
                }

                // lowercase
                if (boolMakeAllLowerCase) {
                    String strTmpWord = processLowerCases(strWord);

                    if (!strWord.equals(strTmpWord)) {
                        countStats[Constants.LOWER_CASE_INDEX]++;
                    }

                    strWord = strTmpWord;
                }

                // diachritics
                if (boolRemoveDiachritics) {
                    String strTmpWord = this.processRemoveDiachritics(strWord);

                    if (!strWord.equals(strTmpWord)) {
                        countStats[Constants.REMOVE_DIACHRITICS]++;
                    }

                    strWord = strTmpWord;
                }


                // word length
                if (boolReplaceWordByWordLength) {
                    String strTmpWord = this.processWord2WordLength(strWord);

                    if (!strWord.equals(strTmpWord)) {
                        countStats[Constants.WORD_LENGTH_INDEX]++;
                    }

                    strWord = strTmpWord;
                }


                if (!strWord.equals(strOrigWord)) {
                    countStats[Constants.OVERALL_CHANGED_TOKENS_INDEX]++;
                }

                objBuffer.append(strWord.trim() + " ");
                intStart = intEnd;
            }
        }

        return objBuffer.toString();
    }

    protected String getData(String strWord, HashMap<String, String> objMapping) {
        String strResult = objMapping.get(strWord);

        if (strResult == null) {
            return strWord;
        }

        return strResult;
    }

    protected String processLemmatisation(String strWord) {
        return getData(strWord, this.objWord2Lemma);
    }

    protected String processSynonyms(String strWord) {
        return getData(strWord, this.objWord2Synonym);
    }

    protected String processReplaceStringSimilarWords(String strWord) {
        return getData(strWord, this.objWord2StringSimilarWord);
    }

    private String processReplaceReducedString(String strWord) {
        return getData(strWord, this.objWord2ReducedString);
    }

    public String getFolderName() throws IOException {
        String stInputFileName = ConfigurationContainer.getSentenceFileName();
        String strDirectory = new File(stInputFileName).getParentFile().toString();

        String strFolderName = strDirectory + "/TRACER_DATA/01-02-WLP-";

        // add parameters
        strFolderName += "lem_" + this.boolLemmatisation + "_";
        strFolderName += "syn_" + this.boolReplaceSynonyms + "_";
        strFolderName += "ssim_" + this.boolReplaceStringSimilarWords + "_";
        strFolderName += "redwo_" + this.boolReplaceByReducedString + "-ngram_" + this.intNGramSize
                + "-LLR_" + weigthByLogLikelihoodRatio + "_";
        strFolderName += "toLC_" + this.boolMakeAllLowerCase + "_";
        strFolderName += "rDia_" + this.boolRemoveDiachritics + "_";
        strFolderName += "w2wl_" + boolReplaceWordByWordLength + "-wlt_" + this.intMinWordLengthThreshold;

        return strFolderName;
    }

    protected String processWord2WordLength(String strWord) {
        String strResult = strWord.trim();

        if (strResult.length() >= intMinWordLengthThreshold) {
            strResult = Integer.toString(strResult.length());
        }

        return strResult;
    }
}

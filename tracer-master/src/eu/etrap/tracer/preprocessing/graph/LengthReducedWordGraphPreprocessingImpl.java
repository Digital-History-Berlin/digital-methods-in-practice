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
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.TracerException;
import eu.etrap.tracer.math.LogLikelihoodRatioCalculator;
import eu.etrap.tracer.utils.FileManager;
import org.apache.commons.math.MathException;

/**
 *
 * This implementation reduce a word the most frequent/most significant
 * substring of length n.
 * UNderlying there is an observation that many words in ancient greek
 * differ just by smaller changes. On the one hand this could be solved
 * by a tree. However, it is not really good decideable if a prefix or
 * a suffix tree is chosen. For this reason it is decided for the most
 * frequent or most significant substring regarding the entire corpus.
 * 
 * Created on 01.03.2011 09:37:25 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class LengthReducedWordGraphPreprocessingImpl extends AsymmetricWordGraphHandlerImpl implements WordGraphPreprocessing {

    protected HashMap<String, Double> objKnownNGrams = null;
    protected HashMap<String, Double> objCharacterDist = null;
    protected long longExpectationDenumerator = 0;
    protected long longNumberWhitespace = 0;
    protected int intNumberOfExperiments = 0;
    protected LogLikelihoodRatioCalculator objCalc = null;
    protected int intNGramSize = 0;
    protected boolean weigthByLogLikelihoodRatio = false;

    @Override
    public void init() throws ConfigurationException {
        super.init();
        objKnownNGrams = new HashMap<String, Double>();
        objCharacterDist = new HashMap<String, Double>();
        longExpectationDenumerator = 0;
        longNumberWhitespace = 0;
        intNumberOfExperiments = 0;
        objCalc = new LogLikelihoodRatioCalculator();

        if (intNGramSize < 1) {
            intNGramSize = 4;
        }
    }

    public void preprocessing() throws TracerException {

        String strInFile = FileManager.getLengthReducedWordsFileName(intNGramSize, weigthByLogLikelihoodRatio);
        String strOutFile = FileManager.getCleanedLengthReducedWordsFileName(intNGramSize, weigthByLogLikelihoodRatio);

        this.computeReducedString();
        this.loadInputFile(strInFile);
        this.reduceGraph();
        this.writeOutputFile(strOutFile);
    }

    protected void computeReducedString() throws TracerException {

        String strNGramFileName = FileManager.getCharNGramDistFileName(intNGramSize);
        String strCharDistFileName = FileManager.getCharDistFileName();

        ConfigurationContainer.println("Creating reduced string graph by using ngram distribution in " + strNGramFileName
                + " as well as character distribution in " + strCharDistFileName
                + " and weigthByLogLikelihoodRatio=" + weigthByLogLikelihoodRatio);

        try {
            // load letter n-gram file
            loadNGramFile(strNGramFileName);


            // load character distribution n-gram file
            loadCharDistFile(strCharDistFileName);
            objCalc.init(1, longExpectationDenumerator);

            ConfigurationContainer.println("Totoal number of loaded ngram types is " + objKnownNGrams.size());
            ConfigurationContainer.println("Total number of loaded character types is " + objCharacterDist.size());

            // iterate through word list
            String strWNCFileName = ConfigurationContainer.getWordNumbersCompleteName();
            BufferedReader objReader = new BufferedReader(new FileReader(strWNCFileName));

            String strLengthReducedWordsGraphOutFile =
                    FileManager.getLengthReducedWordsFileName(intNGramSize, weigthByLogLikelihoodRatio);
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strLengthReducedWordsGraphOutFile));

            String strLine = null;

            int intNumberOfWords = 0;
            int intNumberOfDifferentDecision = 0;

            while ((strLine = objReader.readLine()) != null) {
                intNumberOfWords++;
                String strSplit[] = strLine.split("\t");
                String strWord = Constants.strWordStart + strSplit[1].trim()
                        + Constants.strWordEnd;

                if (strSplit[1].trim().length() < intNGramSize + 1) {
                    /*objWriter.write( strSplit[1].trim() + "\t" + strSplit[1].trim()
                    + "\t0\n");*/
                } else {
                    String strWordArgMaxFreqWeight = null;
                    double dblWordArgMaxFreqWeight = 0;

                    String strWordArgMaxLGLWeight = null;
                    double dblWordArgMaxLGLWeight = 0;


                    // segementize words into n-grams
                    for (int i = 0; i < strWord.length() - intNGramSize + 1; i++) {

                        // iterate through selected n-grams if count(n-grams) > 1 (words that
                        // have the same size as the n-gram size or smaller)
                        String strSubString = strWord.substring(i, i + intNGramSize);


                        // weight the ngram
                        double dblNgramFreqWeight = weightLetterNgramByFrequency(strSubString);
                        double dblNgramLGLWeight = weightLetterNgramByLikelihood(strSubString, strWord);
                        //System.out.println(strWord + "\t" + strSubString + "\t" + dblNgramFreqWeight);

                        // arg max on freq weights
                        if (dblNgramFreqWeight > dblWordArgMaxFreqWeight) {
                            dblWordArgMaxFreqWeight = dblNgramFreqWeight;
                            strWordArgMaxFreqWeight = strSubString;
                        }

                        // arg max on lgl weights
                        if (dblNgramLGLWeight > dblWordArgMaxLGLWeight) {
                            dblWordArgMaxLGLWeight = dblNgramLGLWeight;
                            strWordArgMaxLGLWeight = strSubString;
                        }
                    }

                    // write the results out

                    if (weigthByLogLikelihoodRatio) {
                        objWriter.write(strSplit[1].trim() + "\t" + strWordArgMaxLGLWeight
                                + "\t" + dblWordArgMaxLGLWeight + "\n");
                    } else {
                        objWriter.write(strSplit[1].trim() + "\t" + strWordArgMaxFreqWeight
                                + "\t" + dblWordArgMaxFreqWeight + "\n");
                    }

                    if (!strWordArgMaxFreqWeight.equals(strWordArgMaxLGLWeight)) {
                        intNumberOfDifferentDecision++;
                    }
                }
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();

            ConfigurationContainer.println("Reduced string graph created. HINT: "
                    + intNumberOfDifferentDecision + " out of " + intNumberOfWords
                    + " words differ by their reduced strings between frequency weights and log-likelihood weights.");
        } catch (Exception e) {
            throw new TracerException(e);
        }
    }

    protected double weightLetterNgramByFrequency(String strNGram) {
        Double objTemp = objKnownNGrams.get(strNGram);
        double dblNgramFreq = 0;

        if (objTemp != null) {
            dblNgramFreq = objTemp;
        }
        return dblNgramFreq;
    }

    protected double weightLetterNgramByLikelihood(String strNGram, String strWord) throws MathException {
        long longLetterFreqs[] = new long[strNGram.length()];

        for (int j = 0; j < longLetterFreqs.length; j++) {
            //System.out.println( "Process char=" + strNGram.substring(j, j + 1));
            longLetterFreqs[j] = Math.round(objCharacterDist.get(strNGram.substring(j, j + 1)));
        }
        //longLetterFreqs[longLetterFreqs.length - 1] = longNumberWhitespace;

        double dblExp = objCalc.computeExpectation(longLetterFreqs);
        
        //System.out.println( strNGram +  "\t" + objKnownNGrams.get(strNGram));
        double dblChi2 = objCalc.approximateChi2ByLoglikelihoodRatio(intNumberOfExperiments, Math.round(objKnownNGrams.get(strNGram)), longLetterFreqs);
        double dblErrorProb = objCalc.computeErrorProbability(intNumberOfExperiments, Math.round(objKnownNGrams.get(strNGram)), longLetterFreqs);
        int intDegreeOfFreedom = objCalc.getDegreeOfFreedom();

        /*     System.out.println(strWord + "\t" + strNGram + "\t" + Math.round(objKnownNGrams.get(strNGram))
        + "\t" + Math.round(objKnownNGrams.get(strNGram)) / (double) longExpectationDenumerator
        + "\t" + dblExp + "\t" + ((double)  Math.round(objKnownNGrams.get(strNGram)) / (double) longExpectationDenumerator - dblExp)
        + "\t" + dblChi2
        + "\t" + dblErrorProb
        + "\t" + intDegreeOfFreedom
        + "");
         */

        return dblChi2;
    }

    protected void loadNGramFile(String strNGramFileName) throws Exception {
        loadFile(strNGramFileName, objKnownNGrams);
        intNumberOfExperiments = (int) Math.round(objKnownNGrams.get("%TOTOAL%"));
    }

    protected void loadCharDistFile(String strCharDistFileName) throws Exception {
        loadFile(strCharDistFileName, objCharacterDist);
        longExpectationDenumerator = Math.round(objCharacterDist.get("%TOTOAL%"));
        longNumberWhitespace = Math.round(objCharacterDist.get("%WHITESPACE%"));
        int intNumberOfTokens = getWordTokens();
        objCharacterDist.put(Constants.strWordStart, (double) intNumberOfTokens);
        objCharacterDist.put(Constants.strWordEnd, (double) intNumberOfTokens);
        longExpectationDenumerator -= longNumberWhitespace;
        longExpectationDenumerator += 2 * intNumberOfTokens;
    }

    protected int getWordTokens() {
        // TODO: set value? From where?
        return 973;
    }

    protected void loadFile(String strInFile, HashMap<String, Double> objMap) throws Exception {
        BufferedReader objReader = new BufferedReader(new FileReader(strInFile));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            objMap.put(strSplit[1].trim(), new Double(strSplit[3].trim()));
        }

        objReader.close();
    }
}

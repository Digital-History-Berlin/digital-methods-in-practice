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



package eu.etrap.tracer.phd.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.math.LogLikelihoodRatioCalculator;
import org.apache.commons.math.MathException;

/**
 *
 * @author mbuechler
 */
public class StringFragmentTest {

    private static HashMap<String, Double> objKnownNGrams = new HashMap<String, Double>();
    private static HashMap<String, Double> objCharacterDist = new HashMap<String, Double>();
    private static long longExpectationDenumerator = 0;
    private static long longNumberWhitespace = 0;
    private static int intNumberOfExperiments = 0;
    private static LogLikelihoodRatioCalculator objCalc = new LogLikelihoodRatioCalculator();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // This implementation reduce a word the most frequent/most significant
        // substring of length n.
        // UNderlying there is an observation that many words in ancient greek
        // differ just by smaller changes. On the one hand this could be solved
        // by a tree. However, it is not really good decideable if a prefix or
        // a suffix tree is chosen. For this reason it is decided for the most
        // frequent or most significant substring regarding the entire corpus.

        // decide for n-gram length
        int intNGramSize = 4;

        String strNGramFileName = "/home/mbuechler/Development/Traces/data/corpora/example/example.txt.tok.dist.letter.0" + intNGramSize + "gram";
        String strCharDistFileName = "/home/mbuechler/Development/Traces/data/corpora/example/example.txt.tok.dist.char";
        System.out.println(strNGramFileName);
        System.out.println(strCharDistFileName);

        try {
            // load letter n-gram file
            loadNGramFile(strNGramFileName);


            // load character distribution n-gram file
            loadCharDistFile(strCharDistFileName);
            objCalc.init(1, longExpectationDenumerator);

            System.out.println("objKnownNGrams=" + objKnownNGrams.size());
            System.out.println("objCharacterDist=" + objCharacterDist.size());

            // iterate through word list
            String strWNCFileName = "/home/mbuechler/Development/Traces/data/corpora/example/example.txt.wnc";
            BufferedReader objReader = new BufferedReader(new FileReader(strWNCFileName));
            String strLine = null;

            int intNumberOfWords = 0;
            int intNumberOfDifferentDecision = 0;

            while ((strLine = objReader.readLine()) != null) {
                intNumberOfWords++;
                String strSplit[] = strLine.split("\t");
                String strWord = '\u3014' + strSplit[1].trim() + '\u3015';

                if (strWord.length() < intNGramSize) {
                    //System.out.println("Ignoring word=" + strWord);
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
                    System.out.println("word=" + strWord + "\t(FRQ)="
                            + strWordArgMaxFreqWeight + "\t" + dblWordArgMaxFreqWeight
                            + "\t\t(LGL)=" + strWordArgMaxLGLWeight + "\t" + dblWordArgMaxLGLWeight);

                    if (!strWordArgMaxFreqWeight.equals(strWordArgMaxLGLWeight)) {
                        intNumberOfDifferentDecision++;
                        System.out.println("FOUND");
                    }
                }
            }

            System.out.println("intNumberOfDifferentDecision=" + intNumberOfDifferentDecision);
            System.out.println("intNumberOfWords=" + intNumberOfWords);

            objReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static double weightLetterNgramByFrequency(String strNGram) {
        Double objTemp = objKnownNGrams.get(strNGram);
        double dblNgramFreq = 0;

        if (objTemp != null) {
            dblNgramFreq = objTemp;
        }
        return dblNgramFreq;
    }

    private static double weightLetterNgramByLikelihood(String strNGram, String strWord) throws MathException {
        long longLetterFreqs[] = new long[strNGram.length()];

        for (int j = 0; j < longLetterFreqs.length; j++) {
            //System.out.println( "Process char=" + strNGram.substring(j, j + 1));
            longLetterFreqs[j] = Math.round(objCharacterDist.get(strNGram.substring(j, j + 1)));
        }
        //longLetterFreqs[longLetterFreqs.length - 1] = longNumberWhitespace;

        double dblExp = objCalc.computeExpectation(longLetterFreqs);
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

    public static void loadNGramFile(String strNGramFileName) throws Exception {
        loadFile(strNGramFileName, objKnownNGrams);
        intNumberOfExperiments = (int) Math.round(objKnownNGrams.get("%TOTOAL%"));
    }

    public static void loadCharDistFile(String strCharDistFileName) throws Exception {
        loadFile(strCharDistFileName, objCharacterDist);
        longExpectationDenumerator = Math.round(objCharacterDist.get("%TOTOAL%"));
        longNumberWhitespace = Math.round(objCharacterDist.get("%WHITESPACE%"));
        int intNumberOfTokens = getWordTokens();
        objCharacterDist.put( Constants.strWordStart, (double)intNumberOfTokens);
        objCharacterDist.put( Constants.strWordEnd, (double)intNumberOfTokens);
        longExpectationDenumerator -= longNumberWhitespace;
        longExpectationDenumerator += 2*intNumberOfTokens;
    }

    private static int getWordTokens(){
        return 1003;
    }

    private static void loadFile(String strInFile, HashMap<String, Double> objMap) throws Exception {
        BufferedReader objReader = new BufferedReader(new FileReader(strInFile));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            objMap.put(strSplit[1].trim(), new Double(strSplit[3].trim()));
        }

        objReader.close();
    }
}

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



package eu.etrap.tracer.math;

import java.util.Random;

/**
 *
 * @author mbuechler
 */
public class OneLetterRandomWordGeneratorMain {

    private static String min;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Random objRandom = new Random(0);

        double dblProbabilityOfA = 0.8;
        int intScale = 1000;
        int intNumberOfIterations = 10000000;
        int intThreshold = (int) Math.round(dblProbabilityOfA * intScale);

        int counter = 0;
        int aryCountFrequencies[] = new int[10000];


        for (int i = 0; i < intNumberOfIterations; i++) {
            int intValue = objRandom.nextInt(intScale);
            intValue++;

            if (intValue <= intThreshold) {
                counter++;
            } else {
                aryCountFrequencies[counter]++;
                counter = 0;
            }
        }


        long sum = 0;
        for (int i = 0; i < aryCountFrequencies.length; i++) {
            sum += aryCountFrequencies[i];
        }

        System.out.println("sum=" + sum);

        LogLikelihoodRatioCalculator objCalc = new LogLikelihoodRatioCalculator();
        objCalc.init(1, intNumberOfIterations);


        int intFreqLetter = (int) Math.round(dblProbabilityOfA * intNumberOfIterations);
        int intFreqWhitespace = intNumberOfIterations - intFreqLetter;
        System.out.println( "intFreqLetter=" + intFreqLetter );
        System.out.println( "intFreqWhitespace=" + intFreqWhitespace);

        double aryDbl[] = new double[2];

        try {
            for (int i = 0; i < aryCountFrequencies.length; i++) {
                if (aryCountFrequencies[i] != 0) {
                //if (i<14) {
                    long longLetterFreqs[] = new long[i+1];

                    for( int j=0; j<longLetterFreqs.length-1; j++ ){
                        longLetterFreqs[j]=intFreqLetter;
                    }

                    longLetterFreqs[longLetterFreqs.length-1]=intFreqWhitespace;

                    double dblExp = objCalc.computeExpectation(longLetterFreqs);
                    double dblChi2 = objCalc.approximateChi2ByLoglikelihoodRatio(sum, aryCountFrequencies[i], longLetterFreqs);
                    double dblErrorProb = objCalc.computeErrorProbability(sum, aryCountFrequencies[i], longLetterFreqs);
                    int intDegreeOfFreedom = objCalc.getDegreeOfFreedom();

                    aryDbl[0] += dblErrorProb;
                    aryDbl[1]++;

                    System.out.println(i + "\t" + aryCountFrequencies[i]
                            + "\t" + (double) aryCountFrequencies[i] / (double) sum
                            + "\t" + dblExp + "\t" + ((double) aryCountFrequencies[i] / (double) sum -dblExp)
                            + "\t" + dblChi2 +
                            "\t" + dblErrorProb +
                            "\t" + intDegreeOfFreedom +
                            "");
                }
            }

            System.out.println( "error sum is     " + aryDbl[0]);
            System.out.println( "entries are      " + aryDbl[1]);
            System.out.println( "average error is " + aryDbl[0]/aryDbl[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

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
public class RedundancyOneLetterRandomWordGeneratorMain {

    private static String min;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Random objRandom = new Random(0);

        double dblProbabilityOfA = 0.6;
        int intScale = 1000;
        int intNumberOfIterations = 210000000;
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
        System.out.println("intFreqLetter=" + intFreqLetter);
        System.out.println("intFreqWhitespace=" + intFreqWhitespace);

        double aryDbl[] = new double[2];
        double dblMCWL = 0.0;

        try {
            for (int i = 0; i < aryCountFrequencies.length; i++) {
                if (aryCountFrequencies[i] != 0) {
                    double dblLetterEntropy = 0;
                    dblLetterEntropy += (double) intFreqLetter / (double) intNumberOfIterations * Math.log((double) intFreqLetter / (double) intNumberOfIterations) / Math.log(2);
                    dblLetterEntropy += (double) intFreqWhitespace / (double) intNumberOfIterations * Math.log((double) intFreqWhitespace / (double) intNumberOfIterations) / Math.log(2);
                    dblLetterEntropy *= -1;


                    int length = (i + 1);
                    double dblLengthInBit = length * dblLetterEntropy;
                    double dblSelfInformation = -1 * Math.log((double) aryCountFrequencies[i] / (double) sum) / Math.log(2);
                    double dblDiff = dblLengthInBit- dblSelfInformation;
                    
                    System.out.println(i + "\t" + aryCountFrequencies[i]
                            + "\t" + (double) aryCountFrequencies[i] / (double) sum
                            + "\t" + length + "\t" + dblLengthInBit + "\t" + dblSelfInformation + "\t" + dblDiff );

                    dblMCWL += dblLengthInBit * (double) aryCountFrequencies[i] / (double) sum;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("dblMCWL=" + dblMCWL);
    }
}

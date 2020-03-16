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

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ChiSquaredDistribution;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;

/**
 * Created on 07.03.2011 14:04:13 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class LogLikelihoodRatioCalculator {

    private long longExpectationDenumerator = -1;
    private ChiSquaredDistribution objDist = null;
    private int intDegreeOfFreedom = -1;

    public void init(int intDegreeOfFreedom, long longExpectationDenumerator) {
        this.intDegreeOfFreedom = intDegreeOfFreedom;
        this.longExpectationDenumerator = longExpectationDenumerator;

        init();
    }

    public void init() {
        if (intDegreeOfFreedom < 1) {
            objDist = new ChiSquaredDistributionImpl(1);
        } else {
            objDist = new ChiSquaredDistributionImpl(intDegreeOfFreedom);
        }
    }

    public void setDegreeOfFreedom(int intDegreeOfFreedom) {
        this.intDegreeOfFreedom = intDegreeOfFreedom;
    }

    public int getDegreeOfFreedom() {
        return this.intDegreeOfFreedom;
    }

    public double computeLikelihoodRatio(long longNumberOfTrials, long longNumberOfObservations, long longSingleOccurrences[]) {
        double dblExpectationProb = computeExpectation(longSingleOccurrences);

        double dblLLR = computeLikelihoodRatio(longNumberOfTrials, longNumberOfObservations, dblExpectationProb);

        return dblLLR;
    }

    public double computeExpectation(long longSingleOccurrences[]) {
        double dblExpProb = 0;

        for (int i = 0; i < longSingleOccurrences.length; i++) {
            dblExpProb += Math.log(longSingleOccurrences[i]) / Math.log(2)
                    - Math.log(longExpectationDenumerator) / Math.log(2);
        }

        return Math.pow(2, dblExpProb);
    }

    public double computeLikelihoodRatio(long longNumberOfTrials, long longNumberOfObservations, double dblExpectationProb) {
        
        // compute observation prob
        double dblObservationProb = (double) longNumberOfObservations / (double) longNumberOfTrials;
        //System.out.println("\n\nProbs\t" + dblExpectationProb + "\t" + dblObservationProb);

        double dblNullHypothesisLikelihood = computeLikelihood(longNumberOfTrials, longNumberOfObservations, dblExpectationProb);
        double dblAlternativeHypothesisLikelihood = computeLikelihood(longNumberOfTrials, longNumberOfObservations, dblObservationProb);
        //System.out.println("likes\t" + dblAlternativeHypothesisLikelihood + "\t" + dblNullHypothesisLikelihood);

        return dblAlternativeHypothesisLikelihood - dblNullHypothesisLikelihood;
    }

    public double approximateChi2ByLoglikelihoodRatio(long longNumberOfTrials, long longNumberOfObservations, double dblExpectationProb) {
        // actually it must be -2*lambda. Since both hypothesis in the fraction of
        // the method computeLikelihoodRatio are changed, it is here just 2 and
        // not -2.
        return 2 * computeLikelihoodRatio(longNumberOfTrials, longNumberOfObservations, dblExpectationProb);
    }

    public double approximateChi2ByLoglikelihoodRatio(long longNumberOfTrials, long longNumberOfObservations, long longSingleOccurrences[]) {
        // actually it must be -2*lambda. Since both hypothesis in the fraction of
        // the method computeLikelihoodRatio are changed, it is here just 2 and
        // not -2.
        return 2 * computeLikelihoodRatio(longNumberOfTrials, longNumberOfObservations, longSingleOccurrences);
    }

    public double computeErrorProbability(long longNumberOfTrials, long longNumberOfObservations, double dblExpectationProb) throws MathException {
        double dblChi2Value = approximateChi2ByLoglikelihoodRatio(longNumberOfTrials, longNumberOfObservations, dblExpectationProb);

        objDist.setDegreesOfFreedom(intDegreeOfFreedom);
        double dblErrorProbability = objDist.cumulativeProbability(dblChi2Value);

        return 1 - dblErrorProbability;
    }

    public double computeErrorProbability(long longNumberOfTrials, long longNumberOfObservations, long longSingleOccurrences[]) throws MathException {
        double dblChi2Value = approximateChi2ByLoglikelihoodRatio(longNumberOfTrials, longNumberOfObservations, longSingleOccurrences);
        
        this.intDegreeOfFreedom = detectDegreeOfFreedom(longSingleOccurrences);

        objDist.setDegreesOfFreedom( intDegreeOfFreedom );
        double dblErrorProbability = objDist.cumulativeProbability(dblChi2Value);

        return 1 - dblErrorProbability;
    }

    public int detectDegreeOfFreedom(long dblSingleOccurrences[]) {
        if (dblSingleOccurrences.length == 1) {
            return 1;
        }

        return dblSingleOccurrences.length - 1;
    }

    public double computeLikelihood(long longNumberOfTrials, long longNumberOfObservations, double dblProbability) {
        double dblLikelihood = (double) longNumberOfObservations * Math.log(dblProbability)
                + ((double) longNumberOfTrials - (double) longNumberOfObservations) * Math.log(1 - dblProbability);

        //System.out.println( dblProbability + "\t1\t" + (double) longNumberOfObservations * Math.log(dblProbability) );
        //System.out.println( dblProbability + "\t2\t" + ((double) longNumberOfTrials - (double) longNumberOfObservations) * Math.log(1 - dblProbability) );
        return dblLikelihood;
    }

    public static void main(String args[]) {

        try {
            LogLikelihoodRatioCalculator objCalc = new LogLikelihoodRatioCalculator();

            objCalc.init(1, 1000);

            double dblExp = objCalc.computeExpectation(new long[]{800, 800, 800, 800, 800, 200});
            System.out.println("dblExp=" + dblExp);

            double dblLLR = objCalc.computeLikelihoodRatio(199883, 13000, new long[]{800, 800, 800, 800, 800, 200});
            double dblChi2 = objCalc.approximateChi2ByLoglikelihoodRatio(199883, 13000, new long[]{800, 800, 800, 800, 800, 200});
            double dblErrorProb = objCalc.computeErrorProbability(199883, 13000, new long[]{800, 800, 800, 800, 800, 200});

            System.out.println("dblLLR=" + dblLLR);
            System.out.println("dblChi2=" + dblChi2);
            System.out.println("dblErrorProb=" + dblErrorProb);

            /*objCalc.longNullHypothesisDenumerator = 50;
            double dblLLR = objCalc.approximateChi2ByLoglikelihoodRatio(50, 20, new long[]{10});
            double dblErrorProb = objCalc.objDist.cumulativeProbability(dblLLR);
            System.out.println(dblLLR + "\t" + (1 - dblErrorProb) + "\t" + objCalc.computeErrorProbability(50, 20, new long[]{10}));

            objCalc.longNullHypothesisDenumerator = 100;
            //double dblChi2 = objCalc.approximateChi2ByLoglikelihoodRatio(500, 200, new long[]{100});
            double dblChi2 = objCalc.approximateChi2ByLoglikelihoodRatio(100, 40, new long[]{20});
            dblErrorProb = objCalc.objDist.cumulativeProbability(dblChi2);
            System.out.println(dblChi2 + "\t" + (1 - dblErrorProb) + "\t" + objCalc.computeErrorProbability(100, 40, new long[]{20}));

            objCalc.longNullHypothesisDenumerator = 10000;
            dblChi2 = objCalc.approximateChi2ByLoglikelihoodRatio(10000, 1100, new long[]{1000});
            dblErrorProb = objCalc.objDist.cumulativeProbability(dblChi2);
            System.out.println(dblChi2 + "\t" + (1 - dblErrorProb) + "\t" + objCalc.computeErrorProbability(10000, 1100, new long[]{1000}));*/
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

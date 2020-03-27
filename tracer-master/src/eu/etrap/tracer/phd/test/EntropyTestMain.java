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

import java.util.Random;

/**
 *
 * @author mbuechler
 */
public class EntropyTestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int N = 6097057;
        long iterations = 181;
        long aryInt[] = new long[N];
        Random objRandom = new Random();

        for (int j = 0; j < iterations; j++) {
            for (int i = 0; i < N; i++) {
                aryInt[i]++;
                aryInt[objRandom.nextInt(N)]++;
            }
        }

        double entropy = 0;
        for (int i = 0; i < aryInt.length; i++) {
            double p = (double) aryInt[i] / (double) (2 * N * iterations);
            entropy += -1 * p * Math.log(p) / Math.log(2);
        }
        System.out.println("H    =" + entropy);
        double max = (Math.log(N) / Math.log(2));
        System.out.println("H_max=" + max);
        System.out.println("diff=" + (max - entropy));
    }
}

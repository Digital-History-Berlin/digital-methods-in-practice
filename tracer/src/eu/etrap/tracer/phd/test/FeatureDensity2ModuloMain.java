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

import bak.pcj.set.IntSet;
import bak.pcj.set.IntOpenHashSet;
import java.util.Random;

/**
 *
 * @author mbuechler
 */
public class FeatureDensity2ModuloMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        double dblFDValue = 1.0/5.0;
        int max = 100000;

        int intAccuracy = (int)Math.round( Math.pow(10, getAccuracy(dblFDValue)  ) );
        int intNumberOfSelectedClasses = (int) Math.round(dblFDValue * intAccuracy);

        System.out.println("p=" + intNumberOfSelectedClasses);
        IntSet objSelected = new IntOpenHashSet();
        Random objRandom = new Random(0);

        while (objSelected.size() < intNumberOfSelectedClasses) {
            objSelected.add(objRandom.nextInt(intAccuracy));
        }

        System.out.println("size=" + objSelected.size());

        int counter = 0;

        for (int i = 1; i <= max; i++) {


            if (objSelected.contains(i % intAccuracy)) {
                counter++;
            }
        }

        System.out.println("Selected " + counter + " out of " + max);
        System.out.println("Selection percentage=" + ((double) counter / (double) max));
    }

    public static int getAccuracy(double dblFDValue) {

        String strFeatureDensityAsString = Double.toString(dblFDValue);
        int intPositionOfSeparator = strFeatureDensityAsString.indexOf(".")+1;

        int intAccuracy = strFeatureDensityAsString.length()-intPositionOfSeparator;

        System.out.println( "dblFDValue=" +  dblFDValue);
        System.out.println( "strFeatureDensityAsString.length()=" +  strFeatureDensityAsString.length());
        System.out.println( "intPositionOfSeparator=" +  intPositionOfSeparator);
        System.out.println( "intAccuracy=" +  intAccuracy);

        return Math.min(intAccuracy, 3);
    }
}

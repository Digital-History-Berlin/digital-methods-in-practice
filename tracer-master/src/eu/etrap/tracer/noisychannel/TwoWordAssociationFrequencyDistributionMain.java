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



package eu.etrap.tracer.noisychannel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;

/**
 *
 * @author mbuechler
 */
public class TwoWordAssociationFrequencyDistributionMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int size = 1000;
        int ary[] = new int[size + 1];
        String strFile = args[0];
        System.out.println( "Processing " + strFile);
        
        try {
            BufferedReader objReader = new BufferedReader(new FileReader(strFile));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                //System.out.println(strLine);
                String strSplit[] = strLine.split("\t");
                int intFreq = Integer.parseInt(strSplit[2]);
                ary[0]++;

                if (intFreq <= size) {
                    ary[intFreq]++;
                }

            }
            objReader.close();

            int sum = -1*ary[0];
            DecimalFormat df =   new DecimalFormat  ( "0.00000" );
            BufferedWriter objWriter = new BufferedWriter( new FileWriter(strFile + ".fdist" ) ); 
            for (int i = 0; i <= size; i++) {
                sum += ary[i];
                double dblCumProb = (double)sum/(double)ary[0];
                objWriter.write(i + "\t" + ary[i] + "\t" + sum
                        + "\t" + df.format(dblCumProb) + "\n");
            }
            
            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

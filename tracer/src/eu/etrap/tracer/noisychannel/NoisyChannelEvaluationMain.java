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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author mbuechler
 */
public class NoisyChannelEvaluationMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String strHomeDir = "";
        String strFile = args[0];
        try {
            String strCoocFileName = strHomeDir + strFile + ".eu.etrap.medusa.filter.sidx.IDXSentenceFilterImpl.hash.lgl2.expo";
            String strBigramFileName = strHomeDir + strFile + ".eu.etrap.medusa.filter.sidx.IDXNeighbourhoodFilterImpl.hash.lgl2.expo";
            String strRandomCoocFileName = strHomeDir + strFile + ".tok.ncrb.eu.etrap.medusa.filter.sidx.IDXSentenceFilterImpl.hash.lgl2.expo";
            String strRandomBigramFileName = strHomeDir + strFile + ".tok.ncrb.eu.etrap.medusa.filter.sidx.IDXNeighbourhoodFilterImpl.hash.lgl2.expo";

            processFile(strCoocFileName, strCoocFileName + ".dist");
            processFile(strRandomCoocFileName, strRandomCoocFileName + ".dist");
            computeSignalNoiseRatio(strCoocFileName + ".dist", strRandomCoocFileName + ".dist", "co-oc");

            processFile(strBigramFileName, strBigramFileName + ".dist");
            processFile(strRandomBigramFileName, strRandomBigramFileName + ".dist");
            computeSignalNoiseRatio(strBigramFileName + ".dist", strRandomBigramFileName + ".dist", "bigram");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void computeSignalNoiseRatio(String strNaturalFileName, String strRandomFileName, String strOutPrefixe) throws FileNotFoundException, IOException {
        System.out.println("\n\nCompute Signal Noise Ratio between ...");
        System.out.println("... " + strNaturalFileName);

        HashMap<Long, Long> objNaturalData = new HashMap<Long, Long>();
        HashMap<Long, Long> objRandomData = new HashMap<Long, Long>();

        long minValue = 10000000;
        long maxValue = -10000000;
        long maxNaturalScore = -10000000;
        long maxRandomScore = -10000000;

        BufferedReader objReader = new BufferedReader(new FileReader(strNaturalFileName));
        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            Long objKey = Long.parseLong(strSplit[0]);
            Long objValue = Long.parseLong(strSplit[2]);

            minValue = Math.min(minValue, objKey);
            maxValue = Math.max(maxValue, objKey);
            maxNaturalScore = Math.max(maxNaturalScore, objValue);

            objNaturalData.put(objKey, objValue);
        }
        objReader.close();

        System.out.println("objNaturalData=" + objNaturalData.size());


        System.out.println("... " + strRandomFileName);
        objReader = new BufferedReader(new FileReader(strRandomFileName));
        strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            Long objKey = Long.parseLong(strSplit[0]);
            Long objValue = Long.parseLong(strSplit[2]);

            minValue = Math.min(minValue, objKey);
            maxValue = Math.max(maxValue, objKey);
            maxRandomScore = Math.max(maxRandomScore, objValue);

            objRandomData.put(objKey, objValue);
        }
        objReader.close();

        System.out.println("objRandomData=" + objRandomData.size());
        System.out.println("\nminValue=" + minValue);
        System.out.println("maxValue=" + maxValue);

        double dblNaturalValue = maxNaturalScore;
        double dblRandomValue = maxRandomScore;

        String strParent = new File(strNaturalFileName).getParent();
        strParent += "." + strOutPrefixe + ".nse";
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strParent));

        for (long i = minValue; i <= maxValue; i++) {

            if (objNaturalData.containsKey(i)) {
                dblNaturalValue = (double) Math.max(1, objNaturalData.get(i));
            }

            if (objRandomData.containsKey(i)) {
                dblRandomValue = (double) Math.max(1, objRandomData.get(i));
            }

            double snr = 10 * Math.log(dblNaturalValue / dblRandomValue) / Math.log(((double) 10));
            objWriter.write(i + "\t"
                    + dblNaturalValue + "\t"
                    + dblRandomValue + "\t" + snr + "\n");

        }

        objWriter.flush();
        objWriter.close();
    }

    protected static void processFile(String strFileName, String strOutFileName) throws FileNotFoundException, IOException {
        System.out.println("\n\nProcessing file " + strFileName);
        BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
        String strLine = null;

        long time1 = System.currentTimeMillis();

        HashMap<Long, Long> objMap = new HashMap<Long, Long>();
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            //System.out.println(strLine);
            double dblValue = Double.parseDouble(strSplit[3]);

            if ( dblValue >= 10 ) {
                long longValue = Math.round(Math.floor(dblValue));
                long freq = 0;

                if (objMap.containsKey(longValue)) {
                    freq = objMap.get(longValue);
                }

                freq++;
                objMap.put(longValue, freq);
            }
        }

        long time2 = System.currentTimeMillis() - time1;

        objReader.close();

        Iterator<Long> objIter = objMap.keySet().iterator();
        long minValue = 1000000;
        long maxValue = -1000000;
        long totalcount = 0;
        while (objIter.hasNext()) {
            Long objKey = objIter.next();
            Long objValue = objMap.get(objKey);
            minValue = Math.min(minValue, objKey);
            maxValue = Math.max(maxValue, objKey);
            totalcount += objValue;
        }
        System.out.println("minValue=" + minValue);
        System.out.println("maxValue=" + maxValue);
        System.out.println("totalcount=" + totalcount);

        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutFileName));
        for (long i = minValue; i <= maxValue + 1; i++) {
            Long objValue = 0L;

            if (objMap.containsKey(i)) {
                objValue = objMap.get(i);
            }

            objWriter.write(i + "\t" + objValue + "\t" + totalcount + "\n");
            totalcount -= objValue;
        }
        objWriter.flush();
        objWriter.close();
    }
}

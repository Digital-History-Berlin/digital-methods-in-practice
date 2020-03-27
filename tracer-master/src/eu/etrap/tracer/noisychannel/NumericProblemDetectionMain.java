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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

/**
 *
 * @author mbuechler
 */
public class NumericProblemDetectionMain {

    private static long longNumberWordTokens = 0;
    private static long longNumberWordTypens = 0;
    private static long longCoocWordTokens = 0;
    private static long longCoocWordTypens = 0;
    private static long longBigramWordTokens = 0;
    private static long longBigramWordTypens = 0;
    private static long longSelectedCoocTokens = 0;
    private static long longSelectedBigramTokens = 0;
    private static HashMap<Integer, Integer> objWordFreqs = null;
    private static double dblThreshold = 0.0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String strCorpusFileName = args[0];//"/home/mbuechler/Dissertation/RandomnessStructure/DEU-10k/DEU-10k.txt";
        objWordFreqs = new HashMap<Integer, Integer>();
        dblThreshold = Double.parseDouble(args[1]);
        
        try {
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strCorpusFileName + ".stat." + dblThreshold));
            DecimalFormat df = new DecimalFormat("0.00000");
            processWNC(strCorpusFileName + ".wnc");
            objWriter.write("longNumberWordTokens=" + longNumberWordTokens + "\n");
            objWriter.write("longNumberWordTypens=" + longNumberWordTypens + "\n");

            processCooc(strCorpusFileName + ".eu.etrap.medusa.filter.sidx.IDXSentenceFilterImpl.hash.lgl2.expo");
            objWriter.write("\n\nlongCoocWordTokens=" + longCoocWordTokens + "\n");
            objWriter.write("longCoocWordTypens=" + longCoocWordTypens + "\n");
            computeCooc(strCorpusFileName + ".eu.etrap.medusa.filter.sidx.IDXSentenceFilterImpl.hash.lgl2.expo");
            objWriter.write("longSelectedCoocTokens=" + longSelectedCoocTokens + "\n");
            objWriter.write("sel cooc ration=" + df.format((double) longSelectedCoocTokens / (double) longCoocWordTokens) + "\n");


            processBigram(strCorpusFileName + ".eu.etrap.medusa.filter.sidx.IDXNeighbourhoodFilterImpl.hash.lgl2.expo");
            objWriter.write("\n\nlongBigramWordTokens=" + longBigramWordTokens + "\n");
            objWriter.write("longBigramWordTypens=" + longBigramWordTypens + "\n");
            computeBigram(strCorpusFileName + ".eu.etrap.medusa.filter.sidx.IDXNeighbourhoodFilterImpl.hash.lgl2.expo");
            objWriter.write("longSelectedBigramTokens=" + longSelectedBigramTokens + "\n");
            objWriter.write("sel cooc ration=" + df.format((double) longSelectedBigramTokens / (double) longBigramWordTokens) + "\n");

            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void processWNC(String strCorpusFileName) throws FileNotFoundException, IOException {
        BufferedReader objReader = new BufferedReader(new FileReader(strCorpusFileName));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            longNumberWordTypens++;
            longNumberWordTokens += Integer.parseInt(strLine.split("\t")[3]);
            objWordFreqs.put(Integer.parseInt(strLine.split("\t")[0]), Integer.parseInt(strLine.split("\t")[3]));
        }

        objReader.close();
    }

    protected static void processCooc(String strCorpusFileName) throws FileNotFoundException, IOException {
        BufferedReader objReader = new BufferedReader(new FileReader(strCorpusFileName));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            longCoocWordTypens++;
            longCoocWordTokens += Integer.parseInt(strLine.split("\t")[2]);
        }

        objReader.close();
    }

    protected static void computeCooc(String strCorpusFileName) throws FileNotFoundException, IOException {
        BufferedReader objReader = new BufferedReader(new FileReader(strCorpusFileName));
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strCorpusFileName + ".stat." + dblThreshold));

        double dblRatio = (double) longCoocWordTokens / (double) longNumberWordTokens / (double) longNumberWordTokens;
        System.out.println("dblRatio=" + dblRatio);

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            int word1 = Integer.parseInt(strLine.split("\t")[0]);
            int word2 = Integer.parseInt(strLine.split("\t")[1]);
            int freq = Integer.parseInt(strLine.split("\t")[2]);

            double dblValue = (double) objWordFreqs.get(word1) * (double) objWordFreqs.get(word2) * dblRatio / (double)freq;

            if (dblValue >= dblThreshold) {
                objWriter.write(strLine + "\t" + dblValue + "\n");
                longSelectedCoocTokens++;
            }
        }

        objReader.close();
        objWriter.flush();
        objWriter.close();
    }

    protected static void processBigram(String strCorpusFileName) throws FileNotFoundException, IOException {
        BufferedReader objReader = new BufferedReader(new FileReader(strCorpusFileName));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            longBigramWordTypens++;
            longBigramWordTokens += Integer.parseInt(strLine.split("\t")[2]);
        }

        objReader.close();
    }

    protected static void computeBigram(String strCorpusFileName) throws FileNotFoundException, IOException {
        BufferedReader objReader = new BufferedReader(new FileReader(strCorpusFileName));
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strCorpusFileName + ".stat." + dblThreshold));

        double dblRatio = (double) longBigramWordTokens / (double) longNumberWordTokens / (double) longNumberWordTokens;
        System.out.println("dblRatio=" + dblRatio);

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            int word1 = Integer.parseInt(strLine.split("\t")[0]);
            int word2 = Integer.parseInt(strLine.split("\t")[1]);
            int freq = Integer.parseInt(strLine.split("\t")[2]);

            double dblValue = (double) objWordFreqs.get(word1) * (double) objWordFreqs.get(word2) * dblRatio / (double)freq;

            if (dblValue >= dblThreshold) {
                objWriter.write(strLine + "\t" + dblValue + "\n");
                longSelectedBigramTokens++;
            }
        }

        objReader.close();
        objWriter.flush();
        objWriter.close();
    }
}

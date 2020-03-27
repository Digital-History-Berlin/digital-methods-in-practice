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


package eu.etrap.tracer.postprocessing.JOCCH;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import bak.pcj.map.IntKeyIntMap;
import bak.pcj.map.IntKeyIntOpenHashMap;
import bak.pcj.map.ObjectKeyIntMap;
import bak.pcj.map.ObjectKeyIntOpenHashMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author mbuechler
 */
public class TextReuseTemperatureMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            ObjectKeyIntMap objAuthors = loadAuthorsFile();
            HashMap<Integer, String> objRUID2Work = loadRUID2WorkFile();

            System.out.println(objAuthors.size());
            System.out.println(objRUID2Work.size());


            //String strScoringFileName = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/PerseusGreek-PerseusGreekBiGramLem.score";

            String strScoringFileName = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/new/PerseusGreek-PerseusGreekLemWord.score";
            BufferedReader objReader = new BufferedReader(new FileReader(strScoringFileName));

            String strLine = null;

            IntKeyIntMap objFrequencies = new IntKeyIntOpenHashMap();
            IntKeyIntMap objContaminationFrequencies = new IntKeyIntOpenHashMap();
            IntKeyIntMap objSelfContaminationFrequencies = new IntKeyIntOpenHashMap();
            IntKeyIntMap objContaminatedFrequencies = new IntKeyIntOpenHashMap();

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intRUID = Integer.parseInt(strSplit[0]);

                int intFreq = 0;
                if (objFrequencies.containsKey(intRUID)) {
                    intFreq = objFrequencies.get(intRUID);
                }

                intFreq++;
                objFrequencies.put(intRUID, intFreq);

                int intRUID2 = Integer.parseInt(strSplit[1]);

                String strWorkTitle1 = objRUID2Work.get(intRUID);
                String strWorkTitle2 = objRUID2Work.get(intRUID2);

                /* if (strWorkTitle1.equals(strWorkTitle2)) {
                intFreq = 0;
                if (objSelfContaminationFrequencies.containsKey(intRUID)) {
                intFreq = objSelfContaminationFrequencies.get(intRUID);
                }

                intFreq++;
                objSelfContaminationFrequencies.put(intRUID, intFreq);
                } else {*/
                String strAuthor1 = strWorkTitle1.split(": ")[0];
                String strAuthor2 = strWorkTitle2.split(": ")[0];

             /*   if( strAuthor1.equals("Plato") ){
                 System.out.println( "--> " + strAuthor1 + "\t" + strAuthor2
                         + "\t" + objAuthors.containsKey(strAuthor1.trim()) 
                         + "\t" + objAuthors.containsKey(strAuthor2.trim()) );
                }*/

                if (objAuthors.containsKey(strAuthor1) && objAuthors.containsKey(strAuthor2)) {
                    int intDatingAuthor1 = objAuthors.get(strAuthor1);
                    int intDatingAuthor2 = objAuthors.get(strAuthor2);

                    if (intDatingAuthor1 <= intDatingAuthor2) {
                        //System.out.println(strWorkTitle1 + "\t" + strWorkTitle2 + "\t"
                          //      + intDatingAuthor1 + "\t" + intDatingAuthor2);
                        
                        intFreq = 0;
                        if (objContaminationFrequencies.containsKey(intRUID)) {
                            intFreq = objContaminationFrequencies.get(intRUID);
                        }

                        intFreq++;
                        objContaminationFrequencies.put(intRUID, intFreq);

                    }else {
                    intFreq = 0;
                    if (objContaminatedFrequencies.containsKey(intRUID)) {
                    intFreq = objContaminatedFrequencies.get(intRUID);
                    }

                    intFreq++;
                    objContaminatedFrequencies.put(intRUID, intFreq);
                    }
                }
                //}
            }

            System.out.println(objFrequencies.size());

            String strGnuPlotFile = loadGnuplotFile();

            String strCorpusFileName = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/PerseusGreek.txt";
            BufferedReader objCorpusReader = new BufferedReader(new FileReader(strCorpusFileName));
            BufferedWriter objTemperatureWriter = new BufferedWriter(new FileWriter(strCorpusFileName + ".trt"));
            BufferedWriter objWorkTemperatureWriter = null;
            BufferedWriter objWorkContaminationTemperatureWriter = null;
            BufferedWriter objWorkSelfContaminationTemperatureWriter = null;
            BufferedWriter objWorkContaminatedTemperatureWriter = null;


            String strOldWorkName = "";
            int intInternalRUID = 0;

            File objDir = new File(strCorpusFileName).getParentFile();
            File objTextReuseTemperaturesFolder = new File(objDir.getPath() + "/TextRe-useTemperatures");
            objTextReuseTemperaturesFolder.mkdirs();

            int counter = 0;
            while ((strLine = objCorpusReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");

                int intRUID = Integer.parseInt(strSplit[0].trim());


                /*String strWorkCheck[] = strSplit[3].split(": ");
                if (strWorkCheck.length == 2 && strWorkCheck[1].trim().equals("Machine readable text")) {
                strSplit[3] = strWorkCheck[0] + ": UNKNOWN WORK " + counter + " " + strWorkCheck[1];
                }*/

                String strTRTOutFile = objTextReuseTemperaturesFolder.getPath()
                        + "/" + strSplit[3].trim().replaceAll(": ", "_").replaceAll(" ", "_").replace("'", "_") + ".trt";


                if (!strOldWorkName.equals(strSplit[3].trim())) {

                    System.out.println(++counter + "\tProcessing work " + strSplit[3]);

                    if (objWorkTemperatureWriter != null) {
                        objWorkTemperatureWriter.flush();
                        objWorkTemperatureWriter.close();
                        System.out.println("\t" + strTRTOutFile);

                        objWorkContaminationTemperatureWriter.flush();
                        objWorkSelfContaminationTemperatureWriter.flush();
                        objWorkContaminatedTemperatureWriter.flush();
                        objWorkContaminationTemperatureWriter.close();
                        objWorkSelfContaminationTemperatureWriter.close();
                        objWorkContaminatedTemperatureWriter.close();
                    }



                    intInternalRUID = 0;
                    strOldWorkName = strSplit[3].trim();

                    writeGnuplotFile(strTRTOutFile, strGnuPlotFile, strOldWorkName);
                    writeGnuplotFile(strTRTOutFile.replace(".trt", ".cont.trt"), strGnuPlotFile, strOldWorkName);
                    writeGnuplotFile(strTRTOutFile.replace(".trt", ".self_cont.trt"), strGnuPlotFile, strOldWorkName);
                    writeGnuplotFile(strTRTOutFile.replace(".trt", ".contd.trt"), strGnuPlotFile, strOldWorkName);

                    objWorkTemperatureWriter =
                            new BufferedWriter(new FileWriter(strTRTOutFile));
                    System.out.println("\tCreate Writer\t" + strTRTOutFile);

                    objWorkContaminationTemperatureWriter = new BufferedWriter(new FileWriter(strTRTOutFile.replace(".trt", ".cont.trt")));
                    objWorkSelfContaminationTemperatureWriter = new BufferedWriter(new FileWriter(strTRTOutFile.replace(".trt", ".self_cont.trt")));
                    objWorkContaminatedTemperatureWriter = new BufferedWriter(new FileWriter(strTRTOutFile.replace(".trt", ".contd.trt")));
                }

                intInternalRUID++;

                int intFrequency = objFrequencies.get(intRUID);
                intFrequency++;
                double dblTemperature = Math.log(intFrequency) / Math.log(10);

                int intContFrequency = objContaminationFrequencies.get(intRUID);
                intContFrequency++;
                double dblContTemperature = Math.log(intContFrequency) / Math.log(10);

                int intSelfContFrequency = objSelfContaminationFrequencies.get(intRUID);
                intSelfContFrequency++;
                double dblSelfContTemperature = Math.log(intSelfContFrequency) / Math.log(10);

                int intContdFrequency = objContaminatedFrequencies.get(intRUID);
                intContdFrequency++;
                double dblContdTemperature = Math.log(intContdFrequency) / Math.log(10);



                // add relative work
                objTemperatureWriter.write(intRUID + "\t" + intInternalRUID + "\t"
                        + (intFrequency - 1) + "\t" + dblTemperature
                        + "\t1"
                        + "\t" + strSplit[3] + "\n");
                objTemperatureWriter.write(intRUID + "\t" + intInternalRUID + "\t"
                        + (intFrequency - 1) + "\t" + dblTemperature
                        + "\t0"
                        + "\t" + strSplit[3] + "\n\n");


                objWorkTemperatureWriter.write(intRUID + "\t" + intInternalRUID + "\t"
                        + (intFrequency - 1) + "\t" + dblTemperature
                        + "\t1"
                        + "\t" + strSplit[3] + "\n");

                objWorkTemperatureWriter.write(intRUID + "\t" + intInternalRUID + "\t"
                        + (intFrequency - 1) + "\t" + dblTemperature
                        + "\t0"
                        + "\t" + strSplit[3] + "\n\n");


                objWorkContaminationTemperatureWriter.write(intRUID + "\t" + intInternalRUID + "\t"
                        + (intContFrequency - 1) + "\t" + dblContTemperature
                        + "\t1"
                        + "\t" + strSplit[3] + "\n");

                objWorkContaminationTemperatureWriter.write(intRUID + "\t" + intInternalRUID + "\t"
                        + (intContFrequency - 1) + "\t" + dblContTemperature
                        + "\t0"
                        + "\t" + strSplit[3] + "\n\n");


                objWorkSelfContaminationTemperatureWriter.write(intRUID + "\t" + intInternalRUID + "\t"
                        + (intSelfContFrequency - 1) + "\t" + dblSelfContTemperature
                        + "\t1"
                        + "\t" + strSplit[3] + "\n");

                objWorkSelfContaminationTemperatureWriter.write(intRUID + "\t" + intInternalRUID + "\t"
                        + (intSelfContFrequency - 1) + "\t" + dblSelfContTemperature
                        + "\t0"
                        + "\t" + strSplit[3] + "\n\n");


                objWorkContaminatedTemperatureWriter.write(intRUID + "\t" + intInternalRUID + "\t"
                        + (dblContdTemperature - 1) + "\t" + dblContdTemperature
                        + "\t1"
                        + "\t" + strSplit[3] + "\n");

                objWorkContaminatedTemperatureWriter.write(intRUID + "\t" + intInternalRUID + "\t"
                        + (dblContdTemperature - 1) + "\t" + dblContdTemperature
                        + "\t0"
                        + "\t" + strSplit[3] + "\n\n");
            }

            objReader.close();
            objTemperatureWriter.flush();
            objTemperatureWriter.close();
            objWorkTemperatureWriter.flush();
            objWorkTemperatureWriter.close();
            objWorkContaminationTemperatureWriter.flush();
            objWorkContaminationTemperatureWriter.close();
            objWorkSelfContaminationTemperatureWriter.flush();
            objWorkSelfContaminationTemperatureWriter.close();
            objWorkContaminatedTemperatureWriter.flush();
            objWorkContaminatedTemperatureWriter.close();

            String strTRTOutFile = objTextReuseTemperaturesFolder.getPath()
                    + "/" + strOldWorkName.replaceAll(": ", "_").replaceAll(" ", "_").replace("'", "_") + ".trt";
            writeGnuplotFile(strTRTOutFile, strGnuPlotFile, strOldWorkName);
            writeGnuplotFile(strTRTOutFile.replace(".trt", ".cont.trt"), strGnuPlotFile, strOldWorkName);
            writeGnuplotFile(strTRTOutFile.replace(".trt", ".self_cont.trt"), strGnuPlotFile, strOldWorkName);
            writeGnuplotFile(strTRTOutFile.replace(".trt", ".contd.trt"), strGnuPlotFile, strOldWorkName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String loadGnuplotFile() throws FileNotFoundException, IOException {
        String strResultFile = "";

        String strScoringFileName = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/text-reuse-temperature.plt";
        BufferedReader objReader = new BufferedReader(new FileReader(strScoringFileName));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            strResultFile += strLine + "\n";
        }
        objReader.close();

        return strResultFile;
    }

    private static void writeGnuplotFile(String strFileName, String strGnuPlotContent, String strWork) throws IOException {
        BufferedWriter objWriter = new BufferedWriter(new FileWriter(strFileName.replace(".trt", ".plt")));

        strGnuPlotContent = strGnuPlotContent.replaceAll("<image_name>", strFileName.replaceAll(".trt", ".png"));
        strGnuPlotContent = strGnuPlotContent.replaceAll("<author_work>", strWork);
        strGnuPlotContent = strGnuPlotContent.replaceAll("<plot.file>", strFileName);

        objWriter.write(strGnuPlotContent + "\n");

        objWriter.flush();
        objWriter.close();
    }

    private static ObjectKeyIntMap loadAuthorsFile() throws FileNotFoundException, IOException {
        ObjectKeyIntMap objAuthors = new ObjectKeyIntOpenHashMap();

        String strScoringFileName = "/home/mbuechler/JOCCH/PerseusAuthors-v2.csv";
        BufferedReader objReader = new BufferedReader(new FileReader(strScoringFileName));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            if (strSplit.length == 6) {
                System.out.println(strSplit[1] + "\t" + Integer.parseInt(strSplit[5]));
                objAuthors.put(strSplit[1].trim(), Integer.parseInt(strSplit[5]));
            } else {
                System.out.println("IGNORE: " + strLine);
            }
        }
        objReader.close();

        return objAuthors;
    }

    private static HashMap<Integer, String> loadRUID2WorkFile() throws FileNotFoundException, IOException {
        HashMap<Integer, String> objRUID2Work = new HashMap<Integer, String>();

        String strFileName = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/PerseusGreek.txt";
        BufferedReader objReader = new BufferedReader(new FileReader(strFileName));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");
            objRUID2Work.put(new Integer(strSplit[0]), strSplit[3]);
        }
        objReader.close();

        return objRUID2Work;
    }
}

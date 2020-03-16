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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author mbuechler
 */
public class DotplotviewCleanerMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            int intSequenceLength = 5;

            String strDirName = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/Dotplots";
            ArrayList<String> objFileList = new ArrayList<String>();
            addFilesToProcess(new File(strDirName), objFileList);

            for (int i = 0; i < objFileList.size(); i++) {
                String strFileName = objFileList.get(i);
                System.out.println("Processing file " + strFileName);
                SortedSet<String> objSelectedData = new TreeSet<String>();

                BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
                String strLine = null;

                SortedSet<String> objData = new TreeSet<String>();
                SortedSet<Integer> objRUIDs = new TreeSet<Integer>();

                while ((strLine = objReader.readLine()) != null) {
                    String strValues[] = strLine.split("\t");
                    objData.add(strValues[1] + "\t" + strValues[3]);
                    objRUIDs.add(new Integer(strValues[1]));
                }

                objReader.close();
                //System.out.println( objData.size() + "\t" + objRUIDs.size() );

                Iterator<Integer> objIter = objRUIDs.iterator();

                while (objIter.hasNext()) {
                    int intRUID = objIter.next();
                    SortedSet<String> objCandidates = objData.subSet(intRUID + "\t", intRUID + "\tZZZZ");

                    Iterator<String> objIter2 = objCandidates.iterator();
                    //System.out.println( "\t\t" + objCandidates.size() );
                    while (objIter2.hasNext()) {
                        String strData = objIter2.next();
                        int intRUID2 = Integer.parseInt(strData.split("\t")[1]);
                        //System.out.println( "\t\t\t" +intRUID + "\t" + intRUID2 );

                        int j = 1;
                        boolean isSequence = true;
                        while (isSequence && j < intSequenceLength) {
                            //System.out.println( "\t\t\t\t" +(intRUID+j) + "\t" + (intRUID2+j) );
                            String strKey = (intRUID + j) + "\t" + (intRUID2 + j);

                            isSequence = objData.contains(strKey);
                            j++;
                        }

                        if (isSequence) {
                            for (int k = 0; k < intSequenceLength; k++) {
                                objSelectedData.add((intRUID + k) + "\t" + (intRUID2 + k));
                            }
                        }
                    }
                }

                System.out.println(objData.size() + " --->" + objSelectedData.size());

                if (objSelectedData.size() > 0) {
                    String strOutputFileName = strFileName.replaceAll("plain", "seq_" + intSequenceLength);
                    new File(new File(strOutputFileName).getParent()).mkdirs();

                    BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutputFileName));

                    Iterator<String> objIter3 = objSelectedData.iterator();

                    while (objIter3.hasNext()) {
                        String strData = objIter3.next();
                        objWriter.write( strData + "\n" );
                    }

                    objWriter.flush();
                    objWriter.close();

                    String strGnuPlotScript =  loadGnuplotFile(strFileName.replaceAll(".dp", ".plt"));
                    strGnuPlotScript = strGnuPlotScript.replaceAll("plain", "seq_" + intSequenceLength);
                    strGnuPlotScript = strGnuPlotScript.replaceAll( "using 2:4", "using 1:2");

                    BufferedWriter objGnuplotWriter = new BufferedWriter(new FileWriter(strOutputFileName.replaceAll( ".dp", ".plt" ) ));
                    objGnuplotWriter.write(strGnuPlotScript + "\n");
                    objGnuplotWriter.flush();
                    objGnuplotWriter.close();
                }
            }

            System.out.println(objFileList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void addFilesToProcess(File inputFiles, ArrayList<String> objFileList) throws IOException {

        if (inputFiles.isDirectory()) {
            File objFiles[] = inputFiles.listFiles();

            for (int i = 0; i < objFiles.length; i++) {
                addFilesToProcess(objFiles[i], objFileList);
            }
        } else {
            if (inputFiles.getName().endsWith(".dp") && inputFiles.getAbsolutePath().contains("plain")) {
                objFileList.add(inputFiles.getAbsolutePath());
            }
        }
    }

        private static String loadGnuplotFile(String strScoringFileName) throws FileNotFoundException, IOException {
        String strResultFile = "";

        BufferedReader objReader = new BufferedReader(new FileReader(strScoringFileName));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            strResultFile += strLine + "\n";
        }
        objReader.close();

        return strResultFile;
    }
}

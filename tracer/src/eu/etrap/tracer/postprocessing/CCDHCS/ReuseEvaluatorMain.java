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



package eu.etrap.tracer.postprocessing.CCDHCS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 * @author mbuechler
 */
public class ReuseEvaluatorMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String strFolder = args[0];
            ArrayList<String> objFileList = new ArrayList<String>();
            addFilesToProcess(new File(strFolder), objFileList);

            int size = objFileList.size();

            for (int a = 0; a < size; a++) {
                System.out.println("Processing " + objFileList.get(a));

                String strFileName = objFileList.get(a);
                BufferedReader objReader = new BufferedReader(new FileReader(strFileName));

                int intEval[][] = new int[8][8];

                String strLine = null;
                while ((strLine = objReader.readLine()) != null) {
                    String strSplit[] = strLine.split("\t");
                    int intRUID1 = Integer.parseInt(strSplit[0]);
                    int intBibleVersion1 = intRUID1 / 1000000;
                    int intRUIDInBibleVersion1 = intRUID1 % 1000000;

                    int intRUID2 = Integer.parseInt(strSplit[1]);
                    int intBibleVersion2 = intRUID2 / 1000000;
                    int intRUIDInBibleVersion2 = intRUID2 % 1000000;

                    if (intRUIDInBibleVersion1 == intRUIDInBibleVersion2) {
                        intEval[intBibleVersion1][intBibleVersion2]++;
                    }
                }

                objReader.close();

                DecimalFormat df = new DecimalFormat("0.000");
                String strBibleLabels[] = new String[]{"", "ASV", "Basic", "Darby", "KJV", "WEB", "Webster", "YLT"};
                BufferedWriter objWriter = new BufferedWriter(new FileWriter(strFileName + ".result"));

                for (int i = 1; i < 8; i++) {
                    for (int j = 1; j < 8; j++) {
                        if (i != j) {
                            double dblValue = (double) intEval[i][j] / 28632.0;
                            objWriter.write(strBibleLabels[i] + " vs. " + strBibleLabels[j] + "\t" + df.format(dblValue) + "\n");
                        }
                    }
                }

                objWriter.flush();
                objWriter.close();
            }
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
            if (inputFiles.getName().endsWith(".score")) {
                objFileList.add(inputFiles.getAbsolutePath());
            }
        }
    }
}

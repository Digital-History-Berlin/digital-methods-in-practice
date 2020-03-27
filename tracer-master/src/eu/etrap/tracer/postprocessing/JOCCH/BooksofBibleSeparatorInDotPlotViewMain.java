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
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author mbuechler
 */
public class BooksofBibleSeparatorInDotPlotViewMain {



    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            int arySeparationCriterions[] = new int[5];

            arySeparationCriterions[0] = 1278;
            arySeparationCriterions[1] = 2067;
            arySeparationCriterions[2] = 2339;
            arySeparationCriterions[3] = 4516;
            arySeparationCriterions[4] = 8677;

            String aryLabels[] = new String[]{"Matthew", "Mark", "Luke", "John", "Other"};



            String strFile = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/JOCCH-Dotplots/_New_Testament____New_Testament.dp";
            BufferedReader objReader = new BufferedReader(new FileReader(strFile));

                        // creating writer
            HashMap<String, BufferedWriter> mapWriters= new HashMap<String, BufferedWriter>();
            mapWriters.put("John.Luke", new BufferedWriter(new FileWriter(strFile + ".John.Luke")) );
            mapWriters.put("John.Mark", new BufferedWriter(new FileWriter(strFile + ".John.Mark")) );
            mapWriters.put("John.Matthew", new BufferedWriter(new FileWriter(strFile + ".John.Matthew")) );
            mapWriters.put("Luke.Mark", new BufferedWriter(new FileWriter(strFile + ".Luke.Mark")) );
            mapWriters.put("John.Matthew", new BufferedWriter(new FileWriter(strFile + ".John.Matthew")) );
            mapWriters.put("Mark.Matthew", new BufferedWriter(new FileWriter(strFile + ".Mark-Matthew")) );
            mapWriters.put( "Others", new BufferedWriter(new FileWriter(strFile + ".others")) );

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                int intRUID1 = Integer.parseInt(strSplit[1]);
                int intRUID2 = Integer.parseInt(strSplit[3]);

                String aryBooks[] = new String[2];
                aryBooks[0] = getBook(arySeparationCriterions, intRUID1, aryLabels);
                aryBooks[1] = getBook(arySeparationCriterions, intRUID2, aryLabels);
                
                Arrays.sort(aryBooks);
                String strKey = aryBooks[0] + "." + aryBooks[1];

                BufferedWriter objWriter = null;
                objWriter = mapWriters.get(strKey);

                if( objWriter == null ){
                    objWriter = mapWriters.get( "Others" );
                }

                objWriter.write( strLine + "\n" );
            }

            objReader.close();

            Iterator<String> iterWriters = mapWriters.keySet().iterator();

            while( iterWriters.hasNext() ){
                String strKey = iterWriters.next();
                BufferedWriter objWriter = mapWriters.get(strKey);
                objWriter.flush();
                objWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getBook(int arySeparationCriterions[], int intRUID, String aryLabels[]) {
        // detect book id

        int intWorkID = -1;
        for (int i = 0; i < arySeparationCriterions.length; i++) {

            if (intRUID <= arySeparationCriterions[i]) {
                intWorkID = i;
                break;
            }
        }
        System.out.println( "--> " + intRUID + "\t" + intWorkID );

        return aryLabels[intWorkID];
    }
}

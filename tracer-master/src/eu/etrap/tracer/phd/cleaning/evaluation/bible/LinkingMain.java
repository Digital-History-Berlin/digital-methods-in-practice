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



package eu.etrap.tracer.phd.cleaning.evaluation.bible;

import bak.pcj.IntIterator;
import bak.pcj.set.IntSet;
import bak.pcj.set.IntOpenHashSet;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author mbuechler
 */
public class LinkingMain {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException if file is not found
     * @throws java.io.IOException if any IO error occurs
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {

        long longUniqLinks = 0;
        long longLinks = 0;
        long longFound = 0;

        String strInFile = args[0];

        System.out.println("Processing file " + strInFile);

        for (int a = 1; a <= 7; a++) {
            for (int b = 1; b <= 7; b++) {

                int bible1 = a;
                int bible2 = b;

                System.out.println("\n\nComparing " + bible1 + " and " + bible2);


                HashMap<Integer, IntSet> objRUID2Feat = new HashMap<Integer, IntSet>();
                HashMap<Integer, IntSet> objFeat2RUID = new HashMap<Integer, IntSet>();



                BufferedReader objReader = new BufferedReader(new FileReader(strInFile));
                String strLine = null;

                while ((strLine = objReader.readLine()) != null) {
                    String strSplit[] = strLine.split("\t");

                    int Feature = Integer.parseInt(strSplit[0]);
                    int VersID = Integer.parseInt(strSplit[1]);

                    int bible = VersID / 1000000;

                    if (bible == bible1) {
                        //System.out.println(strLine);
                        IntSet objData = new IntOpenHashSet();

                        if (objRUID2Feat.containsKey(VersID)) {
                            objData = objRUID2Feat.get(VersID);
                        }

                        objData.add(Feature);
                        objRUID2Feat.put(VersID, objData);
                        //System.out.println( VersID + "\t" + objData);
                    }

                    if (bible == bible2) {
                        IntSet objData = new IntOpenHashSet();

                        if (objFeat2RUID.containsKey(Feature)) {
                            objData = objFeat2RUID.get(Feature);
                        }

                        objData.add(VersID);
                        objFeat2RUID.put(Feature, objData);
                    }
                }

                objReader.close();

                System.out.println("RUID2Feat size=" + objRUID2Feat.size() + "\tFeat2RUID size=" + objFeat2RUID.size());

                int size = 28632;
                int aryData[][] = new int[size + 1][size + 1];

                Iterator<Integer> objIter = objRUID2Feat.keySet().iterator();

                int counter = 0;
                while (objIter.hasNext()) {
                    counter++;
                    Integer objKey = objIter.next();

                    IntSet objData = objRUID2Feat.get(objKey);

                    IntIterator objIter2 = objData.iterator();

                    while (objIter2.hasNext()) {
                        int feature = objIter2.next();

                        IntSet objData2 = objFeat2RUID.get(feature);

                        if (!(objData2 == null)) {
                            IntIterator objIter3 = objData2.iterator();

                            while (objIter3.hasNext()) {
                                int ruid = objIter3.next();

                                aryData[objKey % 1000000][ruid % 1000000]++;
                            }
                        }
                    }
                }


                long links = 0;
                long real = 0;
                long found = 0;

                for (int i = 1; i <= size; i++) {
                    for (int j = 1; j <= size; j++) {
                        if (aryData[i][j] != 0) {
                            real += aryData[i][j];
                            links++;
                            
                            if( i == j && bible1!= bible2 ){
                               found++; 
                            }
                        }
                    }
                }

                System.out.println(links + "\t" + real + "\t" + found + "\tR=" + ((double)found/28632.0));

                longUniqLinks += links;
                longLinks += real;
                longFound += found;
            }
        }

        System.out.println("\n\n\nNUMBER_OF_UNIQUE_LINKS= " + longUniqLinks);
        System.out.println("NUMBER_OF_LINKED_LINKS= " + longLinks);
        System.out.println("FOUND= " + longFound);
        System.out.println("RECALL= " + (double)longFound/(28632.0*42.0));
    }
}

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author mbuechler
 */
public class SourceLinksMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String strFileName = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/PerseusGreek.txt";
            BufferedReader objReader = new BufferedReader(new FileReader(strFileName));
            HashMap<String, String> objRUID2Meta = new HashMap<String, String>();
            HashMap<String, SortedSet<Integer>> objWorksByIDs = new HashMap<String, SortedSet<Integer>>();

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                objRUID2Meta.put(strSplit[0].trim(), strSplit[3].trim());

                SortedSet<Integer> objRUIDsOfAWork = new TreeSet<Integer>();
                if (objWorksByIDs.containsKey(strSplit[3].trim())) {
                    objRUIDsOfAWork = objWorksByIDs.get(strSplit[3].trim());
                }

                objRUIDsOfAWork.add(new Integer(strSplit[0].trim()));
                objWorksByIDs.put(strSplit[3].trim(), objRUIDsOfAWork);

            }

            System.out.println(objRUID2Meta.size());
            System.out.println("Number of works: " + objWorksByIDs.size());
            objReader.close();

            strFileName = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/PerseusGreek-PerseusGreekBiGramLem.score";
            objReader = new BufferedReader(new FileReader(strFileName));
            double dblThreshold = 6.0;
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strFileName + ".agg." + dblThreshold));
            HashMap<String, Integer> objLink2Freq = new HashMap<String, Integer>();
            HashMap<Integer, SortedSet<Integer>> objReuses = new HashMap<Integer, SortedSet<Integer>>();

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                double dblValue = Double.parseDouble(strSplit[2].trim());

                if (Integer.parseInt(strSplit[0].trim()) < Integer.parseInt(strSplit[1].trim()) && dblValue > dblThreshold) {
                    String strKey = objRUID2Meta.get(strSplit[0].trim());
                    strKey += "\t";
                    strKey += objRUID2Meta.get(strSplit[1].trim());

                    int intFreq = 0;

                    if (objLink2Freq.containsKey(strKey)) {
                        intFreq = objLink2Freq.get(strKey);
                    }

                    intFreq++;

                    objLink2Freq.put(strKey, intFreq);
                }

                SortedSet<Integer> objReuseIDs = new TreeSet<Integer>();
                if (objReuses.containsKey(new Integer(strSplit[0].trim()))) {
                    objReuseIDs = objReuses.get(new Integer(strSplit[0].trim()));
                }

                objReuseIDs.add(new Integer(strSplit[1].trim()));
                objReuses.put(new Integer(strSplit[0].trim()), objReuseIDs);

            }

            objReader.close();

            Iterator<String> objIter = objLink2Freq.keySet().iterator();
            while (objIter.hasNext()) {
                String strKey = objIter.next();
                Integer objValue = objLink2Freq.get(strKey);
                objWriter.write(strKey + "\t" + objValue + "\n");
            }

            System.out.println(objLink2Freq.size());
            objWriter.flush();
            objWriter.close();


            String strGnuplotFile = loadGnuplotFile();

            //BufferedWriter objDotPlotWriter
            objIter = objLink2Freq.keySet().iterator();
            while (objIter.hasNext()) {
                String strKey = objIter.next();
                Integer objValue = objLink2Freq.get(strKey);

                if (objValue.intValue() >= 10) {
                    System.out.println(strKey + "\t" + objValue + "\n");
                    String strSplit[] = strKey.split("\t");
                    String strWork1 = strSplit[0];
                    String strWork2 = strSplit[1];

                    String strDotPlotFolder = new File(strFileName).getParent() + "/Dotplots/";

                    strDotPlotFolder += new File( strFileName ).getName().replace(".score", "");
                    strDotPlotFolder += "/";

                    if (strWork1.equals(strWork2)) {
                        strDotPlotFolder += "SameWork/";
                    } else {
                        strDotPlotFolder += "DifferentWork/";
                    }

                    strDotPlotFolder += "plain/";

                    new File(strDotPlotFolder).mkdirs();

                    String strDotPlotFileName = strWork1.replaceAll(": ", "_").replaceAll(" ", "_").replace("'", "_");
                    strDotPlotFileName += "___";
                    strDotPlotFileName += strWork2.replaceAll(": ", "_").replaceAll(" ", "_").replace("'", "_");

                    int intMax = 200;
                    String strDotplotDataFile = null;
                    
                    if(strDotPlotFileName.length() > intMax){
                        strDotplotDataFile = strDotPlotFolder + strDotPlotFileName.substring(0, intMax) + ".dp";
                    }else{
                        strDotplotDataFile = strDotPlotFolder + strDotPlotFileName + ".dp";
                    }
                    
                    BufferedWriter objDotPlotWriter = new BufferedWriter(new FileWriter(strDotplotDataFile));

                    SortedSet<Integer> objRUIDsWork1 = objWorksByIDs.get(strWork1);
                    SortedSet<Integer> objRUIDsWork2 = objWorksByIDs.get(strWork2);

                    int intRUIDRangeWork1Start = objRUIDsWork1.first();
                    int intRUIDRangeWork1End = objRUIDsWork1.last();
                    int intRUIDRangeWork2Start = objRUIDsWork2.first();
                    int intRUIDRangeWork2End = objRUIDsWork2.last();
                    System.out.println("\t" + intRUIDRangeWork2Start + "\t" + intRUIDRangeWork2End);

                    Iterator<Integer> objIter2 = objRUIDsWork1.iterator();
                    while (objIter2.hasNext()) {
                        int intRUIDWork1 = objIter2.next();

                        SortedSet<Integer> _objRUIDsWork2 = objReuses.get(intRUIDWork1);
                        if (_objRUIDsWork2 != null && _objRUIDsWork2.size() > 0) {
                            Iterator<Integer> objIter3 = _objRUIDsWork2.iterator();

                            while (objIter3.hasNext()) {
                                int intRUIDWork2 = objIter3.next();

                                if (intRUIDWork2 >= intRUIDRangeWork2Start && intRUIDWork2 <= intRUIDRangeWork2End) {
                                    objDotPlotWriter.write(intRUIDWork1 + "\t" + (intRUIDWork1 - intRUIDRangeWork1Start + 1) + "\t"
                                            + intRUIDWork2 + "\t" + (intRUIDWork2 - intRUIDRangeWork2Start + 1) + "\n");
                                }
                            }
                        }
                    }

                    objDotPlotWriter.flush();
                    objDotPlotWriter.close();
                    String strDotPlotSkript = new String(strGnuplotFile);

                    BufferedWriter objDotPlotScriptWriter = new BufferedWriter(new FileWriter(strDotplotDataFile.replaceAll(".dp", ".plt")));

                    strDotPlotSkript = strDotPlotSkript.replaceAll("<output.file>", strDotplotDataFile.replaceAll(".dp", ".png"));
                    strDotPlotSkript = strDotPlotSkript.replaceAll("<author.x>", strWork1.split(": ")[0]);
                    strDotPlotSkript = strDotPlotSkript.replaceAll("<author.y>", strWork2.split(": ")[0]);
                    strDotPlotSkript = strDotPlotSkript.replaceAll("<author.work.x>", strWork1);
                    strDotPlotSkript = strDotPlotSkript.replaceAll("<author.work.y>", strWork2);

                    strDotPlotSkript = strDotPlotSkript.replaceAll("<data.file>", strDotplotDataFile);

                    objDotPlotScriptWriter.write(strDotPlotSkript + "\n");

                    objDotPlotScriptWriter.flush();
                    objDotPlotScriptWriter.close();

                    //break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String loadGnuplotFile() throws FileNotFoundException, IOException {
        String strResultFile = "";

        String strScoringFileName = "/home/mbuechler/Development/Traces/data/corpora/PerseusClassics-V.2/dotplot-view.plt";
        BufferedReader objReader = new BufferedReader(new FileReader(strScoringFileName));

        String strLine = null;
        while ((strLine = objReader.readLine()) != null) {
            strResultFile += strLine + "\n";
        }
        objReader.close();

        return strResultFile;
    }
}

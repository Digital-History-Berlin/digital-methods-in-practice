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

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.HashMap;
/*
tokens=5238149
tokens base=772471
number base=6237
tokens ssim=470878
number ssim=15352
 */
 
/**
 *
 * @author mbuechler
 */
public class ComponentEvaluatorMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String strFile = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/CompEval/MultiVersionsOfBible.feats";
            //strFile = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/CompEval/MultiVersionsOfBible.txt.wnc";
            
            BufferedReader objReader = new BufferedReader(new FileReader(strFile));
            String strLine = null;

            HashMap<String, Integer> objTokens = new HashMap<String, Integer>();

            int tokens = 0;
            int sw_tokens = 0;
            int number = 0;
            
            while ((strLine = objReader.readLine()) != null) {
                number++;
                String strSplit[] = strLine.split("\t");

                if (Integer.parseInt(strSplit[0]) >= 101) {
                    //System.out.println(strLine);
                    objTokens.put(strSplit[1].trim(), Integer.parseInt(strSplit[2].trim()));
                    tokens += Integer.parseInt(strSplit[2].trim());
                }
                
                
                if( Integer.parseInt(strSplit[0]) >= 101 && Integer.parseInt(strSplit[0]) < (101+200) ){
                    sw_tokens += Integer.parseInt(strSplit[2].trim());
                }
            }
            objReader.close();

            System.out.println("tokens=" + tokens);
            System.out.println("sw_tokens=" + sw_tokens);
            System.out.println("number=" + number);
            

            processLemma(objTokens);
            processStringSim(objTokens);
            processSyn(objTokens);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static int processLemma(HashMap<String, Integer> objTokens) {
        int tokens = 0;

        try {
            String strFile = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/CompEval/MultiVersionsOfBible.txt.tok.base.prep";

            BufferedReader objReader = new BufferedReader(new FileReader(strFile));
            String strLine = null;

            int number = 0;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                
                int test = 0;
                if(  objTokens.get(strSplit[0].trim()) != null ){
                test=objTokens.get(strSplit[0].trim());
                }
                
                tokens += test;
                //System.out.println(strSplit[0].trim() + "\t" + objTokens.get(strSplit[0].trim()));
                number++;
            }
            objReader.close();

            System.out.println("tokens base=" + tokens);
            System.out.println("number base=" + number);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tokens;
    }
    
        protected static int processSyn(HashMap<String, Integer> objTokens) {
        int tokens = 0;

        try {
            String strFile = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/CompEval/MultiVersionsOfBible.txt.sim.prep";

            BufferedReader objReader = new BufferedReader(new FileReader(strFile));
            String strLine = null;

            int number = 0;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                
                 int test = 0;
                if(  objTokens.get(strSplit[0].trim()) != null ){
                test=objTokens.get(strSplit[0].trim());
                }
                
                tokens += test;
                
                
                //System.out.println(strSplit[0].trim() + "\t" + objTokens.get(strSplit[0].trim()));
                number++;
            }
            objReader.close();

            System.out.println("tokens syn=" + tokens);
            System.out.println("number syb=" + number);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tokens;
    }
        
            protected static int processStringSim(HashMap<String, Integer> objTokens) {
        int tokens = 0;

        try {
            String strFile = "/home/mbuechler/Dissertation/Results/SystemEvaluationChapter/CompEval/MultiVersionsOfBible.txt.ssim.prep";

            BufferedReader objReader = new BufferedReader(new FileReader(strFile));
            String strLine = null;

            int number = 0;

            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                
                 int test = 0;
                if(  objTokens.get(strSplit[0].trim()) != null ){
                test=objTokens.get(strSplit[0].trim());
                }
                
                tokens += test;
                
                //System.out.println(strSplit[0].trim() + "\t" + objTokens.get(strSplit[0].trim()));
                number++;
            }
            objReader.close();

            System.out.println("tokens ssim=" + tokens);
            System.out.println("number ssim=" + number);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tokens;
    }
}

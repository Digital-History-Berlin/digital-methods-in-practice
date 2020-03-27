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


package eu.etrap.tracer.preprocessing.graph;

import eu.etrap.medusa.config.ClassConfig;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import eu.etrap.tracer.TracerException;
import eu.etrap.tracer.utils.FileManager;

/**
 * Created on 01.03.2011 09:23:15 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class AbstractWordGraphHandler extends ClassConfig {

    protected HashMap<String, HashSet<String>> objInputWordAssociations = null;
    protected HashMap<String, String> objCleanedDirectedGraph = null;
    protected HashMap<String, Long> objWordformFrequency = null;
    protected HashMap<String, Long> objReplacementCandidateFreqs = null;

    // this property is used in loadInputFile() in order to determine if only associations
    // are loaded that do occur in the corpus (word frequency > 0).
    // This does makes sense for the most graph implementations such as the baseform
    // graph. If a word form does not exist the data do not have to be dealt with.
    // Especially on smaller corpora only these wordforms are loaded (caused by
    // the frequency threshold) and used. This, however, means on the other hand
    // that a synonym graph is almost completely removed since the nodes of the
    // associations doe exist in the baseform. For this reason by setting
    // ignoreWordFrequency=true those data can be kept in. 
    protected boolean ignoreWordFrequency = false;


    public void init() throws ConfigurationException {
        super.config();
        objInputWordAssociations = new HashMap<String, HashSet<String>>();
        objCleanedDirectedGraph = new HashMap<String, String>();
        objWordformFrequency = new HashMap<String, Long>();
        objReplacementCandidateFreqs = new HashMap<String, Long>();
        ignoreWordFrequency = false;
    }

    private void loadWordFrequencyDistribution() throws TracerException {
        try {
            String strInFile = FileManager.getOriginalCorpusFileName() + ".wnc";

            BufferedReader objReader = new BufferedReader(new FileReader(strInFile));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                String strWordForm = strSplit[1].trim();
                Long objLongFreq = new Long(strSplit[3].trim());

                objWordformFrequency.put(strWordForm, objLongFreq);
            }

            objReader.close();

            ConfigurationContainer.println("Total number of loaded word forms is " + objWordformFrequency.size());
        } catch (Exception e) {
            throw new TracerException(e);
        }
    }

    public void loadInputFile(String strInFile) throws TracerException {

        loadWordFrequencyDistribution();

        try {
            BufferedReader objReader = new BufferedReader(new FileReader(strInFile));

            String strLine = null;
            while ((strLine = objReader.readLine()) != null) {
                String strSplit[] = strLine.split("\t");
                String strWordForm = strSplit[0].trim();
                String strBaseForm = strSplit[1].trim();

                // load only these words that can be found in the investigated
                // corpus if ignoreWordFrequency=false. Otherwise (like a synonym
                // graph) ignore this option.
                if ( ignoreWordFrequency || objWordformFrequency.containsKey(strWordForm)) {
                    HashSet<String> objBaseForms = objInputWordAssociations.get(strWordForm);

                    if (objBaseForms == null) {
                        objBaseForms = new HashSet<String>();
                    }

                    objBaseForms.add(strBaseForm);

                    objInputWordAssociations.put(strWordForm, objBaseForms);
                }
            }

            objReader.close();

            ConfigurationContainer.println( "Total number of loaded directed/indirected associations is "
                    + objInputWordAssociations.size());
        } catch (Exception e) {
            throw new TracerException(e);
        }
    }

    public void writeOutputFile(String strOutFile) throws TracerException {
        // iterate and write final file
        try {

            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutFile));
            Iterator<String> objWordFormIter = objCleanedDirectedGraph.keySet().iterator();

            while (objWordFormIter.hasNext()) {
                String strWordForm = objWordFormIter.next();
                String strBaseform = objCleanedDirectedGraph.get(strWordForm);

                if( !strWordForm.equals(strBaseform) ){
                    objWriter.write(strWordForm + "\t" + strBaseform + "\n");
                }
            }

            objWriter.flush();
            objWriter.close();
        } catch (Exception e) {
            throw new TracerException(e);
        }
    }
}

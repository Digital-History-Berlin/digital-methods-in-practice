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
package eu.etrap.tracer.preprocessing.external.lemmatisation;

import eu.etrap.medusa.config.ClassConfig;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Created on 18.04.2017 11:18:42 by mbuechler
 *
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public abstract class AbstractLemmatiser extends ClassConfig {

    public String strDataDirectory = null;
    public String strFileSuffix = null;
    public String strTagSetMappingFile = null;
    public int intScaleFactor = -1;
    public HashMap<String, String> objTagSetMapping = null;
    public TreeSet<String> objDataLemmaList = null;
    public TreeSet<String> objDataWordform2LemmaMapping = null;
    public boolean boolRemoveNonLetters = false;

    public void init() throws ConfigurationException {
        ConfigurationContainer.println("Initialising pre-processing with class " + this.getClass());

        super.config();

        if (intScaleFactor <= 0) {
            intScaleFactor = 1000000;
        }

        objTagSetMapping = new HashMap<String, String>();
        objDataLemmaList = new TreeSet<>();
        objDataWordform2LemmaMapping = new TreeSet<>();

        if (strDataDirectory == null || strDataDirectory.trim().equals("")) {
            throw new ConfigurationException("strDataDirectory is not set in category " + this.getClass() + " ...");
        }

        if (strFileSuffix == null || strFileSuffix.trim().equals("")) {
            throw new ConfigurationException("strFileSuffix is not set in category " + this.getClass() + " ...");
        }

        if (strTagSetMappingFile == null || strTagSetMappingFile.trim().equals("")) {
            throw new ConfigurationException("strTagSetMappingFile is not set in category " + this.getClass() + " ...");
        }

        ConfigurationContainer.println("\nPARAMETER:");
        ConfigurationContainer.println("Data directory of input files                  : " + strDataDirectory);
        ConfigurationContainer.println("File suffix for identification of tagged files : " + strFileSuffix);
        ConfigurationContainer.println("Parts-of-speech mapping file is                : " + strTagSetMappingFile);
        ConfigurationContainer.println("Work separator                                 : " + intScaleFactor);
    }

    public void readPoSMappingFile() throws FileNotFoundException, IOException {
        BufferedReader objReader = new BufferedReader(new FileReader(strTagSetMappingFile));
        String strLine = null;

        while ((strLine = objReader.readLine()) != null) {
            if (!strLine.startsWith("#")) {
                String strSplit[] = strLine.split("\t");
                objTagSetMapping.put(strSplit[1].trim().toUpperCase(), strSplit[0].trim());
                System.out.println(strSplit[1].trim().toUpperCase() + "\t" + strSplit[0].trim() );
            }
        }

        System.out.println( "SIZE OF TagSetMapping = " +objTagSetMapping.size() );
        objReader.close();
    }

    public void process() throws IOException {
        File obDataDirectory = new File(strDataDirectory);
        File objFiles[] = obDataDirectory.listFiles();
        Arrays.sort(objFiles);

        ConfigurationContainer.println("\t" + objFiles.length + " files to process in folder " + strDataDirectory + " ...\n");

        String strOutputDirectory = obDataDirectory.getParent();
        String strCorpusFileName = strOutputDirectory + "/"
                + obDataDirectory.getName().toLowerCase() + ".txt";
        String strLemmaMappingFileName = strCorpusFileName.replace(".txt", ".lemma");
        String strLemmaListFileName = strCorpusFileName.replace(".txt", ".lemma-list");

        ConfigurationContainer.println("\n\nOUTPUT files:");
        ConfigurationContainer.println("Corpus is written to file " + strCorpusFileName);
        ConfigurationContainer.println("Mapping of wordform to lemma is written to file " + strLemmaMappingFileName);
        ConfigurationContainer.println("The lemma list is written to file " + strLemmaListFileName + "\n");

        int intProcessedFileCounter = 0;
        int intUnprocessedFileCounter = 0;

        BufferedWriter objCorpusWriter = new BufferedWriter(new FileWriter(strCorpusFileName));
        BufferedReader objReader = null;
        String strLine = null;

        ConfigurationContainer.println("Processing files");
        for (int i = 0; i < objFiles.length; i++) {
            //for (int i = 0; i < 2; i++) {
            if (objFiles[i].getAbsolutePath().endsWith(strFileSuffix)) {
                ConfigurationContainer.print("\tProcessing file " + objFiles[i].getAbsolutePath() + " ... ");
                intProcessedFileCounter++;

                objReader = new BufferedReader(new FileReader(objFiles[i].getAbsolutePath()));

                strLine = null;
                String strSentence = "";

                String strFileNameSplit[] = objFiles[i].getName().split("-");
                int intWorkID = Integer.parseInt(strFileNameSplit[0]);
                String strAuthor = strFileNameSplit[1].trim();
                String strWork = strFileNameSplit[2].trim().replace("." +this.strFileSuffix, "").replace(".txt", "");

                // replace underscores by whitespaces for TRV displaying gitlab issue #6
                strAuthor = strAuthor.replaceAll("_", " ");
                strWork = strWork.replaceAll("_", " ");
                        
                processWork(objReader, objCorpusWriter, intWorkID, strAuthor, strWork);
                ConfigurationContainer.println(" DONE!" );
            } else {
                ConfigurationContainer.println("Ignoring file " + objFiles[i].getAbsolutePath() + "!");
                intUnprocessedFileCounter++;
            }

        }

        objCorpusWriter.flush();
        objCorpusWriter.close();

        ConfigurationContainer.println("\n\n\nNumber of processed files  : " + intProcessedFileCounter + " out of " + (intProcessedFileCounter + intUnprocessedFileCounter));
        ConfigurationContainer.println("Number of unprocessed files: " + intUnprocessedFileCounter + " out of " + (intProcessedFileCounter + intUnprocessedFileCounter));
        
        
        writeLemmaList(strLemmaListFileName);
        writeWordforms2LemmaMapping(strLemmaMappingFileName);
    }

    protected void writeLemmaList(String strLemmaListFileName) throws IOException {
        BufferedWriter objLemmaListWriter
                = new BufferedWriter(new FileWriter(strLemmaListFileName));

        Iterator<String> objIterator = objDataLemmaList.iterator();

        while (objIterator.hasNext()) {
            String strLemma = objIterator.next();
            objLemmaListWriter.write(strLemma + "\n");
        }

        objLemmaListWriter.flush();
        objLemmaListWriter.close();
    }

    protected void writeWordforms2LemmaMapping(String strLemmaMappingFileName) throws IOException {
        BufferedWriter objWordform2LemmaWriter
                = new BufferedWriter(new FileWriter(strLemmaMappingFileName));

        Iterator<String> objIterator = objDataWordform2LemmaMapping.iterator();

        while (objIterator.hasNext()) {
            String strDataSet = objIterator.next();
            objWordform2LemmaWriter.write(strDataSet + "\n");
        }

        objWordform2LemmaWriter.flush();
        objWordform2LemmaWriter.close();
    }

    protected void processWork(BufferedReader objReader, BufferedWriter objCorpusWriter, int intWorkID, String strAuthor, String strWork) throws IOException {
    }
}

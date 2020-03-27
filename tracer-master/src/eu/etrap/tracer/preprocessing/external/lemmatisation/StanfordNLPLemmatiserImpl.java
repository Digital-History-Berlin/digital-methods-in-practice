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

import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Created on 18.04.2017 11:49:42 by mbuechler
 *
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class StanfordNLPLemmatiserImpl extends AbstractLemmatiser {

    protected HashSet<String> objNonLetters = null;

    public void init() throws ConfigurationException {
        super.init();

        objNonLetters = new HashSet<String>();
        objNonLetters.add(",");
        objNonLetters.add(".");
        objNonLetters.add("''");
        objNonLetters.add("``");
        objNonLetters.add("!");
        objNonLetters.add("?");
        objNonLetters.add(";");
        objNonLetters.add("-");
        objNonLetters.add("--");
        objNonLetters.add("_");
        objNonLetters.add("'s");
        objNonLetters.add("...");
        objNonLetters.add(":");
        objNonLetters.add(":");
        objNonLetters.add("'''");
        objNonLetters.add("'");
        objNonLetters.add("*");
    }

    protected void processWork(BufferedReader objReader, BufferedWriter objCorpusWriter, int intWorkID, String strAuthor, String strWork) throws IOException {
        String strLine = null;
        String strSentence = "";
        int intSentenceID = 0;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");

            // one sentence per line
            if (strLine.equals("")) {
                intSentenceID++;
                objCorpusWriter.write((intWorkID * intScaleFactor + intSentenceID) + "\t"
                        + strSentence.trim() + "\tNULL\t" + strAuthor + ": " + strWork + "\n");
                strSentence = "";
            } else {
                if (boolRemoveNonLetters) {
                    if (!objNonLetters.contains(strSplit[3].trim().toUpperCase())) {
                        strSentence += strSplit[1].trim() + " ";
                    }
                } else {
                    strSentence += strSplit[1].trim() + " ";
                }

                // Mapping of inflected wordform to baseform
                if (!(strLine.trim().equals(""))) {
                    String strKeyPrefix = strSplit[1].trim();
                    String strLemma = strSplit[2].trim();
                    String strKey = strKeyPrefix + "\t" + strLemma;
                    strKey += "\t";

                    String strPoSTag = strSplit[3].trim().toUpperCase();
                    if (!objNonLetters.contains(strPoSTag)) {
                        String strMorpheusKey = objTagSetMapping.get(strPoSTag);

                        if (strMorpheusKey != null) {
                            strKey += strMorpheusKey.trim();
                            objDataWordform2LemmaMapping.add(strKey);
                        } else {
                            ConfigurationContainer.println("\nIgnoring line " + strLine);
                        }
                    }

                    // lemma list
                    if (!objNonLetters.contains(strPoSTag)) {
                        String strMorpheusKey = objTagSetMapping.get(strPoSTag);
                        objDataLemmaList.add(strLemma.trim() + "\t" + strMorpheusKey + "\t" + strPoSTag);
                    }
                }
            }
        }
    }
}


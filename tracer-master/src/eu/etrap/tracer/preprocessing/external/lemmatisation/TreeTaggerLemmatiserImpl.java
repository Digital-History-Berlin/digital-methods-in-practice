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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created on 18.04.2017 11:49:42 by mbuechler
 *
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class TreeTaggerLemmatiserImpl extends AbstractLemmatiser {

    protected void processWork(BufferedReader objReader, BufferedWriter objCorpusWriter, int intWorkID, String strAuthor, String strWork) throws IOException {
        String strLine = null;
        String strSentence = "";
        int intSentenceID = 0;

        while ((strLine = objReader.readLine()) != null) {
            String strSplit[] = strLine.split("\t");

            if (strSplit.length == 3) {
                // one sentence per line
                if (strSplit[1].trim().equals("SENT") && (strSplit[0].trim().equals(".") || strSplit[0].trim().equals("!") || strSplit[0].trim().equals("?"))) {
                    intSentenceID++;
                    strSentence += strSplit[0].trim();
                    objCorpusWriter.write((intWorkID * intScaleFactor + intSentenceID) + "\t"
                            + strSentence.trim() + "\tNULL\t" + strAuthor + ": " + strWork + "\n");
                    strSentence = "";
                } else {
                    strSentence += strSplit[0].trim() + " ";
                }

                // Mapping of inflected wordform to baseform
                String strKeyPrefix = strSplit[0].trim();

                String strLemmas[] = strSplit[2].trim().split("\\|");

                for (int j = 0; j < strLemmas.length; j++) {
                    if (!(strLemmas[j].trim().equals("<unknown>")
                            || strSplit[1].trim().equals("SENT")
                            || strSplit[1].trim().equals("PUN")
                            || strSplit[2].trim().equals("-"))) {
                        String strKey = strKeyPrefix + "\t" + strLemmas[j];
                        strKey += "\t";

                        String strMorpheusKey = objTagSetMapping.get(strSplit[1].trim().toUpperCase());

                        if (strMorpheusKey != null) {
                            strKey += strMorpheusKey.trim();
                            objDataWordform2LemmaMapping.add(strKey);
                        } else {
                            ConfigurationContainer.println("Ignoring line " + strLine);
                        }
                    }
                }

                // lemma list
                for (int j = 0; j < strLemmas.length; j++) {
                    if (strLemmas[j].trim().equals("<unknown>")) {
                        strLemmas[j] = strSplit[0].trim();
                    }

                    if (!(strSplit[1].trim().equals("SENT")
                            || strSplit[1].trim().equals("PUN")
                            || strSplit[2].trim().equals("-"))) {
                        objDataLemmaList.add(strLemmas[j].trim());
                    }
                }
            }
        }

        ConfigurationContainer.println("DONE!");
    }

}

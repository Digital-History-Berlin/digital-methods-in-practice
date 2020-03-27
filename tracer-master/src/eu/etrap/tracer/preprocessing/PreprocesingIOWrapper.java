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


package eu.etrap.tracer.preprocessing;

import eu.etrap.medusa.config.ConfigurationContainer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import eu.etrap.tracer.Constants;
import eu.etrap.tracer.TracerException;
import eu.etrap.tracer.meta.MetaInformation;
import eu.etrap.tracer.meta.MetaInformationBean;
import eu.etrap.tracer.meta.MetaInformationException;
import eu.etrap.tracer.meta.MetaInformationFactory;

/**
 * Created on 27.12.2010 11:03:02 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class PreprocesingIOWrapper {

    private MetaInformationBean objMIBean = null;

    public MetaInformationBean getMetaInformationBean(){
        return objMIBean;
    }

    public void process(String strInFile, String strOutFile, Preprocessing objPreprocess) throws TracerException {

        try {
            objMIBean = new MetaInformationBean();
            objMIBean.strOrigKorpusFile = strInFile;

            String strFolder[] = objPreprocess.getFolderName().split("/");
            objMIBean.strPreprocessingFolder = strFolder[strFolder.length-1];

            int intLineCounter = 0;

            BufferedReader objReader = new BufferedReader(new FileReader(strInFile));
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutFile));

            String strLine = null;

            ConfigurationContainer.println("Start pre-processing ...");

            // expecting here a Medusa tokenised 4 columns file
            while ((strLine = objReader.readLine()) != null) {
                intLineCounter++;
                String strSplit[] = strLine.split("\t");
                String strTextField = strSplit[1].trim();
                String strPreprocessedTextField = objPreprocess.execute(strTextField);

                
                objWriter.write(strSplit[0] + "\t" + strPreprocessedTextField
                        + "\t" + strSplit[2] + "\t" + strSplit[3] + "\n");

                if (intLineCounter % 10000 == 0) {
                    ConfigurationContainer.printR(intLineCounter + " lines processed ...");
                }
            }

            objReader.close();
            objWriter.flush();
            objWriter.close();

            writeStatsOnDisc(strOutFile, objPreprocess);
            ConfigurationContainer.println("Preprocessing finished.");
        } catch (Exception e) {
            throw new TracerException(e);
        }
    }

    protected void writeStatsOnDisc(String strOutFile, Preprocessing objPreprocess) throws MetaInformationException {
        MetaInformation objMetaInf = MetaInformationFactory.createMetaInformationObject(objMIBean, Constants.TYPE_PREPROCESING);

        int aryStats[] = objPreprocess.getPreprocessingStats();

        if (aryStats == null) {
            return;
        }

        for (int i = 0; i < aryStats.length; i++) {

            switch (i) {
                case Constants.ALL_WORDS:
                    objMetaInf.setProperty( "ALL_WORDS" , "" + aryStats[i] );
                    break;
                case Constants.OVERALL_CHANGED_TOKENS_INDEX:
                    objMetaInf.setProperty( "OVERALL_CHANGED_TOKENS_INDEX" , "" + aryStats[i] );
                    break;
                case Constants.LOWER_CASE_INDEX:
                    objMetaInf.setProperty( "LOWER_CASE_INDEX" , "" + aryStats[i] );
                    break;
                case Constants.REMOVE_DIACHRITICS:
                    objMetaInf.setProperty( "REMOVE_DIACHRITICS" , "" + aryStats[i] );
                    break;
                case Constants.LEMMATISATION_INDEX:
                    objMetaInf.setProperty( "LEMMATISATION_INDEX" , "" + aryStats[i] );
                    break;
                case Constants.SYNONYM_INDEX:
                    objMetaInf.setProperty( "SYNONYM_INDEX" , "" + aryStats[i] );
                    break;
                case Constants.STRING_SIMILARITY_INDEX:
                    objMetaInf.setProperty( "STRING_SIMILARITY_INDEX" , "" + aryStats[i] );
                    break;
                case Constants.LENGTH_REDUCED_WORDs_INDEX:
                    objMetaInf.setProperty( "LENGTH_REDUCED_WORDs_INDEX" , "" + aryStats[i] );
                    break;
                case Constants.WORD_LENGTH_INDEX:
                    objMetaInf.setProperty( "WORD_LENGTH_INDEX" , "" + aryStats[i] );
                    break;
                default:
                    objMetaInf.setProperty( "UNSET_PROPERTY_" + i , "" + aryStats[i] );
            }
        }

        objMetaInf.write();
    }
}

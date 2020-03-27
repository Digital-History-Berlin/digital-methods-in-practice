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

import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.tracer.TracerException;
import eu.etrap.tracer.utils.FileManager;


/**
 * Created on 01.03.2011 09:26:04 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class BaseFormWordGraphPreprocessingImpl extends AsymmetricWordGraphHandlerImpl implements WordGraphPreprocessing{

    public void preprocessing() throws TracerException{
        String strInFile = ConfigurationContainer.getGeneralCategory().getProperty("BASEFORM_FILE_NAME");
        String strOutFile = FileManager.getCleanedBaseformFileName();

        this.loadInputFile( strInFile );
        this.reduceGraph();
        this.writeOutputFile( strOutFile );
    }
}

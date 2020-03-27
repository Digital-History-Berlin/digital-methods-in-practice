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

package eu.etrap.tracer;

import eu.etrap.medusa.config.ClassConfig;
import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.medusa.controlflow.ControlFlow;

/**
 * Created on 07.12.2010 16:57:22 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class Tracer extends ClassConfig{

    private String FRAMEWORK_IMPL = null;

    public void init() throws ConfigurationException {
        config();
    }

    public void process() throws Throwable {
        ConfigurationContainer.println("Using FRAMEWORK_IMPL=" + FRAMEWORK_IMPL );

        Class classControlFlowImpl = Class.forName(FRAMEWORK_IMPL);
        
        TracerFramework objFlow = (TracerFramework) classControlFlowImpl.newInstance();
        objFlow.init();
        objFlow.process();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int exitCode = 1;
        ConfigurationContainer.println("Starting Tracer ...\n\n");
        Tracer objTracer = new Tracer();

        try {
            objTracer.init();
            objTracer.process();
            exitCode = 0;
        } catch (Throwable e) {
            e.printStackTrace(ConfigurationContainer.out());
            ConfigurationContainer.println("Message: " + e.getMessage());
        }

        ConfigurationContainer.println("\n\n");
        System.exit(exitCode);
    }
}

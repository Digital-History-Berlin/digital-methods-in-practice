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
package eu.etrap.tracer.postprocessing.traviz;

import eu.etrap.medusa.config.ConfigurationContainer;
import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.postprocessing.AbstractPostprocessing;
import eu.etrap.tracer.postprocessing.PostprocessingException;
import eu.etrap.tracer.utils.FileManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.apache.commons.io.FileUtils;

//import *.TRVTransformer;
/**
 * Created on 22.01.2017 11:55:22 by mbuechler
 *
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class TraVizPostprocessingImpl extends AbstractPostprocessing {

    public void init() throws ConfigurationException {
        super.config();

        strTaxonomyCode = "001";

        if (intMode <= 0) {
            intMode = 1;
        }

        if (intMinimumFrequency <= 0) {
            intMinimumFrequency = 2;
        }
    }

    @Override
    protected void doPostprocessing() throws PostprocessingException, ConfigurationException {
        String strOutputFile;

        strOutputFile = FileManager.getPostprocessingFileName();

        try {
            String strArgs[] = new String[4];
            strArgs[0] = ConfigurationContainer.getGeneralCategory().getProperty("ORIGINAL_CORPUS");
            strArgs[1] = FileManager.getScoringFileName().trim();
            strArgs[2] = new Integer(this.intMode).toString().trim();
            strArgs[3] = new Integer(this.intMinimumFrequency).toString().trim();

            ConfigurationContainer.println("Corpus file name         : " + strArgs[0]);
            ConfigurationContainer.println("TRACER's score file name : " + strArgs[1]);
            ConfigurationContainer.println("TRV mode                 : " + strArgs[2]);
            ConfigurationContainer.println("Minimum frequency        : " + strArgs[3]);

            //Class c = Class.forName("TRVTransformer");
            //Method m = c.getDeclaredMethod("main", String[].class);
            //m.invoke(null, strArgs);
            Class<?> c = Class.forName("TRVTransformer");
            Method meth = c.getMethod("main", String[].class);
            String[] params = null;
            meth.invoke(null, (Object) strArgs);

            File objHTMLPackage = new File(FileManager.getTRVHTMLpackageFolderName());

            File srcDir = objHTMLPackage;
            File destDir = new File(FileManager.getPostprocessingFolderName() + "/html");
            FileUtils.copyDirectory(srcDir, destDir);
            FileUtils.copyFile(new File("reuses.js"), new File(FileManager.getPostprocessingFolderName() + "/reuses.js"));
            FileUtils.copyFile(new File("segments.js"), new File(FileManager.getPostprocessingFolderName() + "/segments.js"));
            FileUtils.copyFile(new File("texts.js"), new File(FileManager.getPostprocessingFolderName() + "/texts.js"));

            FileUtils.deleteQuietly(new File("reuses.js"));
            FileUtils.deleteQuietly(new File("segments.js"));
            FileUtils.deleteQuietly(new File("texts.js"));
             	
            BufferedWriter objWriter = new BufferedWriter(new FileWriter(strOutputFile));
            objWriter.write("TRV is developed by Stefan Jänicke stjaenicke 'at' informatik.uni-leipzig.de.\n\n");
            objWriter.write("--> see the results in \"html/index.html\"");
            objWriter.flush();
            objWriter.close();

        } catch (Exception e) {
            throw new PostprocessingException(e);
        }
    }
}

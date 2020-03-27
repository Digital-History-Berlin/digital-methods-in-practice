/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.etrap.tracer.preprocessing.external.lemmatisation;

import eu.etrap.medusa.config.ConfigurationException;
import java.io.IOException;

/**
 *
 * @author mbuechler
 */
public class DeveloperTestClassLemmatisationMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ConfigurationException, IOException {
        
        TreeTaggerLemmatiserImpl obTagger = new TreeTaggerLemmatiserImpl();
        obTagger.init();
        obTagger.readPoSMappingFile();
        obTagger.process();
    }
    
}

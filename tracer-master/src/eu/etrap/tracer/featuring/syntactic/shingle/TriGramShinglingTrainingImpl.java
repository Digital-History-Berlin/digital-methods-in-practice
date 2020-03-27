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



package eu.etrap.tracer.featuring.syntactic.shingle;

import eu.etrap.medusa.config.ConfigurationException;
import eu.etrap.tracer.featuring.syntactic.AbstractSyntacticalTraining;
import eu.etrap.tracer.featuring.Training;


/**
 * Created on 07.12.2010 16:57:22 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public class TriGramShinglingTrainingImpl extends AbstractSyntacticalTraining implements Training{

    @Override
    public void init() throws ConfigurationException {
        super.init();

        strHierarchy = "01-02-01-01-02";
        this.strNGramFileSuffix = "trigram.shingle";
        intNgramSize=3;
        this.intFeatureFrequencyColumn=intNgramSize;
    }

            @Override
    protected void doPreparation() {
        super.doPreparation();

        setGlobalProperty("MEMORY_ALLOCATOR_IMPL",
                "eu.etrap.medusa.config.NGramMemoryAllocatorImpl");
        this.setProperty( "intNumberOfWords", "" +intNgramSize,
                "eu.etrap.medusa.config.NGramMemoryAllocatorImpl");

        setGlobalProperty("EXPORTER_IMPL",
                "eu.etrap.medusa.export.NGramFlatFileExporterImpl");

        this.setProperty( "intNumberOfWords", "" +intNgramSize,
                "eu.etrap.medusa.export.NGramFlatFileExporterImpl");

        setGlobalProperty("PARSER_FILTER_IMPL",
                "eu.etrap.medusa.filter.sidx.IDXRightTriGramFilterImpl");


        this.setProperty("dblSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IDXRightTriGramFilterImpl");
        this.setProperty("intMinimumFrequency", "0", "eu.etrap.medusa.filter.sidx.IDXRightTriGramFilterImpl");
        this.setProperty("dblCutoffSignificanceThreshold", "0.0", "eu.etrap.medusa.filter.sidx.IDXRightTriGramFilterImpl");
        this.setProperty("intCutoffMinimumFrequency", "0", "eu.etrap.medusa.filter.sidx.IDXRightTriGramFilterImpl");

        this.setGlobalProperty("SIGNIFICANCE_IMPL", "eu.etrap.medusa.significance.FrequencySignificanceImpl");
        this.setGlobalProperty("CUTOFF_SIGNIFICANCE_IMPL", "eu.etrap.medusa.significance.FrequencySignificanceImpl");

        // in order to get integer values as significance measure
        this.setProperty("intAccuracy", "0", "eu.etrap.medusa.export.NGramFlatFileExporterImpl");
        this.setProperty("boolExportFrequency", "false", "eu.etrap.medusa.export.NGramFlatFileExporterImpl");
        this.setProperty("boolReplaceWordNumbers", "true", "eu.etrap.medusa.export.NGramFlatFileExporterImpl");
    }       
}

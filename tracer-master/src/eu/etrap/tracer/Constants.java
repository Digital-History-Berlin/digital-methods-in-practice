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

/**
 * Created on 08.12.2010 12:46:24 by mbuechler
 * @author Marco Büchler: mbuechler@etrap.eu
 */
public interface Constants {

    public static int SENTENCES_START = 1;
    public static int WORD_START = 100000000;
    public static int FEATURE_START = 1000000000;


    // definition of meta information types. Focussed on one meta information
    // file per level + the requirements such as word counts
    public static int TYPE_REQUIREMENTS = 1;
    public static int TYPE_PREPROCESING = 2;
    public static int TYPE_TRAINING = 3;
    public static int TYPE_SELECTION = 4;
    public static int TYPE_LINKING = 5;
    public static int TYPE_SCORING = 6;
    public static int TYPE_POSTPROCESSING = 7;


    // 01. PRE-PROCESSING SECTION
    public String WHITSPACE_ESCAPE = "______";
    public String strWordStart = "\u3014";
    public String strWordEnd = "\u3015";
    public static int intMaxNgramLength = 7;

    // counting stats for changes by preprocessing techniques
    public static int ALL_WORDS = 0;
    public static int OVERALL_CHANGED_TOKENS_INDEX = 1;
    public static int LOWER_CASE_INDEX = 2;
    public static int REMOVE_DIACHRITICS = 3;
    public static int LEMMATISATION_INDEX = 4;
    public static int SYNONYM_INDEX = 5;
    public static int STRING_SIMILARITY_INDEX = 6;
    public static int WORD_LENGTH_INDEX = 7;
    public static int LENGTH_REDUCED_WORDs_INDEX = 8;


    // 02. TRAINING SECTION
    // counting stats for training techniques
    public static int NUMBER_OF_REUSE_UNITS = 0;
    public static int NUMBER_OF_FEATURE_TYPES = 1;
    public static int NUMBER_OF_FEATURE_TOKENS = 2;
    public static int MAX_FEATURE_FREQUENCY = 3;
    public static int NUMBER_OF_FEATURE_TYPES_WITH_FREQ_1 = 4;
    public static int NUMBER_OF_FEATURE_TYPES_WITH_FREQ_2 = 5;
    public static int NUMBER_OF_FEATURE_TYPES_WITH_FREQ_3 = 6;
    public static int NUMBER_OF_FEATURE_TYPES_WITH_FREQ_4 = 7;
    public static int NUMBER_OF_FEATURE_TYPES_WITH_FREQ_5 = 8;
    public static int NUMBER_OF_FEATURE_TYPES_WITH_FREQ_LARGER_THAN_5 = 9;


    // 03. SELECTION SECTION
    // Special key ids for FeatureDistributionStore
    public static int NUMBER_OF_STORED_TOKENS = -1;
    public static int MINIMUM_FEATURE_ID = -2;
    public static int MAXIMUM_FEATURE_ID = -3;

    // Global 0 mod p: maximum accuracy (number of digits)
    public static int OMODP_MAX_ACCURACY = 3;
    public static int WORD_DEPENDENCY_GRAPH_SCALEFACTOR = 100;

    // counting stats for selection techniques
    // same as for training including the following parameters
    public static int NUMBER_OF_FEATURE_TYPES_WITH_FREQ_0 = 10;


    // 04. LINKING SECTION
    // Special key ids for FeatureDistributionStore
    public static int NUMBER_OF_FINGERPRINTED_RUID = 0;
    public static int NUMBER_OF_FINGERPRINT_FEATURES = 1;
    public static int NUMBER_OF_LINKED_RUID = 2;
    public static int NUMBER_OF_LINKED_FEATURES = 3;
    public static int NUMBER_OF_UNIQUE_LINKS = 4;
    public static int NUMBER_OF_LINKED_LINKS = 5;
    public static int LINKING_TIME_FOR_RUID2FEAT = 6;
    public static int LINKING_TIME_FOR_FEAT2RUID = 7;
    public static int LINKING_LINK_TIME = 8;


    // 04. LINKING SECTION
    // Special key ids for FeatureDistributionStore
    public static int SCORING_THRESHOLD = 0;
    public static int SCORING_TOTAL_NUMBER_OF_LINKS = 1;
    public static int SCORING_SELECTED_NUMBER_OF_LINKS = 2;
    public static int SCORING_TIME = 3;
}

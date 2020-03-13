# lemmatise-vulgata.py
# @author: Philipp Schneider
# @date: 2020-02-24
#
# Lemmatises Vulgata.tsv by using the LiLa lemma-Endpoint <https://lila-erc.eu/sparql/dataset.html?tab=query&ds=/lemmaBank>
# Formates file by tracer standards
# Stores lemmas to Vulgata.lemma
# output file is not sorted

from SPARQLWrapper import SPARQLWrapper, JSON
import sys

# Check if a string is a number
def is_number(string):
    try:
        float(string)
        return True
    except ValueError:
        return False

# Query the LiLa database for lemmas and POS-names for one word
def get_lemmas_by_word(word):
    sparql = SPARQLWrapper("https://lila-erc.eu/sparql/lemmaBank/")
    sparql.setQuery("""
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        SELECT ?subject ?subject_label ?poslink ?pos (group_concat(?wr) as ?wrs) WHERE { 
            ?subject <http://www.w3.org/ns/lemon/ontolex#writtenRep> ?wrp . FILTER regex(?wrp, "^%s","i") . 
        ?subject <http://lila-erc.eu/ontologies/lila/hasPOS> ?poslink . 
        ?poslink <http://www.w3.org/2000/01/rdf-schema#label> ?pos .
        ?subject <http://www.w3.org/ns/lemon/ontolex#writtenRep> ?wr .
        ?subject rdfs:label ?subject_label .
        } GROUP BY ?subject ?subject_label ?poslink ?pos
        ORDER BY ?wrs 
    """ % word)
    sparql.setReturnFormat(JSON)
    results = sparql.query().convert()

    new_lemma = ""

    for result in results["results"]["bindings"]:
        #print(result["subject_label"]["value"])
        #print(result["poslink"]["value"])

        new_lemma += word + "\t" + result["subject_label"]["value"] + "\t"
        pos_label = result["poslink"]["value"]
        pos_label = pos_label.split("/")[-1]
        pos_label = pos_label[0]
        new_lemma += pos_label + "\n"
    
    return new_lemma

# Create lemma list
lemmas = ""

# Open file to create lemmas from
with open("data/Vulgata/01 Segmentation/vulgata.tsv") as full_text_file:
    full_text = full_text_file.read()

# # Prepare log file
# lemma_log = ""
# log_counter = 1

text_length = len(full_text.split())
text_pos = 1

# Get Lemma for each word in the original text
for word in full_text.split():
    word = word.lower()
    if not is_number(word) and not ":" in word and not "-" in word:
        # Check if word has already been queried
        if word not in lemmas:
            lemmas += get_lemmas_by_word(word)
            
            # Log progress
            log_line = "Lemmatising word " + str(text_pos) + "/" + str(text_length)
            print(log_line)
            # lemma_log = lemma_log + "\n" + log_line
            # log_counter += 1

            # if log_counter > 100:
            #     log_file = open("logs/lemma.log")
            #     lemma_old_log = log_file.read()
            #     lemma_log = lemma_old_log + lemma_log
            #     log_file.write(lemma_log)
            #     log_file.close()

    text_pos += 1

    # Toggle for testing purposes
    # if text_pos == 50:
    #     break

# Write lemmas to file
lemma_file = open("data/Vulgata/01 Segmentation/Vulgata.lemma", "w")
lemma_file.write(lemmas)
lemma_file.close()
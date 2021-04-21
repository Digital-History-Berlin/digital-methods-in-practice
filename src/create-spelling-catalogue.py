# create-spelling-catalogue.py
"""
Creates a list of spelling variants from the LiLa database <https://lila-erc.eu/sparql/lemmaBank/>
"""

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
            ?subject <http://www.w3.org/ns/lemon/ontolex#writtenRep> ?wrp .
            FILTER regex(?wrp, "^%s$","i") . 
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
        #print(result)

        if len(result["wrs"]["value"].split(" ")) > 1:
            print(result["subject_label"]["value"])
            print(result["wrs"]["value"].split(" "))
            #print(result["poslink"]["value"])

            written_reps = result["wrs"]["value"].split(" ")

            for variant in written_reps:
                new_lemma += variant + "\t" + word + "\t"
                pos_label = result["poslink"]["value"]
                pos_label = pos_label.split("/")[-1]
                pos_label = pos_label[0]
                new_lemma += pos_label + "\n"
                
                print(variant)



            # new_lemma += word + "\t" + result["subject_label"]["value"] + "\t"
            # pos_label = result["poslink"]["value"]
            # pos_label = pos_label.split("/")[-1]
            # pos_label = pos_label[0]
            # new_lemma += pos_label + "\n"
    
    return new_lemma

#print(get_lemmas_by_word("caryota"))

with open("data/latin.lemma", "r") as lemma_file:
    old_lemmas = lemma_file.readlines()

spelling_cat = ""
lemma_index = 1

for line in old_lemmas:
    current_lemma = line.split("\t")
    current_lemma = current_lemma[1].strip()

    print("Getting spelling variants for lemma " + str(lemma_index) + "/" + str(len(old_lemmas)) + ": " + current_lemma)

    if current_lemma not in spelling_cat:
        spelling_lemma = get_lemmas_by_word(current_lemma)
        spelling_lemma = spelling_lemma.strip()
        spelling_cat += spelling_lemma

        print(spelling_lemma)

    lemma_index += 1

    # if lemma_index == 100:
    #     break

print(spelling_cat)

with open("data/spelling-variants.lemma", "w") as spelling_cat_file:
    spelling_cat_file.write(spelling_cat)

print("Done")
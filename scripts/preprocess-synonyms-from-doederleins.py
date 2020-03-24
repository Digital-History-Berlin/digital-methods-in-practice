# preprocess-synonyms-from-doederleins.py
# @author: Philipp Schneider
# @date: 2020-03-16
# 
# Prepares list of latin synonyms for tracer
# Synonyms are extracted from an ebook version of "Doederlein's Hand-book of Latin Synonymes"
# For provenance and more information on the source see https://github.com/GITenberg/D-derlein-s-Hand-book-of-Latin-Synonymes_33197

import re

with open("data/Doederlein-s-Hand-book-of-Latin-Synonymes/33197-8.txt", "r") as synonyms_file:
    synonyms_source = synonyms_file.read()

# Cut the appendix to avoid wrong matches that do not belong to the list of synonyms
end_index = synonyms_source.find("INDEX OF GREEK WORDS")
synonyms_source = synonyms_source[:end_index]

# Get all lemma entries that include a list of synonym lemmas and are not references to other synonyms in the file
search_pattern = re.compile(r"(([A-Z]+; )+[A-Z]+\.)")
synonyms = search_pattern.findall(synonyms_source)

synonyms_tracer = ""

print("Extracting latin synonyms from Doederleins and preprocessing them for tracer...")

# Extract each synonym list
for syn_entry in synonyms:
    syn_entry = syn_entry[0][:-1]
    syn_entry_list = syn_entry.split("; ")

    # Prepare each synonym entry and add them to final string for file
    syn_lemma = syn_entry_list[0]

    for i in range(1, len(syn_entry_list)):
        synonyms_tracer += syn_lemma.lower() + "\t" + syn_entry_list[i].lower() + "\n"
        synonyms_tracer += syn_entry_list[i].lower() + "\t" + syn_lemma.lower() + "\n"

synonyms_tracer_file = open("data/latin.syns", "w")
synonyms_tracer_file.write(synonyms_tracer)
synonyms_tracer_file.close()

print("Done. Synonym list is stored in data/latin.syns")
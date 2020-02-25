# preprocess-historia-hierosylitama.py
# @author: Philipp Schneider
# @date: 2020-02-25
#
# Prepares Fulcher Chroncile for Tracer
# Extracts segmented text from TEI
# Text is stored as txt accoridng to tracer standards
# Extracts lemmas from TEI
# Stores lemmas in seperate file

from bs4 import BeautifulSoup
import sys
import csv
import time
import datetime
import ast

# Get current timestamp for provenance-date
timestamp = time.time()
timestamp = datetime.datetime.fromtimestamp(timestamp).strftime('%Y-%m-%d %H:%M:%S')

# Open TEI file
with open("data/Historia_Hierosylitama/Historia Hierosolymitana_with_lemmas.xml") as historia_file:
    historia_tei = historia_file.read()

# Create new file to store preprocessed text
new_text_file = open("data/Historia_Hierosylitama/01 Segmentation/historia-hierosylitama.txt", "w")
historia_writer = csv.writer(new_text_file, delimiter = "\t")

# Creta new file to store lemmas
lemma_file = open("data/Historia_Hierosylitama/01 Segmentation/historia-hierosylitama.lemma", "w")

h_soup = BeautifulSoup(historia_tei, "xml")

sentences = h_soup.find_all("s")
#prepared_text = ""
line_index = 1200000001
lemmas = ""

print("Extracting text and lemmas from TEI file...")

for s in sentences:
    # Extract and store text from TEI
    new_line = s.get_text().encode("utf8")
    historia_writer.writerow([line_index] + [new_line] + [timestamp] + ["Historia-Hierosylitama"])
    line_index += 1

    # Extract and store lemmas from TEI
    words = s.find_all("w")
    
    for w in words:
        current_word = w.get_text()
        new_lemma = w.get("lemma")

        if new_lemma is not None and current_word not in lemmas:
            new_lemma = ast.literal_eval(w.get("lemma"))        # cast string to list and dict
            new_lemma = new_lemma[0]["name"].split("@")
            lemmas += current_word + "\t" + new_lemma[0] + "\t" + new_lemma[1] + "\n"

# Write lemmas to file
lemmas = lemmas.encode("utf8")
lemma_file.write(lemmas)

new_text_file.close()
lemma_file.close()

print("Done")
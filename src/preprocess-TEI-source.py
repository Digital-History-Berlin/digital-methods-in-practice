# preprocess-TEI-source.py
"""
@author: Philipp Schneider
@date: 2020-02-25

Prepares a text from <https://www.comphistsem.org/texts.html> for Tracer
Extracts segmented text from TEI
Text is stored as txt accoridng to tracer requirements <https://tracer.gitbook.io/-manual/manual/corpus-preparation>
Extracts lemmas from TEI
Stores lemmas in seperate file according to tracer requirements <https://tracer.gitbook.io/-manual/manual/pos-tagging-lemmatisation-and-wordnets>

The program needs to take three arguments on startup:
1. Path and name of the input TEI file
2. Path and name of the output file. No file suffix. The txt and lemma file will have the same name
3. Number of the source (this is needed for the id in the txt for processing by tracer)
"""

from bs4 import BeautifulSoup
import sys
import csv
import time
import datetime
import ast
import re

# Get current timestamp for provenance-date
timestamp = time.time()
timestamp = datetime.datetime.fromtimestamp(timestamp).strftime('%Y-%m-%d')

allowed_pos_tags = ["n", "v", "a"]

# Check if script is run with enough arguments
try:
    # Open TEI file
    with open(sys.argv[1]) as tei_file:
        text_tei = tei_file.read()
    
    output_file_name = sys.argv[2]
    source_number = sys.argv[3]
    source_title = output_file_name.split("/")[-1]
except IndexError:
    print("Not enough arguments. Please give the TEI file as a first argument, the name of the output file as the second (no file suffix), and the number of your source as the third (for the text id in the tracer input)")
    sys.exit()

# filename = sys.argv[1]
# filename = filename.replace(".xml", "")

# print(filename)
# tei_file.close()

# sys.exit()

# new file name as argument
# store same file name with txt and with lemma

# Create new file to store preprocessed text
#new_text_file = open("data/Historia_Hierosylitama/01 Segmentation/historia-hierosylitama.txt", "w")
new_text_file_path = output_file_name + ".txt"
new_text_file = open(new_text_file_path, "w")
source_writer = csv.writer(new_text_file, delimiter = "\t")

# Creta new file to store lemmas
lemma_path = output_file_name + ".lemma"
lemma_file = open(lemma_path, "w")
#lemma_file = open("data/Historia_Hierosylitama/01 Segmentation/historia-hierosylitama.lemma", "w")

h_soup = BeautifulSoup(text_tei, "xml")

sentences = h_soup.find_all("s")
#prepared_text = ""
line_index = "1" + source_number + "00000001"
line_index = int(line_index)
lemmas = ""

print("Extracting text and lemmas from TEI file...")

full_line = ""

for s in sentences:
    # Extract and store text from TEI
    new_line = s.get_text().encode("utf8")
    new_line = new_line.lower()
    new_line = new_line.strip()

    print(new_line)
    print(new_line[-1])

    if "(" not in new_line and ")" not in new_line:
        #line_segmented = new_line.split(".")
        line_segmented = new_line.split(".")
        line_segmented = re.split('\.|; |\: ', new_line)
        print(line_segmented)
    else:
        line_segmented = re.split('; |\: ', new_line)
        #line_segmented = [new_line]
    # for segment in line_segmented:
    #     if len(segment) < 6:

    print(line_segmented)
    print(len(line_segmented))

    # Remove last segment if it is empty
    if line_segmented[-1] == "":
        del line_segmented[-1]

    print(line_segmented)
    print(len(line_segmented))

    # Reconnect two segments if there is an open bracket without a closed one
    # i = 1
    # for segment in line_segmented:
    #     if ")" in line_segmented[i] and not "(" in line_segmented[i]:
    #         h = i - 1
    #         line_segmented[h] = line_segmented[h] + line_segmented[i]
    #         del line_segmented[i]
    #         i += 1

    # Check if segmentation for sentences is correct in source edition (not multiple sentences in one line)
    if len(line_segmented) > 1:# and line_segmented[-1] != 0:
        # print(len(line_segmented[-1]))
        # if len(line_segmented[-1]) != 0:
        #line_segmented[-1] = line_segmented[-1] + "."

        for segment in line_segmented:
            print(segment)
            print("true")
            source_writer.writerow([line_index] + [segment] + [timestamp] + [source_title])
            line_index += 1
    else:
        # Check if segmentation for sentences is correct in source edition (not one sentence over multiple lines)
        if new_line[-1] != "." and new_line[-1] != "!" and new_line[-1] != "?":
            full_line += new_line + " "
        else:
            print("false")
            full_line += new_line
            source_writer.writerow([line_index] + [full_line] + [timestamp] + [source_title])
            line_index += 1
            full_line = ""

    # Extract and store lemmas from TEI
    words = s.find_all("w")
    
    for w in words:
        current_word = w.get_text()
        current_word = current_word.lower()
        new_lemma = w.get("lemma")

        if new_lemma is not None and current_word not in lemmas:
            new_lemma = ast.literal_eval(w.get("lemma"))        # cast string to list and dict
            new_lemma = new_lemma[0]["name"].split("@")

            # Check if POS-tag is number. If yes skip that word and do not add to lemma file
            if new_lemma[1] == "NUM":
                break

            # Prepare POS-tags
            pos_tag = new_lemma[1]
            pos_tag = pos_tag[0]
            pos_tag = pos_tag.lower()

            if pos_tag in allowed_pos_tags:
                lemmas += current_word + "\t" + new_lemma[0] + "\t" + pos_tag + "\n"

            #lemmas += current_word + "\t" + new_lemma[0] + "\t" + new_lemma[1] + "\n"

if full_line != "":
    source_writer.writerow([line_index] + [full_line] + [timestamp] + [source_title])

# Write lemmas to file
lemmas = lemmas.encode("utf8")
lemma_file.write(lemmas)

new_text_file.close()
lemma_file.close()

print("Done")
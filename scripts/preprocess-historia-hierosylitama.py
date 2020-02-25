# preprocess-historia-hierosylitama.py

from bs4 import BeautifulSoup
import sys
import csv
import time
import datetime

# Get current timestamp for provenance-date
timestamp = time.time()
timestamp = datetime.datetime.fromtimestamp(timestamp).strftime('%Y-%m-%d %H:%M:%S')

# Open TEI file
with open("data/Historia_Hierosylitama/Historia Hierosolymitana_with_lemmas.xml") as historia_file:
    historia_tei = historia_file.read()

new_text_file = open("data/Historia_Hierosylitama/01 Segmentation/historia-hierosylitama.txt", "w")
historia_writer = csv.writer(new_text_file, delimiter = "\t")

h_soup = BeautifulSoup(historia_tei, "xml")

sentences = h_soup.find_all("s")
#prepared_text = ""
line_index = 1200000001

print("Extracting text from TEI file...")

for s in sentences:
    #prepared_text += s.get_text() + "\n"
    new_line = s.get_text().encode("utf8")
    historia_writer.writerow([line_index] + [new_line] + [timestamp] + ["Historia-Hierosylitama"])
    line_index += 1

new_text_file.close()

print("Done")
# preprocess-historia-hierosylitama.py

from bs4 import BeautifulSoup
import sys

# Open TEI file
with open("data/Historia_Hierosylitama/Historia Hierosolymitana_with_lemmas.xml") as historia_file:
    historia_tei = historia_file.read()

h_soup = BeautifulSoup(historia_tei, "xml")

sentences = h_soup.find_all("s")
prepared_text = ""

for s in sentences:
    prepared_text += s.get_text() + "\n"

print(prepared_text)

text_file = open("data/Historia_Hierosylitama/01 Segmentation/historia-hierosylitama.txt", "w")
text_file.write(prepared_text.encode("utf8"))
text_file.close()
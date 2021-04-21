# prepare-vulgata.py
"""
Script preprocesses Vulgata text by removing brackets in the Book of Psalms

This script was written for a different version of the Vulgate, than the
one from CompHistSem, used in the paper.
"""

import re

with open("../data/Vulgata/01 Segmentation/vulgata_no-book-ref.txt", "r") as vulgata_file:
    vulgata = vulgata_file.read()

search_pattern = re.compile(r"\(\d+:.{1,3}\)")
psalm_refs = search_pattern.findall(vulgata)

for entry in psalm_refs:
    print(entry)
    vulgata = vulgata.replace(str(entry), "")

print(vulgata)

vulgata_file = open("../data/Vulgata/01 Segmentation/vulgata_no-book-ref.txt", "w")
vulgata_file.write(vulgata)
vulgata_file.close()

print("Done")
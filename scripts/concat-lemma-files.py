# concat-lemma-files.py
# @author: Philipp Schneider
# @date: 2020-04-18
#
# Concatenates multiple lemma files to one.
# Obliterates doublets

import sys
from os import walk

lemma_dir = "data/lemma_files/"
all_lemmas = ""

# Get all lemma files from first directory level
lemma_files = []
for (dirpath, dirnames, filenames) in walk(lemma_dir):
    print(dirpath)
    print(dirnames)
    print(filenames)
    lemma_files.extend(filenames)
    break

for l_file_name in lemma_files:
    file_name = lemma_dir + l_file_name
    print("Extracting lemmas from " + file_name)

    with open(file_name, "r") as l_file:
        lemma_lines = l_file.readlines()

        for lemma in lemma_lines:
            if lemma not in all_lemmas:
                all_lemmas += lemma

with open("data/latin.lemma", "w") as new_lemma_file:
    new_lemma_file.write(all_lemmas)
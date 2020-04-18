# preprocess-multiple-TEI-source.py
# @author: Philipp Schneider
# @date: 2020-04-18
# 
# Prepares multiple file source from https://www.comphistsem.org/texts.html for Tracer
# requires preprocess-TEI-source.py
# Extracts segmented text from TEI
# Text is stored as txt accoridng to tracer standards
# Extracts lemmas from TEI
# Stores lemmas in one seperate file
#
# The program needs to take three arguments on startup:
# 1. Path and name of the input TEI directory
# 2. Path and name of the directory for the output files. Must end with '/'. The output directory and lemma file will have the same name
# 3. Number of the source (this is needed for the id in the txt for processing by tracer)

import sys
import os
from os import walk

# Check if script is run with enough arguments
try:
    tei_dir = sys.argv[1]
    output_dir = sys.argv[2]

    source_number = sys.argv[3]
except IndexError:
    print("Not enough arguments. Please give the TEI directory as a first argument, the name of the output directory as the second (must end with '/'), and the number of your source as the third (for the text id in the tracer input)")
    sys.exit()

print(tei_dir)

# Get all files from first directory level
tei_files = []
for (dirpath, dirnames, filenames) in walk(tei_dir):
    print(dirpath)
    print(dirnames)
    print(filenames)
    tei_files.extend(filenames)
    break

# Preprocess every file
for tei in tei_files:
    tei_name = tei.split("_")[2:]
    tei_name = "_".join(tei_name)
    print(tei_name)

    move_command = "mv " + tei_dir + "/" + tei + " " + tei_dir + "/" + tei_name
    print(move_command)
    os.system(move_command)

    input_file_path = tei_dir + "/" + tei_name
    tei_name = tei_name.replace(".xml", "")

    os.system('python scripts/preprocess-TEI-source.py "' + input_file_path + '" "' + output_dir + '/' + tei_name + '" ' + source_number)
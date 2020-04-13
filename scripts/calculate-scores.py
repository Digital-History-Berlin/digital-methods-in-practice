# calculate-scores.py

import sys
import csv
import ast

concordance_data = []

# Check if script is run with enough arguments
try:
    concordance_path = sys.argv[1]
    result_path = sys.argv[2]

    # Open concordance file
    with open(concordance_path) as concordances_file:
        # concordances = concordances_file.read()
        concordances = csv.DictReader(concordances_file, delimiter = ";")

        for row in concordances:
            concordance_data.append([row['vulgata_zeile'], row['\xef\xbb\xbfhistoria-francorum_zeile']])

    # Open reuse result file
    with open(result_path) as result_file:
        reuse_results = result_file.read()
        reuse_results = reuse_results.replace("var reusesData = ", "")
        reuse_results = reuse_results[:-1]
        reuse_results = reuse_results.strip()
        reuse_results = reuse_results.replace('i', '"i"')
        reuse_results = reuse_results.replace('j', '"j"')
        reuse_results = reuse_results.replace('s', '"s"')
        reuse_results = ast.literal_eval(reuse_results)
    
    # output_file_name = sys.argv[2]
    # source_number = sys.argv[3]
    # source_title = output_file_name.split("/")[-1]
except IndexError:
    print("Not enough arguments. Please give the TEI file as a first argument, the name of the output file as the second (no file suffix), and the number of your source as the third (for the text id in the tracer input)")
    sys.exit()

evaluation_path = result_path.replace("reuses.js", "") + "evaluation_scores.txt"

print("---")
print("Results can be found in:\n\n" + evaluation_path)
print("---")

sys.stdout = open(evaluation_path, "w")

number_relevant_retrieved = 0

for retrieved in reuse_results:
    for rel in concordance_data:
        if str(retrieved["i"]) == rel[0]:
            if str(retrieved["j"]) == rel[1]:
                number_relevant_retrieved += 1

number_retrieved = len(reuse_results)
number_relevant = len(concordance_data)

precision_frac = str(number_relevant_retrieved) + "/" + str(number_retrieved)
precision = float(number_relevant_retrieved) / float(number_retrieved)

recall_frac = str(number_relevant_retrieved) + "/" + str(number_relevant)
recall = float(number_relevant_retrieved) / float(number_relevant)

f_measure = 2 * ((precision * recall) / precision + recall)

print("precision_frac: " + precision_frac)
print("precision: " + str(precision))
print("---")
print("recall_frac: " + recall_frac)
print("recall: " + str(recall))
print("---")
print("f_measure: " + str(f_measure))
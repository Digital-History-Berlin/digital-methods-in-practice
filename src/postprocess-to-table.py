# postprocess-to-table.py
"""
@author: Philipp Schneider
@date: 2020-04-10

Postprocesses output data from tracer so that it can be read in tabular form.
Reused words are displayed bold to enhance readability.
Output data is returned as html table.
Output path has to be set manually.

Arg1: original text path
Arg2: preprocessed text path
Arg3: result file path (reuses.js)
Arg4: selection file path
Arg5: feats path
"""

import sys
import re
import csv
import ast

#concordance_data = []

# class color:
#     BOLD = '\033[1m'
#     END = '\033[0m'

def open_tracer_corpus(text_dict, filepath):
    # Open text file
    with open(filepath) as text_file:
        segments = text_file.readlines()

        for row in segments:
            new_segment = row.split("\t")
            text_dict[str(new_segment[0])] = new_segment[1]
        
        return text_dict

text_data = {}
preprocessed_data = {}
word_data = {}
sel_data = []

output_table = """<html>
                    <head></head>
                    <body>
                        <table>"""

# try:
#     input_path = sys.argv[1]

#     with open(input_path, "r") as input_file:
#         input_links = input_file.readlines()

#         print(input_links)

#         text_path = input_links[0].replace("\n", "")
#         preprocessed_path = input_links[1].replace("\n", "")
#         result_path = input_links[2].replace("\n", "")
# except IndexError:
#     print("Not enough arguments.")
#     sys.exit()

# Check if script is run with enough arguments
try:
    text_path = sys.argv[1]
    preprocessed_path = sys.argv[2]
    result_path = sys.argv[3]

    text_data = open_tracer_corpus(text_data, text_path)
    preprocessed_data = open_tracer_corpus(preprocessed_data, preprocessed_path)

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

        word_path = sys.argv[5]
        word_data = open_tracer_corpus(word_data, word_path)

        sel_path = sys.argv[4]

        with open(sel_path, "r") as sel_file:
            selected_features = sel_file.readlines()

            for feature in selected_features:
                new_feature = feature.split("\t")
                new_feature[1] = new_feature[1].replace("\n", "")

                new_feature[0] = word_data[new_feature[0]]
                sel_data.append(new_feature)

except IndexError:
    print("Not enough arguments.")
    sys.exit()


#print(reuse_results)

count_i = 1
count_rel = 1

for retrieved in reuse_results:
    index_i = retrieved['i']
    index_i = str(index_i)[:2]

    index_j = retrieved['j']
    index_j = str(index_j)[:2]

    prep_text_i = " " + preprocessed_data[str(retrieved['i'])] + " "
    prep_text_j = " " + preprocessed_data[str(retrieved['j'])] + " "

    # Make all selected words bold
    for entry in sel_data:
        #entry_word = " " + entry[0] + " "

        if entry[1] == str(retrieved['i']):
            bold_word = " <b>" + entry[0] + "</b> "
            prep_text_i = prep_text_i.replace(entry[0], bold_word)
        elif entry[1] == str(retrieved['j']):
            bold_word = " <b>" + entry[0] + "</b> "
            prep_text_j = prep_text_j.replace(entry[0], bold_word)
    
    # Extract all words selected in both sentences into a list
    search_pattern = re.compile(r" <b>\w+<\/b> ")
    sel_i = search_pattern.findall(prep_text_i)
    sel_j = search_pattern.findall(prep_text_j)

    #print(set(sel_i) & set(sel_j))

    common_words = set(sel_i) & set(sel_j)
    comomn_len = len(common_words)
    common_words_str = ""

    # Format common word list and make only words in this list bold    
    prep_text_i = prep_text_i.replace("<b>", "")
    prep_text_i = prep_text_i.replace("</b>", "")
    prep_text_j = prep_text_j.replace("<b>", "")
    prep_text_j = prep_text_j.replace("</b>", "")
    
    for word in common_words:
        common_words_str += word + ", "

        current_word = word.replace("<b>", "")
        current_word = current_word.replace("</b>", "")
        bold_word = "<b>" + current_word + "</b>"
        prep_text_i = prep_text_i.replace(current_word, bold_word)
        prep_text_j = prep_text_j.replace(current_word, bold_word)
    
    common_words_str = common_words_str[:-2]

    if prep_text_i.count("<b>") > 2 and prep_text_j.count("<b>") > 2:
        if index_i == "11":
            output_table += "<tr><td>" + str(retrieved['i']) + "</td><td>" + str(comomn_len)  + "</td><td>" + common_words_str +  "</td><td>" + prep_text_i + "</td><td>" + text_data[str(retrieved['i'])] + "</td></tr>"
            output_table += "<tr><td>" + str(retrieved['j']) + "</td><td>" + "</td><td></td><td>" + prep_text_j + "</td><td>" + text_data[str(retrieved['j'])] + "</td></tr>"
            output_table += "<tr></tr>"
        else:
            output_table += "<tr><td>" + str(retrieved['j']) + "</td><td>" + str(comomn_len)  + "</td><td>" + common_words_str +  "</td><td>" + prep_text_j + "</td><td>" + text_data[str(retrieved['j'])] + "</td></tr>"
            output_table += "<tr><td>" + str(retrieved['i']) + "</td><td>" + "</td><td></td><td>" + prep_text_i + "</td><td>" + text_data[str(retrieved['i'])] + "</td></tr>"
            output_table += "<tr></tr>"
        # else:
        #     print(prep_text_i)
        #     print(prep_text_j)

        count_rel += 1

    print("Working on result " + str(count_i) + "/" + str(len(reuse_results)))

    count_i += 1

print("There are " + str(count_rel) + " reuses with more than 2 common features.")

output_table += """</table>
                    </body>
                </html>"""

with open("result_data/22_Machabaeorum_2_francorum.html", "w") as output_file:
    output_file.write(output_table)
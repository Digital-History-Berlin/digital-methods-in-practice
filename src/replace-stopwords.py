# replace-stopwords.py
"""
@author: Philipp Schneider
@date: 2020-04-10

Removes all stopwords listed in the file latin.txt from the vulgate. Currently no input parameters -- input file is
set via the global variable file_path.
"""

file_path = "../tracer-master/data/corpora/18_apocalypsis_francorum_no-stopwords/18_apocalypsis_francorum_no-stopwords.txt"

with open("../tracer-master/data/stopwords/latin.txt", "r") as stopword_file:
    stopwords = stopword_file.readlines()

with open(file_path, "r") as corpus_file:
    corpus = corpus_file.read()

for stopword_entry in stopwords:
    print(stopword_entry)
    stopword_entry = stopword_entry.replace("\n", "")
    stopword = " " + stopword_entry + " "
    corpus = corpus.replace(stopword, " ")

    stopword = "\t" + stopword_entry + " "
    corpus = corpus.replace(stopword, "\t")

    stopword = " " + stopword_entry + "."
    corpus = corpus.replace(stopword, " ")

    stopword = " " + stopword_entry + ","
    corpus = corpus.replace(stopword, " ")

    stopword = " " + stopword_entry + ";"
    corpus = corpus.replace(stopword, " ")

print(corpus)

corpus_file = open(file_path, "w")
corpus_file.write(corpus)
corpus_file.close()
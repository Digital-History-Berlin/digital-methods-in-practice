# replace-stopwords.py

with open("tracer-master/data/stopwords/latin.txt", "r") as stopword_file:
    stopwords = stopword_file.readlines()

with open("tracer-master/data/corpora/1_5_vulgata_francorum/1_3_vulgata_francorum.txt", "r") as corpus_file:
    corpus = corpus_file.read()

for stopword_entry in stopwords:
    print(stopword_entry)
    stopword_entry = stopword_entry.replace("\n", "")
    stopword = " " + stopword_entry + " "
    corpus = corpus.replace(stopword, " ")

    stopword = " " + stopword_entry + "."
    corpus = corpus.replace(stopword, " ")

    stopword = " " + stopword_entry + ","
    corpus = corpus.replace(stopword, " ")

    stopword = " " + stopword_entry + ";"
    corpus = corpus.replace(stopword, " ")

print(corpus)

corpus_file = open("tracer-master/data/corpora/1_5_vulgata_francorum/1_3_vulgata_francorum.txt", "w")
corpus_file.write(corpus)
corpus_file.close()
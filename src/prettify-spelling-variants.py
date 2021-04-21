# prettify-spelling-variants.py
"""
Script corrects line break errors in the file data/spelling-variants.lemma
"""

with open("../data/spelling-variants.lemma", "r") as spelling_variants_file:
    spelling_variants = spelling_variants_file.readlines()

new_spelling_variants = ""

for line in spelling_variants:
    old_line = line.split("\t")
    if len(old_line) > 3:
        print(old_line)

        split_pos = old_line[2][0]
        split_lemma = old_line[2][1:]
        print(split_pos)
        print(split_lemma)
        new_line_1 = [old_line[0], old_line[1], split_pos]
        new_line_2 = [split_lemma] + old_line[3:]

        print(new_line_1)
        print(new_line_2)

        new_line_1 = "\t".join(new_line_1)
        new_line_1 = new_line_1.replace("\n", "")
        new_line_2 = "\t".join(new_line_2)
        new_line_2 = new_line_2.replace("\n", "")

        new_spelling_variants += new_line_1 + "\n"
        new_spelling_variants += new_line_2 + "\n"

        print(new_line_1)
        print(new_line_2)
    else:
        new_line = line.replace("\n", "")
        new_spelling_variants += new_line + "\n"

print(new_spelling_variants)

with open("../data/spelling-variants_pretty.lemma", "w") as new_spelling_file:
    new_spelling_file.write(new_spelling_variants)
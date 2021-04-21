# Digital Methods in Practice

This repository includes the research data and source code from the following paper:
```
Torsten Hiltmann, Jan Keupp, Melanie Althage, Philipp Schneider: Digital Methods in Practice. The Epistemological Implications of Applying Text Re-Use Analysis to the Bloody Accounts of the Conquest of Jerusalem (1099), in: Geschichte und Gesellschaft 47, 2021, S. 1-36.
```

## Abstract
While the question concerning the possible epistemological impact and implications of digital methods on the production of historical knowledge has been raised time and again, detailed studies in this regard have been scarce. Taking the analysis of the use of the Bible in the accounts on the Conquest of Jerusalem in 1099 as an example, this paper examines the differences between the analogue and the digital approach. Drawing on the method of text re-use analysis and the tool "Tracer", it demonstrates the application of digital methods in practice and shows the consequences this has for historical research. 

## Directory Structure

The research data is divided into three groups to seperate code, pre- and postprocessed data and the result data, discussed in our paper.
* `data`: Includes all working data for the research project. The unprocessed historical sources, used for our study, can be found in `Historia_Francorum` and `Vulgata` respectively. `corpora` includes the pre-processed historical texts as well as the research data created by Tracer, the tool we used for our analysis. The former are named in such a way to make it clear, which historical source texts are being compared to each other in the respective batch (e.g. `20_apocalypsis_francorum` compares the "Book of Revelations" with the "Historia Francorum). The latter are included in the subfolder `TRACER_DATA`. Aside from that, lemmata and sysnonyms are stored in `data`.
* `results`: Includes the postprocessed results, we used for our discussion in the paper. These are tabular data for each relevant analysis, stored as html files. All data in `results` were created by our own scripts from the result data generated by Tracer (stored in `corpora`).
* `src`: Includes the source code created by us for this project. It consists of different pre- and postprocessing scripts. Their particular purpose is documented in the code files. Please note, that all scripts were written for UNIX systems and not tested on Windows machines. All scripts use their own location as directory base.

```bash
digital-methods-in-practice/
├── data
│   ├── corpora
│   │   ├── 20_apocalypsis_francorum
│   │   │   ├── 20_apocalypsis_francorum.txt
│   │   │   ├── TRACER_DATA
│   │   │   ├── ...
│   │   ├── ...
│   ├── Historia_Francorum
│   ├── Vulgata
│   ├── ...
├── results
├── src
│   ├── requirements.txt
│   ├── ...
```

## Authors
* [Torsten Hiltmann](https://www.geschichte.hu-berlin.de/de/bereiche-und-lehrstuehle/digital-history/personen/torsten-hiltmann) ([ORCID](https://orcid.org/0000-0002-6757-6210))
* [Jan Keupp](https://www.uni-muenster.de/Geschichte/histsem/MA-G/L3/organisation/JanKeupp.html)
* [Melanie Althage](https://www.geschichte.hu-berlin.de/de/bereiche-und-lehrstuehle/digital-history/personen/althage) ([ORCID](https://orcid.org/0000-0001-5233-1061))
* [Philipp Schneider](https://www.geschichte.hu-berlin.de/de/bereiche-und-lehrstuehle/digital-history/personen/philipp-schneider-m-a) ([ORCID](https://orcid.org/0000-0002-6743-8600))

## Acknowledgements

The result data from this research project was created with the tool [Tracer](https://vcs.etrap.eu/tracer-framework/tracer) (DOI: doi.org/21.11101/0000-0007-C9CA-3). Tracer is a software for text reuse detection, created by Marco Büchler. You can find more information on this project here: https://www.etrap.eu/research/tracer/
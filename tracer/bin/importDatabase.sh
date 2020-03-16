#!/bin/bash
# creates database and tables in $2 and loads all interesting files
# aborts if database already exists


if [ $# != 4 ]; then
	echo "usage: $0 [BUILD_NAME] [DATABASE_NAME] [HOST] [PATH]"
	echo "Export environment variables PASS and USER (database login) first!"
	echo; echo "Example:"
	echo "export USER=USERNAME"
	echo "export PASS=PASSWORD"
	echo "$0 example exampleDB localhost ../data/corpora/example"
	exit 0
fi

# database parameters
export WS_HOST=$3
export MYSQL="mysql -h $WS_HOST -u $USER -p$PASS"

# general parameters
BUILD_NAME=$1
DATABASE_NAME=$2
WORK_DIR=${PWD%/*}
DB_PATH=$4

# auxiliary function for errors
exitf(){
	echo
	echo ERROR: "$2"
	echo

	exit $1
}

echo "####################################"
echo "### Import files in new database ###"
echo "####################################"
echo "### Creating database and tables ###"
$MYSQL -e "CREATE DATABASE ${DATABASE_NAME};" || exitf $? "cannot create database"
$MYSQL ${DATABASE_NAME} < ../data/db-schema/DefaultDBSchema.sql || exitf $? "cannot create tables"

MYSQL="mysql -h $WS_HOST -u $USER -p$PASS -b ${DATABASE_NAME}"	# modify mysql-Statement

#######################################
echo; echo "### Importing Medusa data ###"
echo "  disabling keys"
$MYSQL -e "ALTER TABLE sentences DISABLE KEYS; ALTER TABLE inv_w DISABLE KEYS; ALTER TABLE co_s DISABLE KEYS; ALTER TABLE co_n DISABLE KEYS; ALTER TABLE sources DISABLE KEYS; ALTER TABLE inv_so DISABLE KEYS;" || exitf $? "cannot disable keys"
echo "   importing sentences"
$MYSQL -e "LOAD DATA LOCAL INFILE '$DB_PATH/${BUILD_NAME}.txt.db' INTO TABLE sentences" || exitf $? "cannot import sentences"
echo "   importing word list"
$MYSQL -e "LOAD DATA LOCAL INFILE '$DB_PATH/${BUILD_NAME}.txt.wnc' INTO TABLE words FIELDS ESCAPED BY '' (w_id, word, @dummy, freq)" || exitf $? "cannot import word list"
echo "   importing inverted words list"
$MYSQL -e "LOAD DATA LOCAL INFILE '$DB_PATH/${BUILD_NAME}.txt.de.uni_leipzig.asv.medusa.filter.sidx.IDXInvertedListFilterImpl.hash.lgl2.expo' INTO TABLE inv_w"  || exitf $? "cannot import inverted word list"
echo "   importing sentence cooccurrences"
$MYSQL -e "LOAD DATA LOCAL INFILE '$DB_PATH/${BUILD_NAME}.txt.de.uni_leipzig.asv.medusa.filter.sidx.IDXSentenceFilterImpl.hash.lgl2.expo' INTO TABLE co_s" || exitf $? "cannot import sentences cooccurrences"
echo "   importing neighbour cooccurrences"
$MYSQL -e "LOAD DATA LOCAL INFILE '$DB_PATH/${BUILD_NAME}.txt.de.uni_leipzig.asv.medusa.filter.sidx.IDXNeighbourhoodFilterImpl.hash.lgl2.expo' INTO TABLE co_n" || exitf $? "cannot import neighbour cooccurrences"
echo "   importing sources"
$MYSQL -e "LOAD DATA LOCAL INFILE '$DB_PATH/${BUILD_NAME}.txt.src' INTO TABLE sources" || exitf $? "cannot import sources"
echo "   importing inverted sources list"
$MYSQL -e "LOAD DATA LOCAL INFILE '$DB_PATH/${BUILD_NAME}.txt.de.uni_leipzig.asv.medusa.filter.sidx.IDXInvertedSourceListFilterImpl.hash.lgl2.expo' INTO TABLE inv_so"  || exitf $? "cannot import inverted sources list"
echo "   importing meta information"
$MYSQL -e "LOAD DATA LOCAL INFILE '$DB_PATH/${BUILD_NAME}.txt.meta' INTO TABLE meta"  || exitf $? "cannot import meta data"
echo "  re-enabling keys"
$MYSQL -e "ALTER TABLE sentences ENABLE KEYS; ALTER TABLE inv_w ENABLE KEYS; ALTER TABLE co_s ENABLE KEYS; ALTER TABLE co_n ENABLE KEYS; ALTER TABLE sources ENABLE KEYS; ALTER TABLE inv_so ENABLE KEYS;" || exitf $? "cannot enable keys"

#######################################
echo; echo "### data import into database '${DATABASE_NAME}' is completed ###"


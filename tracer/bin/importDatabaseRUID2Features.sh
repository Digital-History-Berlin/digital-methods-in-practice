#!/bin/bash
# creates database and tables in $2 and loads all interesting files
# aborts if database already exists


if [ $# != 3 ]; then
	echo "usage: $0 [DATABASE_NAME] [HOST] [LINKING]"
	echo "Export environment variables PASS and USER (database login) first!"
	echo; echo "Example:"
	echo "export USER=USERNAME"
	echo "export PASS=PASSWORD"
	echo "$0 exampleDB localhost ../data/corpora/example.link"
	exit 0
fi

# database parameters
export WS_HOST=$2
export MYSQL="mysql -h $WS_HOST -u $USER -p$PASS"

# general parameters
DATABASE_NAME=$1
WORK_DIR=${PWD%/*}


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
$MYSQL ${DATABASE_NAME} < ../data/db-schema/TextReuseDBSchema.sql || exitf $? "cannot create tables"

MYSQL="mysql -h $WS_HOST -u $USER -p$PASS -b ${DATABASE_NAME}"	# modify mysql-Statement

#######################################
echo; echo "### Importing TRACER data ###"
echo "  disabling keys"
$MYSQL -e "ALTER TABLE features DISABLE KEYS; ALTER TABLE reuse_units DISABLE KEYS; ALTER TABLE ruid2feat DISABLE KEYS;" || exitf $? "cannot disable keys"
echo "   importing ruid2feat"
$MYSQL -e "LOAD DATA LOCAL INFILE '$3' INTO TABLE ruid2feat" || exitf $? "cannot import ruid2feat"
echo "  re-enabling keys"
$MYSQL -e "ALTER TABLE features ENABLE KEYS; ALTER TABLE reuse_units ENABLE KEYS; ALTER TABLE ruid2feat ENABLE KEYS;" || exitf $? "cannot enable keys"

#######################################
echo; echo "### data import into database '${DATABASE_NAME}' is completed ###"


#!/bin/bash

DBARG=apollo

if [ $# -ge 1 ]
then
DBARG=$1
fi

echo "Deleting features from $DBARG"

psql $DBARG -c  "delete from feature_grails_user";
psql $DBARG -c  "delete from feature_dbxref";
psql $DBARG -c  "delete from feature_property";
psql $DBARG -c  "delete from feature_relationship";
psql $DBARG -c  "delete from feature_location";
psql $DBARG -c  "delete from feature";


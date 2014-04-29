#!/bin/bash          
if [ -z "$1" ] 
then
	echo "usage $0 bam_zip_file toobox_for_deployment"
	exit;
fi

echo "edit the cassandra-component.xml according to the cassandra cluster configuration"

if [ -n "$2" ] 
then
	echo toolbox $2 found
	TOOLBOX=$2
fi

DISTRIBUTED_BAM=distrib_bam
RCVR=receiver
ANALYZR=analyzer
DBOARD=dashboard
CASSANDRA=cassandra

BAM_DIR_NAME=${1:0:${#1}-4}

RCVR_PARENT_LOC=$DISTRIBUTED_BAM/$RCVR
ANALYZR_PARENT_LOC=$DISTRIBUTED_BAM/$ANALYZR
DBOARD_PARENT_LOC=$DISTRIBUTED_BAM/$DBOARD
CASSANDRA_PARENT_LOC=$DISTRIBUTED_BAM/$CASSANDRA

RCVRLOC=$DISTRIBUTED_BAM/$RCVR/$BAM_DIR_NAME
ANALYZRLOC=$DISTRIBUTED_BAM/$ANALYZR/$BAM_DIR_NAME
DBOARDLOC=$DISTRIBUTED_BAM/$DBOARD/$BAM_DIR_NAME
CASSANDRALOC=$CASSANDRA_PARENT_LOC/$BAM_DIR_NAME


TOOLBOX_RELATIVE_LOC=repository/deployment/server/bam-toolbox


# remove the distributed bam directory, if present
if [ -d "$DISTRIBUTED_BAM" ] 
then
	rm -rf $DISTRIBUTED_BAM
fi

mkdir -p $RCVR_PARENT_LOC
mkdir -p $ANALYZR_PARENT_LOC
mkdir -p $DBOARD_PARENT_LOC
mkdir -p $CASSANDRA_PARENT_LOC

echo Creating $RCVR node at location : $RCVRLOC
unzip -q $1 -d $RCVR_PARENT_LOC/
cp $RCVR-carbon.xml $RCVRLOC/repository/conf/carbon.xml
cp $RCVR-bundles.info $RCVRLOC/repository/components/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info
cp cassandra-component.xml $RCVRLOC/repository/conf/
if [ $TOOLBOX ] 
then
	mkdir -p $RCVRLOC/$TOOLBOX_RELATIVE_LOC
	cp $TOOLBOX $RCVRLOC/$TOOLBOX_RELATIVE_LOC/
fi

echo creating $ANALYZR node at location : $ANALYZRLOC
unzip -q $1 -d $ANALYZR_PARENT_LOC/
cp $ANALYZR-carbon.xml $ANALYZRLOC/repository/conf/carbon.xml
cp $ANALYZR-bundles.info $ANALYZRLOC/repository/components/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info
cp cassandra-component.xml $ANALYZRLOC/repository/conf/
if [ $TOOLBOX ] 
then
	mkdir -p $ANALYZRLOC/$TOOLBOX_RELATIVE_LOC
	cp $TOOLBOX $ANALYZRLOC/$TOOLBOX_RELATIVE_LOC/
fi

echo creating $DBOARD node at location : $DBOARDLOC
unzip -q $1 -d $DBOARD_PARENT_LOC
cp $DBOARD-carbon.xml $DBOARDLOC/repository/conf/carbon.xml
cp $DBOARD-bundles.info $DBOARDLOC/repository/components/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info
cp cassandra-component.xml $DBOARDLOC/repository/conf/
if [ $TOOLBOX ] 
then
	mkdir -p $DBOARDLOC/$TOOLBOX_RELATIVE_LOC
	cp $TOOLBOX $DBOARDLOC/$TOOLBOX_RELATIVE_LOC/
fi

echo creating $CASSANDRA node at location : $CASSANDRALOC
unzip -q $1 -d $CASSANDRA_PARENT_LOC
cp $CASSANDRA-carbon.xml $CASSANDRALOC/repository/conf/carbon.xml
cp $CASSANDRA-bundles.info $CASSANDRALOC/repository/components/configuration/org.eclipse.equinox.simpleconfigurator/bundles.info
cp cassandra-component.xml $CASSANDRALOC/repository/conf/
if [ $TOOLBOX ] 
then
	mkdir -p $CASSANDRALOC/$TOOLBOX_RELATIVE_LOC
	cp $TOOLBOX $CASSANDRALOC/$TOOLBOX_RELATIVE_LOC/
fi

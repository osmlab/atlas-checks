#!/usr/bin/env bash

export ATLAS_PROFILE_ID=1442a4f451744
export CREATE_STAGING_REPO_RESPONSE=$(curl -X POST -d @nexus_description.xml -u $1:$2 -H Content-Type:application/xml -v https://oss.sonatype.org/service/local/staging/profiles/$ATLAS_PROFILE_ID/start)
# Response looks like this:
# <promoteResponse>  <data>    <stagedRepositoryId>orgopenstreetmapatlas-1147</stagedRepositoryId>    <description>Atlas Release</description>  </data></promoteResponse>
echo $CREATE_STAGING_REPO_RESPONSE | perl -nle 'print "$1" if ($_ =~ /.*<stagedRepositoryId>(.*)<\/stagedRepositoryId>.*/g);' | awk '{$1=$1};1'

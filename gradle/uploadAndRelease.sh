#!/usr/bin/env bash

SONATYPE_USERNAME=$1
SONATYPE_PASSWORD=$2
REPOSITORY_DIR=$3

export API_ENDPOINT=https://oss.sonatype.org/service/local

function runWithRetry()
{
	n=0
	until [ $n -ge 100 ]
	do
		$1 && break
		n=$[$n+1]
        echo "Sleep 15 sec before retry"
		sleep 60
        echo "Retry $n"
	done
}

export ATLAS_PROFILE_ID=1442a4f451744
export DESCRIPTION_PAYLOAD="<promoteRequest>\
    <data>\
        <description>Atlas Release</description>\
    </data>\
</promoteRequest>"
export CLOSE_PAYLOAD="<promoteRequest>\
    <data>\
        <stagedRepositoryId>$STAGING_ID</stagedRepositoryId>\
        <description>Close Atlas repo</description>\
    </data>\
</promoteRequest>"

# Create staging repo
export STAGING_ID=$(curl -s -u $SONATYPE_USERNAME:$SONATYPE_PASSWORD \
    -X POST \
    -H "Content-Type:application/xml" \
    -d "$DESCRIPTION_PAYLOAD" \
    "$API_ENDPOINT/staging/profiles/$ATLAS_PROFILE_ID/start" \
    | perl -nle 'print "$1" if ($_ =~ /.*<stagedRepositoryId>(.*)<\/stagedRepositoryId>.*/g);' \
    | awk '{$1=$1};1')
# Response parsed looks like this:
# <promoteResponse>  <data>    <stagedRepositoryId>orgopenstreetmapatlas-1147</stagedRepositoryId>    <description>Atlas Release</description>  </data></promoteResponse>

# Upload
./uploadToNexus.sh $SONATYPE_USERNAME $SONATYPE_PASSWORD $REPOSITORY_DIR $STAGING_ID

sleep 60

# Close
curl --fail -u $SONATYPE_USERNAME:$SONATYPE_PASSWORD \
    -X POST \
    -H "Content-Type:application/xml" \
    -d "$CLOSE_PAYLOAD" \
    "$API_ENDPOINT/staging/profiles/$ATLAS_PROFILE_ID/finish" \
    > /dev/null 2>&1

sleep 60

# Release
runWithRetry ./releaseSonatype.sh

# Drop if needed. It is usually automatically cleaned up.
# runWithRetry ./dropSonatype.sh

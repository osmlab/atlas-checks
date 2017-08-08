#!/bin/bash
# install github_changelog_generator by using 'gem install github_changelog_generator'
# $1 or first input parameter is a personal token for access to github
# Usage:
#   ./updateChangeLog.sh [REPO_NAME] [ACCESS_TOKEN] [OUTPUT_FILE]
#   REPO_NAME - This is the name of the repository that you are updating the changelog for
#   ACCESS_TOKEN - Personal access token to be able to access GitHub
#   OUTPUT_FILE - The file to generate, if not provided will generate CHANGELOG.md in the current directory
#
#   EXAMPLE:
#       ./updateChangeLog.sh integrity-core 432uofnejkrwncjkdf7893nfd
output=""
if [ ! -z "$3" ]; then
    output="--output $3"
fi
github_changelog_generator -u maps-osm -p $1 --github-site https://github.com/ --github-api https://github.com/api/v3/ -t $2 $output

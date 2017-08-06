#!/bin/bash

set -e

reprotest --diffoscope-arg=--exclude-directory-metadata --dont-vary time 'scripts/rep-build.sh' "*/target/*_2.11-2.5-SNAPSHOT.jar_"

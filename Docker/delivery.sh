#!/bin/bash

# ===============================================
#   delivery script for mediatorserver docker 
#
# to verify checksum use:
#    sha256sum --check $HASHFILENAME
# ===============================================

VERSION=$1

if [ -z "$VERSION" ]; then
  "please enter a version number for the delivery"
  exit 1;
fi


./control.sh

TARGZNAME="mediatorserver."$VERSION".tar.gz"
HASHFNAME=$TARGZNAME".sha256"
FILES="src/ control.sh Dockerfile.tmpl"

tar -czf $TARGZNAME $FILES
sha256sum $TARGZNAME > $HASHFNAME



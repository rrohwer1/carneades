#!/usr/bin/env bash

COOKIE=/tmp/casperjs-cookie.txt

rm -f $COOKIE /tmp/carneades.png

casperjs --url=http://localhost:8080/carneades/#/policies/introduction/copyright --cookies-file=$COOKIE casper/run-scenario-test.js

#!/usr/bin/env bash

# build one standalone and one toolbox WAR
set -e

bash ./scripts/build.sh

echo -e "\nBuilding WAR archives\n"

cd ../CarneadesWebService
lein ring uberwar

cd ../CarneadesWebApp
lein with-profile war ring uberwar

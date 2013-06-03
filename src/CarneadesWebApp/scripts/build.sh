#!/usr/bin/env bash

set -e

lein sub install
lein deps

echo -e "\nBuild is finished. \n"
echo -e "To run the Aston example:\n$ cd ../CarneadesExamples && lein run -m carneades.examples.aston\n"
echo -e "To run the Carneades Web App:\n$ lein ring server 8080"
echo -e "Then point your browser at http://localhost:8080/carneades/#/home"

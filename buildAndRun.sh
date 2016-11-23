#!/bin/bash

mkdir build
cd src
javac Main.java Config.java DataWriter.java Hash.java LinkedList.java Measurement.java Summary.java -d ../build
cd ../build
mkdir A6
java Main

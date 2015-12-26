#!/bin/bash
javac -classpath commons-net-2.2.jar:tagsoup-1.2.jar:xmlserializer1_2.jar:jaxws-rt.jar net/eckschi/lawineserver/*.java
jar cfm test.jar Manifest.txt net/eckschi/lawineserver/*.class *.jar org/eclipse/jdt/internal/jarinjarloader/*.class

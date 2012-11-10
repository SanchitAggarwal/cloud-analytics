#!/bin/bash
echo "export M2_HOME=/usr/local/apache-maven-3.0.4\nexport M2=$M2_HOME/bin\nexport PATH=$M2:$PATH\nexport JAVA_HOME=$HOME/programs/jdk" >> ./.bashrc
cd /usr/local/mahout/
mvn install

#!/bin/bash

# tell grid engine to merge stdout and stderr streams
#$ -j y

# tell grid engine to use current directory
#$ -cwd

#arguments is 1= the number of games; 2 = the model
TRAIN=$(echo '.7 * ' $1 | bc | sed 's/[.].*//')
TEST=$(echo '.3 * ' $1 | bc | sed 's/[.].*//')
java -Xmx500000000 -classpath ./mysql-connector-java-5.1.27-bin.jar:./bin:./ svm.SVMMain --train train$1_$2.txt $TRAIN gameIDS$1_$2.dat
./liblinear-1.94/train -s $2 train$1_$2.txt model_$2_for_$1.model
java -Xmx500000000 -classpath ./mysql-connector-java-5.1.27-bin.jar:./liblinear-1.94.jar:./bin:./ svm.SVMMain --test model_$2_for_$1.model $TEST gameIDS$1_$2.dat

#! /bin/bash
javac -cp .:junit-4.13-beta-3.jar:hamcrest-core-1.3.jar -Xlint:deprecation *.java
java -cp .:junit-4.13-beta-3.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore TestTransactionWindow TestAfterPayCodingChallenge

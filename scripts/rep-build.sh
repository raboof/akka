#!/bin/bash

sbt clean publishLocal

rm */target/akka-persistence-tck*

touch -t 197001010000 *
chmod a+rwx */target
touch -t 197001010000 */target
chmod a+rwx */target/*_2.11-2.5-SNAPSHOT.jar_
touch -t 197001010000 */target/*_2.11-2.5-SNAPSHOT.jar_

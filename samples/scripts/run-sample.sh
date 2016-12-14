#!/bin/sh

#  Copyright 2016 The Apache Software Foundation
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# ----------------------------------------------------------------------------
# Script for running the WSO2 Integration samples
#
# Environment Variable Prerequisites
#
#   CARBON_HOME   Home of WSO2 Carbon installation. If not set I will  try
#                   to figure it out.
#
#   JAVA_HOME       Must point at your Java Development Kit installation.
#
#   JAVA_OPTS       (Optional) Java runtime options used when the commands
#                   is executed.
#
# NOTE: Borrowed generously from Apache Tomcat startup scripts.
# -----------------------------------------------------------------------------

# ----- Process the input command ----------------------------------------------
if [ ! -f ../samples/basic-routing/$1 ]; then
    echo "*** Specified sample configuration file name is not found *** Please specify a correct file name"
    echo "Example, to run sample passthrough.iflow use command as follows: ./run-sample.sh passthrough.iflow"
    exit
else
  SAMPLE_FILE_NAME=$1
  cp -r ../samples/basic-routing/$1 ../deployment/integration-flows/
fi

sh carbon.sh
rm -r ../deployment/integration-flows/$1
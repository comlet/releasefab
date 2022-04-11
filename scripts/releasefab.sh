#!/bin/bash

PROJECT_HOME="$PWD"
export JAVA_HOME="$PROJECT_HOME/build/runtime/build_jre/linux/bin"
export PATH="$JAVA_HOME:$PATH"

export RELEASEFAB_HOMEPATH="$PROJECT_HOME/build/build_process/build_process_sw_releasefab_bin"
export RELEASEFAB_LIBS="$PROJECT_HOME/build/build_process/build_process_sw_releasefab_bin/libs"
export RELEASEFAB_PLUGINS="$PROJECT_HOME/build/build_process/build_process_sw_releasefab_bin/plugins"
export SWT_64BIT_LINUX="$PROJECT_HOME/build/build_process/build_process_sw_releasefab_bin/libs/linux/64bit"

if [ -z "$BUILD_NUMBER" ]
then
   export FULL_USER_NAME=$(getent passwd $USER | cut -d ':' -f 5 | cut -d ',' -f 1)
   echo "local ReleaseFab run for user $FULL_USER_NAME"
else
   echo "Jenkins ReleaseFab run on Jenkins"
fi

java -Dlogback.configurationFile=./logback.xml --module-path $RELEASEFAB_HOMEPATH:$RELEASEFAB_LIBS:$RELEASEFAB_PLUGINS:$SWT_64BIT_LINUX --module=releasefab.application/de.comlet.releasefab.Main source=$PROJECT_HOME $1 $2 $3 $4 $5 $6 $7 $8 $9
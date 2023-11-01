#!/bin/sh
set -e

TS_SRC_ENUMS_JAVA="src/protocolgen/TsSrcEnums.java"

rm -rf build_tmp/
mkdir build_tmp

echo "Gathering sources.."
find src -name "*.java" > sources.txt

echo "Building.."
javac @sources.txt -cp ../code-prober.jar -d build_tmp -source 8 -target 8

echo "Extracing Typescript enums.."

java \
  -DTS_SRC_DIR="../client/ts/src/" \
  -DJAVA_DST_FILE=${TS_SRC_ENUMS_JAVA} \
  -cp build_tmp:../code-prober.jar protocolgen.GenSettings

echo ${TS_SRC_ENUMS_JAVA} >> sources.txt

echo "Rebuilding.."
javac @sources.txt -cp ../code-prober.jar -d build_tmp -source 8 -target 8

java \
  -DJAVA_DST_DIR="../server/src/codeprober/protocol/data/" \
  -DJAVA_DST_PKG="codeprober.protocol.data" \
  -DTS_DST_FILE="../client/ts/src/protocol.ts" \
  -DTS_DST_SETTINGS_FILE="../client/ts/src/settings.generated.ts" \
  -cp build_tmp:../code-prober.jar protocolgen.GenAll

echo "Cleaning up.."
rm sources.txt
rm -rf build_tmp

echo "Done"

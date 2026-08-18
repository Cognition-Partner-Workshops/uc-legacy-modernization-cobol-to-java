#!/usr/bin/env bash
# Runs the whole parity pipeline end-to-end:
#   1. the GnuCOBOL reference run of CBSTM03A (skip with SKIP_COBOL_REF=1)
#   2. the Java port (build + tests)
#   3. the parity comparison, writing carddemo-java/target/parity/parity-summary.json
#   4. the self-contained HTML parity report from the real summary
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "$here/.." && pwd)"
mvn="${MAVEN:-mvn}"

if [[ "${SKIP_COBOL_REF:-0}" != "1" ]]; then
  "$here/cobol-ref/run-cobol-ref.sh" "$repo_root"
fi

cd "$here"
"$mvn" -q package

jar="$(ls "$here"/target/carddemo-java-*.jar | head -1)"
java -jar "$jar" "$repo_root"

summary="$here/target/parity/parity-summary.json"
report="$here/target/parity/parity-report.html"
classpath_file="$here/target/parity/runtime-classpath"
mkdir -p "$here/target/parity"
"$mvn" -q dependency:build-classpath \
  -Dmdep.outputFile="$classpath_file" \
  -Dmdep.outputAbsoluteArtifactFilename=true
runtime_classpath="$here/target/classes:$(cat "$classpath_file")"
java -cp "$runtime_classpath" \
  com.carddemo.parity.ParityReportRenderer "$summary" "$report"

echo "parity summary: $summary"
echo "parity report: $report"

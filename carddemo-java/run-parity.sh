#!/usr/bin/env bash
# Runs the whole parity pipeline end-to-end:
#   1. the GnuCOBOL reference run of CBSTM03A (skip with SKIP_COBOL_REF=1)
#   2. the Java port (build + tests)
#   3. the parity comparison, writing carddemo-java/target/parity/parity-summary.json
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "$here/.." && pwd)"
mvn="${MAVEN:-mvn}"

if [[ "${SKIP_COBOL_REF:-0}" != "1" ]]; then
  "$here/cobol-ref/run-cobol-ref.sh"
fi

cd "$here"
"$mvn" -q package

jar="$(ls "$here"/target/carddemo-java-*.jar | head -1)"
java -jar "$jar" "$repo_root"

echo "parity summary: $here/target/parity/parity-summary.json"

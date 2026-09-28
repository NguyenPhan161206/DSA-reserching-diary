#!/usr/bin/env bash
# Compile and run one java-course lab with a single command.
#
#   ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch01-.../lab/Hello.java
#   ./java-course/00-toolchain/run.sh path/to/lab/          # compiles the whole directory
#
# Compiled classes go to .java-course-build/ (git-ignored) — the repo never holds
# a .class file. Source the toolchain env first:  source java-course/00-toolchain/env.sh
set -euo pipefail

if [ $# -lt 1 ]; then
  echo "usage: $0 <file.java | dir/> [program args...]" >&2
  exit 2
fi

target="$1"
shift

if [ -z "${JAVA_HOME:-}" ] || [ ! -x "$JAVA_HOME/bin/javac" ]; then
  echo "javac not on PATH. Run: source java-course/00-toolchain/env.sh" >&2
  exit 1
fi

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
out="$repo_root/.java-course-build"
mkdir -p "$out"

if [ -d "$target" ]; then
  sources=("$target"/*.java)
  sourcepath="$target"
else
  sources=("$target")
  sourcepath="$(cd "$(dirname "$target")" && pwd)"
fi

# -sourcepath lets a lab reference a helper class that lives beside it without
# having to pass every file on the command line.
echo "==> javac ${sources[*]}"
javac -Xlint:all -sourcepath "$sourcepath" -d "$out" "${sources[@]}"

# The main class is the file stem unless the file declares a package.
main_class="$(basename "${sources[0]}" .java)"
if grep -qE '^\s*package\s+' "${sources[0]}"; then
  main_class="$(grep -oE '^\s*package\s+[a-zA-Z0-9_.]+' "${sources[0]}" | awk '{print $2}').$main_class"
fi

echo "==> java $main_class"
exec java -cp "$out" "$main_class" "$@"

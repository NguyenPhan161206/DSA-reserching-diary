# Source this file to get javac / jshell / mvn on PATH.
#   source java-course/00-toolchain/env.sh
#
# The toolchain is installed under ~/.local (no root required, nothing system-wide
# is modified). Override JAVA_HOME_HOME if you installed the JDK somewhere else.

JDK_DIR="${JDK_DIR:-$HOME/.local/jdk21}"
MAVEN_DIR="${MAVEN_DIR:-$HOME/.local/apache-maven-3.9.16}"

if [ ! -x "$JDK_DIR/bin/javac" ]; then
  echo "javac not found at $JDK_DIR — see java-course/00-toolchain/README.md" >&2
  return 1 2>/dev/null || exit 1
fi

export JAVA_HOME="$JDK_DIR"
export PATH="$JAVA_HOME/bin:$MAVEN_DIR/bin:$PATH"

echo "java   : $(java -version 2>&1 | head -1)"
echo "javac  : $(javac -version 2>&1)"
echo "jshell : $(jshell --version 2>&1)"
echo "maven  : $(mvn -v 2>&1 | head -1)"

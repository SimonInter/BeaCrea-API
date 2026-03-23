#!/bin/sh
# Maven Wrapper — télécharge automatiquement Maven 3.9.9 si absent
set -e

MAVEN_WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES=".mvn/wrapper/maven-wrapper.properties"

# Lire la version depuis les properties
DISTRIBUTION_URL=$(grep distributionUrl "$MAVEN_WRAPPER_PROPERTIES" | cut -d= -f2)

# Dossier de cache Maven wrapper
MAVEN_USER_HOME="${MAVEN_USER_HOME:-$HOME/.m2}"
WRAPPER_DIR="$MAVEN_USER_HOME/wrapper/dists"

# Nom du dossier de distribution (ex: apache-maven-3.9.9-bin)
DIST_NAME=$(echo "$DISTRIBUTION_URL" | sed 's|.*/\(.*\)\.zip|\1|')
MVN_BIN="$WRAPPER_DIR/$DIST_NAME/$DIST_NAME/bin/mvn"

if [ ! -f "$MVN_BIN" ]; then
    echo "[mvnw] Téléchargement de Maven 3.9.9..."
    mkdir -p "$WRAPPER_DIR/$DIST_NAME"
    curl -fsSL "$DISTRIBUTION_URL" -o "/tmp/$DIST_NAME.zip"
    unzip -q "/tmp/$DIST_NAME.zip" -d "$WRAPPER_DIR/$DIST_NAME/"
    rm "/tmp/$DIST_NAME.zip"
    echo "[mvnw] Maven 3.9.9 installé."
fi

exec "$MVN_BIN" "$@"

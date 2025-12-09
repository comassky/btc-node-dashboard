#!/bin/bash

# Exécute le goal de mise à jour des propriétés
echo "Lancement de la mise à jour des propriétés Maven..."

    mvn -U -Dmaven.version.ignore='(?i).*-(alpha|beta|m|rc)([-.]?\d+)?' -DgenerateBackupPoms=false versions:update-properties

if [ $? -ne 0 ]; then
    echo "ERREUR : La mise à jour des propriétés Maven a échoué."
    exit 1
fi

echo "Mise à jour terminée. Veuillez vérifier le pom.xml."

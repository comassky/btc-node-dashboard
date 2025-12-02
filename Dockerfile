# ----------------------------------------------------------------------
# Étape 1: Builder - pour compiler et créer l'archive fast-jar Quarkus
# ----------------------------------------------------------------------
# Utilise une image Java/Maven complète pour le build. C'est l'image qui contient 'mvn'.
FROM maven:3-eclipse-temurin-21-alpine AS builder

# Définit le répertoire de travail dans le conteneur
WORKDIR /build

# Copie le fichier POM et les dépendances (pour utiliser le cache Docker)
COPY pom.xml .
# Télécharge toutes les dépendances Maven (cette étape est mise en cache si le pom.xml ne change pas)
RUN mvn dependency:go-offline

# Copie le code source
COPY src /build/src

# Packaging de l'application Quarkus en fast-jar
# Quarkus génère les fichiers nécessaires dans /target/quarkus-app
RUN mvn package -DskipTests

# ----------------------------------------------------------------------
# Étape 2: Runner - pour exécuter l'application (image JRE minimale)
# ----------------------------------------------------------------------
# Utilise une image JRE/JDK minimaliste pour l'exécution (plus petite que le JDK complet)
FROM eclipse-temurin:25-jre-alpine AS runner

# Définit le port que l'application expose
EXPOSE 8080

# Crée un répertoire non-root pour l'exécution
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Définit le répertoire de travail
WORKDIR /app

# Copie les artefacts du builder (le fast-jar)
# Le dossier 'quarkus-app' contient le jar principal et les bibliothèques
COPY --from=builder /build/target/quarkus-app /app

# Démarre l'application Quarkus.
# L'option -Dquarkus.http.host=0.0.0.0 est nécessaire pour que l'application écoute toutes les interfaces réseau dans Docker.
CMD ["java", "-jar", "quarkus-run.jar"]
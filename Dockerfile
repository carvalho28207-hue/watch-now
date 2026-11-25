# Etapa 1: compila o Java
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY lib lib
COPY src src
RUN javac -encoding UTF-8 -cp lib/gson-2.13.1.jar -d out src/*.java

# Etapa 2: roda só com o necessário
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/out out
COPY lib lib
COPY web web
# Limita a memória para caber no plano grátis (512 MB)
ENV JAVA_TOOL_OPTIONS="-Xmx300m"
CMD ["java", "-cp", "out:lib/gson-2.13.1.jar", "Main"]

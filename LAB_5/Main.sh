#!/bin/bash

# Compilar el archivo Main.kt
kotlinc Main.kt -include-runtime -d Main.jar

# Eliminar el directorio META-INF si existe
rm -rf META-INF

# Ejecutar el archivo .jar
kotlin -classpath Main.jar MainKt
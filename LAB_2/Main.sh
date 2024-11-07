#!/bin/bash

# Compilar el archivo Main.kt
kotlinc -d src -cp "libs/Jama.jar:libs/libGrafo.jar" src/Main.kt

# Eliminar el directorio META-INF si existe
rm -rf src/META-INF

# Ejecutar la clase MainKt
kotlin -cp "src:libs/Jama.jar:libs/libGrafo.jar" MainKt
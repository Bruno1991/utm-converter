#!/bin/bash

# =====================================
# Dump estruturado de código-fonte
# =====================================

OUTPUT="dump_code-estruturado.txt"

> "$OUTPUT"

find . -type f \( \
    -name "*.py" -o \
    -name "*.js" -o \
    -name "*.html" -o \
    -name "*.css" -o \
    -name "*.kt" -o \
    -name "*.java" -o \
    -name "*.xml" -o \
    -name "*.json" \
\) | sort | while read file; do
    echo "======================================" >> "$OUTPUT"
    echo "FILE: $file" >> "$OUTPUT"
    echo "======================================" >> "$OUTPUT"
    cat "$file" >> "$OUTPUT"
    echo -e "\n\n" >> "$OUTPUT"
done

echo "Dump concluído: $OUTPUT"

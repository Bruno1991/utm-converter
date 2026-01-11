#!/bin/bash

# ================================
# Dump simples de código-fonte
# ================================

OUTPUT="dump-src.txt"

# Limpa dump anterior
> "$OUTPUT"

# Extensões que serão dumpadas
EXTENSIONS="py js html css kt java json xml"

for ext in $EXTENSIONS; do
    find . -type f -name "*.${ext}" -print -exec echo -e "\n\n===== {} =====\n" \; -exec cat {} \; >> "$OUTPUT"
done

echo "Dump gerado em: $OUTPUT"
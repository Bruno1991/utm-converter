#!/bin/bash

# =====================================================
# Gerador de senha forte com histórico
# Projeto: UTM Converter
# =====================================================
# - 32 caracteres
# - Alta entropia
# - NÃO sobrescreve senhas anteriores
# - Mantém histórico com timestamp
# - Armazena em arquivo seguro
# =====================================================

# -----------------------------
# CONFIGURAÇÃO
# -----------------------------
BASE_DIR="/opt/android/projects/utm-converter"
SECRETS_DIR="$BASE_DIR/secrets"
OUTPUT_FILE="$SECRETS_DIR/keystore-passwords.log"
LENGTH=32
TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")

# -----------------------------
# GARANTIR DIRETÓRIO
# -----------------------------
mkdir -p "$SECRETS_DIR"
chmod 700 "$SECRETS_DIR"

# -----------------------------
# GERAR SENHA
# -----------------------------
PASSWORD=$(tr -dc 'A-Za-z0-9!@#$%_+=' < /dev/urandom | head -c "$LENGTH")

# -----------------------------
# SALVAR COM HISTÓRICO
# -----------------------------
{
    echo "-------------------------------------"
    echo "Timestamp  📆: $TIMESTAMP"
    echo "Uso        🤖: Keystore / Android"
    echo "Senha      🔑: $PASSWORD"
    echo
} >> "$OUTPUT_FILE"

chmod 600 "$OUTPUT_FILE"

# -----------------------------
# OUTPUT
# -----------------------------
echo "====================================="
echo "Senha forte gerada e armazenada"
echo
echo "Arquivo:"
echo "$OUTPUT_FILE"
echo
echo "Última entrada:"
tail -n 6 "$OUTPUT_FILE"
echo
echo "⚠️  NÃO versionar este arquivo"
echo "⚠️  Backup seguro obrigatório"
echo "====================================="

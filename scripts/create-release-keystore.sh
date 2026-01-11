#!/bin/bash

# =====================================================
# Create Android Release Keystore + Gradle config
# Projeto: UTM Converter
# =====================================================
# - Gera senha forte
# - Cria keystore no local correto
# - Salva senha em secrets
# - Atualiza gradle.properties automaticamente
# =====================================================

set -e

# -----------------------------
# CONFIGURAÇÃO FIXA
# -----------------------------
BASE_DIR="/opt/android/projects/utm-converter"
APP_DIR="$BASE_DIR/app"
KEYSTORE_DIR="$APP_DIR/keystore"
KEYSTORE_FILE="$KEYSTORE_DIR/utmconverter-release.keystore"
SECRETS_DIR="$BASE_DIR/secrets"
SECRETS_FILE="$SECRETS_DIR/keystore-passwords.log"
GRADLE_PROPERTIES="$BASE_DIR/gradle.properties"

ALIAS="utmconverter"
VALIDITY_DAYS=10000
KEY_SIZE=2048

# -----------------------------
# GERAR SENHA ÚNICA
# -----------------------------
PASSWORD=$(tr -dc 'A-Za-z0-9!@#$%_+=' < /dev/urandom | head -c 32)

# -----------------------------
# GARANTIR DIRETÓRIOS
# -----------------------------
mkdir -p "$KEYSTORE_DIR"
mkdir -p "$SECRETS_DIR"

chmod 700 "$KEYSTORE_DIR"
chmod 700 "$SECRETS_DIR"

# -----------------------------
# REGISTRAR SENHA (HISTÓRICO)
# -----------------------------
TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")

{
    echo "-------------------------------------"
    echo "Timestamp : $TIMESTAMP"
    echo "Uso       : Android Release Keystore"
    echo "Alias     : $ALIAS"
    echo "Senha     : $PASSWORD"
    echo
} >> "$SECRETS_FILE"

chmod 600 "$SECRETS_FILE"

# -----------------------------
# CRIAR KEYSTORE
# -----------------------------
if [ -f "$KEYSTORE_FILE" ]; then
    echo "❌ Keystore já existe:"
    echo "   $KEYSTORE_FILE"
    echo "   Remova manualmente se quiser recriar."
    exit 1
fi

keytool -genkeypair \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize "$KEY_SIZE" \
  -validity "$VALIDITY_DAYS" \
  -keystore "$KEYSTORE_FILE" \
  -storepass "$PASSWORD" \
  -keypass "$PASSWORD" \
  -dname "CN=utmconverter, OU=dev, O=utm, L=local, ST=local, C=BR"

# -----------------------------
# ATUALIZAR gradle.properties
# -----------------------------
echo "Atualizando gradle.properties..."

# Remove entradas antigas
sed -i '/RELEASE_STORE_FILE/d' "$GRADLE_PROPERTIES"
sed -i '/RELEASE_KEY_ALIAS/d' "$GRADLE_PROPERTIES"
sed -i '/RELEASE_STORE_PASSWORD/d' "$GRADLE_PROPERTIES"
sed -i '/RELEASE_KEY_PASSWORD/d' "$GRADLE_PROPERTIES"

# Adiciona novas
{
    echo ""
    echo "# ====================================================="
    echo "# Release signing - AUTO GERADO"
    echo "# ====================================================="
    echo "RELEASE_STORE_FILE=keystore/utmconverter-release.keystore"
    echo "RELEASE_KEY_ALIAS=$ALIAS"
    echo "RELEASE_STORE_PASSWORD=$PASSWORD"
    echo "RELEASE_KEY_PASSWORD=$PASSWORD"
} >> "$GRADLE_PROPERTIES"

# -----------------------------
# FINAL
# -----------------------------
echo "====================================="
echo "Keystore criada com sucesso"
echo
echo "Arquivo:"
echo "  $KEYSTORE_FILE"
echo
echo "Senha salva em:"
echo "  $SECRETS_FILE"
echo
echo "gradle.properties atualizado"
echo "====================================="

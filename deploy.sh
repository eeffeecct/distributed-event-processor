#!/bin/bash

set -e

REPO_URL="https://eeffeecct:${GITHUB_TOKEN}@github.com/isicju/java_course_2025.git"

echo "--- 1. Скачиваем свежий код ---"
if [ ! -d "java_course_2025" ]; then
    git clone -b lesson67_hw_eeffeecct "$REPO_URL"
fi

cd java_course_2025
git remote set-url origin "$REPO_URL"
git pull origin lesson67_hw_eeffeecct

echo "--- 2. Собираем проект (Gradle) ---"
export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
chmod +x gradlew
./gradlew clean build -x test

echo "--- 3. Перезапускаем Докер (используем PROD файл) ---"
sudo docker compose -f docker-compose.prod.yml down
sudo docker compose -f docker-compose.prod.yml up -d --build

echo "--- ✅ Готово! ---"
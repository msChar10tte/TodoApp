# Использование многостадийной сборки (Multi-stage build)

# Этап 1: Сборка приложения
FROM maven:3.8.7-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
# Кэширование зависимостей - запускаем mvn dependency:go-offline отдельно
# Это позволяет переиспользовать слои Docker, если pom.xml не изменился
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Этап 2: Создание легковесного образа для запуска
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Дополнительно: Добавьте .dockerignore
# Создайте файл .dockerignore в корне проекта со следующим содержимым:
# target/
# .mvn/
# .idea/
# *.iml
# mvnw
# mvnw.cmd
# Dockerfile
# README.md
# .git/
# .github/
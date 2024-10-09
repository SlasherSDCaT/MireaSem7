# Первый этап: сборка приложения
FROM maven:3.8.1-openjdk-17-slim AS build

# Задание метаинформации
LABEL maintainer="Мухаметшин А. Р., группа ИКБО-20-21"

# Создаем рабочую директорию для сборки
WORKDIR /app

# Копируем pom.xml и зависимости для их предустановки
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn clean package -DskipTests

# Второй этап: создание минимального образа для запуска приложения
FROM openjdk:17-jdk-slim
# Устанавливаем wget
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*
# Задание переменных окружения
ENV APP_HOME=/app
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/postgres
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres
# Задание рабочей директории для запуска
WORKDIR $APP_HOME
# Копирование JAR файла из предыдущей стадии
COPY --from=build /app/target/*.jar app.jar
# Создаем директорию для хранения герба
RUN mkdir -p ./static
# Добавление герба (используем ADD, чтобы продемонстрировать эту команду)
RUN wget https://www.mirea.ru/upload/medialibrary/80f/MIREA_Gerb_Colour.png -O ./static/mirea_gerb.png
# Настройка пользователя для запуска приложения
RUN useradd -ms /bin/bash myuser
USER myuser
# Создаем том для временных данных
VOLUME /tmp
# Открываем порт приложения
EXPOSE 8080
# Настройка точки входа
ENTRYPOINT ["java", "-jar", "app.jar"]
# Устанавливаем команду по умолчанию
CMD ["--spring.profiles.active=prod"]
# Добавляем команду, которая будет выполнена при сборке наследуемого образа
ONBUILD RUN echo "Сборка завершена. Автор: Мухаметшин А. Р."

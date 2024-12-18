# SlasherSDCaT
# Используем официальный образ Golang для сборки
FROM golang:1.23-alpine3.20 as builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы и загружаем зависимости
COPY go.mod go.sum ./
RUN go mod download

# Копируем исходный код
COPY . .

# Сборка бинарного файла
RUN go build -o gateway main.go

FROM alpine:latest

WORKDIR /app

COPY --from=builder /app/gateway .

EXPOSE 8083

# Запуск сервиса
CMD ["./gateway"]

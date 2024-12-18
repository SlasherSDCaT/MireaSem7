FROM golang:1.23-alpine3.20

WORKDIR /app

COPY . .

RUN go mod tidy

RUN go build -o notify-service

EXPOSE 8081

CMD ["./notify-service"]

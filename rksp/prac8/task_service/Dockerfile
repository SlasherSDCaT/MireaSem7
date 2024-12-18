#SlasherSDCaT
FROM golang:1.23-alpine3.20

WORKDIR /app

COPY . .

RUN go mod tidy

RUN go build -o task-service

EXPOSE 8080

CMD ["./task-service"]

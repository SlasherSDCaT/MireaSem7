package main

import (
	"context"
	"fmt"
	"log"
	"net"
	"os"
	"os/signal"

	"notification_service/interfaces"
	"notification_service/repository"

	"notification_service/api"

	"github.com/jackc/pgx/v4"

	"google.golang.org/grpc"
)

func initDB(ctx context.Context) (*pgx.Conn, error) {
	db, err := pgx.Connect(ctx, "postgres://postgres:postgres@notification_postgres:5432/notification")
	if err != nil {
		log.Fatalf("Error opening database: %v", err)
	}

	_, err = db.Exec(
		ctx,
		`CREATE TABLE IF NOT EXISTS notifications (
        id SERIAL PRIMARY KEY,
        task_id VARCHAR(36) NOT NULL,
        message TEXT NOT NULL,
        created_at TIMESTAMP DEFAULT NOW()
    )`,
	)
	if err != nil {
		return nil, fmt.Errorf("error creating table: %w", err)
	}

	return db, nil
}

func main() {
	ctx, cancel := context.WithCancel(context.Background())

	db, err := initDB(ctx)
	if err != nil {
		log.Fatal(err)
	}
	defer func(db *pgx.Conn, ctx context.Context) {
		_ = db.Close(ctx)
	}(db, ctx)

	repo := repository.NewNotificationRepository(db)
	notificationServer := interfaces.NewNotificationServer(repo)

	grpcServer := grpc.NewServer()
	api.RegisterNotificationServiceServer(grpcServer, notificationServer)

	lis, err := net.Listen("tcp", ":8081")
	if err != nil {
		log.Fatalf("Failed to listen on port 8082: %v", err)
	}

	go func() {
		log.Println("Starting gRPC server on :8081...")
		if err := grpcServer.Serve(lis); err != nil {
			log.Fatalf("Failed to serve gRPC server: %v", err)
		}
	}()

	// Graceful shutdown
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt)
	<-c

	log.Println("Shutting down gRPC server...")

	cancel()
	grpcServer.GracefulStop()
}

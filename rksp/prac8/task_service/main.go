package main

import (
	"context"
	"fmt"
	"log"

	"task_service/infrastructure/notification"
	"task_service/interfaces"
	"task_service/repository"

	"github.com/jackc/pgx/v4"
)

func initDB(ctx context.Context) (*pgx.Conn, error) {
	db, err := pgx.Connect(ctx, "postgres://postgres:postgres@task_postgres:5432/task")
	if err != nil {
		log.Fatalf("Error opening database: %v", err)
	}

	_, err = db.Exec(
		ctx,
		`CREATE TABLE IF NOT EXISTS tasks (
        		id VARCHAR(36) PRIMARY KEY,
        		title TEXT NOT NULL,
    			creator_id VARCHAR(36) NOT NULL,
        		assignee_id VARCHAR(36),
    			created_at timestamp default now(),
        		deadline_at timestamp)`,
	)
	if err != nil {
		return nil, fmt.Errorf("error creating table: %w", err)
	}

	return db, nil
}

func main() {
	ctx := context.Background()

	db, err := initDB(ctx)
	if err != nil {
		log.Fatal(err)
	}

	nc, err := notification.NewClient("notification-service:8081")
	if err != nil {
		log.Fatalf("Failed to create notification client: %v", err)
	}

	tr := repository.NewTaskRepository(db)

	srv := interfaces.NewServer(tr, nc)

	if err := srv.Run(); err != nil {
		return
	}
}

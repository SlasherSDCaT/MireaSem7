package main

import (
	"context"
	"fmt"
	"log"

	"user_service/interfaces"
	"user_service/repository"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v4"
)

func initDB(ctx context.Context) (*pgx.Conn, error) {
	dbPool, err := pgx.Connect(ctx, "postgres://postgres:postgres@user_postgres:5432/user")
	if err != nil {
		return nil, fmt.Errorf("unable to connect to database: %w", err)
	}

	// Создаем таблицу, если её нет
	createTableSQL := `
	CREATE TABLE IF NOT EXISTS users (
		id VARCHAR(36) PRIMARY KEY,
		vk_id VARCHAR(255) NOT NULL,
		token VARCHAR(255)
	)`
	_, err = dbPool.Exec(ctx, createTableSQL)
	if err != nil {
		return nil, fmt.Errorf("failed to create users table: %w", err)
	}

	return dbPool, nil
}

func main() {
	ctx := context.Background()

	db, err := initDB(ctx)
	if err != nil {
		log.Fatalf("Unable to connect to database: %v", err)
	}
	defer func(db *pgx.Conn, ctx context.Context) {
		_ = db.Close(ctx)
	}(db, ctx)

	userRepo := repository.NewUserRepository(db)
	authHandler := interfaces.NewAuthHandler(userRepo)

	router := gin.Default()

	router.Use(cors.New(cors.Config{
		AllowOrigins:     []string{"http://localhost", "https://vk.com"}, // Укажите разрешённые источники
		AllowMethods:     []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowCredentials: true,
	}))

	router.GET("/login", authHandler.RedirectToVK)
	router.GET("/auth", authHandler.HandleVKCallback)
	router.GET("/check", authHandler.CheckUserPermission)

	log.Println("Auth service running on :8082...")
	log.Fatal(router.Run(":8082"))
}

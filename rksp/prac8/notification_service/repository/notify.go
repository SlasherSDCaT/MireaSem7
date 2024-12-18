package repository

import (
	"context"

	"github.com/jackc/pgx/v4"
)

type NotificationRepository struct {
	db *pgx.Conn
}

func NewNotificationRepository(db *pgx.Conn) *NotificationRepository {
	return &NotificationRepository{db: db}
}

func (repo *NotificationRepository) CreateNotification(
	ctx context.Context,
	taskID, message string,
) (int, error) {
	var id int

	err := repo.db.QueryRow(
		ctx,
		`INSERT INTO notifications (task_id, message) VALUES ($1, $2) RETURNING id`,
		taskID,
		message,
	).Scan(&id)

	return id, err
}

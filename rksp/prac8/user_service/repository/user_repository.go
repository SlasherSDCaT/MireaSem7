package repository

import (
	"context"
	"errors"

	"user_service/domain"

	"github.com/jackc/pgx/v4"
)

type UserRepository struct {
	db *pgx.Conn
}

func NewUserRepository(db *pgx.Conn) *UserRepository {
	return &UserRepository{db: db}
}

func (ur *UserRepository) FindOrCreateUser(ctx context.Context, Id, vkID string) (*domain.User, error) {
	var user = new(domain.User)

	err := ur.db.QueryRow(ctx, "SELECT id, vk_id FROM users WHERE vk_id=$1", vkID).
		Scan(&user.ID, &user.VKID)
	if errors.Is(err, pgx.ErrNoRows) {
		err = ur.db.QueryRow(ctx, "INSERT INTO users (id, vk_id) VALUES ($1, $2) RETURNING id, vk_id", Id, vkID).
			Scan(&user.ID, &user.VKID)
	}
	return user, err
}

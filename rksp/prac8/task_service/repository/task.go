package repository

import (
	"context"
	"fmt"

	"task_service/domain"

	"github.com/jackc/pgx/v4"
)

//go:generate moq -pkg repository -out task_mock.go  . TaskRepository

type TaskRepository interface {
	Task(ctx context.Context, id string) (*domain.Task, error)
	CreateTask(ctx context.Context, task *domain.Task) (string, error)
	UpdateTask(ctx context.Context, task *domain.Task) error
	DeleteTask(ctx context.Context, id string) error
	ListTasks(ctx context.Context) ([]*domain.Task, error)
}

type TaskRepositoryImpl struct {
	db *pgx.Conn
}

func NewTaskRepository(db *pgx.Conn) *TaskRepositoryImpl {
	return &TaskRepositoryImpl{db: db}
}

func (tr *TaskRepositoryImpl) Task(ctx context.Context, id string) (*domain.Task, error) {
	row := tr.db.QueryRow(
		ctx,
		"SELECT id, title, assignee_id, created_at, deadline_at FROM tasks WHERE id = $1",
		id,
	)

	task := new(domain.Task)
	if err := row.Scan(&task.ID, &task.Title, &task.AssigneeID, &task.CreatedAt, &task.DeadlineAt); err != nil {
		return nil, fmt.Errorf("failed scan task: %v", err)
	}

	return task, nil
}

func (tr *TaskRepositoryImpl) CreateTask(ctx context.Context, task *domain.Task) (string, error) {
	var id string

	err := tr.db.QueryRow(
		ctx,
		`INSERT INTO tasks (id, title, creator_id, assignee_id, created_at, deadline_at) VALUES ($1, $2, $3, $4, $5, $6) RETURNING id`,
		task.ID,
		task.Title,
		task.CreatorID,
		task.AssigneeID,
		task.CreatedAt,
		task.DeadlineAt,
	).Scan(&id)
	if err != nil {
		return "", fmt.Errorf("failed to create task: %v", err)
	}

	return id, nil
}

func (tr *TaskRepositoryImpl) UpdateTask(ctx context.Context, task *domain.Task) error {
	commandTag, err := tr.db.Exec(
		ctx,
		`UPDATE tasks SET title = $1, assignee_id = $2, deadline_at = $3 WHERE id = $4`,
		task.Title,
		task.AssigneeID,
		task.DeadlineAt,
		task.ID,
	)
	if err != nil {
		return fmt.Errorf("failed to update task: %v", err)
	}

	if commandTag.RowsAffected() == 0 {
		return fmt.Errorf("no task found with id: %s", task.ID)
	}

	return nil
}

func (tr *TaskRepositoryImpl) DeleteTask(ctx context.Context, id string) error {
	commandTag, err := tr.db.Exec(
		ctx,
		`DELETE FROM tasks WHERE id = $1`,
		id,
	)
	if err != nil {
		return fmt.Errorf("failed to delete task: %v", err)
	}

	if commandTag.RowsAffected() == 0 {
		return fmt.Errorf("no task found with id: %s", id)
	}

	return nil
}

func (tr *TaskRepositoryImpl) ListTasks(ctx context.Context) ([]*domain.Task, error) {
	rows, err := tr.db.Query(
		ctx,
		"SELECT id, title, creator_id, assignee_id, created_at, deadline_at FROM tasks",
	)
	if err != nil {
		return nil, fmt.Errorf("failed to list tasks: %v", err)
	}
	defer rows.Close()

	var tasks []*domain.Task
	for rows.Next() {
		task := new(domain.Task)
		if err := rows.Scan(&task.ID, &task.Title, &task.CreatorID, &task.AssigneeID, &task.CreatedAt, &task.DeadlineAt); err != nil {
			return nil, fmt.Errorf("failed to scan task: %v", err)
		}
		tasks = append(tasks, task)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("row iteration error: %v", err)
	}

	return tasks, nil
}

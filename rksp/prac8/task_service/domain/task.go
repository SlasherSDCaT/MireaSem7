package domain

import (
	"time"

	"github.com/google/uuid"
)

type Task struct {
	ID         string    `json:"id"`
	Title      string    `json:"title"`
	CreatorID  string    `json:"creator_id"`
	AssigneeID string    `json:"assignee_id,omitempty"`
	CreatedAt  time.Time `json:"created_at"`
	DeadlineAt time.Time `json:"deadline_at,omitempty"`
}

func NewTask(title string, deadlineAt time.Time, creatorID, assigneeID string) *Task {
	return &Task{
		ID:         uuid.New().String(),
		Title:      title,
		CreatorID:  creatorID,
		AssigneeID: assigneeID,
		CreatedAt:  time.Now(),
		DeadlineAt: deadlineAt,
	}
}

type GetTaskRequest struct {
	TaskId string `json:"task_id"`
}

type ErrorResponse struct {
	Detail string `json:"detail"`
}

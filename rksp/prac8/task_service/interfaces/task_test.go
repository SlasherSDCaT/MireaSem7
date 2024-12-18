package interfaces

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"task_service/domain"
	"task_service/infrastructure/notification"
	"task_service/repository"

	"github.com/go-chi/chi/v5"
	"github.com/stretchr/testify/assert"
)

func setupTestServer() *Server {
	mockRepo := &repository.TaskRepositoryMock{
		CreateTaskFunc: func(ctx context.Context, task *domain.Task) (string, error) {
			return task.ID, nil
		},
		DeleteTaskFunc: func(ctx context.Context, id string) error {
			return nil
		},
		ListTasksFunc: func(ctx context.Context) ([]*domain.Task, error) {
			return []*domain.Task{
				{
					ID:         "4b7781bf-daee-4a7a-b1f4-ab6ed68c8110",
					Title:      "new list",
					CreatorID:  "7f197d9b-4001-46e9-9a50-f782e176b53b",
					AssigneeID: "c2a4468e-798b-41df-9844-bfac0df80b62",
					CreatedAt:  time.Now(),
					DeadlineAt: time.Date(2009, time.November, 10, 23, 0, 0, 0, time.UTC),
				},
			}, nil
		},
		TaskFunc: func(ctx context.Context, id string) (*domain.Task, error) {
			return &domain.Task{
				ID:         "6e108349-3e07-4b2b-9b7a-13fd226ac749",
				Title:      "new",
				CreatorID:  "c2a4468e-798b-41df-9844-bfac0df80b62",
				AssigneeID: "7f197d9b-4001-46e9-9a50-f782e176b53b",
				CreatedAt:  time.Date(2009, time.November, 10, 23, 0, 0, 0, time.UTC),
			}, nil
		},
		UpdateTaskFunc: func(ctx context.Context, task *domain.Task) error {
			return nil
		},
	}

	mockNotif := &notification.NotifyClientMock{
		SendNotificationFunc: func(ctx context.Context, taskID string, message string) error {
			return nil
		},
	}
	server := &Server{
		taskRepository:     mockRepo,
		notificationClient: mockNotif,
	}
	return server
}

func TestGetTaskHandler(t *testing.T) {
	server := setupTestServer()

	req := httptest.NewRequest("GET", "/tasks/1", nil)
	w := httptest.NewRecorder()

	r := chi.NewRouter()
	r.Get("/tasks/{taskID}", server.getTaskHandler)
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var task domain.Task
	err := json.Unmarshal(w.Body.Bytes(), &task)
	assert.NoError(t, err)
	assert.Equal(t, "new", task.Title)
}

func TestCreateTaskHandler(t *testing.T) {
	server := setupTestServer()

	reqBody := map[string]interface{}{
		"title":       "New Task",
		"creator_id":  "123",
		"assignee_id": "456",
		"deadline_at": time.Now(),
	}
	reqBodyBytes, _ := json.Marshal(reqBody)
	req := httptest.NewRequest("POST", "/tasks", bytes.NewReader(reqBodyBytes))
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()

	r := chi.NewRouter()
	r.Post("/tasks", server.createTaskHandler)
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusCreated, w.Code)
	var resp map[string]interface{}
	err := json.Unmarshal(w.Body.Bytes(), &resp)
	assert.NoError(t, err)
}

func TestUpdateTaskHandler(t *testing.T) {
	server := setupTestServer()

	reqBody := map[string]interface{}{
		"title": "Updated Task",
	}
	reqBodyBytes, _ := json.Marshal(reqBody)
	req := httptest.NewRequest("PUT", "/tasks/1", bytes.NewReader(reqBodyBytes))
	req.Header.Set("Content-Type", "application/json")
	w := httptest.NewRecorder()

	r := chi.NewRouter()
	r.Put("/tasks/{taskID}", server.updateTaskHandler)
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var resp map[string]interface{}
	err := json.Unmarshal(w.Body.Bytes(), &resp)
	assert.NoError(t, err)
	assert.Equal(t, "updated", resp["status"])
}

func TestDeleteTaskHandler(t *testing.T) {
	server := setupTestServer()

	req := httptest.NewRequest("DELETE", "/tasks/1", nil)
	w := httptest.NewRecorder()

	r := chi.NewRouter()
	r.Delete("/tasks/{taskID}", server.deleteTaskHandler)
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var resp map[string]interface{}
	err := json.Unmarshal(w.Body.Bytes(), &resp)
	assert.NoError(t, err)
	assert.Equal(t, "deleted", resp["status"])
}

func TestListTasksHandler(t *testing.T) {
	server := setupTestServer()

	req := httptest.NewRequest("GET", "/tasks", nil)
	w := httptest.NewRecorder()

	r := chi.NewRouter()
	r.Get("/tasks", server.listTasksHandler)
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var tasks []*domain.Task
	err := json.Unmarshal(w.Body.Bytes(), &tasks)
	assert.NoError(t, err)
	assert.Len(t, tasks, 1)
	assert.Equal(t, "new list", tasks[0].Title)
}

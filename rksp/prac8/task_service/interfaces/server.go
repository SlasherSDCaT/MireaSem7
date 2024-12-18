package interfaces

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"time"

	"task_service/domain"
	"task_service/infrastructure/notification"
	"task_service/repository"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/render"
)

type Server struct {
	taskRepository     repository.TaskRepository
	notificationClient notification.NotifyClient
}

func NewServer(tr *repository.TaskRepositoryImpl, nc notification.NotifyClient) *Server {
	return &Server{
		taskRepository:     tr,
		notificationClient: nc,
	}
}

func (s *Server) Run() error {
	r := chi.NewRouter()

	r.Get("/tasks", s.listTasksHandler)              // List all tasks
	r.Get("/tasks/{taskID}", s.getTaskHandler)       // Get a task by ID
	r.Post("/tasks", s.createTaskHandler)            // Create a new task
	r.Put("/tasks/{taskID}", s.updateTaskHandler)    // Update a task by ID
	r.Delete("/tasks/{taskID}", s.deleteTaskHandler) // Delete a task by ID

	err := http.ListenAndServe(":8080", r)
	if err != nil {
		return fmt.Errorf("http listen err: %v", err)
	}

	log.Println("Task Service running on :8080")
	return nil
}

func (s *Server) getTaskHandler(w http.ResponseWriter, r *http.Request) {
	taskID := chi.URLParam(r, "taskID")
	if taskID == "" {
		w.WriteHeader(http.StatusBadRequest)
		render.JSON(w, r, domain.ErrorResponse{Detail: "taskID is empty"})
		return
	}

	task, err := s.taskRepository.Task(r.Context(), taskID)
	if err != nil {
		w.WriteHeader(http.StatusNotFound)
		render.JSON(w, r, domain.ErrorResponse{Detail: "Task not found"})
		return
	}

	render.JSON(w, r, task)
}

func (s *Server) createTaskHandler(w http.ResponseWriter, r *http.Request) {
	var req struct {
		Title      string    `json:"title"`
		CreatorId  string    `json:"creator_id"`
		AssigneeId string    `json:"assignee_id,omitempty"`
		DeadlineAt time.Time `json:"deadline_at,omitempty"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, domain.ErrorResponse{Detail: "Invalid request payload"})
		return
	}

	task := domain.NewTask(req.Title, req.DeadlineAt, req.CreatorId, req.AssigneeId)

	id, err := s.taskRepository.CreateTask(r.Context(), task)
	if err != nil {
		render.JSON(w, r, domain.ErrorResponse{Detail: "task create error: " + err.Error()})
		return
	}

	if err := s.notificationClient.SendNotification(r.Context(), task.ID, task.Title); err != nil {
		log.Println("notification send error: " + err.Error())
	}

	w.WriteHeader(http.StatusCreated)
	render.JSON(w, r, map[string]interface{}{"id": id})
}

func (s *Server) updateTaskHandler(w http.ResponseWriter, r *http.Request) {
	taskID := chi.URLParam(r, "taskID")
	if taskID == "" {
		w.WriteHeader(http.StatusBadRequest)
		render.JSON(w, r, domain.ErrorResponse{Detail: "taskID is empty"})
		return
	}

	var req struct {
		Title      string    `json:"title"`
		AssigneeId string    `json:"assignee_id,omitempty"`
		DeadlineAt time.Time `json:"deadline_at,omitempty"`
	}

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		render.JSON(w, r, domain.ErrorResponse{Detail: "Invalid request payload"})
		return
	}

	task, err := s.taskRepository.Task(r.Context(), taskID)
	if err != nil {
		w.WriteHeader(http.StatusNotFound)
		render.JSON(w, r, domain.ErrorResponse{Detail: "Task not found"})
		return
	}

	task.Title = req.Title
	task.AssigneeID = req.AssigneeId
	task.DeadlineAt = req.DeadlineAt

	if err := s.taskRepository.UpdateTask(r.Context(), task); err != nil {
		render.JSON(w, r, domain.ErrorResponse{Detail: "task update error: " + err.Error()})
		return
	}

	render.JSON(w, r, map[string]interface{}{"status": "updated"})
}

func (s *Server) deleteTaskHandler(w http.ResponseWriter, r *http.Request) {
	taskID := chi.URLParam(r, "taskID")
	if taskID == "" {
		w.WriteHeader(http.StatusBadRequest)
		render.JSON(w, r, domain.ErrorResponse{Detail: "taskID is empty"})
		return
	}

	if err := s.taskRepository.DeleteTask(r.Context(), taskID); err != nil {
		render.JSON(w, r, domain.ErrorResponse{Detail: "task delete error: " + err.Error()})
		return
	}

	w.WriteHeader(http.StatusOK)
	render.JSON(w, r, map[string]interface{}{"status": "deleted"})
}

func (s *Server) listTasksHandler(w http.ResponseWriter, r *http.Request) {
	tasks, err := s.taskRepository.ListTasks(r.Context())
	if err != nil {
		render.JSON(w, r, domain.ErrorResponse{Detail: "task list error: " + err.Error()})
		return
	}

	render.JSON(w, r, tasks)
}

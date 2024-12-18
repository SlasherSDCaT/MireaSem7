package main

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"net/url"
	"os"

	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
)

type AuthService struct {
	UserServiceURL string
	ParsedURL      *url.URL
}

func NewAuthService(userServiceURL string) (*AuthService, error) {
	parsed, err := url.Parse(userServiceURL)
	if err != nil {
		return nil, fmt.Errorf("invalid task service URL: %w", err)
	}

	return &AuthService{UserServiceURL: userServiceURL, ParsedURL: parsed}, nil
}

func (s *AuthService) ValidateToken(ctx context.Context, token string) (string, error) {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, s.UserServiceURL+"/check", nil)
	if err != nil {
		return "", fmt.Errorf("failed to create request: %w", err)
	}

	q := req.URL.Query()
	q.Add("token", token)
	req.URL.RawQuery = q.Encode()

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return "", fmt.Errorf("failed to validate token: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("token validation failed with status: %s", resp.Status)
	}

	var response struct {
		UserID string `json:"user_id"`
	}

	// Декодируем JSON-ответ
	if err := json.NewDecoder(resp.Body).Decode(&response); err != nil {
		return "", fmt.Errorf("failed to parse JSON response: %w", err)
	}

	return response.UserID, nil
}

type Gateway struct {
	authService *AuthService
	taskService *url.URL
}

func NewGateway(authService *AuthService, taskServiceURL string) (*Gateway, error) {
	parsedURL, err := url.Parse(taskServiceURL)
	if err != nil {
		return nil, fmt.Errorf("invalid task service URL: %w", err)
	}
	return &Gateway{
		authService: authService,
		taskService: parsedURL,
	}, nil
}

func (g *Gateway) JWTMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		cookie, err := r.Cookie("token")
		if err != nil || cookie.Value == "" {
			http.Error(w, "unauthorized: missing or invalid token cookie", http.StatusUnauthorized)
			return
		}

		token := cookie.Value
		userID, err := g.authService.ValidateToken(r.Context(), token)
		if err != nil {
			http.Error(w, "unauthorized: "+err.Error(), http.StatusUnauthorized)
			return
		}

		log.Println("userID: ", userID)

		ctx := context.WithValue(r.Context(), "userID", userID)
		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

func (g *Gateway) ProxyTaskRequest(w http.ResponseWriter, r *http.Request) {
	// Прокси запрос к Task Service
	proxyURL := g.taskService.ResolveReference(r.URL)
	proxyReq, err := http.NewRequest(r.Method, proxyURL.String(), r.Body)
	if err != nil {
		http.Error(w, "failed to create proxy request: "+err.Error(), http.StatusInternalServerError)
		return
	}

	// Копирование заголовков и контекста
	proxyReq.Header = r.Header
	proxyReq = proxyReq.WithContext(r.Context())

	client := http.DefaultClient
	resp, err := client.Do(proxyReq)
	if err != nil {
		http.Error(w, "failed to proxy request: "+err.Error(), http.StatusInternalServerError)
		return
	}
	defer resp.Body.Close()

	// Копирование ответа от Task Service
	for k, v := range resp.Header {
		w.Header()[k] = v
	}
	w.WriteHeader(resp.StatusCode)
	io.Copy(w, resp.Body)
}

func (g *Gateway) ProxyAuthRequest(w http.ResponseWriter, r *http.Request) {
	// Прокси запрос к Auth Service
	proxyURL := g.authService.ParsedURL.ResolveReference(r.URL)
	proxyReq, err := http.NewRequest(r.Method, proxyURL.String(), r.Body)
	if err != nil {
		http.Error(w, "failed to create proxy request: "+err.Error(), http.StatusInternalServerError)
		return
	}

	// Копирование заголовков и контекста
	proxyReq.Header = r.Header
	proxyReq = proxyReq.WithContext(r.Context())

	client := http.DefaultClient
	resp, err := client.Do(proxyReq)
	if err != nil {
		http.Error(w, "failed to proxy request: "+err.Error(), http.StatusInternalServerError)
		return
	}
	defer resp.Body.Close()

	// Копирование ответа от Auth Service
	for k, v := range resp.Header {
		w.Header()[k] = v
	}
	w.WriteHeader(resp.StatusCode)
	io.Copy(w, resp.Body)
}

func (g *Gateway) AuthMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		userID := r.Context().Value("userID").(string)
		taskID := chi.URLParam(r, "taskID")

		// Логика проверки доступа к задаче
		if taskID != "" && userID != taskID {
			http.Error(w, "forbidden: you do not own this task", http.StatusForbidden)
			return
		}

		next.ServeHTTP(w, r)
	})
}

func main() {
	userServiceURL := "http://user-service:8082" // URL User Service
	taskServiceURL := "http://task-service:8080" // URL Task Service

	authService, err := NewAuthService(userServiceURL)
	if err != nil {
		log.Fatalf("failed to create auth service: %v", err)
	}

	gateway, err := NewGateway(authService, taskServiceURL)
	if err != nil {
		log.Fatalf("failed to initialize gateway: %v", err)
	}

	r := chi.NewRouter()
	r.Use(middleware.Logger)

	// Обработка запроса OPTIONS для всех маршрутов, чтобы разрешить preflight
	r.Options("/*", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Cache-Control", "no-store")
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Origin, Content-Type, Authorization")
		w.WriteHeader(http.StatusNoContent)
	})

	// Маршрут для отображения index.html
	r.Get("/", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Cache-Control", "no-store")

		// Открываем файл
		file, err := os.Open("index.html")
		if err != nil {
			http.Error(w, "failed to load index.html: "+err.Error(), http.StatusInternalServerError)
			return
		}
		defer file.Close()

		// Отправляем содержимое файла как ответ
		w.Header().Set("Content-Type", "text/html")
		http.ServeFile(w, r, "index.html")
	})

	// Новый маршрут для проксирования запросов к Auth Service
	r.Get("/auth", gateway.ProxyAuthRequest)
	r.Get("/login", gateway.ProxyAuthRequest)

	r.Route("/tasks", func(r chi.Router) {
		r.Use(gateway.JWTMiddleware)  // Проверка токена
		r.Use(gateway.AuthMiddleware) // Проверка прав

		r.Get("/", gateway.ProxyTaskRequest)
		r.Get("/{taskID}", gateway.ProxyTaskRequest)
		r.Post("/", gateway.ProxyTaskRequest)
		r.Put("/{taskID}", gateway.ProxyTaskRequest)
		r.Delete("/{taskID}", gateway.ProxyTaskRequest)
	})

	log.Println("API Gateway running on :8083")
	log.Fatal(http.ListenAndServe(":8083", r))
}

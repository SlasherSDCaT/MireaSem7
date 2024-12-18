package interfaces

import (
	"context"
	"log"
	"strconv"

	"notification_service/repository"

	"notification_service/api"
)

type NotificationServer struct {
	api.UnimplementedNotificationServiceServer
	notificationRepository *repository.NotificationRepository
}

func NewNotificationServer(repo *repository.NotificationRepository) *NotificationServer {
	return &NotificationServer{notificationRepository: repo}
}

func (s *NotificationServer) CreateNotification(
	ctx context.Context,
	req *api.CreateNotificationRequest,
) (*api.CreateNotificationResponse, error) {
	id, err := s.notificationRepository.CreateNotification(ctx, req.TaskId, req.Message)
	if err != nil {
		log.Printf("Failed to create notification: %v", err)
		return nil, err
	}
	return &api.CreateNotificationResponse{
		Id: strconv.Itoa(id),
	}, nil
}

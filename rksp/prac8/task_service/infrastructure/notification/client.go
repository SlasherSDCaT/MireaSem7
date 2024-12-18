package notification

import (
	"context"
	"fmt"
	"log"

	"task_service/api"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

//go:generate moq -pkg notification -out notification_mock.go  . NotifyClient

type NotifyClient interface {
	SendNotification(ctx context.Context, taskID, message string) error
}

type ClientImpl struct {
	grpc api.NotificationServiceClient
}

func NewClient(address string) (NotifyClient, error) {
	conn, err := grpc.NewClient(address, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		return nil, fmt.Errorf("failed to connect to notification service: %v", err)
	}

	client := api.NewNotificationServiceClient(conn)
	return &ClientImpl{grpc: client}, nil
}

func (c *ClientImpl) SendNotification(ctx context.Context, taskID, message string) error {
	// Создаем запрос с параметрами задачи
	req := &api.CreateNotificationRequest{
		TaskId:  taskID,
		Message: message,
	}

	// Отправляем запрос
	res, err := c.grpc.CreateNotification(ctx, req)
	if err != nil {
		return fmt.Errorf("failed to send notification: %v", err)
	}

	log.Printf("send notification with ID (%s) for task (%s)\n", res.Id, taskID)

	return nil
}

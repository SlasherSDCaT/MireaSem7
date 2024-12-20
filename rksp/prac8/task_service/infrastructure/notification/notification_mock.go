// Code generated by moq; DO NOT EDIT.
// github.com/matryer/moq

package notification

import (
	"context"
	"sync"
)

// Ensure, that NotifyClientMock does implement NotifyClient.
// If this is not the case, regenerate this file with moq.
var _ NotifyClient = &NotifyClientMock{}

// NotifyClientMock is a mock implementation of NotifyClient.
//
//	func TestSomethingThatUsesNotifyClient(t *testing.T) {
//
//		// make and configure a mocked NotifyClient
//		mockedNotifyClient := &NotifyClientMock{
//			SendNotificationFunc: func(ctx context.Context, taskID string, message string) error {
//				panic("mock out the SendNotification method")
//			},
//		}
//
//		// use mockedNotifyClient in code that requires NotifyClient
//		// and then make assertions.
//
//	}
type NotifyClientMock struct {
	// SendNotificationFunc mocks the SendNotification method.
	SendNotificationFunc func(ctx context.Context, taskID string, message string) error

	// calls tracks calls to the methods.
	calls struct {
		// SendNotification holds details about calls to the SendNotification method.
		SendNotification []struct {
			// Ctx is the ctx argument value.
			Ctx context.Context
			// TaskID is the taskID argument value.
			TaskID string
			// Message is the message argument value.
			Message string
		}
	}
	lockSendNotification sync.RWMutex
}

// SendNotification calls SendNotificationFunc.
func (mock *NotifyClientMock) SendNotification(ctx context.Context, taskID string, message string) error {
	if mock.SendNotificationFunc == nil {
		panic("NotifyClientMock.SendNotificationFunc: method is nil but NotifyClient.SendNotification was just called")
	}
	callInfo := struct {
		Ctx     context.Context
		TaskID  string
		Message string
	}{
		Ctx:     ctx,
		TaskID:  taskID,
		Message: message,
	}
	mock.lockSendNotification.Lock()
	mock.calls.SendNotification = append(mock.calls.SendNotification, callInfo)
	mock.lockSendNotification.Unlock()
	return mock.SendNotificationFunc(ctx, taskID, message)
}

// SendNotificationCalls gets all the calls that were made to SendNotification.
// Check the length with:
//
//	len(mockedNotifyClient.SendNotificationCalls())
func (mock *NotifyClientMock) SendNotificationCalls() []struct {
	Ctx     context.Context
	TaskID  string
	Message string
} {
	var calls []struct {
		Ctx     context.Context
		TaskID  string
		Message string
	}
	mock.lockSendNotification.RLock()
	calls = mock.calls.SendNotification
	mock.lockSendNotification.RUnlock()
	return calls
}

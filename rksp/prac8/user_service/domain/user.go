package domain

// User represents a user in the database
type User struct {
	ID    int
	VKID  string
	Email string
	Token string
}

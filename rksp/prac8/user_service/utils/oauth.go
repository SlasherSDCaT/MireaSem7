package utils

import (
	"crypto/sha256"
	"encoding/base64"
	"math/rand"
	"time"

	"github.com/golang-jwt/jwt"
)

func GenerateCodeVerifier() string {
	const charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
	rand.Seed(time.Now().UnixNano())
	verifier := make([]byte, 43)
	for i := range verifier {
		verifier[i] = charset[rand.Intn(len(charset))]
	}
	return string(verifier)
}

func GenerateCodeChallenge(verifier string) string {
	hash := sha256.Sum256([]byte(verifier))
	return base64.URLEncoding.WithPadding(base64.NoPadding).EncodeToString(hash[:])
}

func GenerateJWT(userID, jwtSecret string) (string, error) {
	claims := jwt.MapClaims{
		"user_id": "404289592",
		"exp":     time.Now().Add(time.Hour).Unix(),
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString([]byte(jwtSecret))
}

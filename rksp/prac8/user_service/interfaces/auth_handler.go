package interfaces

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"net/url"
	"strconv"

	"user_service/repository"
	"user_service/utils"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt"
)

const (
	clientID     = "52867090"
	clientSecret = "dGoCH8zaUlVVjd3tC5SF"
	redirectURI  = "http://localhost/auth"
	jwtSecret    = "JWT_SECRET"
	selfUrl      = "http://localhost:8082/"
)

type AuthHandler struct {
	userRepo *repository.UserRepository

	stateCodeChallenges map[string]string
}

func NewAuthHandler(userRepo *repository.UserRepository) *AuthHandler {
	return &AuthHandler{userRepo: userRepo, stateCodeChallenges: make(map[string]string, 10)}
}

func (h *AuthHandler) RedirectToVK(c *gin.Context) {
	state := utils.GenerateCodeVerifier()
	codeVerifier := utils.GenerateCodeVerifier()
	codeChallenge := utils.GenerateCodeChallenge(codeVerifier)

	h.stateCodeChallenges[state] = codeVerifier

	log.Println("code challenge: ", codeChallenge)

	authURL := fmt.Sprintf(
		"https://id.vk.com/authorize?response_type=code&client_id=%s&redirect_uri=%s&scope=email&state=%s&code_challenge=%s&code_challenge_method=s256",
		clientID,
		url.QueryEscape(redirectURI),
		state,
		codeChallenge,
	)
	//c.Redirect(http.StatusTemporaryRedirect, authURL)
	// Возвращаем URL клиенту
	c.JSON(http.StatusOK, gin.H{
		"auth_url": authURL,
	})
}

func (h *AuthHandler) HandleVKCallback(c *gin.Context) {
	code := c.Query("code")
	if code == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Code not found"})
		return
	}

	deviceID := c.Query("device_id")
	if deviceID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "deviceID not found"})
		return
	}

	state := c.Query("state")
	if state == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "state not found"})
		return
	}

	codeVerifier, ok := h.stateCodeChallenges[state]
	if !ok {
		c.JSON(http.StatusBadRequest, gin.H{"error": "codeChallenge not found in cache"})
		return
	}

	log.Println("code challenge after map: ", codeVerifier)

	tokenURL := "https://id.vk.com/oauth2/auth"
	data := url.Values{
		"grant_type":    {"authorization_code"},
		"client_id":     {clientID},
		"client_secret": {clientSecret},
		"redirect_uri":  {redirectURI},
		"code":          {code},
		"device_id":     {deviceID},
		"state":         {state},
		"code_verifier": {codeVerifier},
	}

	resp, err := http.PostForm(tokenURL, data)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to request access token"})
		return
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "bad HTTP status"})
		return
	}

	var tokenResponse struct {
		AccessToken string `json:"access_token"`
		UserID      int64  `json:"user_id"`
	}

	if err := json.NewDecoder(resp.Body).Decode(&tokenResponse); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to parse token response"})
		return
	}

	log.Printf("%v", tokenResponse)

	parsedUID := strconv.FormatInt(tokenResponse.UserID, 10)

	tokenString, err := utils.GenerateJWT(parsedUID, jwtSecret)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate JWT"})
		return
	}

	log.Println("parsed UID: ", parsedUID)

	log.Println("token: ", tokenString)

	c.SetCookie("token", tokenString, 3600, "/", "localhost", false, false)
	//c.Redirect(http.StatusFound, "http://localhost/")
}

func (h *AuthHandler) CheckUserPermission(c *gin.Context) {
	uidFromRequest := c.Query("user_id")

	jwtToken := c.Query("token")
	log.Println("jwt token: ", jwtToken)

	token, err := jwt.Parse(jwtToken, func(token *jwt.Token) (interface{}, error) {
		return []byte(jwtSecret), nil
	})

	if token.Valid {
		fmt.Println("You look nice today")
	} else if ve, ok := err.(*jwt.ValidationError); ok {
		if ve.Errors&jwt.ValidationErrorMalformed != 0 {
			fmt.Println("That's not even a token")
		} else if ve.Errors&(jwt.ValidationErrorExpired|jwt.ValidationErrorNotValidYet) != 0 {
			fmt.Println("Timing is everything")
		} else {
			fmt.Println("Couldn't handle this token:", err)
		}
	} else {
		fmt.Println("Couldn't handle this token:", err)
	}

	if err != nil {
		log.Println("bad jwt parse")
		c.JSON(http.StatusUnauthorized, gin.H{"error": err.Error()})
		return
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok || !token.Valid {
		log.Println("invalid token")
		c.JSON(http.StatusUnauthorized, gin.H{"error": "bad claims"})
		return
	}

	uid, ok := claims["user_id"].(string)
	if !ok {
		log.Println("invalid user id")
		c.JSON(http.StatusUnauthorized, gin.H{"error": "invalid user id"})
	}

	if uidFromRequest != "" {
		if uidFromRequest != uid {
			c.JSON(http.StatusForbidden, gin.H{"error": "invalid user id"})
		}
	}

	c.JSON(http.StatusOK, gin.H{"user_id": uid})
}

# TasteThread API

This API allows you to manage recipes through CRUD operations (Create, Read, Update, Delete). Users can create an account, log in, obtain a JWT token to securely access protected resources, and interact with recipe-related functionalities. Some features require authentication.

---

## Features

### User Management
- \*Create an Account\*: Register a new user.
- \*Login\*: Authenticate a user and retrieve a JWT token.
- \*JWT-Based Protection\*: Secure access to protected resources using JSON Web Tokens.

### Recipe Management
- Endpoints for creating, updating, deleting, liking and commenting recipes.
- File upload support (multipart) for recipe images and user avatar updates.

---

## Prerequisites

- Java 21
- Gradle
- PostgreSQL (can run from Docker)
- A configuration file (`application.yaml`) in `resources`
- Firebase project with storage for image uploads (service-account-key.json) if uploads are used

---

## Installation and Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/RosvaldeMANFO/TasteThread-backend.git
   cd TasteThread-backend
    ```

## Configure the application:

Rename application.yaml.template in resources to application.yaml.
Fill in database, JWT, Firebase and other required fields.
Add your Firebase service-account-key.json to the resources folder if using Firebase storage.

1. Build the project:
    ```bash
    ./gradlew clean build
    ```
    
2. Run the application:
    ```bash
    ./gradlew run
    ```

## Key Endpoints

### Auth
- **POST** `/auth/login` : Log in and retrieve a JWT token.  
- **POST** `/auth/refresh` : Refresh access token using a refresh token.

### Users (public)
- **POST** `/users/register` : Register a new user.  
- **POST** `/users/request-password-reset` : Request password reset (body: email string).  
- **POST** `/users/request-account-activation` : Request account activation email (body: email string).

### Users (protected)
- **GET** `/users/profile` : Get current user profile.  
- **POST** `/users/activate` : Activate authenticated user's account.  
- **POST** `/users/reset-password` : Reset password for authenticated user (body: new password string).  
- **PUT** `/users` : Update account. Accepts JSON `UserDTO` or multipart with form field `dto` (JSON) and file field `image`.  
- **DELETE** `/users` : Delete authenticated user's account.

### Recipes (public / protected behavior shown in code)
- **GET** `/recipes` : Retrieve recipes (query: `limit`, `offset`).  
- **GET** `/recipes/{id}` : Retrieve recipe by id.  
- **POST** `/recipes/search` : Search recipes (body: `FilterDTO`, query: `limit`, `offset`).

### Recipes (protected)
- **POST** `/recipes` : Create a recipe. Accepts JSON `RecipeDTO` or multipart with form field `recipe` (JSON) and file field `image`.  
- **PUT** `/recipes/{id}` : Update a recipe. Accepts the same payloads as create.  
- **DELETE** `/recipes/{id}` : Delete a recipe.  
- **POST** `/recipes/like` : Like a recipe (body: recipe id string).  
- **POST** `/recipes/comments` : Add a comment (body: `RecipeCommentDTO`).  
- **GET** `/recipes/my` : Get recipes created by the authenticated user.

### Admin (requires admin privileges)
- **GET** `/admin/stats` : Retrieve admin stats.  
- **POST** `/admin/approve/{id}` : Approve a recipe by id.  
- **GET** `/admin/recipes` : List recipes for admin (query: `limit`, `offset`, `pending`).  
- **POST** `/admin/search` : Search recipes as admin (body: `FilterDTO`, query: `limit`, `offset`, `pending`).

---

## Multipart field names
- Recipe create/update multipart: form field `recipe` (JSON) and file field `image`.  
- User update multipart: form field `dto` (JSON) and file field `image`.

---

## Notes
- Ensure `application.yaml` is correctly populated and required service account keys are present in `resources`.  
- For local development, you can use the Cloud SQL Auth Proxy to avoid using the Cloud SQL socket factory.
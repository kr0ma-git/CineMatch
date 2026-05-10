# User API Documentation

Base path: `/api/users` _(adjust prefix to match your server config)_

---

## Table of Contents

- [Register User](#1-register-user)
- [Login User](#2-login-user)
- [Logout User](#3-logout-user)
- [Get User by ID](#4-get-user-by-id)
- [Get User by Email](#5-get-user-by-email)
- [Get All Users](#6-get-all-users)

---

## 1. Register User

**`POST /register`**

Creates a new user account.

### Request Body

| Field      | Type   | Required | Description          |
| ---------- | ------ | -------- | -------------------- |
| `username` | string | ✅       | Desired username     |
| `email`    | string | ✅       | User's email address |
| `password` | string | ✅       | Plain-text password  |

```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "secret123"
}
```

### Responses

| Status | Description                     |
| ------ | ------------------------------- |
| `201`  | User successfully registered    |
| `404`  | Missing required fields         |
| `409`  | Email or username already taken |
| `500`  | Internal server error           |

#### ✅ 201 — Success

```json
{
  "message": "User registered",
  "user": {
    "id": "64abc123...",
    "email": "john@example.com",
    "username": "johndoe"
  }
}
```

#### ❌ 404 — Missing Fields

```json
{ "message": "Missing input fields" }
```

#### ❌ 409 — Conflict

```json
{ "message": "Email already taken" }
```

```json
{ "message": "Username already taken" }
```

---

## 2. Login User

**`POST /login`**

Authenticates an existing user.

### Request Body

| Field      | Type   | Required | Description      |
| ---------- | ------ | -------- | ---------------- |
| `email`    | string | ✅       | Registered email |
| `password` | string | ✅       | Account password |

```json
{
  "email": "john@example.com",
  "password": "secret123"
}
```

### Responses

| Status | Description                 |
| ------ | --------------------------- |
| `200`  | Login successful            |
| `401`  | Incorrect email or password |
| `500`  | Internal server error       |

#### ✅ 200 — Success

```json
{
  "message": "User logged in",
  "user": {
    "id": "64abc123...",
    "email": "john@example.com",
    "username": "johndoe"
  }
}
```

#### ❌ 401 — Invalid Credentials

```json
{ "message": "Incorrect username or password" }
```

---

## 3. Logout User

**`POST /logout`**

Ends a user session.

### Request Body

| Field   | Type   | Required | Description                 |
| ------- | ------ | -------- | --------------------------- |
| `email` | string | ✅       | Email of the logged-in user |

```json
{
  "email": "john@example.com"
}
```

### Responses

| Status | Description                      |
| ------ | -------------------------------- |
| `200`  | Logout successful                |
| `404`  | User not found / invalid session |
| `500`  | Internal server error            |

#### ✅ 200 — Success

```json
{ "message": "Logout Successful" }
```

#### ❌ 404 — User Not Found

```json
{ "message": "Invalid user session" }
```

---

## 4. Get User by ID

**`GET /id/:userId`**

Fetches a single user by their MongoDB `_id`.

### URL Parameters

| Parameter | Type   | Required | Description      |
| --------- | ------ | -------- | ---------------- |
| `userId`  | string | ✅       | The user's `_id` |

**Example:** `GET /id/64abc123def456`

### Responses

| Status | Description           |
| ------ | --------------------- |
| `200`  | User found            |
| `404`  | User not found        |
| `500`  | Internal server error |

#### ✅ 200 — Success

```json
{
  "message": "User found",
  "user": {
    "id": "64abc123...",
    "email": "john@example.com",
    "username": "johndoe"
  }
}
```

#### ❌ 404 — Not Found

```json
{ "message": "User not found" }
```

---

## 5. Get User by Email

**`GET /email/:email`**

Fetches a single user by their email address. Password is excluded from the response.

### URL Parameters

| Parameter | Type   | Required | Description      |
| --------- | ------ | -------- | ---------------- |
| `email`   | string | ✅       | The user's email |

**Example:** `GET /email/john@example.com`

### Responses

| Status | Description           |
| ------ | --------------------- |
| `200`  | User found            |
| `404`  | User not found        |
| `500`  | Internal server error |

#### ✅ 200 — Success

```json
{
  "message": "User found",
  "user": {
    "_id": "64abc123...",
    "email": "john@example.com",
    "username": "johndoe"
  }
}
```

> **Note:** `password_hash` is excluded from this response.

#### ❌ 404 — Not Found

```json
{ "message": "User not found" }
```

---

## 6. Get All Users

**`GET /`**

Returns a list of all registered users. Passwords are excluded.

**Example:** `GET /`

### Responses

| Status | Description                    |
| ------ | ------------------------------ |
| `200`  | Users fetched successfully     |
| `404`  | No users found in the database |
| `500`  | Internal server error          |

#### ✅ 200 — Success

```json
{
  "message": "Users fetched",
  "userData": [
    {
      "_id": "64abc123...",
      "email": "john@example.com",
      "username": "johndoe"
    },
    {
      "_id": "64def456...",
      "email": "jane@example.com",
      "username": "janedoe"
    }
  ]
}
```

> **Note:** `password_hash` is excluded for all users in this response.

#### ❌ 404 — Empty Database

```json
{ "message": "No users in the database" }
```

---

## General Notes

- All email inputs are **normalized to lowercase** before processing.
- The `password_hash` field is **never returned** in any GET response.
- Error responses follow the shape: `{ "message": "...", "error": "..." }` where `error` provides additional detail on 500s.

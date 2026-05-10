# Bookmark API Documentation

Base path: `/api/bookmarks` _(adjust prefix to match your server config)_

---

## Table of Contents

- [Create Bookmark](#1-create-bookmark)
- [Get All Bookmarks](#2-get-all-bookmarks)
- [Get Bookmarks by User ID](#3-get-bookmarks-by-user-id)
- [Delete Bookmark](#4-delete-bookmark)

---

## Bookmark Object Shape

This is what a bookmark object looks like when returned from the API:

| Field            | Type   | Description                                     |
| ---------------- | ------ | ----------------------------------------------- |
| `_id`            | string | MongoDB-generated bookmark ID                   |
| `user`           | string | MongoDB `_id` of the user who owns the bookmark |
| `imdbMovieId`    | string | IMDb ID of the bookmarked title                 |
| `imdbMovieTitle` | string | Title of the bookmarked movie/series (optional) |
| `type`           | string | Either `"movie"` or `"series"`                  |
| `createdAt`      | string | ISO 8601 timestamp of when bookmark was created |
| `updatedAt`      | string | ISO 8601 timestamp of last update               |

---

## 1. Create Bookmark

**`POST /create`**

Adds a new bookmark for a user. Duplicate bookmarks (same user + same movie) are not allowed.

### Request Body

| Field        | Type   | Required | Description                      |
| ------------ | ------ | -------- | -------------------------------- |
| `userId`     | string | ✅       | MongoDB `_id` of the user        |
| `movieId`    | string | ✅       | IMDb ID of the title to bookmark |
| `movieTitle` | string | ❌       | Title of the movie/series        |
| `type`       | string | ✅       | Must be `"movie"` or `"series"`  |

```json
{
  "userId": "64abc123...",
  "movieId": "tt1234567",
  "movieTitle": "Inception",
  "type": "movie"
}
```

### Responses

| Status | Description                             |
| ------ | --------------------------------------- |
| `201`  | Bookmark created successfully           |
| `400`  | Missing required fields or invalid type |
| `409`  | Bookmark already exists for this user   |
| `500`  | Internal server error                   |

#### ✅ 201 — Success

```json
{
  "message": "Bookmark successful",
  "bookmark": {
    "userId": "64abc123...",
    "movieId": "tt1234567"
  }
}
```

#### ❌ 400 — Missing Fields

```json
{ "message": "Movie ID and/or Movie Type required" }
```

#### ❌ 400 — Invalid Type

```json
{ "message": "Movie Type can only be 'movie' or 'series'" }
```

#### ❌ 409 — Already Bookmarked

```json
{ "message": "Movie already bookmarked" }
```

---

## 2. Get All Bookmarks

**`GET /`**

Returns every bookmark in the database across all users. Intended for admin/debugging use.

**Example:** `GET /`

### Responses

| Status | Description                        |
| ------ | ---------------------------------- |
| `200`  | Bookmarks fetched successfully     |
| `404`  | No bookmarks found in the database |
| `500`  | Internal server error              |

#### ✅ 200 — Success

```json
{
  "message": "Bookmarks fetched",
  "bookmarks": [
    {
      "_id": "64xyz789...",
      "user": "64abc123...",
      "imdbMovieId": "tt1234567",
      "imdbMovieTitle": "Inception",
      "type": "movie",
      "createdAt": "2024-01-15T10:30:00.000Z",
      "updatedAt": "2024-01-15T10:30:00.000Z"
    }
  ]
}
```

#### ❌ 404 — No Bookmarks

```json
{ "message": "No bookmarks in the database" }
```

---

## 3. Get Bookmarks by User ID

**`GET /:userId`**

Returns all bookmarks belonging to a specific user.

### URL Parameters

| Parameter | Type   | Required | Description               |
| --------- | ------ | -------- | ------------------------- |
| `userId`  | string | ✅       | MongoDB `_id` of the user |

**Example:** `GET /64abc123...`

### Responses

| Status | Description            |
| ------ | ---------------------- |
| `200`  | User bookmarks fetched |
| `404`  | User has no bookmarks  |
| `500`  | Internal server error  |

#### ✅ 200 — Success

```json
{
  "message": "User bookmarks fetched",
  "bookmarks": [
    {
      "_id": "64xyz789...",
      "user": "64abc123...",
      "imdbMovieId": "tt1234567",
      "imdbMovieTitle": "Inception",
      "type": "movie",
      "createdAt": "2024-01-15T10:30:00.000Z",
      "updatedAt": "2024-01-15T10:30:00.000Z"
    },
    {
      "_id": "64xyz000...",
      "user": "64abc123...",
      "imdbMovieId": "tt9999999",
      "imdbMovieTitle": "Breaking Bad",
      "type": "series",
      "createdAt": "2024-01-16T08:00:00.000Z",
      "updatedAt": "2024-01-16T08:00:00.000Z"
    }
  ]
}
```

#### ❌ 404 — No Bookmarks for User

```json
{ "message": "User has no bookmarks" }
```

---

## 4. Delete Bookmark

**`DELETE /delete/:userId/:movieId`**

Removes a specific bookmark for a user, matched by both the user's ID and the IMDb movie ID.

### URL Parameters

| Parameter | Type   | Required | Description                         |
| --------- | ------ | -------- | ----------------------------------- |
| `userId`  | string | ✅       | MongoDB `_id` of the user           |
| `movieId` | string | ✅       | IMDb ID of the title to un-bookmark |

**Example:** `DELETE /delete/64abc123.../tt1234567`

### Responses

| Status | Description                              |
| ------ | ---------------------------------------- |
| `200`  | Bookmark deleted successfully            |
| `400`  | No matching bookmark found for this user |
| `500`  | Internal server error                    |

#### ✅ 200 — Success

```json
{
  "message": "Bookmark deleted",
  "movieDeletedInfo": {
    "userId": "64abc123...",
    "movieId": "tt1234567"
  }
}
```

#### ❌ 400 — Bookmark Not Found

```json
{
  "message": "Bookmark could not be found for user, therefore no bookmark was deleted"
}
```

---

## General Notes

- A bookmark is **unique per user + movie pair** — the same user cannot bookmark the same title twice.
- `imdbMovieTitle` is optional on creation but recommended to store so it can be displayed without an extra IMDb lookup.
- The `type` field strictly accepts `"movie"` or `"series"` only — any other value returns a `400`.
- `GET /` returns **all bookmarks globally** and should be restricted to admin roles once auth is in place.
- `userId` is always a MongoDB `ObjectId` string; `movieId` is always an IMDb ID string (e.g. `tt1234567`).

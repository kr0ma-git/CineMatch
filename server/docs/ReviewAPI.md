# Review API Documentation

Base path: `/api/reviews` _(adjust prefix to match your server config)_

---

## Table of Contents

- [Create Review](#1-create-review)
- [Get All Reviews](#2-get-all-reviews)
- [Get All Reviews by Movie ID](#3-get-all-reviews-by-movie-id)
- [Get All Reviews by User ID](#4-get-all-reviews-by-user-id)
- [Get Specific Review for User](#5-get-specific-review-for-user)
- [Delete Review](#6-delete-review)

---

## Review Object Shape

This is what a review object looks like when returned from the API:

| Field               | Type   | Description                                    |
| ------------------- | ------ | ---------------------------------------------- |
| `_id`               | string | MongoDB-generated review ID                    |
| `user`              | string | MongoDB `_id` of the user who wrote the review |
| `imdbMovieId`       | string | IMDb ID of the reviewed title                  |
| `imdbMovieTitle`    | string | Title of the reviewed movie/series (optional)  |
| `movieReviewString` | string | The written review text                        |
| `movieRatingStars`  | number | Star rating between `0` and `5` (inclusive)    |
| `createdAt`         | string | ISO 8601 timestamp of when review was created  |
| `updatedAt`         | string | ISO 8601 timestamp of last update              |

---

## 1. Create Review

**`POST /create`**

Submits a new review for a movie or series.

### Request Body

| Field              | Type   | Required | Description                               |
| ------------------ | ------ | -------- | ----------------------------------------- |
| `userId`           | string | ✅       | MongoDB `_id` of the reviewing user       |
| `movieId`          | string | ✅       | IMDb ID of the title being reviewed       |
| `movieTitle`       | string | ❌       | Title of the movie/series                 |
| `movieReview`      | string | ✅       | The written review text                   |
| `movieReviewStars` | number | ✅       | Star rating — must be between `0` and `5` |

```json
{
  "userId": "64abc123...",
  "movieId": "tt1234567",
  "movieTitle": "Inception",
  "movieReview": "A mind-bending masterpiece with stunning visuals.",
  "movieReviewStars": 5
}
```

### Responses

| Status | Description                                    |
| ------ | ---------------------------------------------- |
| `201`  | Review created successfully                    |
| `400`  | Missing required fields or invalid star rating |
| `500`  | Internal server error                          |

#### ✅ 201 — Success

```json
{
  "message": "Review created successfully",
  "review": {
    "_id": "64rev001...",
    "user": "64abc123...",
    "imdbMovieId": "tt1234567",
    "imdbMovieTitle": "Inception",
    "movieReviewString": "A mind-bending masterpiece with stunning visuals.",
    "movieRatingStars": 5,
    "createdAt": "2024-01-15T10:30:00.000Z",
    "updatedAt": "2024-01-15T10:30:00.000Z"
  }
}
```

#### ❌ 400 — Missing Fields

```json
{ "message": "Movie ID, review, and star rating are required" }
```

#### ❌ 400 — Invalid Star Rating

```json
{ "message": "Rating must be between 0 and 5" }
```

> **Note:** `movieReviewStars` accepts `0` as a valid rating. The field must be explicitly present in the request body — omitting it entirely will trigger the missing fields error.

---

## 2. Get All Reviews

**`GET /`**

Returns every review in the database across all users and movies. Intended for admin/debugging use.

**Example:** `GET /`

### Responses

| Status | Description                      |
| ------ | -------------------------------- |
| `200`  | Reviews fetched successfully     |
| `404`  | No reviews found in the database |
| `500`  | Internal server error            |

#### ✅ 200 — Success

```json
{
  "message": "Reviews fetched",
  "reviews": [
    {
      "_id": "64rev001...",
      "user": "64abc123...",
      "imdbMovieId": "tt1234567",
      "imdbMovieTitle": "Inception",
      "movieReviewString": "A mind-bending masterpiece.",
      "movieRatingStars": 5,
      "createdAt": "2024-01-15T10:30:00.000Z",
      "updatedAt": "2024-01-15T10:30:00.000Z"
    }
  ]
}
```

#### ❌ 404 — No Reviews

```json
{ "message": "No reviews in the database" }
```

---

## 3. Get All Reviews by Movie ID

**`GET /movie/:movieId`**

Returns all reviews written for a specific movie or series.

### URL Parameters

| Parameter | Type   | Required | Description          |
| --------- | ------ | -------- | -------------------- |
| `movieId` | string | ✅       | IMDb ID of the title |

**Example:** `GET /movie/tt1234567`

### Responses

| Status | Description                |
| ------ | -------------------------- |
| `200`  | Movie reviews fetched      |
| `404`  | No reviews found for movie |
| `500`  | Internal server error      |

#### ✅ 200 — Success

```json
{
  "message": "Movie reviews fetched",
  "reviews": [
    {
      "_id": "64rev001...",
      "user": "64abc123...",
      "imdbMovieId": "tt1234567",
      "imdbMovieTitle": "Inception",
      "movieReviewString": "A mind-bending masterpiece.",
      "movieRatingStars": 5,
      "createdAt": "2024-01-15T10:30:00.000Z",
      "updatedAt": "2024-01-15T10:30:00.000Z"
    },
    {
      "_id": "64rev002...",
      "user": "64def456...",
      "imdbMovieId": "tt1234567",
      "imdbMovieTitle": "Inception",
      "movieReviewString": "Confusing but rewatchable.",
      "movieRatingStars": 3,
      "createdAt": "2024-01-16T09:00:00.000Z",
      "updatedAt": "2024-01-16T09:00:00.000Z"
    }
  ]
}
```

#### ❌ 404 — No Reviews for Movie

```json
{ "message": "Movie has no reviews" }
```

---

## 4. Get All Reviews by User ID

**`GET /user/:userId`**

Returns all reviews written by a specific user.

### URL Parameters

| Parameter | Type   | Required | Description               |
| --------- | ------ | -------- | ------------------------- |
| `userId`  | string | ✅       | MongoDB `_id` of the user |

**Example:** `GET /user/64abc123...`

### Responses

| Status | Description           |
| ------ | --------------------- |
| `200`  | User reviews fetched  |
| `404`  | User has no reviews   |
| `500`  | Internal server error |

#### ✅ 200 — Success

```json
{
  "message": "User reviews fetched",
  "reviews": [
    {
      "_id": "64rev001...",
      "user": "64abc123...",
      "imdbMovieId": "tt1234567",
      "imdbMovieTitle": "Inception",
      "movieReviewString": "A mind-bending masterpiece.",
      "movieRatingStars": 5,
      "createdAt": "2024-01-15T10:30:00.000Z",
      "updatedAt": "2024-01-15T10:30:00.000Z"
    }
  ]
}
```

#### ❌ 404 — No Reviews for User

```json
{ "message": "User has no reviews" }
```

---

## 5. Get Specific Review for User

**`GET /specific/:userId/:movieId`**

Returns the review written by a specific user for a specific movie.

### URL Parameters

| Parameter | Type   | Required | Description               |
| --------- | ------ | -------- | ------------------------- |
| `userId`  | string | ✅       | MongoDB `_id` of the user |
| `movieId` | string | ✅       | IMDb ID of the title      |

**Example:** `GET /specific/64abc123.../tt1234567`

### Responses

| Status | Description                                   |
| ------ | --------------------------------------------- |
| `200`  | Review fetched successfully                   |
| `404`  | No review found for this user + movie pairing |
| `500`  | Internal server error                         |

#### ✅ 200 — Success

```json
{
  "message": "Reviews for specific movie fetched",
  "reviews": {
    "_id": "64rev001...",
    "user": "64abc123...",
    "imdbMovieId": "tt1234567",
    "imdbMovieTitle": "Inception",
    "movieReviewString": "A mind-bending masterpiece.",
    "movieRatingStars": 5,
    "createdAt": "2024-01-15T10:30:00.000Z",
    "updatedAt": "2024-01-15T10:30:00.000Z"
  }
}
```

> **Note:** Unlike other review endpoints, `reviews` here is a **single object**, not an array, since `findOne()` is used internally.

#### ❌ 404 — No Review Found

```json
{ "message": "No reviews made by user for this movie" }
```

---

## 6. Delete Review

**`DELETE /delete/:reviewId`**

Deletes a review by its MongoDB `_id`.

### URL Parameters

| Parameter  | Type   | Required | Description                 |
| ---------- | ------ | -------- | --------------------------- |
| `reviewId` | string | ✅       | MongoDB `_id` of the review |

**Example:** `DELETE /delete/64rev001...`

### Responses

| Status | Description                 |
| ------ | --------------------------- |
| `200`  | Review deleted successfully |
| `400`  | No matching review found    |
| `500`  | Internal server error       |

#### ✅ 200 — Success

```json
{
  "message": "Review deleted",
  "deletedReview": {
    "acknowledged": true,
    "deletedCount": 1
  }
}
```

#### ❌ 400 — Review Not Found

```json
{
  "message": "Review could not be found for user, therefore no review was deleted"
}
```

---

## General Notes

- `movieReviewStars` is validated both in the controller (`0–5`) and enforced at the database level via the model schema (`min: 0`, `max: 5`).
- `imdbMovieTitle` is optional on creation but recommended to store to avoid extra IMDb lookups on the frontend.
- `GET /` returns **all reviews globally** and should be restricted to admin roles once auth is in place.
- The `DELETE /delete/:reviewId` endpoint deletes by **review ID only** — there is no user ownership check at the controller level, so any `reviewId` can be deleted. Consider adding auth middleware to verify the requesting user owns the review before deletion.
- `userId` is always a MongoDB `ObjectId` string; `movieId` is always an IMDb ID string (e.g. `tt1234567`).

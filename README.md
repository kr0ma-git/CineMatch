# CineMatch API v1.0

## Changelog

Optionals/TBI

- User session with expo-session and connect-mongo

Initial Commit:

- Connected to MongoDB Atlas Database
- Created model for the User
- Added controllers and routes for user-specific server actions
- Added user registration, login, logout

2026-04-29

- Added new endpoints for user-related queries
- Fixed bugs for the user controller
- Added/Updated bookmark schema
- Added review schema
- Updated user routes
- Added and specified routing for bookmarking
- Added basic bookmark endpoints

## Endpoints

Base URL

```
/api/v1
```

---

### User-Related:

Base URL

```
/api/v1/users
```

---

**Registration (POST):**

```
/register
```

- parameters:

```
{
    username,
    email,
    password,
}
```

- response:

For any missing input fields, status: **404**

```
{
    message: "Missing input fields"
}
```

Upon successful registration, status: **201**

```
{
    message: "User registered",
    user: {
        id: <userId>,
        email: <userEmail>,
        username: <username>,
    }
}
```

If server is unresponsive, status: **500**

```
{
    message: "Internal Server Error",
    error: <error.message>
}
```

---

**Login (POST):**

```
/login
```

- parameters:

```
{
    email,
    password,
}
```

- response

For any incorrect or missing input fields, status: **401**

```
{
    message: "Incorrect username or password"
}
```

Upon successful login, status **200**

```
{
    message: "User logged in",
    user: {
        id: <userId>,
        email: <userEmail>,
        username: <username>,
    }
}
```

If server is unresponsive, status: **500**

```
{
    message: "Internal Server Error",
    error: <error.message>
}
```

---

**Logout (POST):**

```
/logout
```

- parameters:

```
{
    email,
}
```

- response:
  If invalid email, status: **404**

```
{
    message: "Invalid user session"
}

```

If valid email, status: **200**

```
{
    message: "Logout Successful"
}

```

If server is unresponsive, status: **500**

```
{
    message: "Internal Server Error",
    error: <error.message>
}
```

---

**Get User By ID (GET):**

```
/:userId
```

- parameters:

```
/users/userId
```

- response:

If user is not found in database, status: **404**

```
{
    message: "User not found"
}
```

If user found, status: **200**

```
{
    message: "User found",
    user
}

```

If server is unresponsive, status: **500**

```
{
    message: "Internal Server Error",
    error: <error.message>
}
```

---

**Get User By Email (GET)**

```
/:email
```

- parameters:

```
/users/email
```

- response:

If user is not found, status: **404**

```
{
    message: "User not found"
}
```

if user is found, status: **200**

```
{
    message: "User found",
    user,
}
```

If server is unresponsive, status: **500**

```
{
    message: "Internal Server Error",
    error: <error.message>
}
```

---

**Get All Users (GET)**

```
/
```

- parameters: none

- response:

If no users are in the database, status: **404**

```
{
    message: "No users in the database"
}
```

If users are fetched, status: **200**

```
{
    message: "Users fetched",
    userData
}
```

If server is unresponsive, status: **500**

```
{
    message: "Internal Server Error",
    error: <error.message>
}
```

---

### Bookmark-Related:

Base URL

```
/api/v1/bookmarks
```

---

**Bookmark Creation (POST):**

```
/create
```

- parameters

```
{
    userId,
    movieId,
    movieTitle, // Optional
    type, // "movie" or "series
}
```

- response:

If movieId or type is missing, status: **400**

```
{
    message: "Movie ID and/or Movie Type required:
}
```

If movie type is anything other than specified, status: **400**

```
{
    message: "Movie Type can only be 'movie' or 'series'
}
```

If bookmark already exists for the user, status: **409**

```
{
    message: "Movie already bookmarked"
}
```

If bookmark is created, status: **201**

```
{
    message: "Bookmark successful,
    bookmark: {
        userId,
        imdbMovieId
    }
}
```

If server is unresponsive, status: **500**

```
{
    message: "Internal Server Error",
    error: <error.message>
}
```

---

**Get All Bookmarks (GET)**

```
/
```

- parameters: none

- response

If there are no bookmarks in the database, status: **404**

```
{
    message: "No bookmarks in the database"
}
```

If bookmarks are fetched, status: **200**

```
{
    message: "Bookmarks fetched",
    bookmarks
}
```

If server is unresponsive, status: **500**

```
{
    message: "Internal Server Error",
    error: <error.message>
}
```

---

**Get Bookmarks By User ID (GET)**

```
/:userId
```

- parameters:

```
/bookmarks/<userId>
```

- response:

If user has not bookmarks, status: **404**

```
{
    message: "User has no bookmarks"
}
```

If user bookmarks are fetched, status: **200**

```
{
    message: "User bookmarks fetched",
    bookmarks
}
```

If server is unresponsive, status: **500**

```
{
    message: "Internal Server Error",
    error: <error.message>
}
```

---

**Delete User Bookmark (DELETE)**

```
/:userId/:movieId

```

- parameters:

```
/bookmarks/<userId>/<movieId>
```

- response:

If user bookmark was not found, status: **404**

```
{
    message: "Bookmark could nto be found for user, therefore no bookmark was deleted"
}
```

If user bookmark was found and deleted, status: **200**

```
{
    message: "Bookmark deleted",
    movieDeletedInfo: {
        userId,
        imdbMovieId
    }
}
```

If server is unresponsive, status: **500**

```
{
    message: "Internal Server Error",
    error: <error.message>
}
```

## Data Models

**User Schema**

```
{
    username: {
        type: String,
        required: true,
        unique: true,
        trim: true,
        minLength: 5,
        maxLength: 15,
    },
    email: {
        type: String,
        required: true,
        lowercase: true,
        trim: true,
    },
    password_hash: {
        type: String,
        required: true,
        minLength: 6,
        maxLength: 200,
    }
}
```

**Bookmark Schema**

```
{
    user: {
        type: Schema.Types.ObjectId,
        ref: "User",
        required: true,
        index: true,
    },
    imdbMovieId: {
        type: String,
        required: true,
        trim: true,
    },
    imdbMovieTitle: {
        type: String,
        required: false,
        default: "",
    },
    type: {
        type: String,
        enum: ["movie", "series"],
        required: true,
    },
}
```

**Review Schema**

```
{
    userId: {
        type: Schema.Types.ObjectId,
        ref: "User",
        required: true,
        index: true
    },
    imdbMovieId: {
        type: String,
        required: true,
        trim: true
    },
    imdbMovieTitle: {
        type: String
        required: false
    },
    movieReviewString: {
        type: String,
        required: true
    },
    movieRatingStars: {
        type: Number,
        required: true,
        min: 0,
        max: 5
    }
}
```

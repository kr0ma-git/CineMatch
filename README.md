# CineMatch API v1.0

## Changelog

Optionals/TBI

- User session with expo-session and connect-mongo (will have to update controllers)

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

2026-04-30

- Bug fixes in the bookmark controller
- Adjusted the user property in the user model
- Added the review controller

## Endpoints

**Added soon**

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

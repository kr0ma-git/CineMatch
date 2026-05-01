import { Review } from "../models/bookmark.model.js";
import { User } from "../models/user.model.js";

const createReview = async (req, res) => {
    try {
        const { userId, movieId, movieTitle, movieReviewString, movieRatingStars } = req.body;

        if (!userId || !movieId || !movieReviewString || !movieRatingStars) {
            return res.status(400).json({ message: "Missing entry fields" });
        }

        const existingUser = await User.findById(userId);

        if (!existingUser) {
            return res.status(404).json({ message: "User does not exist" });
        }

        const review = await Review.create({
            user: userId,
            imdbMovieId: movieId,
            imdbMovieTitle: movieTitle,
            movieReviewString,
            movieRatingStars,
        })

        return res.status(201).json({
            message: "Review created",
            review,
        })
    } catch(error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const getAllReviewsByMovie = async (req, res) => {
    try {
        const { movieId } = req.params;

        const reviews = await Review.find({
            imdbMovieId: movieId,
        });

        if (reviews.length === 0) {
            return res.status(404).json({ message: "Movie has no reviews or Movie does not exist" });
        }

        return res.status(200).json({
            message: "Movie reviews fetched",
            reviews,
        })
    } catch(error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const getAllMovieReviewsByUserId = async (req, res) => {
    try {
        const { userId, movieId } = req.params;
        const existingUser = await User.findById(userId);

        if (!existingUser) {
            return res.status(404).json({ message: "User does not exist" });
        }

        const reviews = await Review.find({
            user: userId,
            imdbMovieId: movieId,
        });

        if (reviews.length === 0) {
            return res.status(404).json({ message: "User has no reviews for movie" });
        }

        return res.status(200).json({
            message: "User reviews for movie fetched",
            reviews,
        })
    } catch(error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const getAllReviewsByUserId = async (req, res) => {
    try {
        const { userId } = req.params;
        const existingUser = await User.findById(userId);

        if (!existingUser) {
            return res.status(404).json({ message: "User does not exist" });
        }

        const reviews = await Review.find({
            user: userId,
        });

        if (reviews.length === 0) {
            return res.status(404).json({ message: "User has no reviews" });
        }

        return res.status(200).json({
            message: "User reviews fetched",
            reviews,
        });
    } catch(error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const getAllReviews = async (req, res) => {
    try {
        const reviews = await Review.find();

        if (reviews.length === 0) {
            return res.status(404).json({ message: "No reviews found" });
        }

        return res.status(200).json({
            message: "Reviews fetched",
            reviews,
        });
    } catch(error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

/*
const editMovieReviewByUserId = async (req, res) => {
    try {
    } catch(error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}
*/

const deleteMovieReviewByUserId = async (req, res) => {
    try {
        const { userId, movieId } = req.body;
        const existingUser = await User.findById(userId)

        if (!existingUser) {
            return res.status(404).json({ message: "User not found" });
        }

        const deletedReview = await Review.deleteOne({
            user: userId,
            imdbMovieId: movieId,
        });

        if (deletedReview.deletedCount === 0) {
            return res.status(400).json({ message: "Review does not exist" });
        }

        return res.status(200).json({
            message: "Review deleted",
            reviewDeletedInfo: {
                userId,
                movieId,
            }
        })
    } catch(error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

/*
    try {

    } catch(error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
*/

export {
    // POST
    createReview,
    // GET
    getAllReviewsByMovie,
    getAllMovieReviewsByUserId,
    getAllReviewsByUserId,
    getAllReviews,
    // PATCH
    // editMovieReviewByUserId,
    // DELETE
    deleteMovieReviewByUserId,
}
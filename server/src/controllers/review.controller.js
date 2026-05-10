import { Review } from "../models/review.model.js";

const createReview = async (req, res) => {
    try {
        const { userId, movieId, movieTitle, movieReview, movieReviewStars } = req.body;

        if (!movieId || !movieReview || movieReviewStars === undefined) {
            return res.status(400).json({
                message: "Movie ID, review, and star rating are required"
            });
        }

        if (movieReviewStars < 0 || movieReviewStars > 5) {
            return res.status(400).json({
                message: "Rating must be between 0 and 5"
            });
        }

        const review = await Review.create({
            user: userId,
            imdbMovieId: movieId,
            imdbMovieTitle: movieTitle || "",
            movieReviewString: movieReview,
            movieRatingStars: movieReviewStars,
        });

        return res.status(201).json({
            message: "Review created successfully",
            review,
        })
    } catch (error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const getAllReviews = async (req, res) => {
    try {
        const reviews = await Review.find();

        if (reviews.length === 0) {
            return res.status(404).json({ message: "No reviews in the database" });
        }

        return res.status(200).json({
            message: "Reviews fetched",
            reviews,
        })
    } catch (error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const getAllReviewsByMovieId = async (req, res) => {
    try {
        const { movieId } = req.params;
        const reviews = await Review.find({
            imdbMovieId: movieId,
        });

        if (reviews.length === 0) {
            return res.status(404).json({ message: "Movie has no reviews" });
        }

        return res.status(200).json({
            message: "Movie reviews fetched",
            reviews,
        });
    } catch (error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const getAllReviewsByUserId = async (req, res) => {
    try {
        const { userId } = req.params;
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
    } catch (error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const deleteReviewForUser = async (req, res) => {
    try {
        const { reviewId } = req.params;

        const deletedReview = await Review.deleteOne({
            _id: reviewId,
        });

        if (deletedReview.deletedCount === 0) {
            return res.status(400).json({ message: "Review could not be found for user, therefore no review was deleted" });
        }

        return res.status(200).json({
            message: "Review deleted",
            deletedReview,
        })
    } catch (error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const getSpecificReviewForUser = async (req, res) => {
    try {
        const { userId, movieId } = req.params;
        const reviews = await Review.findOne({
            user: userId,
            imdbMovieId: movieId,
        });

        if (reviews.length === 0) {
            return res.status(404).json({ message: "No reviews made by user for this movie" });
        }

        return res.status(200).json({
            message: "Reviews for specific movie fetched",
            reviews,
        })
    } catch (error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

export {
    // POST
    createReview,
    // GET
    getAllReviews,
    getAllReviewsByMovieId,
    getAllReviewsByUserId,
    getSpecificReviewForUser,
    // DELETE
    deleteReviewForUser,
}
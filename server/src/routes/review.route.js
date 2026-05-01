import { Router } from "express";
import {
    createReview,
    getAllReviewsByMovie,
    getAllMovieReviewsByUserId,
    getAllReviewsByUserId,
    getAllReviews,
    // editMovieReviewByUserId,
    deleteMovieReviewByUserId,
} from "../controllers/review.controller.js";

const router = Router();

// POST
router.route("/create").post(createReview);
// GET
router.route("/:movieId").get(getAllReviewsByMovie);
router.route("/:userId/:movieId").get(getAllMovieReviewsByUserId);
router.route("/:userId").get(getAllReviewsByUserId);
router.route("/").get(getAllReviews);
// PATCH
// router.route("/:userId/:movieId").patch(editMovieReviewByUser);
// DELETE
router.route(":/userId/:movieId").delete(deleteMovieReviewByUserId);

export default router;
import { Router } from "express";
import {
    createReview,
    getAllReviews,
    getAllReviewsByMovieId,
    getAllReviewsByUserId,
    deleteReviewForUser,
    getSpecificReviewForUser,
} from "../controllers/review.controller.js";

const router = Router();

// POST
router.route("/create").post(createReview);
// GET
router.route("/").get(getAllReviews);
router.route("/movie/:movieId").get(getAllReviewsByMovieId);
router.route("/user/:userId").get(getAllReviewsByUserId);
router.route("/specific/:userId/:movieId").get(getSpecificReviewForUser);
// DELETE
router.route("/delete/:reviewId").delete(deleteReviewForUser);

export default router;
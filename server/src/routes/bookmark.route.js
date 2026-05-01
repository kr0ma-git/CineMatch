import { Router } from "express";
import {
    createBookmark,
    getAllBookmarks,
    getAllBookmarksByUserId,
    deleteMovieBookmarkByUserId,
} from "../controllers/bookmark.controller.js";

const router = Router();

// POST
router.route("/create").post(createBookmark);
// GET
router.route("/").get(getAllBookmarks);
router.route("/:userId").get(getAllBookmarksByUserId);
// DELETE
router.route("/:userId/:movieId").delete(deleteMovieBookmarkByUserId);

export default router;
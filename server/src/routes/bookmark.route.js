import { Router } from "express";
import {
    createBookmark,
    getAllBookmarks,
    getBookmarksByUserId,
    deleteBookmarkForUser,
} from "../controllers/bookmark.controller.js";

const router = Router();

// POST
router.route("/create").post(createBookmark);
// GET
router.route("/").get(getAllBookmarks);
router.route("/:userId").get(getBookmarksByUserId);
// DELETE
router.route("/:userId/:movieId").delete(deleteBookmarkForUser);

export default router;
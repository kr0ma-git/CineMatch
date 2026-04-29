import { Bookmark } from "../models/bookmark.model.js";

const createBookmark = async (req, res) => {
    try {
        const { userId, movieId, movieTitle, type } = req.body;

        if (!movieId || !type) {
            return res.status(400).json({ message: "Movie ID and/or Movie Type required" });
        }

        const allowedTypes = ["movie", "series"];
        if (!allowedTypes.includes(type)) {
            return res.status(400).json({ message: "Movie Type can only be 'movie' or 'series'" });
        }

        const existingBookmark = await Bookmark.findOne({
            userId,
            imdbMovieId: movieId,
        });

        if (existingBookmark) {
            return res.status(409).json({ message: "Movie already bookmarked" });
        }

        const bookmark = await Bookmark.create({
            userId,
            imdbMovieId: movieId,
            imdbMovieTitle: movieTitle,
            type,
        });

        return res.status(201).json({
            message: "Bookmark successful",
            bookmark: {
                userId,
                imdbMovieId,
            }
        })
    } catch (error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const getAllBookmarks = async (req, res) => {
    try {
        const bookmarks = await Bookmark.find();

        if (bookmarks.length === 0) {
            return res.status(404).json({ message: "No bookmarks in the database" });
        }

        return res.status(200).json({
            message: "Bookmarks fetched",
            bookmarks,
        })
    } catch (error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const getBookmarksByUserId = async (req, res) => {
    try {
        const { userId } = req.params;
        const bookmarks = await Bookmarks.find({
            userId
        });

        if (bookmarks.length === 0) {
            return res.status(404).json({ message: "User has no bookmarks" });
        }

        return res.status(200).json({
            message: "User bookmarks fetched",
            bookmarks,
        });
    } catch (error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const deleteBookmarkForUser = async (req, res) => {
    try {
        const { userId, movieId } = req.params;

        const deletedBookmark = await Bookmark.deleteOne({
            userId,
            imdbMovieId: movieId,
        });

        if (deletedBookmark.deletedCount === 0) {
            return res.status(400).json({ message: "Bookmark could not be found for user, therefore no bookmark was deleted" });
        }

        return res.status(200).json({
            message: "Bookmark deleted",
            movieDeletedInfo: {
                userId,
                imdbMovieId,
            }
        });
    } catch (error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

export {
    // POST
    createBookmark,
    // GET
    getAllBookmarks,
    getBookmarksByUserId,
    // DELETE
    deleteBookmarkForUser,
}
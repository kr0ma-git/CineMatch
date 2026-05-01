import { Bookmark } from "../models/bookmark.model.js";
import { User } from "../models/user.model.js";

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

        const existingUser = await User.findById(userId);

        if (!existingUser) {
            return res.status(404).json({ message: "User does not exist" });
        }

        const existingBookmark = await Bookmark.findOne({
            user: userId,
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
            bookmark,
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

const getAllBookmarksByUserId = async (req, res) => {
    try {
        const { userId } = req.params;
        const existingUser = await User.findById(userId);

        if (!existingUser) {
            return res.status(404).json({ message: "User does not exist" });
        }

        const bookmarks = await Bookmark.find({
            user: userId
        });


        if (bookmarks.length === 0) {
            return res.status(404).json({ message: "User has no bookmarks or User does not exist" });
        }

        return res.status(200).json({
            message: "User bookmarks fetched",
            bookmarks,
        });
    } catch (error) {
        return res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const deleteMovieBookmarkByUserId = async (req, res) => {
    try {
        const { userId, movieId } = req.params;
        const existingUser = await User.findById(userId);

        if (!existingUser) {
            return res.statsu(404).json({ message: "User does not exist" });
        }

        const deletedBookmark = await Bookmark.deleteOne({
            user: userId,
            imdbMovieId: movieId,
        });

        if (deletedBookmark.deletedCount === 0) {
            return res.status(400).json({ message: "Bookmark does not exit" });
        }

        return res.status(200).json({
            message: "Bookmark deleted",
            movieDeletedInfo: {
                userId,
                movieId,
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
    getAllBookmarksByUserId,
    // DELETE
    deleteMovieBookmarkByUserId,
}
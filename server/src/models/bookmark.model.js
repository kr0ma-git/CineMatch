import mongoose, { Schema } from "mongoose";

const bookmarkSchema = new Schema(
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
        },
        type: {
            type: String,
            enum: ["movie", "series"],
            required: true
        },
    },
    {
        timestamps: true,
    }
)

bookmarkSchema.index({ userId: 1, imdbMovieId: 1 }, { unique: true });

export const Bookmark = mongoose.model("Bookmark", bookmarkSchema);

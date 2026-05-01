import mongoose, { Schema } from "mongoose"

const reviewSchema = new Schema(
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
        movieReviewString: {
            type: String,
            required: true,
        },
        movieRatingStars: {
            type: Number,
            required: true,
            min: 0,
            max: 5,
        },
    },
    {
        timestamps: true,
    }
)

export const Review = mongoose.model("Review", reviewSchema);
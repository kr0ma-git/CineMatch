import express, { application } from "express";
import userRouter from "./routes/user.route.js";
import bookmarkRouter from "./routes/bookmark.route.js";
import reviewRouter from "./routes/review.route.js";

const app = express();

app.use(express.json());

app.use("/api/v1/users", userRouter);
app.use("/api/v1/bookmarks", bookmarkRouter);
app.use("/api/v1/reviews", reviewRouter);

export default app;
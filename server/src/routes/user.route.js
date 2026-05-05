import { Router } from "express";
import { logoutUser, loginUser, registerUser, getUserById } from "../controllers/user.controller.js";

const router = Router();

// POST
router.route("/register").post(registerUser);
router.route("/login").post(loginUser);
router.route("/logout").post(logoutUser);

// GET
router.route("/:id").get(getUserById);

export default router;
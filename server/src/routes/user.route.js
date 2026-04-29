import { Router } from "express";
import { 
    logoutUser,
    loginUser,
    registerUser,
    getUserById,
    getUserByEmail,
    getAllUsers,
} from "../controllers/user.controller.js";

const router = Router();

// POST
router.route("/register").post(registerUser);
router.route("/login").post(loginUser);
router.route("/logout").post(logoutUser);

// GET
router.route("/:userId").get(getUserById);
router.route("/:email").get(getUserByEmail);
router.route("/").get(getAllUsers);

export default router;
import { User } from "../models/user.model.js";

const registerUser = async (req, res) => {
    try {
        const { username, email, password } = req.body;

        if (!username || !email || !password) {
            return res.status(400).json({ message: "Missing input fields"} );
        }

        const existingEmail = await User.findOne({ email: email.toLowerCase() });
        if (existingEmail) {
            return res.status(400).json({ message: "Email already taken"} );
        }

        const existingUsername = await User.findOne({ username: username });
        if (existingUsername) {
            return res.status(400).json({ message: "Username already taken"} );
        }

        const user = await User.create({
            username,
            email: email.toLowerCase(),
            password_hash: password,
            loggedIn: false,
        })

        return res.status(201).json({
            message: "User registered",
            user: {
                id: user._id,
                email: user.email,
                username: user.username,
            }
        });
    } catch(error) {
        res.status(500).json({ message: "Internal server error", error: error.message});
    }
}

const loginUser = async (req, res) => {
    try {
        const { email, password } = req.body;
        const user = await User.findOne({
            email: email.toLowerCase()
        });

        if (!user) {
            return res.status(404).json({ message: "Incorrect username or password" });
        }

        const isMatching = await user.comparePassword(password);
        if (!isMatching) {
            return res.status(404).json({ message: "Incorrect username or password" })
        }

        res.status(200).json({
            message: "User logged in",
            user: {
                id: user._id,
                email: user.email,
                username: user.username
            }
        });
    } catch(error) {
        res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const logoutUser = async (req, res) => {
    try {
        const { email } = req.body;

        const user = await User.findOne({ email: email.toLowerCase() });

        if (!user) {
            res.status(404).json({ message: "User not found" });
        }

        res.status(200).json({ message: "Logout Successful" });
    } catch(error) {
        res.status(500).json({ message: "Internal Server Error", error: error.message });
    }
}

const getUserById = async (req, res) => {
    try {
        const { userId } = req.params;
        const user = await User.findById(userId);

        if (!user) {
            return res.status(404).json({ message: "User not found" });
        }

        res.status(200).json({
            message: "User found",
            user,
        });
    } catch(error) {
        res.status(500).json({ message: "Internal server error", error: error.message });
    }
}

const getUserByEmail = async (req, res) => {
    try {
        const { email } = req.params;
        const user = await User.findOne({
            email: email.toLowerCase()
        });

        if (!user) {
            return res.status(404).json({ message: "User not found" });
        }

        res.status(200).json({
            message: "User found",
            user,
        });
    } catch(error) {
        res.status(500).json({ message: "Internal server error", error: error.message});
    }
}

const getAllUsers = async (req, res) => {
    try {
        // Does not include passwords
        const userData = await User.find().select("-password");

        if (userData.length === 0) {
            res.status(404).json({ message: "No users in the database" });
        }

        return res.status(200).json({
            message: "Users fetched",
            userData,
        })
    } catch(error) {
        res.status(500).json({ message: "Internal server error", error: error.message});
    }
}

export {
    // POST
    registerUser,
    loginUser,
    logoutUser,
    // GET
    getUserById,
    getUserByEmail,
    getAllUsers,
}
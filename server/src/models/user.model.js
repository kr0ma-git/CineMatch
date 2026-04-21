import mongoose, { Schema } from "mongoose";
import validator from "validator";
import bcrypt from "bcrypt";

const userSchema = new Schema(
    {
        username: {
            type: String,
            required: true,
            unique: true,
            trim: true,
            minLength: 5,
            maxLength: 15,
            validate(value) {
                if (!validator.isAlphanumeric(value)) {
                    throw new Error("Username must only be in alphanumeric characters!"); 
                }
            }
        },
        email: {
            type: String,
            required: true,
            unique: true,
            lowercase: true,
            trim: true,
            validate(value) {
                if (!validator.isEmail(value)) {
                    throw new Error("Invalid email!");    
                }
            }
        },
        password_hash: {
            type: String,
            required: true,
            minLength: 6,
            maxLength: 200,
            validate(value) {
                if (value.length < 6 || value.length > 30) {
                    throw new Error("Password must be more than 6 characters and less than 30 characters!");
                }
            }
        }
    },
    {
        timestamps: true
    }
);

userSchema.pre("save", async function() {
    const user = this;

    if (!user.isModified("password_hash")) {
        return ;
    }

    user.password_hash = await bcrypt.hash(user.password_hash, 10);
});


userSchema.methods.comparePassword = async function (password) {
    return await bcrypt.compare(password, this.password_hash);
}

export const User = mongoose.model("User", userSchema);
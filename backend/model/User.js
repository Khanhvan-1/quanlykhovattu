const mongoose = require("mongoose");

const UserSchema = new mongoose.Schema({
  username: { type: String, required: true },
  email:    { type: String, required: true, unique: true },
  password: { type: String, required: true },
  role: {
    type: String,
    enum: ["admin", "nhap_kho", "xuat_kho"],
    default: "nhap_kho"
  }
}, { timestamps: true });

module.exports = mongoose.model("User", UserSchema);

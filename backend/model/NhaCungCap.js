const mongoose = require("mongoose");

const NCCSchema = new mongoose.Schema(
  {
    tenNCC: { type: String, required: true, trim: true },
    soDienThoai: { type: String, default: "" },
    diaChi: { type: String, default: "" }
  },
  { timestamps: true }
);

module.exports = mongoose.model("NhaCungCap", NCCSchema);

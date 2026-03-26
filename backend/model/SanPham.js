const mongoose = require("mongoose");

const SanPhamSchema = new mongoose.Schema(
  {
    maHang: { type: String, required: true, unique: true, trim: true },
    tenHang: { type: String, required: true, trim: true },

    loaiHang: { type: String, default: "Khác" },

    tonKho: { type: Number, default: 0, min: 0 },
    soLuong: { type: Number, default: 0, min: 0 },

    giaNhap: { type: Number, default: 0, min: 0 },
    giaXuat: { type: Number, default: 0, min: 0 },

    ghiChu: { type: String, default: "" },

    // trạng thái cảnh báo tồn kho
    canhBaoLowStock: { type: Boolean, default: false },
  },
  { timestamps: true }
);


SanPhamSchema.pre("save", function (next) {

  // tonKho luôn đồng bộ với soLuong nếu thiếu
  if (this.tonKho === null || this.tonKho === undefined) {
    this.tonKho = this.soLuong;
  }

  // giá xuất = giá nhập
  if (this.giaXuat !== this.giaNhap) {
    this.giaXuat = this.giaNhap;
  }

  next();
});

module.exports = mongoose.model("SanPham", SanPhamSchema);

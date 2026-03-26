const mongoose = require("mongoose");

const nhapKhoSchema = new mongoose.Schema(
  {
    maPhieu: { type: String, unique: true },
    ngayNhap: { type: Date, default: Date.now },
    nguoiThucHien: { type: String, required: true },
    nhaCungCap: { type: String, default: "Không rõ" },

    sanPham: [
      {
        maHang: { type: String, required: true },
        tenHang: { type: String, required: true },
        soLuong: { type: Number, required: true, min: 1 },
        giaNhap: { type: Number, required: true, min: 0 },
        thanhTien: { type: Number, default: 0 },
      },
    ],

    tongTien: { type: Number, default: 0 },
    ghiChu: { type: String, default: "" },
  },
  { timestamps: true }
);

nhapKhoSchema.pre("save", function (next) {
  if (this.sanPham && this.sanPham.length > 0) {
    this.tongTien = this.sanPham.reduce((sum, sp) => {
      sp.thanhTien = sp.soLuong * sp.giaNhap;
      return sum + sp.thanhTien;
    }, 0);
  }

  if (!this.maPhieu) {
    const prefix = "NK";
    const random = Math.floor(1000 + Math.random() * 9000);
    this.maPhieu = `${prefix}${random}`;
  }

  next();
});

module.exports = mongoose.model("NhapKho", nhapKhoSchema);

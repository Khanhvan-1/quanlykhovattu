const mongoose = require("mongoose");

const xuatKhoSchema = new mongoose.Schema(
  {
    maPhieu: { type: String, unique: true },
    ngayXuat: { type: Date, default: Date.now },
    khachHang: { type: String, default: "Không rõ" },
    nguoiThucHien: { type: String, required: true },

    sanPham: [
      {
        maHang: { type: String, required: true },
        tenHang: { type: String, required: true },
        soLuong: { type: Number, required: true, min: 1 },
        giaXuat: { type: Number, required: true, min: 0 },
        thanhTien: { type: Number, default: 0 },
      },
    ],

    tongTien: { type: Number, default: 0 },
    ghiChu: { type: String, default: "" },
  },
  { timestamps: true }
);

// ✅ Tự tính tổng tiền & sinh mã phiếu
xuatKhoSchema.pre("save", async function (next) {
  if (this.sanPham && this.sanPham.length > 0) {
    this.tongTien = this.sanPham.reduce((sum, sp) => {
      // Nếu giá xuất bị thiếu → lấy lại giá nhập từ sản phẩm gốc
      if (!sp.giaXuat || sp.giaXuat === 0) {
        const SanPham = require("./SanPham");
        SanPham.findOne({ maHang: sp.maHang }).then((p) => {
          if (p) sp.giaXuat = p.giaNhap;
        });
      }
      sp.thanhTien = sp.soLuong * sp.giaXuat;
      return sum + sp.thanhTien;
    }, 0);
  }

  // Tạo mã phiếu tự động
  if (!this.maPhieu) {
    const prefix = "XK";
    const random = Math.floor(1000 + Math.random() * 9000);
    this.maPhieu = `${prefix}${random}`;
  }

  next();
});

module.exports = mongoose.model("XuatKho", xuatKhoSchema);

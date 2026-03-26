const express = require("express");
const router = express.Router();
const XuatKho = require("../model/XuatKho");
const SanPham = require("../model/SanPham");
const auth = require("../middleware/auth");
const authorizeRoles = require("../middleware/roleAuth");
const { sendLowStockAlert } = require("../utils/notify");
const logHistory = require("../middleware/logHistory");

// ====================== API XUẤT KHO ======================
router.post("/", auth, authorizeRoles("admin", "xuat_kho"), async (req, res) => {
  try {
    const { sanPham, ghiChu, khachHang } = req.body;

    const dsSanPham = Array.isArray(sanPham) ? sanPham : [req.body];
    if (!dsSanPham.length)
      return res.status(400).json({ message: "Không có sản phẩm để xuất!" });

    // ⭐ LẤY THÔNG TIN NGƯỜI DÙNG TỪ TOKEN
    const username = req.user.username;
    const maNV = req.user.maNV;
    const userId = req.user.id;
    const role = req.user.role;

    const count = await XuatKho.countDocuments();
    const maPhieu = `XK${String(count + 1).padStart(5, "0")}`;

    const thanhCong = [];
    const thatBai = [];

    // ========== XỬ LÝ TỪNG SẢN PHẨM ==========
    for (const sp of dsSanPham) {
      const { maHang, tenHang, soLuong } = sp;

      if (!maHang || !soLuong) {
        thatBai.push({ maHang, lyDo: "Thiếu mã hàng hoặc số lượng!" });
        continue;
      }

      const product = await SanPham.findOne({ maHang });
      if (!product) {
        thatBai.push({ maHang, lyDo: "Không tìm thấy sản phẩm!" });
        continue;
      }

      const currentTon = product.tonKho ?? product.soLuong ?? 0;

      // ❗ Không đủ hàng
      if (currentTon < Number(soLuong)) {
        thatBai.push({
          maHang,
          lyDo: `Không đủ hàng (còn ${currentTon})`,
        });
        continue;
      }

      const soXuat = Number(soLuong);
      const giaXuat = Number(product.giaNhap) || 0;

      // ===== Cập nhật tồn kho =====
      const newTon = currentTon - soXuat;
      product.tonKho = newTon;
      product.soLuong = newTon;
      await product.save();

      // ========== GHI LỊCH SỬ XUẤT ==========
      await logHistory({
        loai: "xuat",
        maPhieu,
        tenSanPham: tenHang || product.tenHang,
        soLuong: soXuat,
        gia: giaXuat,
        tongTien: soXuat * giaXuat,

        // ⭐ 4 thông tin quan trọng để hiện lịch sử cá nhân
        userName: username,
        maNV,
        userId,
        role,

        ghiChu: "Xuất kho",
      });

      // ========== Cảnh báo tồn kho thấp ==========
      if (newTon < 10) {
        await sendLowStockAlert(product.tenHang, newTon);

        const maPhieuSystem = `SYS${Date.now()}`;

        await logHistory({
          loai: "system",
          maPhieu: maPhieuSystem,
          tenSanPham: product.tenHang,
          soLuong: newTon,
          gia: 0,
          tongTien: 0,
          userName: "Hệ thống",
          maNV: "SYSTEM",
          userId: null,
          role: "system",
          ghiChu: "Cảnh báo tồn kho thấp",
        });
      }

      thanhCong.push({
        maHang,
        tenHang: tenHang || product.tenHang,
        soLuong: soXuat,
        giaXuat,
        thanhTien: soXuat * giaXuat,
      });
    }

    if (!thanhCong.length)
      return res.status(400).json({
        message: "Không đủ sản phẩm để xuất kho",
        thatBai,
      });

    // ===== Lưu phiếu xuất =====
    const phieu = await XuatKho.create({
      maPhieu,
      sanPham: thanhCong,
      tongTien: thanhCong.reduce((s, i) => s + i.thanhTien, 0),
      nguoiThucHien: username,
      khachHang: khachHang || "Không rõ",
      ghiChu: ghiChu || "",
    });

    return res.status(200).json({
      message: "🚚 Xuất kho thành công!",
      maPhieu,
      nguoiThucHien: username,
      tongTien: phieu.tongTien,
      daXuat: thanhCong.length,
      thanhCong,
      thatBai,
    });

  } catch (err) {
    console.error("❌ Lỗi xuất kho:", err);
    res.status(500).json({ message: "Lỗi xuất kho: " + err.message });
  }
});

module.exports = router;

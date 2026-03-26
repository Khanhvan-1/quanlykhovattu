const express = require("express");
const router = express.Router();
const NhapKho = require("../model/NhapKho");
const SanPham = require("../model/SanPham");
const auth = require("../middleware/auth");
const authorizeRoles = require("../middleware/roleAuth");
const { sendLowStockAlert } = require("../utils/notify");
const logHistory = require("../middleware/logHistory");

router.post("/", auth, authorizeRoles("admin", "nhap_kho"), async (req, res) => {
  try {
    let dsSanPham = Array.isArray(req.body.sanPham)
      ? req.body.sanPham
      : req.body.danhSachSanPhamDaChon || [];

    if (!dsSanPham.length)
      return res.status(400).json({ message: "Không có sản phẩm để nhập!" });

    // ⭐ Lấy thông tin user từ token
    const username = req.user.username;
    const maNV = req.user.maNV;
    const userId = req.user.id;
    const role = req.user.role;

    const count = await NhapKho.countDocuments();
    const maPhieu = `NK${String(count + 1).padStart(5, "0")}`;

    const ketQua = [];

    for (const sp of dsSanPham) {
      const { maHang, tenHang, soLuong, giaNhap, loaiHang } = sp;

      if (!maHang || !tenHang || giaNhap == null) continue;

      const soMoi = Number(soLuong);
      const giaMoi = Number(giaNhap);

      if (soMoi <= 0) {
        return res.status(400).json({
          message: `Số lượng của sản phẩm '${tenHang}' phải > 0!`,
        });
      }

      let product = await SanPham.findOne({ maHang });

      // ====== Cập nhật tồn kho ======
      if (product) {
        const currentTon = product.tonKho ?? product.soLuong ?? 0;
        const newTon = currentTon + soMoi;

        product.tonKho = newTon;
        product.soLuong = newTon;
        product.giaNhap = giaMoi;
        product.giaXuat = giaMoi;
        await product.save();

      } else {
        const loaiTuApp = loaiHang || "Khác";

        product = await SanPham.create({
          maHang,
          tenHang,
          loaiHang: loaiTuApp,
          tonKho: soMoi,
          soLuong: soMoi,
          giaNhap: giaMoi,
          giaXuat: giaMoi,
        });
      }

      // ================================
      // ⭐ GHI LỊCH SỬ NHẬP KHO
      // ================================
      await logHistory({
        loai: "nhap",
        maPhieu,
        tenSanPham: tenHang,
        soLuong: soMoi,
        gia: giaMoi,
        tongTien: soMoi * giaMoi,

        // ⭐ 4 FIELD QUAN TRỌNG
        userName: username,
        maNV: maNV,
        userId: userId,
        role: role,

        ghiChu: "Nhập kho",
      });


      // ===== Cảnh báo tồn kho thấp =====
      if (product.tonKho < 10) {
        await sendLowStockAlert(product.tenHang, product.tonKho);

        const maPhieuSystem = `SYS${Date.now()}`;

        await logHistory({
          loai: "system",
          maPhieu: maPhieuSystem,
          tenSanPham: product.tenHang,
          soLuong: product.tonKho,
          gia: 0,
          tongTien: 0,
          userName: "Hệ thống",
          maNV: "SYSTEM",
          userId: null,
          role: "system",
          ghiChu: "Cảnh báo tồn kho thấp",
        });
      }

      ketQua.push(product);
    }

    // ===== Lưu phiếu =====
    const phieu = await NhapKho.create({
      maPhieu,
      sanPham: dsSanPham.map((sp) => ({
        maHang: sp.maHang,
        tenHang: sp.tenHang,
        soLuong: Number(sp.soLuong),
        giaNhap: Number(sp.giaNhap),
        thanhTien: Number(sp.soLuong) * Number(sp.giaNhap),
        loaiHang: sp.loaiHang || "Khác",
      })),
      tongTien: dsSanPham.reduce(
        (sum, sp) => sum + Number(sp.soLuong) * Number(sp.giaNhap),
        0
      ),
      nhaCungCap: req.body.nhaCungCap || "Không rõ",
      ghiChu: req.body.ghiChu || "",
      nguoiThucHien: username,
    });

    return res.status(200).json({
      message: "✅ Nhập kho thành công!",
      maPhieu,
      nguoiThucHien: username,
      tongSanPham: ketQua.length,
      ketQua,
    });

  } catch (err) {
    console.error("❌ Lỗi nhập kho:", err);
    res.status(500).json({ message: "Lỗi nhập kho: " + err.message });
  }
});

module.exports = router;

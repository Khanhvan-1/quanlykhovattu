const express = require("express");
const router = express.Router();
const KhachHang = require("../model/KhachHang");

// 📌 Lấy danh sách khách hàng
router.get("/", async (_req, res) => {
  try {
    const list = await KhachHang.find().sort({ ngayTao: -1 });
    res.json(list);
  } catch (err) {
    console.error("❌ Lỗi lấy danh sách khách hàng:", err);
    res.status(500).json({ message: "Lỗi máy chủ", error: err.message });
  }
});

// 📌 Thêm khách hàng mới
router.post("/", async (req, res) => {
  try {
    const { ten, soDienThoai, diaChi, email } = req.body || {};

    if (!ten || !soDienThoai) {
      return res
        .status(400)
        .json({ message: "Thiếu tên hoặc số điện thoại." });
    }

    const kh = new KhachHang({ ten, soDienThoai, diaChi, email });
    await kh.save();

    res.status(201).json({
      message: "Thêm khách hàng thành công.",
      data: kh,
    });
  } catch (err) {
    console.error("❌ Lỗi thêm khách hàng:", err);
    res.status(500).json({ message: "Lỗi thêm khách hàng", error: err.message });
  }
});

module.exports = router;

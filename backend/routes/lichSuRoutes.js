const express = require("express");
const router = express.Router();

const LichSu = require("../model/LichSu");
const auth = require("../middleware/auth");
const authorizeRoles = require("../middleware/roleAuth");


// =========================================================
// ✔ ADMIN: LẤY TẤT CẢ LỊCH SỬ
// =========================================================
router.get("/", auth, authorizeRoles("admin"), async (req, res) => {
  try {
    const list = await LichSu.find().sort({ createdAt: -1 });
    res.status(200).json(list);

  } catch (err) {
    console.error("❌ Lỗi lấy lịch sử:", err);
    res.status(500).json({ message: "Lỗi server!", error: err.message });
  }
});


// =========================================================
// ✔ LỊCH SỬ CÁ NHÂN (lọc theo maNV)
// =========================================================
router.get("/ca-nhan", auth, async (req, res) => {
  try {
    const maNV = req.user?.maNV;

    if (!maNV) {
      return res.status(400).json({
        message: "Không xác định được mã nhân viên!"
      });
    }

    // ⭐ Lọc theo maNV
    const list = await LichSu.find({ maNV }).sort({ createdAt: -1 });

    res.status(200).json(list);

  } catch (err) {
    console.error("❌ Lỗi lấy lịch sử cá nhân:", err);
    res.status(500).json({
      message: "Lỗi server!",
      error: err.message
    });
  }
});


// =========================================================
// ✔ LỌC THEO LOẠI (nhap / xuat / system)
// =========================================================
router.get("/loai", auth, async (req, res) => {
  try {
    const { type } = req.query;

    if (!type) return res.status(400).json({ message: "Thiếu type!" });

    const list = await LichSu.find({ loai: type }).sort({ createdAt: -1 });

    res.status(200).json(list);

  } catch (err) {
    console.error("❌ Lỗi lọc theo loại:", err);
    res.status(500).json({ message: err.message });
  }
});


// =========================================================
// ✔ LỌC THEO NGÀY (from - to)
// =========================================================
router.get("/filter-by-date", auth, async (req, res) => {
  try {
    let { from, to } = req.query;

    if (!from && !to) {
      return res.status(400).json({ message: "Thiếu from hoặc to!" });
    }

    const parseDate = (d) => {
      if (!d) return null;
      const [day, month, year] = d.split("-");
      return new Date(`${year}-${month}-${day}T00:00:00.000Z`);
    };

    const fromDate = parseDate(from);
    const toDate = parseDate(to);

    const query = {};

    if (fromDate || toDate) {
      query.createdAt = {};
      if (fromDate) query.createdAt.$gte = fromDate;
      if (toDate) {
        query.createdAt.$lte = new Date(
          new Date(toDate).setHours(23, 59, 59, 999)
        );
      }
    }

    const list = await LichSu.find(query).sort({ createdAt: -1 });

    res.status(200).json(list);

  } catch (err) {
    console.error("❌ Lỗi lọc theo ngày:", err);
    res.status(500).json({
      message: "Lỗi server!",
      error: err.message
    });
  }
});

module.exports = router;

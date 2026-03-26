const express = require("express");
const router = express.Router();
const jwt = require("jsonwebtoken");

const NhapKho = require("../model/NhapKho");
const XuatKho = require("../model/XuatKho");
const SanPham = require("../model/SanPham");

const JWT_SECRET = "MY_SECRET_KEY";

/* ============================================================
   VERIFY TOKEN
============================================================ */
function verifyToken(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader)
    return res.status(401).json({ message: "Thiếu token xác thực!" });

  try {
    const token = authHeader.split(" ")[1];
    const decoded = jwt.verify(token, JWT_SECRET);
    req.user = decoded;
    next();
  } catch (err) {
    return res.status(401).json({ message: "Token không hợp lệ hoặc đã hết hạn!" });
  }
}

/* ============================================================
   1. TỔNG QUAN DOANH THU – CHI PHÍ
============================================================ */
router.get("/", verifyToken, async (req, res) => {
  try {
    const nhap = await NhapKho.aggregate([
      { $unwind: "$sanPham" },
      {
        $group: {
          _id: null,
          tongChiPhi: { $sum: { $multiply: ["$sanPham.soLuong", "$sanPham.giaNhap"] } },
          tongSLNhap: { $sum: "$sanPham.soLuong" }
        }
      }
    ]);

    const xuat = await XuatKho.aggregate([
      { $unwind: "$sanPham" },
      {
        $group: {
          _id: null,
          tongDoanhThu: { $sum: { $multiply: ["$sanPham.soLuong", "$sanPham.giaXuat"] } },
          tongSLXuat: { $sum: "$sanPham.soLuong" }
        }
      }
    ]);

    res.json({
      tongNhap: nhap[0]?.tongSLNhap || 0,
      tongXuat: xuat[0]?.tongSLXuat || 0,
      doanhThu: xuat[0]?.tongDoanhThu || 0,
      chiPhi: nhap[0]?.tongChiPhi || 0
    });
  } catch (err) {
    res.status(500).json({ message: "Lỗi tổng hợp báo cáo: " + err.message });
  }
});

/* ============================================================
   2. TỒN KHO
============================================================ */
router.get("/tonkho", verifyToken, async (req, res) => {
  try {
    const nhapData = await NhapKho.aggregate([
      { $unwind: "$sanPham" },
      { $group: { _id: "$sanPham.maHang", tongNhap: { $sum: "$sanPham.soLuong" } } }
    ]);

    const xuatData = await XuatKho.aggregate([
      { $unwind: "$sanPham" },
      { $group: { _id: "$sanPham.maHang", tongXuat: { $sum: "$sanPham.soLuong" } } }
    ]);

    const sanPhams = await SanPham.find({}, { maHang: 1, tenHang: 1 }).sort({ tenHang: 1 });

    const tonKhoData = sanPhams.map((sp) => ({
      maHang: sp.maHang,
      tenHang: sp.tenHang,
      nhap: nhapData.find((n) => n._id === sp.maHang)?.tongNhap || 0,
      xuat: xuatData.find((x) => x._id === sp.maHang)?.tongXuat || 0,
      tonKho: Math.max(
        (nhapData.find((n) => n._id === sp.maHang)?.tongNhap || 0) -
        (xuatData.find((x) => x._id === sp.maHang)?.tongXuat || 0),
        0
      )
    }));

    res.json(tonKhoData);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

/* ============================================================
   3. BIỂU ĐỒ NHẬP – XUẤT (fix lỗi thiếu ngày)
============================================================ */
router.get("/bieudo/nhapxuat", verifyToken, async (req, res) => {
  try {
    const { from, to } = req.query;

    let matchNhap = {};
    let matchXuat = {};

    if (from && to) {
      matchNhap = {
        ngayNhap: {
          $gte: new Date(from + "T00:00:00Z"),
          $lte: new Date(to + "T23:59:59Z")
        }
      };

      matchXuat = {
        ngayXuat: {
          $gte: new Date(from + "T00:00:00Z"),
          $lte: new Date(to + "T23:59:59Z")
        }
      };
    } else {
      const seven = new Date();
      seven.setHours(0, 0, 0, 0);
      seven.setDate(seven.getDate() - 6);

      matchNhap = { ngayNhap: { $gte: seven } };
      matchXuat = { ngayXuat: { $gte: seven } };
    }

    // ---- Nhập ----
    const nhap = await NhapKho.aggregate([
      { $match: matchNhap },
      {
        $group: {
          _id: {
            $dateToString: { format: "%Y-%m-%d", date: "$ngayNhap", timezone: "Asia/Ho_Chi_Minh" }
          },
          tongNhap: { $sum: "$tongTien" }
        }
      },
      { $sort: { _id: 1 } }
    ]);

    // ---- Xuất ----
    const xuat = await XuatKho.aggregate([
      { $match: matchXuat },
      {
        $group: {
          _id: {
            $dateToString: { format: "%Y-%m-%d", date: "$ngayXuat", timezone: "Asia/Ho_Chi_Minh" }
          },
          tongXuat: { $sum: "$tongTien" }
        }
      },
      { $sort: { _id: 1 } }
    ]);

    // GHÉP LẠI TẤT CẢ CÁC NGÀY
    const days = new Set();

    nhap.forEach((e) => days.add(e._id));
    xuat.forEach((e) => days.add(e._id));

    const sortedDays = [...days].sort();

    const result = sortedDays.map((day) => ({
      ngay: day,
      tongNhap: nhap.find((i) => i._id === day)?.tongNhap || 0,
      tongXuat: xuat.find((i) => i._id === day)?.tongXuat || 0
    }));

    res.json(result);
  } catch (err) {
    console.log("❌ Lỗi biểu đồ nhập/xuất:", err);
    res.status(500).json({ message: err.message });
  }
});

/* ============================================================
   4. BÁO CÁO THEO LOẠI
============================================================ */
router.get("/:type", verifyToken, async (req, res) => {
  const { type } = req.params;

  try {
    const nhap = await NhapKho.aggregate([
      { $unwind: "$sanPham" },
      {
        $group: {
          _id: null,
          tongChiPhi: { $sum: { $multiply: ["$sanPham.soLuong", "$sanPham.giaNhap"] } }
        }
      }
    ]);

    const xuat = await XuatKho.aggregate([
      { $unwind: "$sanPham" },
      {
        $group: {
          _id: null,
          tongDoanhThu: { $sum: { $multiply: ["$sanPham.soLuong", "$sanPham.giaXuat"] } }
        }
      }
    ]);

    const chiPhi = nhap[0]?.tongChiPhi || 0;
    const doanhThu = xuat[0]?.tongDoanhThu || 0;

    if (type === "doanhthu") return res.json({ value: doanhThu });
    if (type === "chiphi") return res.json({ value: chiPhi });

    res.status(400).json({ message: "Loại báo cáo không hợp lệ!" });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

module.exports = router;

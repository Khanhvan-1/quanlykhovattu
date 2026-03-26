const express = require("express");
const router = express.Router();
const jwt = require("jsonwebtoken");
const SanPham = require("../model/SanPham");
const { classifyProductAI } = require("../utils/aiClassifier");

const JWT_SECRET = "MY_SECRET_KEY";

router.use(express.json());
router.use(express.urlencoded({ extended: true }));

// ======================= AUTH =======================
function verifyToken(req, res, next) {
  const header = req.headers.authorization;
  if (!header)
    return res.status(401).json({ message: "Thiếu token!" });

  try {
    const token = header.split(" ")[1];
    req.user = jwt.verify(token, JWT_SECRET);
    next();
  } catch {
    return res.status(401).json({ message: "Token không hợp lệ!" });
  }
}

// ======================= KEYWORD MAP =======================
const keywordMap = {
  maipin: "Máy mài pin",
  khoanpin: "Máy khoan pin",
  khoantu: "Máy khoan pin",
  khoantudo: "Máy khoan pin",
  moocbin: "Máy mở ốc bin",
  sietbulong: "Máy siết bulông",
  mooc: "Máy mở ốc",
  mai: "Máy mài",
  khoan: "Máy khoan",
  cua: "Máy cưa",
  catgo: "Máy cắt gỗ",
  han: "Máy hàn",
  cualong: "Máy cưa lọng",
  tiahangrao: "Máy tỉa hàng rào",
  catcanh: "Máy cắt cành",
  catla: "Máy cắt lá",
  duc: "Máy đục",
  muikhoan: "Mũi khoan",
  muiduc: "Mũi đục",
  toi: "Máy tời",

  mayruaxe: "Máy rửa xe",
  ruaxe: "Máy rửa xe",
  mayruaxehonda: "Máy rửa xe",

  ongnuoc: "Dụng cụ nước",
  voixit: "Dụng cụ nước",
  dayruaxe: "Dụng cụ nước",
  sungruaxe: "Dụng cụ nước",

  bulong: "Bulong & ốc vít",
  son: "Máy phun sơn",
  catsat: "Máy cắt sắt",
  phukien: "Phụ kiện"
};

function removeVie(str) {
  return str.normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d");
}

async function classifyProduct(name, maHang) {
  let clean = removeVie(name.toLowerCase().replace(/\s+/g, ""));

  let keys = Object.keys(keywordMap).sort((a, b) => b.length - a.length);
  for (let k of keys) {
    if (clean.includes(k)) return keywordMap[k];
  }

  try {
    return await classifyProductAI(name, maHang) || "Khác";
  } catch {
    return "Khác";
  }
}

// ======================= GET LIST =======================
router.get("/", verifyToken, async (req, res) => {
  try {
    const list = await SanPham.find().sort({ tenHang: 1 });
    res.json(list);
  } catch (e) {
    res.status(500).json({ message: e.message });
  }
});

// ======================= CREATE + IMPORT =======================
router.post("/", verifyToken, async (req, res) => {
  try {
    if (!["admin", "nhap_kho"].includes(req.user.role))
      return res.status(403).json({ message: "Không có quyền!" });

    const { maHang, tenHang, soLuong, giaNhap, ghiChu, loaiHang } = req.body;

    if (!maHang || !tenHang)
      return res.status(400).json({ message: "Thiếu mã hoặc tên!" });

    let existing = await SanPham.findOne({ maHang });

    const so = Number(soLuong) || 0;
    const gia = Number(giaNhap) || 0;

    // ---- UPDATE SẢN PHẨM TỒN TẠI ----
    if (existing) {

      // 🔥 FIX QUAN TRỌNG — GIỮ LOẠI HÀNG CŨ, KHÔNG CHO RESET THÀNH "Khác"
      existing.loaiHang = existing.loaiHang || "Khác";

      existing.soLuong += so;
      existing.tonKho = existing.soLuong;

      if (gia > 0) {
        existing.giaNhap = gia;
        existing.giaXuat = gia;
      }

      if (ghiChu) existing.ghiChu = ghiChu;

      await existing.save();

      return res.json({
        message: "Đã cập nhật số lượng!",
        data: existing
      });
    }

    // ---- TẠO MỚI ----
    let finalLoaiHang = loaiHang && loaiHang !== "" && loaiHang !== "null"
      ? loaiHang
      : null;

    if (!finalLoaiHang) {
      finalLoaiHang = await classifyProduct(tenHang, maHang);
    }

    const sp = await SanPham.create({
      maHang,
      tenHang,
      loaiHang: finalLoaiHang,
      soLuong: so,
      tonKho: so,
      giaNhap: gia,
      giaXuat: gia,
      ghiChu
    });
    await logHistory({
      loai: "system",                // thêm sản phẩm
      tenSanPham: tenHang,
      soLuong: so,
      gia: gia,
      tongTien: so * gia,
      userName: req.user.username,   // ⭐ THÊM
      role: req.user.role,           // ⭐ THÊM
      ghiChu: "Thêm sản phẩm mới"
    });


    return res.status(201).json({
      message: "Thêm sản phẩm thành công!",
      data: sp
    });

  } catch (e) {
    res.status(500).json({ message: e.message });
  }
});

// ======================= UPDATE =======================
router.put("/:id", verifyToken, async (req, res) => {
  try {
    if (!["admin", "nhap_kho"].includes(req.user.role))
      return res.status(403).json({ message: "Không có quyền!" });

    let spOld = await SanPham.findById(req.params.id);
    if (!spOld) return res.status(404).json({ message: "Không tìm thấy!" });

    let data = req.body;

    // 🔥 CHỈ PHÂN LOẠI LẠI KHI ĐỔI TÊN
    if (data.tenHang && data.tenHang !== spOld.tenHang)
      data.loaiHang = await classifyProduct(data.tenHang, spOld.maHang);
    else
      data.loaiHang = spOld.loaiHang; // Giữ nguyên phân loại

    if (data.giaNhap)
      data.giaXuat = data.giaNhap;

    const sp = await SanPham.findByIdAndUpdate(req.params.id, data, { new: true });
    await logHistory({
      loai: "system",
      tenSanPham: sp.tenHang,
      soLuong: sp.soLuong,
      gia: sp.giaNhap,
      tongTien: sp.soLuong * sp.giaNhap,
      userName: req.user.username,   // ⭐ THÊM
      role: req.user.role,           // ⭐ THÊM
      ghiChu: "Cập nhật sản phẩm"
    });


    res.json({ message: "Cập nhật thành công!", data: sp });

  } catch (e) {
    res.status(500).json({ message: e.message });
  }
});

// ======================= DELETE =======================
router.delete("/:id", verifyToken, async (req, res) => {
  try {
    if (req.user.role !== "admin")
      return res.status(403).json({ message: "Không có quyền xoá!" });

    const sp = await SanPham.findByIdAndDelete(req.params.id);
    await logHistory({
      loai: "system",
      tenSanPham: sp.tenHang,
      soLuong: sp.soLuong,
      gia: sp.giaNhap,
      tongTien: sp.soLuong * sp.giaNhap,
      userName: req.user.username,  // ⭐ THÊM
      role: req.user.role,          // ⭐ THÊM
      ghiChu: "Xoá sản phẩm"
    });

    if (!sp) return res.status(404).json({ message: "Không tìm thấy!" });

    res.json({ message: "Đã xoá sản phẩm!" });

  } catch (e) {
    res.status(500).json({ message: e.message });
  }
});

module.exports = router;

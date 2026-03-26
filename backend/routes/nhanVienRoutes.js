const express = require("express");
const router = express.Router();
const bcrypt = require("bcryptjs");
const NhanVien = require("../model/NhanVien");
const User = require("../model/User");
const LichSu = require("../model/LichSu");
const auth = require("../middleware/auth");
const authorizeRoles = require("../middleware/roleAuth");

// Tự tạo mã NV tăng dần
async function generateMaNV() {
  const count = await NhanVien.countDocuments();
  return `NV${String(count + 1).padStart(4, "0")}`;
}

// ==========================
// 📌 Thêm nhân viên
// ==========================
router.post("/", auth, authorizeRoles("admin"), async (req, res) => {
  try {
    let { tenNV, gioiTinh, tuoi, chucVu, email, password } = req.body;

    if (!tenNV || !tuoi || !chucVu || !email || !password)
      return res.status(400).json({ message: "Thiếu thông tin nhân viên!" });

    email = email.trim().toLowerCase();

    const existedNV = await NhanVien.findOne({ email });
    const existedUser = await User.findOne({ email });

    if (existedNV || existedUser)
      return res.status(400).json({ message: "Email đã tồn tại!" });

    const maNV = await generateMaNV();
    const hashed = await bcrypt.hash(password, 10);

    // CHUẨN HÓA GIỚI TÍNH
    let gt = (gioiTinh || "").trim().toLowerCase();
    let gioiTinhChuan = "Nam";

    if (["nu", "nữ", "female", "f"].includes(gt)) gioiTinhChuan = "Nữ";
    else if (["khac", "khác", "other"].includes(gt)) gioiTinhChuan = "Khác";

    // CHUẨN HÓA CHỨC VỤ – GIỮ 3 GIÁ TRỊ
    const allowedRoles = ["admin", "nhap_kho", "xuat_kho"];
    if (!allowedRoles.includes(chucVu)) chucVu = "nhap_kho";

    // Tạo nhân viên
    const nv = await NhanVien.create({
      maNV,
      tenNV: tenNV.trim(),
      gioiTinh: gioiTinhChuan,
      tuoi,
      chucVu,
      email,
      password: hashed,
    });

    // Tạo user đăng nhập
    await User.create({
      username: tenNV,
      email,
      password: hashed,
      role: chucVu,
    });

    // LỊCH SỬ
    await LichSu.create({
      loai: "system",
      maPhieu: maNV,
      tenSanPham: "Thêm nhân viên",
      soLuong: 0,
      gia: 0,
      tongTien: 0,
      nguoiThucHien: req.user.email,
      ghiChu: `Admin thêm nhân viên ${tenNV} (${email})`
    });

    res.status(201).json({ message: "Thêm nhân viên thành công!", nv });

  } catch (err) {
    console.error("❌ Lỗi thêm nhân viên:", err);
    res.status(500).json({ message: "Lỗi máy chủ: " + err.message });
  }
});

// ==========================
// 📌 Cập nhật nhân viên
// ==========================
router.put("/:maNV", auth, authorizeRoles("admin"), async (req, res) => {
  try {
    const { maNV } = req.params;
    const nv = await NhanVien.findOne({ maNV });

    if (!nv)
      return res.status(404).json({ message: "Không tìm thấy nhân viên!" });

    let { tenNV, gioiTinh, tuoi, chucVu, email, password } = req.body;

    const oldEmail = nv.email;

    if (tenNV) nv.tenNV = tenNV.trim();
    if (tuoi) nv.tuoi = tuoi;

    // Giới tính
    if (gioiTinh) {
      let gt = gioiTinh.trim().toLowerCase();
      if (["nu", "nữ", "female", "f"].includes(gt)) nv.gioiTinh = "Nữ";
      else if (["khac", "khác", "other"].includes(gt)) nv.gioiTinh = "Khác";
      else nv.gioiTinh = "Nam";
    }

    // Chức vụ
    const allowedRoles = ["admin", "nhap_kho", "xuat_kho"];
    if (chucVu && allowedRoles.includes(chucVu)) {
      nv.chucVu = chucVu;
    }

    // Email
    if (email) {
      const emailLower = email.trim().toLowerCase();
      const exists = await NhanVien.findOne({ email: emailLower });
      if (exists && emailLower !== oldEmail)
        return res.status(400).json({ message: "Email đã tồn tại!" });

      nv.email = emailLower;
    }

    // Password
    if (password) nv.password = await bcrypt.hash(password, 10);

    await nv.save();

    // Đồng bộ sang User
    const user = await User.findOne({ email: oldEmail });
    if (user) {
      if (email) user.email = nv.email;
      if (tenNV) user.username = tenNV;
      if (chucVu) user.role = nv.chucVu;
      if (password) user.password = nv.password;
      await user.save();
    }

    await LichSu.create({
      loai: "system",
      maPhieu: maNV,
      tenSanPham: "Cập nhật nhân viên",
      soLuong: 0,
      gia: 0,
      tongTien: 0,
      nguoiThucHien: req.user.email,
      ghiChu: `Admin cập nhật nhân viên ${nv.tenNV}`
    });

    res.status(200).json({ message: "Cập nhật nhân viên thành công!", nv });

  } catch (err) {
    console.error("❌ Lỗi cập nhật nhân viên:", err);
    res.status(500).json({ message: "Lỗi máy chủ: " + err.message });
  }
});

// ==========================
// 📌 Xoá nhân viên
// ==========================
router.delete("/:maNV", auth, authorizeRoles("admin"), async (req, res) => {
  try {
    const { maNV } = req.params;
    const nv = await NhanVien.findOneAndDelete({ maNV });

    if (!nv)
      return res.status(404).json({ message: "Không tìm thấy nhân viên!" });

    await User.findOneAndDelete({ email: nv.email });

    await LichSu.create({
      loai: "system",
      maPhieu: maNV,
      tenSanPham: "Xoá nhân viên",
      soLuong: 0,
      gia: 0,
      tongTien: 0,
      nguoiThucHien: req.user.email,
      ghiChu: `Admin xoá nhân viên ${nv.tenNV}`
    });

    res.status(200).json({ message: "Xoá nhân viên thành công!" });

  } catch (err) {
    console.error("❌ Lỗi xoá nhân viên:", err);
    res.status(500).json({ message: "Lỗi máy chủ: " + err.message });
  }
});

// ==========================
// 📌 Danh sách nhân viên
// ==========================
router.get("/", auth, authorizeRoles("admin"), async (req, res) => {
  try {
    const ds = await NhanVien.find().sort({ createdAt: -1 });
    res.status(200).json(ds);
  } catch (err) {
    console.error("❌ Lỗi lấy danh sách nhân viên:", err);
    res.status(500).json({ message: "Lỗi máy chủ: " + err.message });
  }
});

// Lọc nhân viên theo chức vụ
router.get("/loc/:role", auth, authorizeRoles("admin"), async (req, res) => {
  try {
    const role = req.params.role;
    const ds = await NhanVien.find({ chucVu: role });

    res.status(200).json(ds);
  } catch (err) {
    res.status(500).json({ message: "Lỗi server: " + err.message });
  }
});


module.exports = router;

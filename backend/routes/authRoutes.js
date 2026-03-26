const express = require("express");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const User = require("../model/User");
const NhanVien = require("../model/NhanVien");
const router = express.Router();
const ENV = require("../config/config");

function detectRoleFromEmail(email) {
  email = email.toLowerCase();
  if (email.endsWith("@admin.com")) return "admin";
  if (email.endsWith("@nhapkho.com")) return "nhap_kho";
  if (email.endsWith("@xuatkho.com")) return "xuat_kho";
  return "nhap_kho";
}

async function generateMaNV() {
  const count = await NhanVien.countDocuments();
  return `NV${String(count + 1).padStart(4, "0")}`;
}

/* ============================================================
   ĐĂNG KÝ
============================================================ */
router.post("/register", async (req, res) => {
  try {
    let { username, email, password, gioiTinh } = req.body;

    if (!username || !email || !password)
      return res.status(400).json({ success: false, message: "Thiếu thông tin!" });

    email = email.trim().toLowerCase();

    const existedUser = await User.findOne({ email });
    const existedNV = await NhanVien.findOne({ email });

    if (existedUser || existedNV)
      return res.status(400).json({
        success: false,
        message: "Email đã tồn tại!",
      });

    const hashed = await bcrypt.hash(password, 10);
    const role = detectRoleFromEmail(email);

    let gender = "Nam";
    let gt = (gioiTinh || "").trim().toLowerCase();
    if (["nu", "nữ"].includes(gt)) gender = "Nữ";
    else if (gt === "khác") gender = "Khác";

    const maNV = await generateMaNV();

    await User.create({ username, email, password: hashed, role });

    await NhanVien.create({
      maNV,
      tenNV: username,
      gioiTinh: gender,
      tuoi: 22,
      chucVu: role,
      email,
      password: hashed,
    });

    return res.status(201).json({
      success: true,
      message: "Đăng ký thành công!",
      user: { username, email, role, gioiTinh: gender, maNV },
    });

  } catch (err) {
    console.error("❌ Lỗi đăng ký:", err);
    res.status(500).json({ success: false, message: err.message });
  }
});



/* ============================================================
   ĐĂNG NHẬP — ⭐ FIX TOKEN ĐẦY ĐỦ maNV + TEN NV
============================================================ */
router.post("/login", async (req, res) => {
  try {
    let { email, password } = req.body;
    email = email.trim().toLowerCase();

    const user = await User.findOne({ email });
    if (!user)
      return res.status(400).json({ success: false, message: "Email không tồn tại!" });

    const match = await bcrypt.compare(password, user.password);
    if (!match)
      return res.status(400).json({ success: false, message: "Sai mật khẩu!" });

    // ⭐ Lấy thêm thông tin nhân viên
    const nv = await NhanVien.findOne({ email });
    if (!nv)
      return res.status(500).json({ message: "Không tìm thấy dữ liệu nhân viên!" });

    // ⭐ TOKEN ĐẦY ĐỦ (fix lịch sử cá nhân)
    const token = jwt.sign(
      {
        id: user._id,
        username: user.username,
        email: user.email,
        role: user.role,

        // ⭐ ĐỂ LỌC LỊCH SỬ
        maNV: nv.maNV,
        tenNV: nv.tenNV,
        gioiTinh: nv.gioiTinh,
        tuoi: nv.tuoi,
        chucVu: nv.chucVu,
      },
      ENV.JWT_SECRET,
      { expiresIn: "6h" }
    );

    return res.json({
      success: true,
      message: "Đăng nhập thành công!",
      token,
      user: {
        id: user._id,
        username: user.username,
        email: user.email,
        role: user.role,
        maNV: nv.maNV,
        tenNV: nv.tenNV,
      },
    });

  } catch (err) {
    console.error("❌ Lỗi login:", err);
    res.status(500).json({ message: err.message });
  }
});



/* ============================================================
   ĐỔI MẬT KHẨU
============================================================ */
router.post("/change-password", async (req, res) => {
  try {
    const header = req.headers.authorization;
    if (!header) return res.status(401).json({ message: "Không có token!" });

    const decoded = jwt.verify(header.split(" ")[1], ENV.JWT_SECRET);

    const user = await User.findById(decoded.id);
    if (!user) return res.status(404).json({ message: "Không tìm thấy tài khoản!" });

    const { oldPassword, newPassword } = req.body;

    const match = await bcrypt.compare(oldPassword, user.password);
    if (!match)
      return res.status(400).json({ message: "Mật khẩu cũ không đúng!" });

    const hashed = await bcrypt.hash(newPassword, 10);

    user.password = hashed;
    await user.save();

    await NhanVien.findOneAndUpdate({ email: user.email }, { password: hashed });

    res.json({ success: true, message: "Đổi mật khẩu thành công!" });

  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});


/* ============================================================
   ADMIN LẤY DANH SÁCH USER
============================================================ */
router.get("/users", async (req, res) => {
  try {
    const decoded = jwt.verify(req.headers.authorization.split(" ")[1], ENV.JWT_SECRET);
    if (decoded.role !== "admin")
      return res.status(403).json({ message: "Không có quyền!" });

    const users = await User.find().select("-password");
    res.json(users);

  } catch (err) {
    res.status(401).json({ message: "Token không hợp lệ!" });
  }
});


/* ============================================================
   ADMIN XOÁ TÀI KHOẢN
============================================================ */
router.delete("/users/:id", async (req, res) => {
  try {
    const decoded = jwt.verify(req.headers.authorization.split(" ")[1], ENV.JWT_SECRET);
    if (decoded.role !== "admin")
      return res.status(403).json({ message: "Không có quyền!" });

    const user = await User.findByIdAndDelete(req.params.id);
    if (user) await NhanVien.findOneAndDelete({ email: user.email });

    res.json({ message: "🗑 Đã xóa tài khoản!" });

  } catch (err) {
    res.status(401).json({ message: "Token không hợp lệ!" });
  }
});

module.exports = router;

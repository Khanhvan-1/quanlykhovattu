const LichSu = require("../model/LichSu");
const moment = require("moment");

// 🔥 Tạo mã phiếu mặc định
function taoMaPhieu(loai) {
  const time = moment().format("YYYYMMDD_HHmmss");
  return `${loai.toUpperCase()}_${time}`;
}

async function logHistory({
  loai,           // "nhap" | "xuat" | "system"
  maPhieu,
  tenSanPham,
  soLuong,
  gia,
  tongTien,

  // ⭐ Thông tin nhân viên thực hiện
  userName,
  maNV,
  userId,
  role,

  ghiChu,
}) {
  try {
    // Nếu không có mã phiếu → tự tạo
    if (!maPhieu) {
      const prefix = loai === "system" ? "SYSTEM" : loai?.toUpperCase() || "SYS";
      maPhieu = taoMaPhieu(prefix);
    }

    // Chuẩn hoá số
    soLuong = Number(soLuong) || 0;
    gia = Number(gia) || 0;
    tongTien = Number(tongTien) || soLuong * gia;

    // ⭐ Gán mặc định nếu thiếu
    if (!userName) userName = "Hệ thống";
    if (!maNV) maNV = "SYSTEM";
    if (!role) role = "system";
    if (!userId) userId = null;

    // ⭐ Ghi vào DB
    await LichSu.create({
      loai,
      maPhieu,
      tenSanPham,
      soLuong,
      gia,
      tongTien,

      userName,
      maNV,
      userId,
      role,

      ghiChu,
    });

  } catch (err) {
    console.error("⚠️ Lỗi ghi lịch sử:", err.message);
  }
}

module.exports = logHistory;

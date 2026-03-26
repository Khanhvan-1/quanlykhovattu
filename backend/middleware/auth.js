const jwt = require("jsonwebtoken");
const ENV = require("../config/config");
const JWT_SECRET = ENV.JWT_SECRET;

function authMiddleware(req, res, next) {
  const authHeader = req.headers.authorization;

  if (!authHeader) {
    return res.status(401).json({ message: "Không có token" });
  }

  try {
    const token = authHeader.split(" ")[1];
    const decoded = jwt.verify(token, JWT_SECRET);

    // ⭐ THÊM maNV để dùng lọc lịch sử cá nhân
    req.user = {
      id: decoded.id,
      email: decoded.email,
      username: decoded.username,
      maNV: decoded.maNV || decoded.manv || null,   // hỗ trợ nhiều dạng viết
      role: decoded.role,
    };

    next();
  } catch (err) {
    return res.status(401).json({ message: "Token không hợp lệ" });
  }
}

module.exports = authMiddleware;

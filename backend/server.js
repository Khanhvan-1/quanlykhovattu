const express = require("express");
const mongoose = require("mongoose");
const cors = require("cors");
const ENV = require("./config/config");

const { GoogleGenerativeAI } = require("@google/generative-ai");

try {
  global.gemini = new GoogleGenerativeAI(ENV.GEMINI_API_KEY);

  global.geminiModel = global.gemini.getGenerativeModel({
    model: "gemini-1.5-flash",
  });

  console.log("🤖 Gemini AI initialized!");
} catch (err) {
  console.error("⚠️ Lỗi khởi tạo Gemini:", err.message);
}

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json({ limit: "10mb" }));
app.use(express.urlencoded({ extended: true, limit: "10mb" }));
app.use(
  cors({
    origin: "*",
    methods: ["GET", "POST", "PUT", "DELETE"],
    allowedHeaders: ["Content-Type", "Authorization"],
  })
);

// Log request để debug
app.use((req, res, next) => {
  console.log(`➡️ ${req.method} ${req.originalUrl}`);
  next();
});

mongoose
  .connect(ENV.MONGO_URI)
  .then(() => console.log("✅ MongoDB connected"))
  .catch((err) => console.error("❌ MongoDB error:", err.message));

app.use("/auth", require("./routes/authRoutes"));
app.use("/api/nhanvien", require("./routes/nhanVienRoutes"));
app.use("/api/baocao", require("./routes/baoCaoRoutes"));
app.use("/api/export", require("./routes/exportExcelRoutes"));
app.use("/api/sanpham", require("./routes/sanPhamRoutes"));
app.use("/api/nhapkho", require("./routes/nhapKhoRoutes"));
app.use("/api/xuatkho", require("./routes/xuatKhoRoutes"));
app.use("/api/lichsu", require("./routes/lichSuRoutes"));
app.use("/api/sms", require("./routes/smsRoutes"));
app.use("/api/khachhang", require("./routes/khachHangRoutes"));
app.use("/api/ncc", require("./routes/nhaCungCapRoutes"));

app.get("/", (req, res) => res.send("💡 API Kho Vật Tư hoạt động OK 🚀"));

app.use((err, req, res, next) => {
  console.error("🔥 Lỗi không xác định:", err);
  res.status(500).json({ message: "Lỗi máy chủ nội bộ!" });
});

app.listen(PORT, "0.0.0.0", () =>
  console.log(`🚀 Server đang chạy tại: http://localhost:${PORT}`)
);

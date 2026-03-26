const express = require("express");
const router = express.Router();
const twilio = require("twilio"); // tùy chọn, có thể bỏ nếu chưa có tài khoản
const ENV = require("../config/config");

// 🔹 Nếu bạn có Twilio thật
let client = null;
try {
  if (ENV.TWILIO_SID && ENV.TWILIO_TOKEN) {
    client = twilio(ENV.TWILIO_SID, ENV.TWILIO_TOKEN);
  }
} catch {
  client = null;
}

// ✅ API gửi SMS cảnh báo
router.post("/canhbao", async (req, res) => {
  const { soDienThoai, noiDung } = req.body;

  if (!soDienThoai || !noiDung)
    return res.status(400).json({ success: false, message: "Thiếu số điện thoại hoặc nội dung!" });

  try {
    // 🔸 Nếu có Twilio thật
    if (client) {
      const msg = await client.messages.create({
        body: noiDung,
        from: ENV.TWILIO_PHONE_NUMBER,
        to: soDienThoai,
      });
      return res.json({ success: true, sid: msg.sid });
    }

    // 🔹 Giả lập (khi chưa có Twilio)
    console.log(`📩 [FAKE SMS] Gửi đến ${soDienThoai}: ${noiDung}`);
    res.json({ success: true, sid: "FAKE_SMS_" + Date.now() });
  } catch (err) {
    console.error("❌ Lỗi gửi SMS:", err);
    res.status(500).json({ success: false, message: err.message });
  }
});

module.exports = router;

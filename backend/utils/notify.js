const axios = require("axios");
const ENV = require("../config/config");

async function sendLowStockAlert(tenHang, tonKho) {
  const msg =
    `📦 CẢNH BÁO TỒN KHO\n` +
    `———————————————\n` +
    `• Sản phẩm: ${tenHang}\n` +
    `• Còn lại: ${tonKho} cái\n` +
    `⚠️ Vui lòng nhập thêm để tránh hết hàng!`;

  console.log("🔔 SMS cảnh báo:", msg);

  try {
    if (ENV.SMS_API_URL && ENV.SMS_PHONE) {
      await axios.post(`${ENV.SMS_API_URL}/canhbao`, {
        soDienThoai: ENV.SMS_PHONE,
        noiDung: msg,
      });

      console.log(`📩 Đã gửi SMS cảnh báo tới ${ENV.SMS_PHONE}`);
    }
  } catch (err) {
    console.error("⚠️ Lỗi gửi SMS:", err.message);
  }
}

module.exports = { sendLowStockAlert };

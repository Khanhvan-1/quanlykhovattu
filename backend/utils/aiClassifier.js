const { GoogleGenerativeAI } = require("@google/generative-ai");
const config = require("../config/config");

async function classifyProductAI(tenSP, maHang) {
  try {
    if (!global.geminiModel) {
      console.warn("⚠️ AI chưa khởi tạo.");
      return "Khác";
    }
    if (!tenSP) return "Khác";

    const prompt = `
Phân loại sản phẩm vào đúng 1 nhóm sau:

- Máy khoan
- Máy mài
- Máy cưa
- Máy hàn
- Máy mở ốc
- Máy cắt sắt
- Máy đục
- Máy rửa xe
- Máy phun sơn
- Dụng cụ nước
- Bulong & ốc vít
- Phụ kiện
- Khác

Yêu cầu:
- Chỉ trả về đúng TÊN NHÓM (không giải thích).
- Nếu không chắc chắn → trả về "Khác".

Tên: ${tenSP}
Mã: ${maHang}
`;

    // Gọi Gemini
    const result = await Promise.race([
      global.geminiModel.generateContent(prompt),
      new Promise((_, reject) => setTimeout(() => reject("AI timeout"), 5000))
    ]);

    const text = result?.response?.text()?.trim();
    if (!text) return "Khác";

    // Làm sạch output
    return text.replace(/[^a-zA-ZÀ-ỹ0-9\s&]/g, "").trim();

  } catch (err) {
    console.error("❌ Lỗi AI:", err);
    return "Khác";
  }
}

module.exports = { classifyProductAI };

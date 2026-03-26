const { GoogleGenerativeAI } = require("@google/generative-ai");
const config = require("../config/config");

function initGemini() {
  try {
    global.gemini = new GoogleGenerativeAI(config.GEMINI_API_KEY);
    global.geminiModel = global.gemini.getGenerativeModel({
      model: "gemini-1.5-flash"
    });

    console.log("🤖 Gemini AI initialized!");
  } catch (e) {
    console.error("❌ Lỗi khởi tạo Gemini:", e.message);
  }
}

module.exports = initGemini;

module.exports = {
  JWT_SECRET: process.env.JWT_SECRET || "MY_SECRET_KEY",
  MONGO_URI: process.env.MONGO_URI || "mongodb://127.0.0.1:27017/kho",

  SMS_API_URL: "http://localhost:3000/api/sms",
  SMS_PHONE: "+84934090104",

  EMAIL_ADMIN: "nguyenhungkhanh09012004@gmail.com",

  GEMINI_API_KEY:
    "AIzaSyDXwQ2m7AlNoP7yVvzqhikflCrqtQEmtew",
};

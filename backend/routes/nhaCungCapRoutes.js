const express = require("express");
const router = express.Router();
const auth = require("../middleware/auth");
const authorizeRoles = require("../middleware/roleAuth");
const NhaCungCap = require("../model/NhaCungCap");

router.get("/", auth, async (req, res) => {
  const list = await NhaCungCap.find().sort({ tenNCC: 1 });
  res.json(list);
});


router.post("/", auth, authorizeRoles("admin", "nhap_kho"), async (req, res) => {
  const { tenNCC, soDienThoai, diaChi } = req.body;
  if (!tenNCC) return res.status(400).json({ message: "Tên nhà cung cấp bắt buộc" });

  const ncc = await NhaCungCap.create({ tenNCC, soDienThoai, diaChi });
  res.status(201).json(ncc);
});


router.put("/:id", auth, authorizeRoles("admin", "nhap_kho"), async (req, res) => {
  const updated = await NhaCungCap.findByIdAndUpdate(
    req.params.id,
    req.body,
    { new: true }
  );
  res.json(updated);
});


router.delete("/:id", auth, authorizeRoles("admin"), async (req, res) => {
  await NhaCungCap.findByIdAndDelete(req.params.id);
  res.json({ message: "Đã xoá nhà cung cấp!" });
});

module.exports = router;

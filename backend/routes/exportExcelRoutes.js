const express = require("express");
const router = express.Router();
const ExcelJS = require("exceljs");

const NhapKho = require("../model/NhapKho");
const XuatKho = require("../model/XuatKho");
const SanPham = require("../model/SanPham");
const auth = require("../middleware/auth");

function styleHeader(row) {
    row.font = { bold: true, size: 13, color: { argb: "FFFFFFFF" } };
    row.fill = { type: "pattern", pattern: "solid", fgColor: { argb: "FF4A90E2" } };
    row.alignment = { horizontal: "center", vertical: "middle" };
}

function styleRow(row, index) {
    row.fill = {
        type: "pattern",
        pattern: "solid",
        fgColor: { argb: index % 2 === 0 ? "FFF9F9F9" : "FFFFFFFF" }
    };
}

function setBorder(row) {
    row.eachCell((cell) => {
        cell.border = {
            top: { style: "thin" },
            left: { style: "thin" },
            bottom: { style: "thin" },
            right: { style: "thin" }
        };
    });
}

router.get("/excel", auth, async (req, res) => {
    try {

        const nhap = await NhapKho.aggregate([
            { $unwind: "$sanPham" },
            {
                $group: {
                    _id: null,
                    tongGiaTri: {
                        $sum: { $multiply: ["$sanPham.soLuong", "$sanPham.giaNhap"] }
                    }
                }
            }
        ]);

        const xuat = await XuatKho.aggregate([
            { $unwind: "$sanPham" },
            {
                $group: {
                    _id: null,
                    tongGiaTri: {
                        $sum: { $multiply: ["$sanPham.soLuong", "$sanPham.giaXuat"] }
                    }
                }
            }
        ]);

        const tongNhap = nhap[0]?.tongGiaTri || 0;
        const tongXuat = xuat[0]?.tongGiaTri || 0;
        const chenhLech = tongNhap - tongXuat;


        const listNhap = await NhapKho.aggregate([
            { $unwind: "$sanPham" },
            { $group: { _id: "$sanPham.maHang", tongNhap: { $sum: "$sanPham.soLuong" } } }
        ]);

        const listXuat = await XuatKho.aggregate([
            { $unwind: "$sanPham" },
            { $group: { _id: "$sanPham.maHang", tongXuat: { $sum: "$sanPham.soLuong" } } }
        ]);

        const dsSP = await SanPham.find({}).sort({ tenHang: 1 });
        const tonKho = dsSP.map(sp => {
            const n = listNhap.find(i => i._id === sp.maHang)?.tongNhap || 0;
            const x = listXuat.find(i => i._id === sp.maHang)?.tongXuat || 0;

            return {
                maHang: sp.maHang,
                tenHang: sp.tenHang,
                nhap: n,
                xuat: x,
                ton: Math.max(n - x, 0)
            };
        });

        const nhapNgay = await NhapKho.aggregate([
            {
                $group: {
                    _id: { $dateToString: { format: "%Y-%m-%d", date: "$ngayNhap" } },
                    tongNhap: { $sum: "$tongTien" }
                }
            },
            { $sort: { _id: 1 } }
        ]);

        const xuatNgay = await XuatKho.aggregate([
            {
                $group: {
                    _id: { $dateToString: { format: "%Y-%m-%d", date: "$ngayXuat" } },
                    tongXuat: { $sum: "$tongTien" }
                }
            },
            { $sort: { _id: 1 } }
        ]);

        const ngaySet = new Set([...nhapNgay.map(n => n._id), ...xuatNgay.map(x => x._id)]);
        const ngayList = [...ngaySet].sort();

        const theoNgay = ngayList.map(ngay => ({
            ngay,
            tongNhap: nhapNgay.find(i => i._id === ngay)?.tongNhap || 0,
            tongXuat: xuatNgay.find(i => i._id === ngay)?.tongXuat || 0,
            tonCuoi: (nhapNgay.find(i => i._id === ngay)?.tongNhap || 0) -
                     (xuatNgay.find(i => i._id === ngay)?.tongXuat || 0)
        }));

        const wb = new ExcelJS.Workbook();


        const sheet1 = wb.addWorksheet("TongHopThang");

        sheet1.columns = [
            { header: "Hạng mục", key: "label", width: 30 },
            { header: "Giá trị (VND)", key: "value", width: 20 }
        ];

        styleHeader(sheet1.getRow(1));
        setBorder(sheet1.getRow(1));

        const rows1 = [
            { label: "Tổng Nhập", value: tongNhap },
            { label: "Tổng Xuất", value: tongXuat },
            { label: "Chênh lệch tồn", value: chenhLech }
        ];

        rows1.forEach((r, i) => {
            const row = sheet1.addRow(r);
            styleRow(row, i);
            setBorder(row);
        });

        sheet1.getColumn("value").numFmt = "#,##0 ₫";


        const sheet2 = wb.addWorksheet("TonKho");

        sheet2.columns = [
            { header: "Mã hàng", key: "maHang", width: 15 },
            { header: "Tên hàng", key: "tenHang", width: 32 },
            { header: "Nhập", key: "nhap", width: 12 },
            { header: "Xuất", key: "xuat", width: 12 },
            { header: "Tồn kho", key: "ton", width: 12 }
        ];

        styleHeader(sheet2.getRow(1));
        setBorder(sheet2.getRow(1));

        tonKho.forEach((r, i) => {
            const row = sheet2.addRow(r);
            styleRow(row, i);
            setBorder(row);
        });


        const sheet3 = wb.addWorksheet("TheoNgay");

        sheet3.columns = [
            { header: "Ngày", key: "ngay", width: 15 },
            { header: "Tổng nhập", key: "tongNhap", width: 20 },
            { header: "Tổng xuất", key: "tongXuat", width: 20 },
            { header: "Tồn cuối ngày", key: "tonCuoi", width: 20 }
        ];

        styleHeader(sheet3.getRow(1));
        setBorder(sheet3.getRow(1));

        theoNgay.forEach((r, i) => {
            const row = sheet3.addRow(r);
            styleRow(row, i);
            setBorder(row);
        });

        sheet3.getColumn("tongNhap").numFmt = "#,##0 ₫";
        sheet3.getColumn("tongXuat").numFmt = "#,##0 ₫";
        sheet3.getColumn("tonCuoi").numFmt = "#,##0 ₫";


        res.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        res.setHeader("Content-Disposition", "attachment; filename=KhoVatTu.xlsx");

        await wb.xlsx.write(res);
        res.end();

    } catch (err) {
        console.log(err);
        res.status(500).json({ message: "Lỗi xuất Excel: " + err.message });
    }
});

module.exports = router;

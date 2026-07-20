package com.gov.service.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class CertificateGenerator {

    public byte[] generateCertificate(String acceptNo, String itemName, String userName, Map<String, Object> formData) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdfDoc);

            // 尝试加载中文字体
            PdfFont chineseFont = loadChineseFont();

            // 标题
            Paragraph title = new Paragraph("政务服务电子证照")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(new DeviceRgb(0, 51, 153))
                    .setMarginBottom(30);
            if (chineseFont != null) title.setFont(chineseFont);
            document.add(title);

            // 证照编号
            Paragraph licenseNo = new Paragraph("证照编号: " + acceptNo)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY);
            if (chineseFont != null) licenseNo.setFont(chineseFont);
            document.add(licenseNo);

            // 分隔线
            Paragraph line = new Paragraph("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(0, 51, 153))
                    .setMarginBottom(20);
            document.add(line);

            // 基本信息表格
            Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .useAllAvailableWidth()
                    .setMarginBottom(20);

            addTableRow(table, "事项名称", itemName, chineseFont);
            addTableRow(table, "申请人", userName, chineseFont);
            addTableRow(table, "受理号", acceptNo, chineseFont);

            // 添加表单数据
            if (formData != null) {
                for (Map.Entry<String, Object> entry : formData.entrySet()) {
                    String label = formatLabel(entry.getKey());
                    String value = entry.getValue() != null ? entry.getValue().toString() : "";
                    addTableRow(table, label, value, chineseFont);
                }
            }

            document.add(table);

            // 审批结论
            Paragraph conclusion = new Paragraph("审批结论: 予以批准")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(new DeviceRgb(0, 128, 0))
                    .setMarginTop(20)
                    .setMarginBottom(30);
            if (chineseFont != null) conclusion.setFont(chineseFont);
            document.add(conclusion);

            // 发证信息
            String issueDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
            String expiryDate = LocalDateTime.now().plusYears(5).format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));

            Paragraph issueInfo = new Paragraph()
                    .add("发证日期: " + issueDate + "\n")
                    .add("有效期至: " + expiryDate + "\n")
                    .add("发证机关: 政务服务中心")
                    .setFontSize(11);
            if (chineseFont != null) issueInfo.setFont(chineseFont);
            document.add(issueInfo);

            // 底部印章区域
            Paragraph seal = new Paragraph("\n\n（电子印章）")
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12)
                    .setFontColor(new DeviceRgb(200, 0, 0))
                    .setMarginTop(30);
            if (chineseFont != null) seal.setFont(chineseFont);
            document.add(seal);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("证照生成失败: " + e.getMessage(), e);
        }
    }

    private PdfFont loadChineseFont() {
        try {
            // 尝试加载系统中文字体
            String[] fontPaths = {
                    "C:/Windows/Fonts/simhei.ttf",  // 黑体
                    "C:/Windows/Fonts/simsun.ttc",  // 宋体
                    "C:/Windows/Fonts/msyh.ttc",    // 微软雅黑
                    "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
                    "/System/Library/Fonts/PingFang.ttc"
            };
            for (String path : fontPaths) {
                try {
                    return PdfFontFactory.createFont(path, PdfEncodings.IDENTITY_H);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void addTableRow(Table table, String label, String value, PdfFont chineseFont) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold().setFontSize(11))
                .setBackgroundColor(new DeviceRgb(240, 240, 240))
                .setPadding(8);
        if (chineseFont != null) labelCell.setFont(chineseFont);

        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFontSize(11))
                .setPadding(8);
        if (chineseFont != null) valueCell.setFont(chineseFont);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String formatLabel(String key) {
        // 将英文字段名转换为中文标签
        Map<String, String> labelMap = Map.of(
                "phone", "联系电话",
                "address", "联系地址",
                "reason", "申请事由",
                "name", "姓名",
                "idCard", "身份证号",
                "email", "电子邮箱"
        );
        return labelMap.getOrDefault(key, key);
    }
}

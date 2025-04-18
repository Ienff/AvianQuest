package com.example.avianquest;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfExporter {
    public static boolean exportToPdf(OutputStream outputStream, List<SamplePoint> samplePoints) {
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 设置中文字体
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            document.setFont(font);

            // Add title
            Paragraph title = new Paragraph("鸟类样线调查记录表")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // 添加表头信息
            String[] headerInfo = {
                "网格编号：____  省____市（州）县____乡（镇）村（小地名）",
                "地点：____  经纬度：起点：E____ N____  终点：E____ N____",
                "海拔幅度：____m ~ ____m  植被类型：____",
                "坡向：____  坡度：____  坡位：____",
                "日期：____  起止时间：____时____分 至____时____分",
                "天气：____  样线长：____m",
                "调查者：____  表格编号：____"
            };

            for (String info : headerInfo) {
                document.add(new Paragraph(info)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.LEFT));
            }

            document.add(new Paragraph("\n"));

            // 创建表格
            float[] columnWidths = {60, 80, 80, 40, 40, 60, 60, 60, 100};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setFont(font);

            // 添加表头
            String[] headers = {"时间", "坐标", "鸟种", "性别", "数量",
                              "生境", "观察距离", "状态", "备注"};
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header));
                cell.setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
            }

            // 添加数据行
            for (SamplePoint point : samplePoints) {
                table.addCell(new Cell().add(new Paragraph(point.getTime())));
                table.addCell(new Cell().add(new Paragraph(String.format(Locale.getDefault(),
                    "%.4f, %.4f", point.getPosition().latitude, point.getPosition().longitude))));
                table.addCell(new Cell().add(new Paragraph(point.getBirdSpecies() != null ?
                    point.getBirdSpecies() : "")));
                table.addCell(new Cell().add(new Paragraph(point.getGender() != null ?
                    point.getGender() : "")));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(point.getQuantity()))));
                table.addCell(new Cell().add(new Paragraph(point.getHabitatType() != null ?
                    point.getHabitatType() : "")));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(point.getDistanceToLine()))));
                table.addCell(new Cell().add(new Paragraph(point.getStatus() != null ?
                    point.getStatus() : "")));
                table.addCell(new Cell().add(new Paragraph(point.getRemarks() != null ?
                    point.getRemarks() : "")));
            }

            document.add(table);
            document.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
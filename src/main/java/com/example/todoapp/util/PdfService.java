package com.example.todoapp.util;

import com.example.examplefeature.Task;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PdfService {

    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    .withLocale(Locale.getDefault());
    
    private static final DateTimeFormatter DATETIME_FORMATTER = 
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .withLocale(Locale.getDefault())
                    .withZone(ZoneId.systemDefault());

    /**
     * Gera um PDF com a lista de tarefas
     * @param tasks Lista de tarefas a incluir no PDF
     * @return ByteArrayOutputStream contendo o PDF gerado
     */
    public ByteArrayOutputStream generateTaskListPdf(List<Task> tasks) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // Criar o documento PDF
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Adicionar título
            Paragraph title = new Paragraph("Lista de Tarefas")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Adicionar espaço
            document.add(new Paragraph("\n"));

            // Criar tabela com 3 colunas
            Table table = new Table(UnitValue.createPercentArray(new float[]{40, 20, 30}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Adicionar cabeçalhos
            table.addHeaderCell(new Cell().add(new Paragraph("Descrição").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Data Limite").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Data de Criação").setBold()));

            // Adicionar tarefas
            for (Task task : tasks) {
                table.addCell(new Cell().add(new Paragraph(task.getDescription())));

                String dueDateStr = Optional.ofNullable(task.getDueDate())
                        .map(DATE_FORMATTER::format)
                        .orElse("Sem limite");
                table.addCell(new Cell().add(new Paragraph(dueDateStr)));

                String creationDateStr = DATETIME_FORMATTER.format(task.getCreationDate());
                table.addCell(new Cell().add(new Paragraph(creationDateStr)));
            }

            document.add(table);

            // Adicionar rodapé
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Total de tarefas: " + tasks.size())
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setItalic());

            // Fechar documento
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }

        return baos;
    }
}

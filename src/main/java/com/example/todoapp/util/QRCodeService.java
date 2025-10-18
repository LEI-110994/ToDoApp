package com.example.todoapp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QRCodeService {

    public void generateQRCode(String text, String filePath, int width, int height)
            throws WriterException, IOException {
        // Validação dos parâmetros
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("O texto para o QR code não pode ser vazio ou nulo.");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("O caminho do arquivo não pode ser vazio ou nulo.");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("A largura e altura devem ser valores positivos.");
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            System.out.println("QR code gerado com sucesso no caminho: " + filePath);
        } catch (WriterException e) {
            System.err.println("Erro ao gerar o QR code: " + e.getMessage());
            throw e;
        } catch (IOException e) {
            System.err.println("Erro ao salvar o QR code no arquivo: " + e.getMessage());
            throw e;
        }
    }

    public byte[] generateQRCodeToByteArray(String text, int width, int height)
            throws WriterException, IOException {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("O texto para o QR code não pode ser vazio ou nulo.");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("A largura e altura devem ser valores positivos.");
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

}

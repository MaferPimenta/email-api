package com.email.Services;

import com.email.Models.Mail;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AttachmentService {
    private String fileSource = "temp" + File.separator;

    public void convert(String fileName, String encodedBytes) {
        try {
            byte[] decodedBytes = Base64.decodeBase64(encodedBytes);
            Files.write(Paths.get(fileSource + fileName), decodedBytes);

        } catch (IOException e) {
            String developerMessage = e.getMessage() + " - Falha ao acessar a pasta " + fileSource + fileName;
            System.out.println(developerMessage);
        }
    }

    public void deleteFile(String filename) throws IOException {
        Path path = Paths.get(fileSource + filename);
        Files.delete(path);
    }

    public String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

}

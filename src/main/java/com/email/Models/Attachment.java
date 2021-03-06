package com.email.Models;

import org.springframework.stereotype.Component;

@Component
public class Attachment {
    private String base64;
    private String filename;

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

}

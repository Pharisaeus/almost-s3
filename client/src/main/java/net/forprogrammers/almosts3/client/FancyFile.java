package net.forprogrammers.almosts3.client;

import java.io.InputStream;

public class FancyFile {
    private final InputStream contentStream;

    public FancyFile(InputStream contentStream) {
        this.contentStream = contentStream;
    }

    public InputStream getContentStream() {
        return contentStream;
    }
}

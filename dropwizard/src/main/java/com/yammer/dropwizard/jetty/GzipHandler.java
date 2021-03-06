package com.yammer.dropwizard.jetty;

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

// TODO: 10/12/11 <coda> -- write tests for GzipHandler
// TODO: 10/12/11 <coda> -- write docs for GzipHandler

public class GzipHandler extends org.eclipse.jetty.server.handler.GzipHandler {
    private static final String GZIP_ENCODING = "gzip";

    private static class GzipServletInputStream extends ServletInputStream {
        private final GZIPInputStream input;

        private GzipServletInputStream(HttpServletRequest request, int bufferSize) throws IOException {
            this.input = new GZIPInputStream(request.getInputStream(), bufferSize);
        }

        @Override
        public void close() throws IOException {
            input.close();
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            return input.read(buf, off, len);
        }

        @Override
        public int available() throws IOException {
            return input.available();
        }

        @Override
        public void mark(int limit) {
            input.mark(limit);
        }

        @Override
        public boolean markSupported() {
            return input.markSupported();
        }

        @Override
        public int read() throws IOException {
            return input.read();
        }

        @Override
        public void reset() throws IOException {
            input.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return input.skip(n);
        }

        @Override
        public int read(byte[] b) throws IOException {
            return input.read(b);
        }
    }

    private static class GzipServletRequest extends HttpServletRequestWrapper {
        private final ServletInputStream input;
        private final BufferedReader reader;

        private GzipServletRequest(HttpServletRequest request, int bufferSize) throws IOException {
            super(request);
            this.input = new GzipServletInputStream(request, bufferSize);
            this.reader = new BufferedReader(new InputStreamReader(input));
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return input;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return reader;
        }
    }

    public GzipHandler(Handler underlying) {
        setHandler(underlying);
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        if (GZIP_ENCODING.equalsIgnoreCase(request.getHeader(HttpHeaders.CONTENT_ENCODING))) {
            super.handle(target,
                         baseRequest,
                         new GzipServletRequest(request, super.getBufferSize()),
                         response);
        } else {
            super.handle(target, baseRequest, request, response);
        }
    }
}

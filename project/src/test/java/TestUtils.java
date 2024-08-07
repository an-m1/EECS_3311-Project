package ca.yorku.eecs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

public class TestUtils {

    public static HttpExchange createMockHttpExchange(String method, String uri, String requestBody) throws IOException {
        return new HttpExchange() {
            private final InputStream requestBodyStream = new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8));
            private final ByteArrayOutputStream responseBodyStream = new ByteArrayOutputStream();

            @Override
            public Headers getRequestHeaders() {
                return new Headers();
            }

            @Override
            public Headers getResponseHeaders() {
                return new Headers();
            }

            @Override
            public URI getRequestURI() {
                return URI.create(uri);
            }

            @Override
            public String getRequestMethod() {
                return method;
            }

            @Override
            public HttpContext getHttpContext() {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public InputStream getRequestBody() {
                return requestBodyStream;
            }

            @Override
            public OutputStream getResponseBody() {
                return responseBodyStream;
            }

            @Override
            public InetSocketAddress getRemoteAddress() {
                return null;
            }

            @Override
            public InetSocketAddress getLocalAddress() {
                return null;
            }

            @Override
            public String getProtocol() {
                return null;
            }

            @Override
            public SSLSession getSSLSession() {
                return null;
            }

            @Override
            public Object getAttribute(String s) {
                return null;
            }

            @Override
            public void setAttribute(String s, Object o) {
            }

            @Override
            public void setStreams(InputStream inputStream, OutputStream outputStream) {
            }

            @Override
            public HttpPrincipal getPrincipal() {
                return null;
            }
        };
    }

    public static String getResponseBody(HttpExchange exchange) throws IOException {
        return exchange.getResponseBody().toString();
    }
}

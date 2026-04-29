package dev.sayaya.handbook.client.usecase;

public interface SessionEnvironment {
    String getCookies();
    String decodeBase64(String encoded);
    Object parseJson(String json);
    void redirect(String path);
    void clearInterval(double handle);
}

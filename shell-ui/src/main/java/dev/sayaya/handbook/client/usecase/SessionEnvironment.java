package dev.sayaya.handbook.client.usecase;

public interface SessionEnvironment {
    String getCookies();
    Double getJwtClaimAsDouble(String token, String claim);
    void redirect(String path);
    void clearInterval(double handle);
}


package br.ufpr.sept.so2.modules.iam.application;

public interface IamSettings {

    long accessTtlSeconds();

    long refreshTtlSeconds();

    long resetTtlSeconds();

    int maxFalhasConsecutivas();

    int minutosBloqueio();

    String frontendBaseUrl();
}

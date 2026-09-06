package br.ufpr.sept.so2.modules.iam.application.ports;

public interface OutboxPort {

    void enqueue(String tipo, String payload);
}

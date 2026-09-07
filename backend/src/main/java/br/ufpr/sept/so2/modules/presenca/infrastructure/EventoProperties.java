package br.ufpr.sept.so2.modules.presenca.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.eventos")
public class EventoProperties {

    private String devPin = "123456";

    public String getDevPin() {
        return devPin;
    }

    public void setDevPin(String devPin) {
        this.devPin = devPin;
    }
}

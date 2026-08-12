package com.taller.backend.dto;

import java.util.List;

public class CreateFacturacionLoteRequest {

    private String discordId;
    private List<CreateFacturaRequest> elementos;

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public List<CreateFacturaRequest> getElementos() {
        return elementos;
    }

    public void setElementos(List<CreateFacturaRequest> elementos) {
        this.elementos = elementos;
    }
}

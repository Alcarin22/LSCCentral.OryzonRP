package com.taller.backend.dto;

import java.util.List;

public class CreateFacturacionLoteRequest {

    private List<CreateFacturaRequest> elementos;

    public List<CreateFacturaRequest> getElementos() {
        return elementos;
    }

    public void setElementos(
            List<CreateFacturaRequest> elementos
    ) {
        this.elementos = elementos;
    }
}
package com.taller.backend.dto;

import java.util.List;

public class CreateFacturacionLoteResponse {

    private Integer totalFacturas;
    private Long totalGeneral;
    private List<FacturaListadoResponse> facturas;

    public Integer getTotalFacturas() {
        return totalFacturas;
    }

    public void setTotalFacturas(Integer totalFacturas) {
        this.totalFacturas = totalFacturas;
    }

    public Long getTotalGeneral() {
        return totalGeneral;
    }

    public void setTotalGeneral(Long totalGeneral) {
        this.totalGeneral = totalGeneral;
    }

    public List<FacturaListadoResponse> getFacturas() {
        return facturas;
    }

    public void setFacturas(List<FacturaListadoResponse> facturas) {
        this.facturas = facturas;
    }
}

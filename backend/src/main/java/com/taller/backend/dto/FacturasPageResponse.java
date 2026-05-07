package com.taller.backend.dto;

import java.util.List;

public class FacturasPageResponse {

    private List<FacturaListadoResponse> content;

    private long totalElements;

    private int totalPages;

    private int page;

    private int size;

    private long totalFacturado;

    private long promedioFactura;

    public FacturasPageResponse() {
    }

    public List<FacturaListadoResponse> getContent() {
        return content;
    }

    public void setContent(List<FacturaListadoResponse> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalFacturado() {
        return totalFacturado;
    }

    public void setTotalFacturado(long totalFacturado) {
        this.totalFacturado = totalFacturado;
    }

    public long getPromedioFactura() {
        return promedioFactura;
    }

    public void setPromedioFactura(long promedioFactura) {
        this.promedioFactura = promedioFactura;
    }
}
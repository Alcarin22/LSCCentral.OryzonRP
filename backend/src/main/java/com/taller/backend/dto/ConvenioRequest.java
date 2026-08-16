package com.taller.backend.dto;

public class ConvenioRequest {

    private String local;
    private String categoria;
    private String estado;
    private String condicionesLsc;
    private String condicionesLocal;

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCondicionesLsc() {
        return condicionesLsc;
    }

    public void setCondicionesLsc(String condicionesLsc) {
        this.condicionesLsc = condicionesLsc;
    }

    public String getCondicionesLocal() {
        return condicionesLocal;
    }

    public void setCondicionesLocal(String condicionesLocal) {
        this.condicionesLocal = condicionesLocal;
    }
}

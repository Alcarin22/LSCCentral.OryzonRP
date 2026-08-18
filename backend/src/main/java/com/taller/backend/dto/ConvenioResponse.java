package com.taller.backend.dto;

public class ConvenioResponse {

    private Long id;
    private String local;
    private String categoria;
    private String estado;
    private String condicionesLsc;
    private String condicionesLocal;
    private Boolean tieneArchivo;
    private String archivoNombre;
    private String archivoTipoMime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCondicionesLsc() { return condicionesLsc; }
    public void setCondicionesLsc(String condicionesLsc) { this.condicionesLsc = condicionesLsc; }

    public String getCondicionesLocal() { return condicionesLocal; }
    public void setCondicionesLocal(String condicionesLocal) { this.condicionesLocal = condicionesLocal; }

    public Boolean getTieneArchivo() { return tieneArchivo; }
    public void setTieneArchivo(Boolean tieneArchivo) { this.tieneArchivo = tieneArchivo; }

    public String getArchivoNombre() { return archivoNombre; }
    public void setArchivoNombre(String archivoNombre) { this.archivoNombre = archivoNombre; }

    public String getArchivoTipoMime() { return archivoTipoMime; }
    public void setArchivoTipoMime(String archivoTipoMime) { this.archivoTipoMime = archivoTipoMime; }
}

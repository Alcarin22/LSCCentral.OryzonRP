package com.taller.backend.dto;

import java.util.List;

public class ConvenioResponse {

    private Long id;
    private String nombre;
    private String categoria;
    private String estado;
    private String descuento;
    private String contacto;
    private String descripcion;
    private String documentoUrl;
    private List<String> condiciones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getDescuento() {
        return descuento;
    }

    public void setDescuento(String descuento) {
        this.descuento = descuento;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDocumentoUrl() {
        return documentoUrl;
    }

    public void setDocumentoUrl(String documentoUrl) {
        this.documentoUrl = documentoUrl;
    }

    public List<String> getCondiciones() {
        return condiciones;
    }

    public void setCondiciones(List<String> condiciones) {
        this.condiciones = condiciones;
    }
}
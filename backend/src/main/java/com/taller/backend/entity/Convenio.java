package com.taller.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "convenios")
public class Convenio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String local;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "condiciones_lsc", columnDefinition = "TEXT")
    private String condicionesLsc;

    @Column(name = "condiciones_local", columnDefinition = "TEXT")
    private String condicionesLocal;

    @Column(name = "archivo_nombre", length = 255)
    private String archivoNombre;

    @Column(name = "archivo_tipo_mime", length = 150)
    private String archivoTipoMime;

    @Column(name = "archivo_ruta", length = 500)
    private String archivoRuta;

    public Long getId() { return id; }

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

    public String getArchivoNombre() { return archivoNombre; }
    public void setArchivoNombre(String archivoNombre) { this.archivoNombre = archivoNombre; }

    public String getArchivoTipoMime() { return archivoTipoMime; }
    public void setArchivoTipoMime(String archivoTipoMime) { this.archivoTipoMime = archivoTipoMime; }

    public String getArchivoRuta() { return archivoRuta; }
    public void setArchivoRuta(String archivoRuta) { this.archivoRuta = archivoRuta; }
}

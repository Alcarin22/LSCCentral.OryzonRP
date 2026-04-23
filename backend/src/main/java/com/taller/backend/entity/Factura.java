package com.taller.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_empleado", nullable = false)
    private Long idEmpleado;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column
    private String matricula;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private Integer total;

    @Column(nullable = false)
    private Boolean convenio;

    @Column
    private String modelo;

    @Column
    private String estado;

    @Column
    private Integer cantidad;

    @Column
    private String item;

    @Column
    private String categoria;

    @Column
    private String gravedad;

    @Column(name = "tuneo_plate")
    private String tuneoPlate;

    @Column(name = "tuneo_seleccionados", length = 1000)
    private String tuneoSeleccionados;

    @Column(nullable = false)
    private Boolean grua = false;

    public Long getId() {
        return id;
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Boolean getConvenio() {
        return convenio;
    }

    public void setConvenio(Boolean convenio) {
        this.convenio = convenio;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getGravedad() {
        return gravedad;
    }

    public void setGravedad(String gravedad) {
        this.gravedad = gravedad;
    }

    public String getTuneoPlate() {
        return tuneoPlate;
    }

    public void setTuneoPlate(String tuneoPlate) {
        this.tuneoPlate = tuneoPlate;
    }

    public String getTuneoSeleccionados() {
        return tuneoSeleccionados;
    }

    public void setTuneoSeleccionados(String tuneoSeleccionados) {
        this.tuneoSeleccionados = tuneoSeleccionados;
    }

    public Boolean getGrua() {
        return grua;
    }

    public void setGrua(Boolean grua) {
        this.grua = grua;
    }
}
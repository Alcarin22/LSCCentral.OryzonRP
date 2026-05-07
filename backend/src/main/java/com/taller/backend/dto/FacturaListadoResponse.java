package com.taller.backend.dto;

public class FacturaListadoResponse {

    private Long id;
    private Long idEmpleado;
    private String nombreEmpleado;

    private String fecha;
    private String tipo;

    private Integer total;

    private Boolean convenio;

    private String matricula;
    private String modelo;
    private String estado;

    private Integer cantidad;
    private String item;

    private String categoria;
    private String gravedad;

    private String tuneoPlate;
    private String tuneoSeleccionados;

    private Boolean grua;

    public FacturaListadoResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
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

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
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
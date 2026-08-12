package com.taller.backend.dto;

public class FacturaItemResponse {

    private Long id;
    private String item;
    private Integer cantidad;
    private Integer precioUnitario;
    private Integer subtotal;
    private Boolean lspd;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Integer precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Integer getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Integer subtotal) {
        this.subtotal = subtotal;
    }

    public Boolean getLspd() {
        return lspd;
    }

    public void setLspd(Boolean lspd) {
        this.lspd = lspd;
    }
}

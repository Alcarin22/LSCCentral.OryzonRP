package com.taller.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(
    name = "primas",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_primas_empleado_semana", columnNames = {"empleado_id", "semana"})
    }
)
public class Prima {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "semana", nullable = false)
    private Integer semana;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "facturado", nullable = false, precision = 12, scale = 2)
    private BigDecimal facturado = BigDecimal.ZERO;

    @Column(name = "horas", nullable = false, precision = 8, scale = 2)
    private BigDecimal horas = BigDecimal.ZERO;

    @Column(name = "servicios", nullable = false)
    private Integer servicios = 0;

    @Column(name = "porcentaje_aplicado", nullable = false)
    private Integer porcentajeAplicado = 0;

    @Column(name = "prima_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal primaBase = BigDecimal.ZERO;

    @Column(name = "extra_horas", nullable = false, precision = 12, scale = 2)
    private BigDecimal extraHoras = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "pagada", nullable = false)
    private Boolean pagada = false;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    public Long getId() {
        return id;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Integer getSemana() {
        return semana;
    }

    public void setSemana(Integer semana) {
        this.semana = semana;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getFacturado() {
        return facturado;
    }

    public void setFacturado(BigDecimal facturado) {
        this.facturado = facturado;
    }

    public BigDecimal getHoras() {
        return horas;
    }

    public void setHoras(BigDecimal horas) {
        this.horas = horas;
    }

    public Integer getServicios() {
        return servicios;
    }

    public void setServicios(Integer servicios) {
        this.servicios = servicios;
    }

    public Integer getPorcentajeAplicado() {
        return porcentajeAplicado;
    }

    public void setPorcentajeAplicado(Integer porcentajeAplicado) {
        this.porcentajeAplicado = porcentajeAplicado;
    }

    public BigDecimal getPrimaBase() {
        return primaBase;
    }

    public void setPrimaBase(BigDecimal primaBase) {
        this.primaBase = primaBase;
    }

    public BigDecimal getExtraHoras() {
        return extraHoras;
    }

    public void setExtraHoras(BigDecimal extraHoras) {
        this.extraHoras = extraHoras;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Boolean getPagada() {
        return pagada;
    }

    public void setPagada(Boolean pagada) {
        this.pagada = pagada;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}
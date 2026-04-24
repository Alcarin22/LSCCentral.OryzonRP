package com.taller.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tuneo")
public class Tuneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String pieza;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    public Long getId() {
        return id;
    }

    public String getPieza() {
        return pieza;
    }

    public void setPieza(String pieza) {
        this.pieza = pieza;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}
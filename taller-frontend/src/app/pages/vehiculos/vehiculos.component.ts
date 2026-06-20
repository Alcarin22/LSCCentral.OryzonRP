import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  Vehiculo,
  VehiculosService
} from '../../core/services/vehiculos.service';

@Component({
  selector: 'app-vehiculos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: 'vehiculos.component.html',
  styleUrls: ['vehiculos.component.css']
})
export class VehiculosComponent implements OnInit {
  vehiculos: Vehiculo[] = [];

  cargando = false;
  error = '';

  busqueda = '';
  marcaSeleccionada = '';
  categoriaSeleccionada = '';
  ordenSeleccionado = 'marca';

  vehiculoSeleccionado: Vehiculo | null = null;

  constructor(
    private vehiculosService: VehiculosService
  ) {}

  ngOnInit(): void {
    this.cargarVehiculos();
  }

  cargarVehiculos(): void {
    this.cargando = true;
    this.error = '';

    this.vehiculosService.listarVehiculos().subscribe({
      next: (data) => {
        this.vehiculos = data ?? [];
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error cargando vehículos:', error);
        this.error = 'No se pudieron cargar los vehículos.';
        this.cargando = false;
      }
    });
  }

  get marcasDisponibles(): string[] {
    return [...new Set(
      this.vehiculos
        .map(v => v.marca)
        .filter(Boolean)
    )].sort((a, b) => a.localeCompare(b));
  }

  get categoriasDisponibles(): string[] {
    return [...new Set(
      this.vehiculos
        .map(v => v.categoria)
        .filter(Boolean)
    )].sort((a, b) => a.localeCompare(b));
  }

  get vehiculosFiltrados(): Vehiculo[] {
    const texto = this.normalizar(this.busqueda);

    let resultado = this.vehiculos.filter(vehiculo => {
      const coincideBusqueda =
        !texto ||
        this.normalizar(vehiculo.marca).includes(texto) ||
        this.normalizar(vehiculo.modelo).includes(texto) ||
        this.normalizar(`${vehiculo.marca} ${vehiculo.modelo}`).includes(texto);

      const coincideMarca =
        !this.marcaSeleccionada ||
        vehiculo.marca === this.marcaSeleccionada;

      const coincideCategoria =
        !this.categoriaSeleccionada ||
        vehiculo.categoria === this.categoriaSeleccionada;

      return coincideBusqueda && coincideMarca && coincideCategoria;
    });

    resultado = [...resultado].sort((a, b) => {
      switch (this.ordenSeleccionado) {
        case 'precio-asc':
          return a.precio - b.precio;

        case 'precio-desc':
          return b.precio - a.precio;

        case 'modelo':
          return a.modelo.localeCompare(b.modelo);

        case 'categoria':
          return a.categoria.localeCompare(b.categoria);

        case 'marca':
        default: {
          const marcaCompare = a.marca.localeCompare(b.marca);

          if (marcaCompare !== 0) {
            return marcaCompare;
          }

          return a.modelo.localeCompare(b.modelo);
        }
      }
    });

    return resultado;
  }

  limpiarFiltros(): void {
    this.busqueda = '';
    this.marcaSeleccionada = '';
    this.categoriaSeleccionada = '';
    this.ordenSeleccionado = 'marca';
  }

  abrirDetalle(vehiculo: Vehiculo): void {
    this.vehiculoSeleccionado = vehiculo;
  }

  cerrarDetalle(): void {
    this.vehiculoSeleccionado = null;
  }

  ocultarImagenRota(vehiculo: Vehiculo): void {
    vehiculo.imagenUrl = null;
  }

  private normalizar(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toLowerCase();
  }
}
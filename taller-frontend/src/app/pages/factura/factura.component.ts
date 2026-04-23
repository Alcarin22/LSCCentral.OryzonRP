import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  SessionEmpleado,
  SessionService
} from '../../core/services/session.service';

import {
  FacturaService,
  CreateFacturaRequest,
  ReparacionDto,
  ItemDto,
  TasacionPrecioDto
} from '../../../app/services/factura.service';

@Component({
  selector: 'app-factura',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './factura.component.html',
  styleUrls: ['./factura.component.css']
})
export class FacturaComponent implements OnInit {
  empleado: SessionEmpleado | null = null;

  tipoSeleccionado = '';
  total = 0;

  matricula = '';
  modelo = '';
  estado = 'SERIE';
  convenio = false;
  cantidad = 1;
  item = '';
  otros = '';

  categoria = 'Compacto';
  tuneoPlate = '';
  gravedad = '';
  grua = false;

  tuneoItems: string[] = [
    'Parte estética',
    'Pintura',
    'Vinilo',
    'Pintura de ruedas'
  ];

  tuneoSeleccionados: string[] = [];
  reparaciones: ReparacionDto[] = [];
  itemsDisponibles: ItemDto[] = [];
  tasacionPrecios: TasacionPrecioDto[] = [];

  enviando = false;
  cargandoReparaciones = false;
  cargandoItems = false;
  cargandoTasacionPrecios = false;

  constructor(
    private sessionService: SessionService,
    private facturaService: FacturaService
  ) {}

  ngOnInit(): void {
    this.empleado = this.sessionService.getEmpleado();
    this.cargarReparaciones();
    this.cargarItems();
    this.cargarTasacionPrecios();
    this.actualizarTotal();
  }

  cargarReparaciones(): void {
    this.cargandoReparaciones = true;

    this.facturaService.getReparaciones().subscribe({
      next: (data) => {
        this.reparaciones = data ?? [];
        this.cargandoReparaciones = false;
        this.actualizarTotal();
      },
      error: (error) => {
        console.error('Error cargando reparaciones:', error);
        this.cargandoReparaciones = false;
      }
    });
  }

  cargarItems(): void {
    this.cargandoItems = true;

    this.facturaService.getItems().subscribe({
      next: (data) => {
        this.itemsDisponibles = data ?? [];
        this.cargandoItems = false;
        this.actualizarTotal();
      },
      error: (error) => {
        console.error('Error cargando items:', error);
        this.cargandoItems = false;
      }
    });
  }

  cargarTasacionPrecios(): void {
    this.cargandoTasacionPrecios = true;

    this.facturaService.getTasacionPrecios().subscribe({
      next: (data) => {
        this.tasacionPrecios = data ?? [];
        this.cargandoTasacionPrecios = false;
        this.actualizarTotal();
      },
      error: (error) => {
        console.error('Error cargando precios de tasación:', error);
        this.cargandoTasacionPrecios = false;
      }
    });
  }

  onTipoFacturaChange(): void {
    this.total = 0;

    if (this.tipoSeleccionado !== 'Reparación') {
      this.gravedad = '';
      this.grua = false;
    }

    if (this.tipoSeleccionado !== 'Items') {
      this.item = '';
      this.cantidad = 1;
    }

    if (this.tipoSeleccionado !== 'Tasación') {
      this.estado = 'SERIE';
      this.otros = '';
    }

    if (this.tipoSeleccionado === 'Tasación') {
      this.convenio = false;
    }

    this.actualizarTotal();
  }

  isTuneoSelected(item: string): boolean {
    return this.tuneoSeleccionados.includes(item);
  }

  toggleTuneoItem(item: string, checked: boolean): void {
    if (checked) {
      if (!this.tuneoSeleccionados.includes(item)) {
        this.tuneoSeleccionados.push(item);
      }
    } else {
      this.tuneoSeleccionados = this.tuneoSeleccionados.filter(i => i !== item);
    }

    this.actualizarTotal();
  }

  actualizarTotal(): void {
    let base = 0;

    switch (this.tipoSeleccionado) {
      case 'Reparación': {
        const reparacionSeleccionada = this.reparaciones.find(
          r => this.normalizarClave(r.tipo) === this.normalizarClave(this.gravedad)
        );

        base = reparacionSeleccionada?.precio ?? 0;

        if (this.grua) {
          base += 600;
        }
        break;
      }

      case 'Items': {
        const itemSeleccionado = this.itemsDisponibles.find(
          i => this.normalizarClave(i.nombre) === this.normalizarClave(this.item)
        );

        base = (itemSeleccionado?.precio ?? 0) * (this.cantidad || 1);
        break;
      }

      case 'Tasación': {
        const precioSeleccionado = this.tasacionPrecios.find(
          t => this.normalizarClave(t.estado) === this.normalizarClave(this.estado)
        );

        base = precioSeleccionado?.precio ?? 0;
        break;
      }

      case 'Tuneo':
        base = this.tuneoSeleccionados.length * 800;
        if (!base) {
          base = 800;
        }
        break;

      case 'Full Tuning':
        switch (this.categoria) {
          case 'Sedán':
            base = 6500;
            break;
          case 'SUV':
            base = 8000;
            break;
          case 'Deportivo':
            base = 12000;
            break;
          case 'Super':
            base = 18000;
            break;
          case 'Moto':
            base = 5000;
            break;
          default:
            base = 5500;
        }
        break;

      default:
        base = 0;
    }

    if (this.convenio && this.tipoSeleccionado !== 'Tasación') {
      base = Math.round(base * 0.8);
    }

    this.total = base;
  }

  enviarFactura(): void {
    if (!this.empleado?.discordId) {
      alert('No hay sesión de empleado activa.');
      return;
    }

    if (!this.tipoSeleccionado) {
      alert('Debes seleccionar un tipo de factura.');
      return;
    }

    if (this.tipoSeleccionado === 'Reparación' && !this.gravedad) {
      alert('Debes seleccionar un tipo de reparación.');
      return;
    }

    if (this.tipoSeleccionado === 'Items' && !this.item) {
      alert('Debes seleccionar un item.');
      return;
    }

    if (this.tipoSeleccionado === 'Tasación') {
      if (!this.estado) {
        alert('Debes seleccionar un estado para la tasación.');
        return;
      }

      if (!this.modelo.trim()) {
        alert('Debes indicar el modelo del vehículo en la tasación.');
        return;
      }
    }

    if (this.enviando) {
      return;
    }

    const payload: CreateFacturaRequest = {
      discordId: this.empleado.discordId,
      matricula: this.obtenerMatriculaParaBackend(),
      tipo: this.tipoSeleccionado,
      total: this.total,
      convenio: this.tipoSeleccionado === 'Tasación' ? false : this.convenio,
      modelo: this.tipoSeleccionado === 'Reparación' || this.tipoSeleccionado === 'Items'
        ? null
        : this.normalizarTexto(this.modelo),
      estado: this.normalizarTexto(this.estado),
      cantidad: this.tipoSeleccionado === 'Items' ? this.cantidad : null,
      item: this.tipoSeleccionado === 'Items' ? this.normalizarTexto(this.item) : null,
      categoria: this.tipoSeleccionado === 'Full Tuning' ? this.normalizarTexto(this.categoria) : null,
      gravedad: this.tipoSeleccionado === 'Reparación' ? this.normalizarTexto(this.gravedad) : null,
      tuneoPlate: this.tipoSeleccionado === 'Tuneo' ? this.normalizarTexto(this.tuneoPlate) : null,
      tuneoSeleccionados: this.tipoSeleccionado === 'Tuneo'
        ? (this.tuneoSeleccionados.length ? this.tuneoSeleccionados.join(', ') : null)
        : null,
      grua: this.tipoSeleccionado === 'Reparación' ? this.grua : false,
      otros: this.tipoSeleccionado === 'Tasación' ? this.normalizarTexto(this.otros) : null
    };

    this.enviando = true;

    this.facturaService.crearFactura(payload).subscribe({
      next: (response) => {
        console.log('Factura guardada en backend:', response);
        alert(`Factura guardada correctamente. Total final: $${response.total ?? this.total}`);
        this.resetFormulario();
        this.enviando = false;
      },
      error: (error) => {
        console.error('Error guardando factura:', error);
        alert('No se pudo guardar la factura.');
        this.enviando = false;
      }
    });
  }

  private obtenerMatriculaParaBackend(): string | null {
    if (this.tipoSeleccionado === 'Tuneo') {
      return this.normalizarTexto(this.tuneoPlate);
    }

    return this.normalizarTexto(this.matricula);
  }

  private normalizarTexto(valor: string | null | undefined): string | null {
    if (!valor) {
      return null;
    }

    const limpio = valor.trim();
    return limpio.length ? limpio : null;
  }

  private normalizarClave(valor: string | null | undefined): string {
    if (!valor) {
      return '';
    }

    return valor
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toLowerCase();
  }

  private resetFormulario(): void {
    this.tipoSeleccionado = '';
    this.total = 0;
    this.matricula = '';
    this.modelo = '';
    this.estado = 'SERIE';
    this.convenio = false;
    this.cantidad = 1;
    this.item = '';
    this.otros = '';
    this.categoria = 'Compacto';
    this.tuneoPlate = '';
    this.gravedad = '';
    this.grua = false;
    this.tuneoSeleccionados = [];
  }
}
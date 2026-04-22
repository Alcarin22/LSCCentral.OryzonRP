import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SessionEmpleado, SessionService } from '../../core/services/session.service';
import { FacturaService, CreateFacturaRequest } from '../../../app/services/factura.service';

@Component({
  selector: 'app-factura',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './factura.component.html',
  styleUrls: ['./factura.component.css']
})
export class FacturaComponent implements OnInit {
  empleado: SessionEmpleado | null = null;

  tipoSeleccionado = 'Reparación';

  total = 0;
  matricula = '';
  modelo = '';
  estado = 'SERIE';
  convenio = false;
  cantidad = 1;
  item = '';

  categoria = 'Compacto';
  tuneoPlate = '';
  gravedad = 'Básica';

  tuneoItems: string[] = [
    'Parte estética',
    'Pintura',
    'Vinilo',
    'Pintura de ruedas'
  ];

  tuneoSeleccionados: string[] = [];

  enviando = false;

  constructor(
    private sessionService: SessionService,
    private facturaService: FacturaService
  ) {}

  ngOnInit(): void {
    this.empleado = this.sessionService.getEmpleado();
    this.actualizarTotal();
  }

  onTipoFacturaChange(): void {
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
      case 'Reparación':
        // El precio real de reparación ahora se calcula en backend.
        // Aquí dejamos una vista orientativa simple.
        base = 0;
        break;

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

      case 'Items':
        base = (this.cantidad || 1) * 250;
        break;

      case 'Tasación':
        base = this.estado === 'FULL_TUNING' ? 1500 : 700;
        break;

      default:
        base = 0;
    }

    if (this.convenio) {
      base = Math.round(base * 0.8);
    }

    this.total = base;
  }

  enviarFactura(): void {
    if (!this.empleado?.discordId) {
      alert('No hay sesión de empleado activa.');
      return;
    }

    if (this.enviando) {
      return;
    }

    const payload: CreateFacturaRequest = {
      discordId: this.empleado.discordId,
      matricula: this.obtenerMatriculaParaBackend(),
      tipo: this.tipoSeleccionado,
      total: this.total,
      convenio: this.convenio,
      modelo: this.normalizarTexto(this.modelo),
      estado: this.normalizarTexto(this.estado),
      cantidad: this.tipoSeleccionado === 'Items' ? this.cantidad : null,
      item: this.tipoSeleccionado === 'Items' ? this.normalizarTexto(this.item) : null,
      categoria: this.tipoSeleccionado === 'Full Tuning' ? this.normalizarTexto(this.categoria) : null,
      gravedad: this.tipoSeleccionado === 'Reparación' ? this.normalizarTexto(this.gravedad) : null,
      tuneoPlate: this.tipoSeleccionado === 'Tuneo' ? this.normalizarTexto(this.tuneoPlate) : null,
      tuneoSeleccionados: this.tipoSeleccionado === 'Tuneo'
        ? (this.tuneoSeleccionados.length ? this.tuneoSeleccionados.join(', ') : null)
        : null
    };

    this.enviando = true;

    this.facturaService.crearFactura(payload).subscribe({
      next: (response) => {
        console.log('Factura guardada en backend:', response);
        alert(`Factura guardada correctamente. Total final: $${response.total ?? 'calculado'}`);
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

  private resetFormulario(): void {
    this.tipoSeleccionado = 'Reparación';

    this.total = 0;
    this.matricula = '';
    this.modelo = '';
    this.estado = 'SERIE';
    this.convenio = false;
    this.cantidad = 1;
    this.item = '';
    this.categoria = 'Compacto';
    this.tuneoPlate = '';
    this.gravedad = 'Básica';
    this.tuneoSeleccionados = [];

    this.actualizarTotal();
  }
}
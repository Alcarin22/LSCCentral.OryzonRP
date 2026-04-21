import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SessionEmpleado, SessionService } from '../../core/services/session.service';
import { FacturaService } from '../../../app/services/factura.service';

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
        switch (this.gravedad) {
          case 'Media':
            base = 1200;
            break;
          case 'Avanzada':
            base = 2500;
            break;
          case 'LSPD':
            base = 4000;
            break;
          default:
            base = 600;
        }
        break;

      case 'Tuneo':
        base = this.tuneoSeleccionados.length * 800;
        if (!base) base = 800;
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

    const payload = {
      discordId: this.empleado.discordId,
      matricula: this.obtenerMatriculaParaBackend(),
      tipo: this.tipoSeleccionado,
      total: this.total,
      convenio: this.convenio,
      modelo: this.modelo?.trim() || null,
      estado: this.estado || null,
      cantidad: this.cantidad || null,
      item: this.item?.trim() || null,
      categoria: this.categoria || null,
      gravedad: this.gravedad || null,
      tuneoPlate: this.tuneoPlate?.trim() || null,
      tuneoSeleccionados: this.tuneoSeleccionados.length ? this.tuneoSeleccionados.join(', ') : null
    };

    this.enviando = true;

    this.facturaService.crearFactura(payload).subscribe({
      next: () => {
        alert('Factura guardada correctamente en la base de datos.');
        this.resetFormulario();
        this.enviando = false;
      },
      error: (error) => {
        console.error('Error al guardar factura:', error);
        alert('No se pudo guardar la factura.');
        this.enviando = false;
      }
    });
  }

  private obtenerMatriculaParaBackend(): string | null {
    if (this.tipoSeleccionado === 'Tuneo') {
      return this.tuneoPlate?.trim() || null;
    }

    return this.matricula?.trim() || null;
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
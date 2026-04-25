import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  FacturacionService,
  FacturaListado,
  FacturacionFiltros
} from '../../core/services/facturacion.service';

@Component({
  selector: 'app-facturacion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: 'facturacion.component.html',
  styleUrls: ['./facturacion.component.css']
})
export class FacturacionComponent implements OnInit {

  facturas: FacturaListado[] = [];

  loading = false;
  error = '';

  filtros: FacturacionFiltros = {
    fechaInicio: '',
    fechaFin: '',
    tipo: ''
  };

  tiposFactura = [
    'Reparación',
    'Items',
    'Tasación',
    'Full Tuning',
    'Tuneo'
  ];

  facturaSeleccionada: FacturaListado | null = null;

  constructor(private facturacionService: FacturacionService) {}

  ngOnInit(): void {
    this.cargarMesActual();
  }

  cargarMesActual(): void {
    const hoy = new Date();

    const inicio = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    const fin = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);

    this.filtros.fechaInicio = this.toInputDate(inicio);
    this.filtros.fechaFin = this.toInputDate(fin);
    this.filtros.tipo = '';

    this.buscar();
  }

  buscar(): void {
    this.loading = true;
    this.error = '';

    this.facturacionService.listarFacturas(this.filtros).subscribe({
      next: (res) => {
        this.facturas = res || [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.error = 'Error cargando facturación';
        this.loading = false;
      }
    });
  }

  limpiar(): void {
    this.filtros = {
      fechaInicio: '',
      fechaFin: '',
      tipo: ''
    };

    this.buscar();
  }

  seleccionarFactura(f: FacturaListado): void {
    this.facturaSeleccionada = f;
  }

  cerrarDetalle(): void {
    this.facturaSeleccionada = null;
  }

  get totalFacturado(): number {
    return this.facturas.reduce((acc, f) => acc + (f.total || 0), 0);
  }

  get promedio(): number {
    if (!this.facturas.length) return 0;
    return Math.round(this.totalFacturado / this.facturas.length);
  }

  getDescripcion(f: FacturaListado): string {
    switch (f.tipo) {
      case 'Reparación':
        return `${f.gravedad}${f.grua ? ' + Grúa' : ''}`;

      case 'Items':
        return `${f.item} x${f.cantidad}`;

      case 'Tasación':
        return `${f.modelo} (${f.estado})`;

      case 'Full Tuning':
        return f.categoria || '';

      case 'Tuneo':
        return `${f.categoria} (${f.tuneoSeleccionados})`;

      default:
        return '';
    }
  }

  private toInputDate(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');

    return `${y}-${m}-${day}`;
  }
}
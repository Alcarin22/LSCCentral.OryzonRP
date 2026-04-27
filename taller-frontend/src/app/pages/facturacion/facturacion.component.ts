import { ChangeDetectorRef, Component, NgZone, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import {
  FacturacionService,
  FacturaListado,
  FacturacionFiltros
} from '../../core/services/facturacion.service';

interface SemanaFacturacion {
  nombre: string;
  rango: string;
  fechaInicio: string;
  fechaFin: string;
  facturacionTotal: number;
  facturasTotales: number;
  promedio: number;
}

@Component({
  selector: 'app-facturacion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './facturacion.component.html',
  styleUrls: ['./facturacion.component.css']
})
export class FacturacionComponent implements OnInit {
  facturas: FacturaListado[] = [];
  semanas: SemanaFacturacion[] = [];

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

  facturaAbiertaId: number | null = null;

  constructor(
    private facturacionService: FacturacionService,
    private zone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarUltimasSemanas();
  }

  cargarUltimasSemanas(): void {
    const semanas = this.generarUltimasSemanas(5);

    this.filtros.fechaInicio = semanas[0].fechaInicio;
    this.filtros.fechaFin = semanas[semanas.length - 1].fechaFin;
    this.filtros.tipo = '';

    this.buscar();
  }

  setSemana(offset: number): void {
    const hoy = new Date();
    const lunes = this.getLunesSemana(hoy);

    lunes.setDate(lunes.getDate() - offset * 7);

    const domingo = new Date(lunes);
    domingo.setDate(lunes.getDate() + 6);

    this.filtros.fechaInicio = this.toInputDate(lunes);
    this.filtros.fechaFin = this.toInputDate(domingo);

    this.buscar();
  }

  async buscar(): Promise<void> {
    this.zone.run(() => {
      this.loading = true;
      this.error = '';
      this.cdr.detectChanges();
    });

    try {
      const response = await firstValueFrom(
        this.facturacionService.listarFacturas(this.filtros)
      );

      this.zone.run(() => {
        this.facturas = (response ?? []).sort((a, b) =>
          new Date(b.fecha).getTime() - new Date(a.fecha).getTime()
        );

        this.semanas = this.generarSemanasDesdeFiltros();
        this.calcularDatosSemanales();

        this.loading = false;
        this.error = '';

        this.cdr.detectChanges();
      });
    } catch (error) {
      console.error('ERROR CARGANDO FACTURACIÓN:', error);

      this.zone.run(() => {
        this.facturas = [];
        this.semanas = this.generarSemanasDesdeFiltros();
        this.calcularDatosSemanales();

        this.error = 'No se pudo cargar la facturación.';
        this.loading = false;

        this.cdr.detectChanges();
      });
    }
  }

  limpiar(): void {
    this.cargarUltimasSemanas();
  }

  toggleDetalle(factura: FacturaListado): void {
    this.facturaAbiertaId = this.facturaAbiertaId === factura.id ? null : factura.id;
  }

  isFacturaAbierta(factura: FacturaListado): boolean {
    return this.facturaAbiertaId === factura.id;
  }

  get totalFacturado(): number {
    return this.facturas.reduce((acc, f) => acc + (f.total || 0), 0);
  }

  get totalFacturas(): number {
    return this.facturas.length;
  }

  get promedioFactura(): number {
    if (!this.facturas.length) return 0;
    return Math.round(this.totalFacturado / this.facturas.length);
  }

  get mejorSemana(): SemanaFacturacion | null {
    if (!this.semanas.length) return null;

    return this.semanas.reduce((a, b) =>
      b.facturacionTotal > a.facturacionTotal ? b : a
    );
  }

  getDescripcion(f: FacturaListado): string {
    switch (f.tipo) {
      case 'Reparación':
        return `${f.gravedad || 'Reparación'}${f.grua ? ' · Grúa' : ''}`;

      case 'Items':
        return `${f.item || 'Item'} x${f.cantidad || 1}`;

      case 'Tasación':
        return `${f.modelo || 'Modelo'} · ${f.estado || 'Estado'}`;

      case 'Full Tuning':
        return `${f.categoria || 'Categoría'}${f.matricula ? ' · ' + f.matricula : ''}`;

      case 'Tuneo':
        return `${f.categoria || 'Categoría'} · ${f.tuneoSeleccionados || 'Tuneo'}`;

      default:
        return '-';
    }
  }

  getMatricula(f: FacturaListado): string {
    return f.matricula || f.tuneoPlate || '-';
  }

  // 🔥 NUEVO: INFORME TASACIÓN
getInformeTasacion(f: FacturaListado): string {
  return [
    `Modelo: ${f.modelo || '-'}`,
    `Estado: ${f.estado || '-'}`,
    `Matricula: ${this.getMatricula(f)}`
  ].join('\n');
}

copiarInformeTasacion(f: FacturaListado): void {
  const texto = this.getInformeTasacion(f);

  navigator.clipboard.writeText(texto)
    .then(() => alert('Informe copiado'))
    .catch(() => alert('Error al copiar'));
}

  formatearFecha(fecha: string): Date | null {
    if (!fecha) return null;
    return new Date(fecha);
  }

  private generarSemanasDesdeFiltros(): SemanaFacturacion[] {
    if (!this.filtros.fechaInicio || !this.filtros.fechaFin) {
      return this.generarUltimasSemanas(5);
    }

    const inicio = this.parseInputDate(this.filtros.fechaInicio);
    const fin = this.parseInputDate(this.filtros.fechaFin);
    const lunesInicial = this.getLunesSemana(inicio);

    const semanas: SemanaFacturacion[] = [];
    let cursor = new Date(lunesInicial);
    let index = 1;

    while (cursor <= fin) {
      const lunes = new Date(cursor);
      const domingo = new Date(cursor);
      domingo.setDate(lunes.getDate() + 6);

      semanas.push({
        nombre: `Semana ${index}`,
        rango: `${this.formatearDiaMes(lunes)} - ${this.formatearDiaMes(domingo)}`,
        fechaInicio: this.toInputDate(lunes),
        fechaFin: this.toInputDate(domingo),
        facturacionTotal: 0,
        facturasTotales: 0,
        promedio: 0
      });

      cursor.setDate(cursor.getDate() + 7);
      index++;
    }

    return semanas;
  }

  private generarUltimasSemanas(cantidad: number): SemanaFacturacion[] {
    const hoy = new Date();
    const lunesActual = this.getLunesSemana(hoy);
    const semanas: SemanaFacturacion[] = [];

    for (let i = cantidad - 1; i >= 0; i--) {
      const lunes = new Date(lunesActual);
      lunes.setDate(lunesActual.getDate() - i * 7);

      const domingo = new Date(lunes);
      domingo.setDate(lunes.getDate() + 6);

      semanas.push({
        nombre: `Semana ${semanas.length + 1}`,
        rango: `${this.formatearDiaMes(lunes)} - ${this.formatearDiaMes(domingo)}`,
        fechaInicio: this.toInputDate(lunes),
        fechaFin: this.toInputDate(domingo),
        facturacionTotal: 0,
        facturasTotales: 0,
        promedio: 0
      });
    }

    return semanas;
  }

  private calcularDatosSemanales(): void {
    this.semanas = this.semanas.map(semana => {
      const inicio = this.parseInputDate(semana.fechaInicio);
      const fin = this.parseInputDate(semana.fechaFin);
      fin.setHours(23, 59, 59, 999);

      const facturasSemana = this.facturas.filter(factura => {
        const fecha = this.formatearFecha(factura.fecha);
        return fecha && fecha >= inicio && fecha <= fin;
      });

      const total = facturasSemana.reduce((acc, f) => acc + (f.total || 0), 0);

      return {
        ...semana,
        facturacionTotal: total,
        facturasTotales: facturasSemana.length,
        promedio: facturasSemana.length ? Math.round(total / facturasSemana.length) : 0
      };
    });
  }

  private getLunesSemana(fecha: Date): Date {
    const copia = new Date(fecha);
    const dia = copia.getDay();
    const diff = dia === 0 ? -6 : 1 - dia;

    copia.setDate(copia.getDate() + diff);
    copia.setHours(0, 0, 0, 0);

    return copia;
  }

  private parseInputDate(value: string): Date {
    const [year, month, day] = value.split('-').map(Number);
    return new Date(year, month - 1, day);
  }

  private toInputDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  private formatearDiaMes(date: Date): string {
    return `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}`;
  }
}


import { ChangeDetectorRef, Component, NgZone, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import {
  FacturacionService,
  FacturaListado,
  FacturacionFiltros
} from '../../core/services/facturacion.service';

import {
  AdminService,
  EmpleadoAdmin
} from '../../core/services/admin.service';

import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-facturacion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './facturacion.component.html',
  styleUrls: ['./facturacion.component.css']
})
export class FacturacionComponent implements OnInit {
  facturas: FacturaListado[] = [];
  empleadosActivos: EmpleadoAdmin[] = [];

  loading = false;
  loadingEmpleados = false;
  error = '';

  filtros: FacturacionFiltros = {
    fechaInicio: '',
    fechaFin: '',
    tipo: '',
    idEmpleado: null
  };

  tiposFactura = [
    'Reparación',
    'Items',
    'Tasación',
    'Full Tuning',
    'Tuneo'
  ];

  facturaAbiertaId: number | null = null;

  paginaActual = 1;
  facturasPorPagina = 10;
  opcionesPaginacion = [10, 20, 50];

  constructor(
    private facturacionService: FacturacionService,
    private adminService: AdminService,
    private toastService: ToastService,
    private zone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarEmpleadosActivos();
    this.buscar();
  }

  async cargarEmpleadosActivos(): Promise<void> {
    this.loadingEmpleados = true;

    try {
      const empleados = await firstValueFrom(this.adminService.listarEmpleados());

      this.zone.run(() => {
        this.empleadosActivos = (empleados ?? [])
          .filter(e => e.activo)
          .sort((a, b) => a.nombre.localeCompare(b.nombre));

        this.loadingEmpleados = false;
        this.cdr.detectChanges();
      });
    } catch (error) {
      console.error('ERROR CARGANDO EMPLEADOS ACTIVOS:', error);

      this.zone.run(() => {
        this.empleadosActivos = [];
        this.loadingEmpleados = false;
        this.toastService.error('No se pudieron cargar los empleados activos.');
        this.cdr.detectChanges();
      });
    }
  }

  async buscar(): Promise<void> {
    this.zone.run(() => {
      this.loading = true;
      this.error = '';
      this.facturaAbiertaId = null;
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

        this.paginaActual = 1;
        this.loading = false;
        this.error = '';

        this.cdr.detectChanges();
      });
    } catch (error) {
      console.error('ERROR CARGANDO FACTURACIÓN:', error);

      this.zone.run(() => {
        this.facturas = [];
        this.paginaActual = 1;
        this.error = 'No se pudo cargar la facturación.';
        this.loading = false;

        this.cdr.detectChanges();
      });
    }
  }

  limpiar(): void {
    this.filtros = {
      fechaInicio: '',
      fechaFin: '',
      tipo: '',
      idEmpleado: null
    };

    this.facturasPorPagina = 10;
    this.paginaActual = 1;
    this.buscar();
  }

  cambiarTamanoPagina(): void {
    this.paginaActual = 1;
  }

  paginaAnterior(): void {
    if (this.paginaActual > 1) {
      this.paginaActual--;
    }
  }

  paginaSiguiente(): void {
    if (this.paginaActual < this.totalPaginas) {
      this.paginaActual++;
    }
  }

  irAPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas) {
      this.paginaActual = pagina;
    }
  }

  toggleDetalle(factura: FacturaListado): void {
    this.facturaAbiertaId = this.facturaAbiertaId === factura.id ? null : factura.id;
  }

  isFacturaAbierta(factura: FacturaListado): boolean {
    return this.facturaAbiertaId === factura.id;
  }

  get facturasPaginadas(): FacturaListado[] {
    const inicio = (this.paginaActual - 1) * this.facturasPorPagina;
    const fin = inicio + this.facturasPorPagina;

    return this.facturas.slice(inicio, fin);
  }

  get totalPaginas(): number {
    return Math.max(1, Math.ceil(this.facturas.length / this.facturasPorPagina));
  }

  get paginasVisibles(): number[] {
    const total = this.totalPaginas;
    const actual = this.paginaActual;
    const paginas: number[] = [];

    const inicio = Math.max(1, actual - 2);
    const fin = Math.min(total, actual + 2);

    for (let i = inicio; i <= fin; i++) {
      paginas.push(i);
    }

    return paginas;
  }

  get inicioMostrado(): number {
    if (!this.facturas.length) return 0;
    return (this.paginaActual - 1) * this.facturasPorPagina + 1;
  }

  get finMostrado(): number {
    return Math.min(this.paginaActual * this.facturasPorPagina, this.facturas.length);
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
      .then(() => {
        this.toastService.success('Informe copiado al portapapeles.');
      })
      .catch(error => {
        console.error('Error copiando informe:', error);
        this.toastService.error('No se pudo copiar el informe.');
      });
  }

  formatearFecha(fecha: string): Date | null {
    if (!fecha) return null;
    return new Date(fecha);
  }
}
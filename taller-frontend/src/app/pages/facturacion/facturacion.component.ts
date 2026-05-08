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
import { SessionService } from '../../core/services/session.service';

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

  totalFacturasBackend = 0;
  totalPaginasBackend = 1;
  totalFacturadoBackend = 0;
  promedioFacturaBackend = 0;

  private filtroTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private facturacionService: FacturacionService,
    private adminService: AdminService,
    private toastService: ToastService,
    private sessionService: SessionService,
    private zone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarEmpleadosActivos();
    this.buscar();
  }

  get puedeEliminarFacturas(): boolean {
    const empleado = this.sessionService.getEmpleado();
    return (empleado?.rango?.nivel ?? 0) >= 3;
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
        this.facturacionService.listarFacturas(
          this.filtros,
          this.paginaActual - 1,
          this.facturasPorPagina
        )
      );

      this.zone.run(() => {
        this.facturas = response.content ?? [];
        this.totalFacturasBackend = response.totalElements ?? 0;
        this.totalPaginasBackend = response.totalPages ?? 1;
        this.totalFacturadoBackend = response.totalFacturado ?? 0;
        this.promedioFacturaBackend = response.promedioFactura ?? 0;

        this.loading = false;
        this.error = '';
        this.cdr.detectChanges();
      });
    } catch (error) {
      console.error('ERROR CARGANDO FACTURACIÓN:', error);

      this.zone.run(() => {
        this.facturas = [];
        this.totalFacturasBackend = 0;
        this.totalPaginasBackend = 1;
        this.totalFacturadoBackend = 0;
        this.promedioFacturaBackend = 0;
        this.error = 'No se pudo cargar la facturación.';
        this.loading = false;
        this.cdr.detectChanges();
      });
    }
  }

  onFiltrosChange(): void {
    if (this.filtroTimeout) {
      clearTimeout(this.filtroTimeout);
    }

    this.filtroTimeout = setTimeout(() => {
      this.paginaActual = 1;
      this.buscar();
    }, 350);
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
    this.buscar();
  }

  paginaAnterior(): void {
    if (this.paginaActual > 1) {
      this.paginaActual--;
      this.buscar();
    }
  }

  paginaSiguiente(): void {
    if (this.paginaActual < this.totalPaginas) {
      this.paginaActual++;
      this.buscar();
    }
  }

  irAPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas) {
      this.paginaActual = pagina;
      this.buscar();
    }
  }

  toggleDetalle(factura: FacturaListado): void {
    this.facturaAbiertaId = this.facturaAbiertaId === factura.id ? null : factura.id;
  }

  isFacturaAbierta(factura: FacturaListado): boolean {
    return this.facturaAbiertaId === factura.id;
  }

  eliminarFactura(factura: FacturaListado, event: MouseEvent): void {
    event.stopPropagation();

    if (!this.puedeEliminarFacturas) {
      this.toastService.error('No tienes permisos para eliminar facturas.');
      return;
    }

    const confirmar = confirm(`¿Seguro que quieres eliminar la factura #${factura.id}?`);

    if (!confirmar) {
      return;
    }

    this.facturacionService.eliminarFactura(factura.id).subscribe({
      next: () => {
        this.toastService.success(`Factura #${factura.id} eliminada correctamente.`);

        if (this.facturas.length === 1 && this.paginaActual > 1) {
          this.paginaActual--;
        }

        this.buscar();
      },
      error: (error) => {
        console.error('ERROR ELIMINANDO FACTURA:', error);
        this.toastService.error('No se pudo eliminar la factura.');
      }
    });
  }

  get facturasPaginadas(): FacturaListado[] {
    return this.facturas;
  }

  get totalPaginas(): number {
    return Math.max(1, this.totalPaginasBackend);
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
    if (!this.totalFacturas) return 0;
    return (this.paginaActual - 1) * this.facturasPorPagina + 1;
  }

  get finMostrado(): number {
    return Math.min(this.paginaActual * this.facturasPorPagina, this.totalFacturas);
  }

  get totalFacturado(): number {
    return this.totalFacturadoBackend;
  }

  get totalFacturas(): number {
    return this.totalFacturasBackend;
  }

  get promedioFactura(): number {
    return this.promedioFacturaBackend;
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
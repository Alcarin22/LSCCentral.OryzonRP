import {
  ChangeDetectorRef,
  Component,
  NgZone,
  OnInit
} from '@angular/core';

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
  tasacionesPendientes: FacturaListado[] = [];

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

    if (this.puedeGestionarTasaciones) {
      this.cargarTasacionesPendientes();
    }
  }

  get puedeEliminarFacturas(): boolean {
    const empleado = this.sessionService.getEmpleado();
    return (empleado?.rango?.nivel ?? 0) >= 3;
  }

  get puedeGestionarTasaciones(): boolean {
    const empleado = this.sessionService.getEmpleado();
    return (empleado?.rango?.nivel ?? 0) >= 3;
  }

  async cargarEmpleadosActivos(): Promise<void> {
    this.loadingEmpleados = true;

    try {
      const empleados = await firstValueFrom(
        this.adminService.listarEmpleados()
      );

      this.zone.run(() => {
        this.empleadosActivos = (empleados ?? [])
          .filter(empleado => empleado.activo)
          .sort((a, b) => a.nombre.localeCompare(b.nombre));

        this.loadingEmpleados = false;
        this.cdr.detectChanges();
      });
    } catch (error) {
      console.error('ERROR CARGANDO EMPLEADOS ACTIVOS:', error);

      this.zone.run(() => {
        this.empleadosActivos = [];
        this.loadingEmpleados = false;

        this.toastService.error(
          'No se pudieron cargar los empleados activos.'
        );

        this.cdr.detectChanges();
      });
    }
  }

  async cargarTasacionesPendientes(): Promise<void> {
    if (!this.puedeGestionarTasaciones) {
      this.tasacionesPendientes = [];
      return;
    }

    try {
      const response = await firstValueFrom(
        this.facturacionService.listarFacturas(
          {
            fechaInicio: '',
            fechaFin: '',
            tipo: 'Tasación',
            idEmpleado: null
          },
          0,
          50
        )
      );

      this.zone.run(() => {
        this.tasacionesPendientes = (response.content ?? [])
          .filter(factura => this.getEstadoTasacion(factura) !== 'Enviada');

        this.cdr.detectChanges();
      });
    } catch (error) {
      console.error(
        'ERROR CARGANDO TASACIONES PENDIENTES:',
        error
      );
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

        this.totalFacturasBackend =
          response.totalElements ?? 0;

        this.totalPaginasBackend =
          response.totalPages ?? 1;

        this.totalFacturadoBackend =
          response.totalFacturado ?? 0;

        this.promedioFacturaBackend =
          response.promedioFactura ?? 0;

        this.loading = false;
        this.error = '';

        this.cdr.detectChanges();
      });

      if (this.puedeGestionarTasaciones) {
        this.cargarTasacionesPendientes();
      }
    } catch (error) {
      console.error(
        'ERROR CARGANDO FACTURACIÓN:',
        error
      );

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
    if (
      pagina >= 1 &&
      pagina <= this.totalPaginas
    ) {
      this.paginaActual = pagina;
      this.buscar();
    }
  }

  toggleDetalle(factura: FacturaListado): void {
    this.facturaAbiertaId =
      this.facturaAbiertaId === factura.id
        ? null
        : factura.id;
  }

  abrirFacturaDesdeAlerta(
    factura: FacturaListado
  ): void {
    this.facturaAbiertaId = factura.id;

    this.toastService.info(
      `Abierta la tasación #${factura.id}.`
    );
  }

  isFacturaAbierta(
    factura: FacturaListado
  ): boolean {
    return this.facturaAbiertaId === factura.id;
  }

  eliminarFactura(
    factura: FacturaListado,
    event: MouseEvent
  ): void {
    event.stopPropagation();

    if (!this.puedeEliminarFacturas) {
      this.toastService.error(
        'No tienes permisos para eliminar facturas.'
      );

      return;
    }

    const confirmar = confirm(
      `¿Seguro que quieres eliminar la factura #${factura.id}?`
    );

    if (!confirmar) {
      return;
    }

    this.facturacionService
      .eliminarFactura(factura.id)
      .subscribe({
        next: () => {
          this.toastService.success(
            `Factura #${factura.id} eliminada correctamente.`
          );

          if (
            this.facturas.length === 1 &&
            this.paginaActual > 1
          ) {
            this.paginaActual--;
          }

          this.buscar();
        },
        error: (error) => {
          console.error(
            'ERROR ELIMINANDO FACTURA:',
            error
          );

          this.toastService.error(
            'No se pudo eliminar la factura.'
          );
        }
      });
  }

  marcarTasacionEnviada(
    factura: FacturaListado,
    event?: MouseEvent
  ): void {
    if (event) {
      event.stopPropagation();
    }

    if (!this.puedeGestionarTasaciones) {
      this.toastService.error(
        'No tienes permisos para cambiar el estado de tasaciones.'
      );

      return;
    }

    if (factura.tipo !== 'Tasación') {
      return;
    }

    if (
      this.getEstadoTasacion(factura) === 'Enviada'
    ) {
      this.toastService.info(
        'Esta tasación ya está marcada como enviada.'
      );

      return;
    }

    this.facturacionService
      .marcarTasacionEnviada(factura.id)
      .subscribe({
        next: (actualizada) => {
          this.zone.run(() => {
            this.facturas = this.facturas.map(facturaLista =>
              facturaLista.id === actualizada.id
                ? actualizada
                : facturaLista
            );

            this.tasacionesPendientes =
              this.tasacionesPendientes.filter(
                facturaPendiente =>
                  facturaPendiente.id !== actualizada.id
              );

            this.toastService.success(
              `Tasación #${actualizada.id} marcada como enviada.`
            );

            this.cdr.detectChanges();
          });
        },
        error: (error) => {
          console.error(
            'ERROR MARCANDO TASACIÓN COMO ENVIADA:',
            error
          );

          this.toastService.error(
            'No se pudo cambiar el estado de la tasación.'
          );
        }
      });
  }

  get facturasPaginadas(): FacturaListado[] {
    return this.facturas;
  }

  get totalPaginas(): number {
    return Math.max(
      1,
      this.totalPaginasBackend
    );
  }

  get paginasVisibles(): number[] {
    const total = this.totalPaginas;
    const actual = this.paginaActual;
    const paginas: number[] = [];

    const inicio = Math.max(
      1,
      actual - 2
    );

    const fin = Math.min(
      total,
      actual + 2
    );

    for (let pagina = inicio; pagina <= fin; pagina++) {
      paginas.push(pagina);
    }

    return paginas;
  }

  get inicioMostrado(): number {
    if (!this.totalFacturas) {
      return 0;
    }

    return (
      (this.paginaActual - 1) *
      this.facturasPorPagina +
      1
    );
  }

  get finMostrado(): number {
    return Math.min(
      this.paginaActual * this.facturasPorPagina,
      this.totalFacturas
    );
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

  getDescripcion(
    factura: FacturaListado
  ): string {
    switch (factura.tipo) {
      case 'Reparación':
        return (
          `${factura.gravedad || 'Reparación'}` +
          `${factura.grua ? ' · Grúa' : ''}`
        );

      case 'Items': {
        if (factura.items && factura.items.length > 0) {
          return factura.items
            .map(linea => `${linea.item} x${linea.cantidad}`)
            .join(' · ');
        }

        return (
          `${factura.item || 'Item'} ` +
          `x${factura.cantidad || 1}`
        );
      }

      case 'Tasación':
        return (
          `${factura.modelo || 'Modelo'} · ` +
          `${factura.estado || 'Estado'}`
        );

      case 'Full Tuning':
        return (
          `${factura.categoria || 'Categoría'}` +
          `${factura.matricula ? ' · ' + factura.matricula : ''}`
        );

      case 'Tuneo':
        return (
          `${factura.categoria || 'Sin categoría'} · ` +
          `${factura.tuneoSeleccionados || 'Tuneo'}`
        );

      default:
        return '-';
    }
  }

  getMatricula(
    factura: FacturaListado
  ): string {
    return (
      factura.matricula ||
      factura.tuneoPlate ||
      '-'
    );
  }

  getEstadoTasacion(
    factura: FacturaListado
  ): string {
    return factura.estadoTasacion || 'Pendiente';
  }

  getInformeTasacion(
    factura: FacturaListado
  ): string {
    return [
      `Modelo: ${factura.modelo || '-'}`,
      `Estado: ${factura.estado || '-'}`,
      `Matricula: ${this.getMatricula(factura)}`
    ].join('\n');
  }

  copiarInformeTasacion(
    factura: FacturaListado,
    event?: MouseEvent
  ): void {
    if (event) {
      event.stopPropagation();
    }

    const texto =
      this.getInformeTasacion(factura);

    navigator.clipboard
      .writeText(texto)
      .then(() => {
        this.toastService.success(
          'Plantilla de tasación copiada.'
        );
      })
      .catch(error => {
        console.error(
          'Error copiando informe:',
          error
        );

        this.toastService.error(
          'No se pudo copiar la plantilla.'
        );
      });
  }

  /**
   * Muestra la fecha recibida del backend como una fecha local literal.
   *
   * No utiliza new Date(), porque LocalDateTime no contiene zona horaria
   * y no debe convertirse a UTC ni ajustarse según la zona del navegador.
   */
  formatearFechaHora(
    fecha: string | null | undefined,
    incluirSegundos = false
  ): string {
    if (!fecha) {
      return '-';
    }

    const fechaNormalizada = fecha
      .trim()
      .replace(' ', 'T');

    const coincidencia = fechaNormalizada.match(
      /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?/
    );

    if (!coincidencia) {
      return fecha;
    }

    const [
      ,
      anio,
      mes,
      dia,
      hora,
      minuto,
      segundo
    ] = coincidencia;

    const fechaBase =
      `${dia}/${mes}/${anio} ${hora}:${minuto}`;

    if (!incluirSegundos) {
      return fechaBase;
    }

    return `${fechaBase}:${segundo ?? '00'}`;
  }
}
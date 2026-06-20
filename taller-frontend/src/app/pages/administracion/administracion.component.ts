import {
  ChangeDetectorRef,
  Component,
  NgZone,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  SessionEmpleado,
  SessionService
} from '../../core/services/session.service';

import {
  AdminService,
  EmpleadoAdmin,
  RangoAdmin,
  VehiculoAdmin,
  VehiculoAdminRequest
} from '../../core/services/admin.service';

import {
  AdminPrima,
  AdminPrimasService
} from '../../core/services/admin-primas.service';

type AdminTab = 'empleados' | 'primas' | 'precios' | 'vehiculos';

@Component({
  selector: 'app-administracion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './administracion.component.html',
  styleUrls: ['./administracion.component.css']
})
export class AdministracionComponent implements OnInit {
  empleadoSesion: SessionEmpleado | null = null;

  activeTab: AdminTab = 'empleados';

  empleados: EmpleadoAdmin[] = [];
  rangos: RangoAdmin[] = [];

  primas: AdminPrima[] = [];
  semanaSeleccionada: number | null = null;

  vehiculos: VehiculoAdmin[] = [];
  mostrarVehiculosInactivos = false;
  loadingVehiculos = false;
  errorVehiculos = '';

  editandoVehiculoId: number | null = null;
  mostrarFormularioNuevoVehiculo = false;

  vehiculoForm: VehiculoAdminRequest = this.getVehiculoFormVacio();

  categoriasVehiculos: string[] = [
    'Compacto',
    'Sedán',
    'SUV',
    'Coupé',
    'Deportivo',
    'Superdeportivo',
    'Muscle',
    'Clásico Deportivo',
    'Clásico',
    'Moto',
    'Todoterreno',
    'Todoterreno extremo',
    'Industrial',
    'Comercial',
    'Servicio',
    'Emergencia',
    'Avión',
    'Helicóptero',
    'Barco'
  ];

  mostrarInactivos = false;

  loading = false;
  loadingPrimas = false;
  error = '';
  errorPrimas = '';

  empleadoAbiertoId: number | null = null;

  constructor(
    private sessionService: SessionService,
    private adminService: AdminService,
    private adminPrimasService: AdminPrimasService,
    private zone: NgZone,
    private cdr: ChangeDetectorRef
  ) {
    this.empleadoSesion = this.sessionService.getEmpleado();
  }

  ngOnInit(): void {
    if (!this.puedeAcceder()) {
      this.error = 'No tienes permisos para acceder a administración.';
      return;
    }

    this.cargarDatos();
  }

  puedeAcceder(): boolean {
    return (this.empleadoSesion?.rango?.nivel ?? 0) >= 4;
  }

  setTab(tab: AdminTab): void {
    this.activeTab = tab;

    if (tab === 'primas') {
      this.cargarPrimas();
    }

    if (tab === 'vehiculos') {
      this.cargarVehiculos();
    }
  }

  cargarDatos(): void {
    this.zone.run(() => {
      this.loading = true;
      this.error = '';
      this.cdr.detectChanges();
    });

    this.adminService.listarRangos().subscribe({
      next: (rangos) => {
        this.rangos = rangos ?? [];

        this.adminService.listarEmpleados().subscribe({
          next: (empleados) => {
            this.zone.run(() => {
              this.empleados = (empleados ?? []).sort((a, b) => {
                if (a.activo !== b.activo) return a.activo ? -1 : 1;
                return a.nombre.localeCompare(b.nombre);
              });

              this.loading = false;
              this.error = '';
              this.cdr.detectChanges();
            });
          },
          error: (error) => {
            console.error('Error cargando empleados:', error);

            this.zone.run(() => {
              this.error = 'No se pudieron cargar los empleados.';
              this.loading = false;
              this.cdr.detectChanges();
            });
          }
        });
      },
      error: (error) => {
        console.error('Error cargando rangos:', error);

        this.zone.run(() => {
          this.error = 'No se pudieron cargar los rangos.';
          this.loading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  cargarPrimas(): void {
    this.zone.run(() => {
      this.loadingPrimas = true;
      this.errorPrimas = '';
      this.cdr.detectChanges();
    });

    this.adminPrimasService.listarPrimas(this.semanaSeleccionada).subscribe({
      next: (data) => {
        this.zone.run(() => {
          this.primas = data ?? [];

          if (this.primas.length > 0) {
            this.semanaSeleccionada = this.primas[0].semana;
          }

          this.loadingPrimas = false;
          this.errorPrimas = '';
          this.cdr.detectChanges();
        });
      },
      error: (error) => {
        console.error('Error cargando primas:', error);

        this.zone.run(() => {
          this.primas = [];
          this.errorPrimas = 'No se pudieron cargar las primas.';
          this.loadingPrimas = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  setSemanaActualPrimas(): void {
    this.semanaSeleccionada = null;
    this.cargarPrimas();
  }

  setSemanaAnteriorPrimas(): void {
    const semanaBase = this.semanaSeleccionada ?? 0;
    this.semanaSeleccionada = Math.max(0, semanaBase - 1);
    this.cargarPrimas();
  }

  setSemanaSiguientePrimas(): void {
    const semanaBase = this.semanaSeleccionada ?? 0;
    this.semanaSeleccionada = semanaBase + 1;
    this.cargarPrimas();
  }

  togglePagada(prima: AdminPrima): void {
    const nuevaPagada = !prima.pagada;
    const accion = nuevaPagada ? 'marcar como pagada' : 'marcar como pendiente';

    const confirmar = confirm(
      `¿Quieres ${accion} la prima de "${prima.nombreEmpleado}"?`
    );

    if (!confirmar) return;

    this.adminPrimasService.actualizarPagada(prima.id, nuevaPagada).subscribe({
      next: (actualizada) => {
        this.zone.run(() => {
          this.primas = this.primas.map(p =>
            p.id === actualizada.id ? actualizada : p
          );

          this.cdr.detectChanges();
        });
      },
      error: (error) => {
        console.error('Error actualizando estado de prima:', error);
        alert(
          error?.error?.message ||
          error?.error?.error ||
          'No se pudo actualizar el estado de la prima.'
        );
      }
    });
  }

  toggleEmpleado(empleado: EmpleadoAdmin): void {
    this.empleadoAbiertoId = this.empleadoAbiertoId === empleado.id ? null : empleado.id;
  }

  isEmpleadoAbierto(empleado: EmpleadoAdmin): boolean {
    return this.empleadoAbiertoId === empleado.id;
  }

  cambiarRango(empleado: EmpleadoAdmin, rangoIdValue: string | number): void {
    const rangoId = Number(rangoIdValue);

    if (!rangoId || rangoId === empleado.rangoId) return;

    const rango = this.rangos.find(r => r.id === rangoId);

    const confirmar = confirm(
      `¿Cambiar el rango de "${empleado.nombre}" a "${rango?.nombre ?? 'nuevo rango'}"?`
    );

    if (!confirmar) return;

    this.adminService.actualizarEmpleado(empleado.id, {
      rangoId,
      activo: null
    }).subscribe({
      next: (actualizado) => {
        this.actualizarEmpleadoEnLista(actualizado);
      },
      error: (error) => {
        console.error('Error cambiando rango:', error);
        alert('No se pudo cambiar el rango del empleado.');
      }
    });
  }

  toggleActivo(empleado: EmpleadoAdmin): void {
    const nuevoEstado = !empleado.activo;
    const accion = nuevoEstado ? 'reactivar' : 'despedir/desactivar';

    const confirmar = confirm(
      `¿Seguro que quieres ${accion} a "${empleado.nombre}"?`
    );

    if (!confirmar) return;

    this.adminService.actualizarEmpleado(empleado.id, {
      rangoId: null,
      activo: nuevoEstado
    }).subscribe({
      next: (actualizado) => {
        this.actualizarEmpleadoEnLista(actualizado);
      },
      error: (error) => {
        console.error('Error actualizando estado:', error);
        alert('No se pudo actualizar el estado del empleado.');
      }
    });
  }

  cargarVehiculos(): void {
    this.zone.run(() => {
      this.loadingVehiculos = true;
      this.errorVehiculos = '';
      this.cdr.detectChanges();
    });

    this.adminService.listarVehiculos().subscribe({
      next: (vehiculos) => {
        this.zone.run(() => {
          this.vehiculos = (vehiculos ?? []).sort((a, b) => {
            if (a.activo !== b.activo) return a.activo ? -1 : 1;

            const marcaCompare = a.marca.localeCompare(b.marca);
            if (marcaCompare !== 0) return marcaCompare;

            return a.modelo.localeCompare(b.modelo);
          });

          this.loadingVehiculos = false;
          this.errorVehiculos = '';
          this.cdr.detectChanges();
        });
      },
      error: (error) => {
        console.error('Error cargando vehículos:', error);

        this.zone.run(() => {
          this.vehiculos = [];
          this.errorVehiculos = 'No se pudieron cargar los vehículos.';
          this.loadingVehiculos = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  mostrarNuevoVehiculo(): void {
    this.editandoVehiculoId = null;
    this.vehiculoForm = this.getVehiculoFormVacio();
    this.mostrarFormularioNuevoVehiculo = true;
  }

  guardarVehiculo(): void {
    const payload: VehiculoAdminRequest = {
      marca: this.vehiculoForm.marca?.trim() ?? '',
      modelo: this.vehiculoForm.modelo?.trim() ?? '',
      categoria: this.vehiculoForm.categoria?.trim() ?? '',
      precio: Number(this.vehiculoForm.precio ?? 0),
      imagenUrl: this.normalizarTextoOpcional(this.vehiculoForm.imagenUrl),
      activo: this.vehiculoForm.activo
    };

    if (!payload.marca) {
      alert('La marca es obligatoria.');
      return;
    }

    if (!payload.modelo) {
      alert('El modelo es obligatorio.');
      return;
    }

    if (!payload.categoria) {
      alert('La categoría es obligatoria.');
      return;
    }

    if (payload.precio < 0) {
      alert('El precio no puede ser negativo.');
      return;
    }

    const request$ = this.editandoVehiculoId
      ? this.adminService.actualizarVehiculo(this.editandoVehiculoId, payload)
      : this.adminService.crearVehiculo(payload);

    request$.subscribe({
      next: () => {
        this.cancelarEdicionVehiculo();
        this.cargarVehiculos();
      },
      error: (error) => {
        console.error('Error guardando vehículo:', error);
        alert(
          error?.error?.message ||
          error?.error?.error ||
          'No se pudo guardar el vehículo.'
        );
      }
    });
  }

  editarVehiculo(vehiculo: VehiculoAdmin): void {
    this.mostrarFormularioNuevoVehiculo = false;
    this.editandoVehiculoId = vehiculo.id;

    this.vehiculoForm = {
      marca: vehiculo.marca,
      modelo: vehiculo.modelo,
      categoria: vehiculo.categoria,
      precio: vehiculo.precio,
      imagenUrl: vehiculo.imagenUrl,
      activo: vehiculo.activo
    };
  }

  cancelarEdicionVehiculo(): void {
    this.editandoVehiculoId = null;
    this.mostrarFormularioNuevoVehiculo = false;
    this.vehiculoForm = this.getVehiculoFormVacio();
  }

  toggleActivoVehiculo(vehiculo: VehiculoAdmin): void {
    const nuevoEstado = !vehiculo.activo;
    const accion = nuevoEstado ? 'reactivar' : 'desactivar';

    const confirmar = confirm(
      `¿Seguro que quieres ${accion} el vehículo "${vehiculo.marca} ${vehiculo.modelo}"?`
    );

    if (!confirmar) return;

    this.adminService.cambiarEstadoVehiculo(vehiculo.id, nuevoEstado).subscribe({
      next: (actualizado) => {
        this.zone.run(() => {
          this.vehiculos = this.vehiculos.map(v =>
            v.id === actualizado.id ? actualizado : v
          );

          this.vehiculos = [...this.vehiculos].sort((a, b) => {
            if (a.activo !== b.activo) return a.activo ? -1 : 1;

            const marcaCompare = a.marca.localeCompare(b.marca);
            if (marcaCompare !== 0) return marcaCompare;

            return a.modelo.localeCompare(b.modelo);
          });

          this.cdr.detectChanges();
        });
      },
      error: (error) => {
        console.error('Error cambiando estado de vehículo:', error);
        alert('No se pudo cambiar el estado del vehículo.');
      }
    });
  }

  get empleadosFiltrados(): EmpleadoAdmin[] {
    if (this.mostrarInactivos) return this.empleados;
    return this.empleados.filter(e => e.activo);
  }

  get vehiculosFiltrados(): VehiculoAdmin[] {
    if (this.mostrarVehiculosInactivos) return this.vehiculos;
    return this.vehiculos.filter(v => v.activo);
  }

  get totalEmpleados(): number {
    return this.empleados.length;
  }

  get empleadosActivos(): number {
    return this.empleados.filter(e => e.activo).length;
  }

  get empleadosInactivos(): number {
    return this.empleados.filter(e => !e.activo).length;
  }

  get rangoMaximo(): number {
    return this.empleados.length
      ? Math.max(...this.empleados.map(e => e.rangoNivel ?? 0))
      : 0;
  }

  get totalPrimas(): number {
    return this.primas.length;
  }

  get primasPagadas(): number {
    return this.primas.filter(p => p.pagada).length;
  }

  get primasPendientes(): number {
    return this.primas.filter(p => !p.pagada).length;
  }

  get totalAPagar(): number {
    return this.primas
      .filter(p => !p.pagada)
      .reduce((acc, p) => acc + Number(p.total || 0), 0);
  }

  get totalPagado(): number {
    return this.primas
      .filter(p => p.pagada)
      .reduce((acc, p) => acc + Number(p.total || 0), 0);
  }

  get totalVehiculos(): number {
    return this.vehiculos.length;
  }

  get vehiculosActivos(): number {
    return this.vehiculos.filter(v => v.activo).length;
  }

  get vehiculosInactivos(): number {
    return this.vehiculos.filter(v => !v.activo).length;
  }

  formatearFecha(value: string | null): Date | null {
    if (!value) return null;
    return new Date(value);
  }

  formatearHoras(valor: number | string | null | undefined): string {
    if (valor === null || valor === undefined) return '0:00';

    const horasDecimal = Number(valor);

    if (Number.isNaN(horasDecimal)) return '0:00';

    const horas = Math.floor(horasDecimal);
    const minutos = Math.round((horasDecimal - horas) * 60);

    return `${horas}:${String(minutos).padStart(2, '0')}`;
  }

  private actualizarEmpleadoEnLista(actualizado: EmpleadoAdmin): void {
    this.zone.run(() => {
      this.empleados = this.empleados.map(e =>
        e.id === actualizado.id ? actualizado : e
      );

      this.empleados = [...this.empleados].sort((a, b) => {
        if (a.activo !== b.activo) return a.activo ? -1 : 1;
        return a.nombre.localeCompare(b.nombre);
      });

      this.cdr.detectChanges();
    });
  }

  private getVehiculoFormVacio(): VehiculoAdminRequest {
    return {
      marca: '',
      modelo: '',
      categoria: '',
      precio: 0,
      imagenUrl: null,
      activo: true
    };
  }

  private normalizarTextoOpcional(value: string | null | undefined): string | null {
    if (!value) return null;

    const limpio = value.trim();

    return limpio.length ? limpio : null;
  }
}
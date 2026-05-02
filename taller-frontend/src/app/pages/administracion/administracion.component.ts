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
  RangoAdmin
} from '../../core/services/admin.service';

type AdminTab = 'empleados' | 'primas' | 'precios';

@Component({
  selector: 'app-administracion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: 'administracion.component.html',
  styleUrls: ['administracion.component.css']
})
export class AdministracionComponent implements OnInit {
  empleadoSesion: SessionEmpleado | null = null;

  activeTab: AdminTab = 'empleados';

  empleados: EmpleadoAdmin[] = [];
  rangos: RangoAdmin[] = [];

  loading = false;
  error = '';

  empleadoAbiertoId: number | null = null;

  constructor(
    private sessionService: SessionService,
    private adminService: AdminService,
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
              this.empleados = empleados ?? [];
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

  toggleEmpleado(empleado: EmpleadoAdmin): void {
    this.empleadoAbiertoId = this.empleadoAbiertoId === empleado.id ? null : empleado.id;
  }

  isEmpleadoAbierto(empleado: EmpleadoAdmin): boolean {
    return this.empleadoAbiertoId === empleado.id;
  }

  cambiarRango(empleado: EmpleadoAdmin, rangoIdValue: string | number): void {
    const rangoId = Number(rangoIdValue);

    if (!rangoId || rangoId === empleado.rangoId) {
      return;
    }

    const rango = this.rangos.find(r => r.id === rangoId);

    const confirmar = confirm(
      `¿Cambiar el rango de "${empleado.nombre}" a "${rango?.nombre ?? 'nuevo rango'}"?`
    );

    if (!confirmar) {
      return;
    }

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

    if (!confirmar) {
      return;
    }

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

  private actualizarEmpleadoEnLista(actualizado: EmpleadoAdmin): void {
    this.zone.run(() => {
      this.empleados = this.empleados.map(e =>
        e.id === actualizado.id ? actualizado : e
      );

      this.empleados = [...this.empleados].sort((a, b) => {
        if (a.activo !== b.activo) {
          return a.activo ? -1 : 1;
        }

        return a.nombre.localeCompare(b.nombre);
      });

      this.cdr.detectChanges();
    });
  }
}
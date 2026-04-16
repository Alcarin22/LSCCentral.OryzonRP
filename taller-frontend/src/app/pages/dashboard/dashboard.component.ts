import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { SessionEmpleado, SessionService } from '../../core/services/session.service';
import {
  DashboardHoyResponse,
  DashboardMesResponse,
  DashboardSemanaResponse,
  DashboardService
} from '../../core/services/dashboard.service';
import { FichajeResponse, FichajeService } from '../../core/services/fichaje.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit, OnDestroy {
  empleado: SessionEmpleado | null = null;

  resumenHoy: DashboardHoyResponse = this.getResumenHoyVacio();
  resumenSemana: DashboardSemanaResponse = this.getResumenSemanaVacio();
  resumenMes: DashboardMesResponse = this.getResumenMesVacio();

  fichajeActivo = false;
  fechaHoraEntrada: string | null = null;
  minutosTrabajados: number | null = null;

  cargandoHoy = false;
  cargandoSemana = false;
  cargandoMes = false;
  cargandoFichaje = false;
  procesandoToggle = false;

  private subs: Subscription[] = [];

  constructor(
    private sessionService: SessionService,
    private dashboardService: DashboardService,
    private fichajeService: FichajeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const empleado = this.sessionService.getEmpleado();

    if (!empleado?.discordId) {
      this.empleado = null;
      this.resetDashboard();
      this.cdr.detectChanges();
      return;
    }

    this.empleado = empleado;
    this.cargarTodo();

    const sessionSub = this.sessionService.empleado$.subscribe((empleadoSesion) => {
      if (!empleadoSesion?.discordId) {
        this.empleado = null;
        this.resetDashboard();
        this.cdr.detectChanges();
        return;
      }

      if (!this.empleado || this.empleado.discordId !== empleadoSesion.discordId) {
        this.empleado = empleadoSesion;
        this.cargarTodo();
      }
    });

    this.subs.push(sessionSub);
  }

  ngOnDestroy(): void {
    this.subs.forEach((sub) => sub.unsubscribe());
  }

  cargarTodo(): void {
    if (!this.empleado?.discordId) {
      this.resetDashboard();
      this.cdr.detectChanges();
      return;
    }

    this.cargarResumenHoy();
    this.cargarResumenSemana();
    this.cargarResumenMes();
    this.cargarEstadoFichaje();
  }

  toggleFichaje(): void {
    if (!this.empleado?.discordId || this.procesandoToggle) {
      return;
    }

    this.procesandoToggle = true;
    this.cdr.detectChanges();

    const sub = this.fichajeService.toggleFichaje(this.empleado.discordId).subscribe({
      next: (response: FichajeResponse) => {
        console.log('Respuesta toggle fichaje:', response);

        this.fichajeActivo = response.fichajeActivo ?? false;
        this.fechaHoraEntrada = response.fechaHoraEntrada ?? null;
        this.minutosTrabajados = response.minutosTrabajados ?? null;

        this.procesandoToggle = false;

        this.cargarResumenHoy();
        this.cargarResumenSemana();
        this.cargarResumenMes();
        this.cargarEstadoFichaje();

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al cambiar fichaje:', error);
        this.procesandoToggle = false;
        this.cdr.detectChanges();
      }
    });

    this.subs.push(sub);
  }

  cargarResumenHoy(): void {
    if (!this.empleado?.discordId) {
      return;
    }

    this.cargandoHoy = true;
    this.cdr.detectChanges();

    const sub = this.dashboardService.getResumenHoy(this.empleado.discordId).subscribe({
      next: (response: DashboardHoyResponse) => {
        console.log('Resumen hoy:', response);

        this.resumenHoy = {
          horaEntrada: response.horaEntrada ?? null,
          serviciosRealizadosHoy: response.serviciosRealizadosHoy ?? 0,
          fichajeActivo: response.fichajeActivo ?? false
        };

        this.cargandoHoy = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar resumen de hoy:', error);
        this.resumenHoy = this.getResumenHoyVacio();
        this.cargandoHoy = false;
        this.cdr.detectChanges();
      }
    });

    this.subs.push(sub);
  }

  cargarResumenSemana(): void {
    if (!this.empleado?.discordId) {
      return;
    }

    this.cargandoSemana = true;
    this.cdr.detectChanges();

    const sub = this.dashboardService.getResumenSemana(this.empleado.discordId).subscribe({
      next: (response: DashboardSemanaResponse) => {
        console.log('Resumen semana:', response);

        this.resumenSemana = {
          horasRegistradas: response.horasRegistradas ?? '0h 0m',
          diasTrabajados: response.diasTrabajados ?? 0,
          serviciosCompletados: response.serviciosCompletados ?? 0,
          primaEstimada: response.primaEstimada ?? '$0'
        };

        this.cargandoSemana = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar resumen semanal:', error);
        this.resumenSemana = this.getResumenSemanaVacio();
        this.cargandoSemana = false;
        this.cdr.detectChanges();
      }
    });

    this.subs.push(sub);
  }

  cargarResumenMes(): void {
    if (!this.empleado?.discordId) {
      return;
    }

    this.cargandoMes = true;
    this.cdr.detectChanges();

    const sub = this.dashboardService.getResumenMes(this.empleado.discordId).subscribe({
      next: (response: DashboardMesResponse) => {
        console.log('Resumen mes:', response);

        this.resumenMes = {
          horasTotales: response.horasTotales ?? '0h 0m',
          jornadasCompletadas: response.jornadasCompletadas ?? 0,
          serviciosRealizados: response.serviciosRealizados ?? 0,
          rendimiento: response.rendimiento ?? 'Bajo'
        };

        this.cargandoMes = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar resumen mensual:', error);
        this.resumenMes = this.getResumenMesVacio();
        this.cargandoMes = false;
        this.cdr.detectChanges();
      }
    });

    this.subs.push(sub);
  }

  cargarEstadoFichaje(): void {
    if (!this.empleado?.discordId) {
      return;
    }

    this.cargandoFichaje = true;
    this.cdr.detectChanges();

    const sub = this.fichajeService.obtenerEstado(this.empleado.discordId).subscribe({
      next: (response: FichajeResponse) => {
        console.log('Estado fichaje:', response);

        this.fichajeActivo = response.fichajeActivo ?? false;
        this.fechaHoraEntrada = response.fechaHoraEntrada ?? null;
        this.minutosTrabajados = response.minutosTrabajados ?? null;

        this.cargandoFichaje = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar estado de fichaje:', error);
        this.fichajeActivo = false;
        this.fechaHoraEntrada = null;
        this.minutosTrabajados = null;
        this.cargandoFichaje = false;
        this.cdr.detectChanges();
      }
    });

    this.subs.push(sub);
  }

  get nombreVisible(): string {
    if (!this.empleado) {
      return 'Sin sesión';
    }

    return this.empleado.nickServidor || this.empleado.nombre || 'Sin sesión';
  }

  get rangoVisible(): string {
    if (!this.empleado?.rango) {
      return 'Sin rango';
    }

    if (typeof this.empleado.rango === 'string') {
      return this.empleado.rango;
    }

    return this.empleado.rango.nombre || 'Sin rango';
  }

  get textoBotonFichaje(): string {
    if (this.procesandoToggle) {
      return 'Procesando...';
    }

    return this.fichajeActivo ? 'Finalizar fichaje' : 'Empezar fichaje';
  }

  get estadoActualTexto(): string {
    return this.fichajeActivo ? 'En turno' : 'Sin iniciar';
  }

  get tiempoTrabajadoActual(): string {
    if (this.minutosTrabajados == null) {
      return '0h 0m';
    }

    return this.formatearMinutos(this.minutosTrabajados);
  }

  get horaEntradaFormateada(): string {
    if (!this.resumenHoy.horaEntrada) {
      return '--:--';
    }

    const date = new Date(this.resumenHoy.horaEntrada);

    if (Number.isNaN(date.getTime())) {
      return this.resumenHoy.horaEntrada;
    }

    return date.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  private resetDashboard(): void {
    this.resumenHoy = this.getResumenHoyVacio();
    this.resumenSemana = this.getResumenSemanaVacio();
    this.resumenMes = this.getResumenMesVacio();
    this.fichajeActivo = false;
    this.fechaHoraEntrada = null;
    this.minutosTrabajados = null;
  }

  private getResumenHoyVacio(): DashboardHoyResponse {
    return {
      horaEntrada: null,
      serviciosRealizadosHoy: 0,
      fichajeActivo: false
    };
  }

  private getResumenSemanaVacio(): DashboardSemanaResponse {
    return {
      horasRegistradas: '0h 0m',
      diasTrabajados: 0,
      serviciosCompletados: 0,
      primaEstimada: '$0'
    };
  }

  private getResumenMesVacio(): DashboardMesResponse {
    return {
      horasTotales: '0h 0m',
      jornadasCompletadas: 0,
      serviciosRealizados: 0,
      rendimiento: 'Bajo'
    };
  }

  private formatearMinutos(minutos: number): string {
    const horas = Math.floor(minutos / 60);
    const resto = minutos % 60;
    return `${horas}h ${resto}m`;
  }
}
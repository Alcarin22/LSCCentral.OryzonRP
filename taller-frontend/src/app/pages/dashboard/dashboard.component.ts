import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Subscription, forkJoin, interval } from 'rxjs';

import { SessionEmpleado, SessionService } from '../../core/services/session.service';
import { environment } from '../../../environments/environment';
import { FichajeService, FichajeResponse } from '../../core/services/fichaje.service';

interface DashboardHoyResponse {
  horaEntrada: string | null;
  fichajeActivo: boolean;
  serviciosRealizadosHoy: number;
}

interface DashboardSemanaResponse {
  horasRegistradas: string;
  diasTrabajados: number;
  serviciosCompletados: number;
  primaEstimada: string;
}

interface DashboardMesResponse {
  horasTotales: string;
  jornadasCompletadas: number;
  serviciosRealizados: number;
  rendimiento: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  empleado: SessionEmpleado | null = null;

  nombreVisible = 'Empleado';
  rangoVisible = 'Sin rango';

  procesandoToggle = false;
  fichado = false;

  textoBotonFichaje = 'Iniciar fichaje';
  horaEntradaFormateada = '--:--';
  tiempoTrabajadoActual = '00:00:00';
  estadoActualTexto = 'Fuera de servicio';

  resumenHoy = {
    serviciosRealizadosHoy: 0
  };

  resumenSemana = {
    horasRegistradas: '0h 0m',
    diasTrabajados: 0,
    serviciosCompletados: 0,
    primaEstimada: '$0'
  };

  resumenMes = {
    horasTotales: '0h 0m',
    jornadasCompletadas: 0,
    serviciosRealizados: 0,
    rendimiento: 'Bajo'
  };

  private sub!: Subscription;
  private timerSub!: Subscription;
  private fechaEntrada: Date | null = null;
  private requestVersion = 0;

  constructor(
    private sessionService: SessionService,
    private http: HttpClient,
    private fichajeService: FichajeService
  ) {}

  ngOnInit(): void {
    this.sub = this.sessionService.empleado$.subscribe((empleadoSesion: SessionEmpleado | null) => {
      this.empleado = empleadoSesion;
      this.nombreVisible = empleadoSesion?.nickServidor || empleadoSesion?.nombre || 'Empleado';
      this.rangoVisible = empleadoSesion?.rango?.nombre || 'Sin rango';

      if (empleadoSesion?.discordId) {
        this.cargarDashboardCompleto(empleadoSesion.discordId);
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.timerSub?.unsubscribe();
  }

  toggleFichaje(): void {
    if (!this.empleado?.discordId || this.procesandoToggle) return;

    this.procesandoToggle = true;

    this.fichajeService.toggleFichaje(this.empleado.discordId).subscribe({
      next: (response: FichajeResponse) => {
        this.aplicarEstadoDesdeToggle(response);
        this.procesandoToggle = false;

        if (this.empleado?.discordId) {
          this.cargarDashboardCompleto(this.empleado.discordId);
        }
      },
      error: (error) => {
        console.error('Error al hacer toggle de fichaje:', error);
        this.procesandoToggle = false;
        alert('No se pudo actualizar el fichaje.');
      }
    });
  }

  private cargarDashboardCompleto(discordId: string): void {
    const baseUrl = environment.backendUrl;
    const currentVersion = ++this.requestVersion;

    forkJoin({
      hoy: this.http.get<DashboardHoyResponse>(`${baseUrl}/api/dashboard/hoy/${discordId}`),
      semana: this.http.get<DashboardSemanaResponse>(`${baseUrl}/api/dashboard/semana/${discordId}`),
      mes: this.http.get<DashboardMesResponse>(`${baseUrl}/api/dashboard/mes/${discordId}`)
    }).subscribe({
      next: ({ hoy, semana, mes }) => {
        if (currentVersion !== this.requestVersion) return;

        this.aplicarEstadoDesdeDashboardHoy(hoy);

        this.resumenSemana = {
          horasRegistradas: semana.horasRegistradas ?? '0h 0m',
          diasTrabajados: semana.diasTrabajados ?? 0,
          serviciosCompletados: semana.serviciosCompletados ?? 0,
          primaEstimada: semana.primaEstimada ?? '$0'
        };

        this.resumenMes = {
          horasTotales: mes.horasTotales ?? '0h 0m',
          jornadasCompletadas: mes.jornadasCompletadas ?? 0,
          serviciosRealizados: mes.serviciosRealizados ?? 0,
          rendimiento: mes.rendimiento ?? 'Bajo'
        };
      },
      error: (error) => {
        console.error('Error cargando dashboard real:', error);
      }
    });
  }

  private aplicarEstadoDesdeToggle(response: FichajeResponse): void {
    this.fichado = !!response.fichajeActivo;

    if (this.fichado) {
      this.estadoActualTexto = 'En servicio';
      this.textoBotonFichaje = 'Finalizar fichaje';

      if (response.fechaHoraEntrada) {
        this.fechaEntrada = this.parseLocalDateTime(response.fechaHoraEntrada);
        if (this.fechaEntrada) {
          this.horaEntradaFormateada = this.formatearHora(this.fechaEntrada);
          this.iniciarTemporizador();
        }
      }
    } else {
      this.estadoActualTexto = 'Fuera de servicio';
      this.textoBotonFichaje = 'Iniciar fichaje';
      this.resetEstadoFichajeVisual();
    }
  }

  private aplicarEstadoDesdeDashboardHoy(hoy: DashboardHoyResponse): void {
    if (hoy.fichajeActivo === undefined) {
      console.warn('Respuesta inválida de dashboard hoy:', hoy);
      return;
    }

    this.resumenHoy = {
      serviciosRealizadosHoy: hoy.serviciosRealizadosHoy ?? 0
    };

    this.fichado = !!hoy.fichajeActivo;

    if (this.fichado && hoy.horaEntrada) {
      this.estadoActualTexto = 'En servicio';
      this.textoBotonFichaje = 'Finalizar fichaje';

      this.fechaEntrada = this.parseLocalDateTime(hoy.horaEntrada);
      if (this.fechaEntrada) {
        this.horaEntradaFormateada = this.formatearHora(this.fechaEntrada);
        this.iniciarTemporizador();
      }
    } else {
      this.estadoActualTexto = 'Fuera de servicio';
      this.textoBotonFichaje = 'Iniciar fichaje';
      this.resetEstadoFichajeVisual();
    }
  }

  private resetEstadoFichajeVisual(): void {
    this.fichado = false;
    this.fechaEntrada = null;
    this.horaEntradaFormateada = '--:--';
    this.tiempoTrabajadoActual = '00:00:00';
    this.timerSub?.unsubscribe();
  }

  private iniciarTemporizador(): void {
    this.timerSub?.unsubscribe();

    if (!this.fechaEntrada) {
      this.tiempoTrabajadoActual = '00:00:00';
      return;
    }

    this.tiempoTrabajadoActual = this.formatearDuracion(
      Math.floor((Date.now() - this.fechaEntrada.getTime()) / 1000)
    );

    this.timerSub = interval(1000).subscribe(() => {
      if (!this.fechaEntrada) return;

      this.tiempoTrabajadoActual = this.formatearDuracion(
        Math.floor((Date.now() - this.fechaEntrada.getTime()) / 1000)
      );
    });
  }

  private parseLocalDateTime(value: string | null | undefined): Date | null {
    if (!value) return null;

    const [datePart, timePart] = value.split('T');
    if (!datePart || !timePart) return null;

    const [year, month, day] = datePart.split('-').map(Number);
    const [hour, minute, secondWithMs] = timePart.split(':');
    const second = Number((secondWithMs ?? '0').split('.')[0]);

    return new Date(
      year,
      month - 1,
      day,
      Number(hour),
      Number(minute),
      second
    );
  }

  private formatearHora(fecha: Date): string {
    return fecha.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  private formatearDuracion(totalSegundos: number): string {
    const horas = Math.floor(totalSegundos / 3600);
    const minutos = Math.floor((totalSegundos % 3600) / 60);
    const segundos = totalSegundos % 60;

    return [horas, minutos, segundos]
      .map(v => String(v).padStart(2, '0'))
      .join(':');
  }
}
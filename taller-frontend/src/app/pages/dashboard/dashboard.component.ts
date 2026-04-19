import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription, interval } from 'rxjs';

import { SessionEmpleado, SessionService } from '../../core/services/session.service';

interface ResumenHoy {
  serviciosRealizadosHoy: number;
}

interface ResumenSemana {
  horasRegistradas: string;
  diasTrabajados: number;
  serviciosCompletados: number;
  primaEstimada: string;
}

interface ResumenMes {
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

  resumenHoy: ResumenHoy = {
    serviciosRealizadosHoy: 0
  };

  resumenSemana: ResumenSemana = {
    horasRegistradas: '00:00',
    diasTrabajados: 0,
    serviciosCompletados: 0,
    primaEstimada: '$0'
  };

  resumenMes: ResumenMes = {
    horasTotales: '00:00',
    jornadasCompletadas: 0,
    serviciosRealizados: 0,
    rendimiento: '0%'
  };

  private sub!: Subscription;
  private timerSub!: Subscription;
  private fechaEntrada: Date | null = null;

  constructor(private sessionService: SessionService) {}

  ngOnInit(): void {
    this.sub = this.sessionService.empleado$
      .subscribe((empleadoSesion: SessionEmpleado | null) => {
        this.empleado = empleadoSesion;
        this.nombreVisible = empleadoSesion?.nickServidor || empleadoSesion?.nombre || 'Empleado';
        this.rangoVisible = empleadoSesion?.rango?.nombre || 'Sin rango';
      });

    this.cargarResumenesMock();
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.timerSub?.unsubscribe();
  }

  toggleFichaje(): void {
    this.procesandoToggle = true;

    setTimeout(() => {
      if (!this.fichado) {
        this.iniciarFichaje();
      } else {
        this.detenerFichaje();
      }
      this.procesandoToggle = false;
    }, 250);
  }

  private iniciarFichaje(): void {
    this.fichado = true;
    this.fechaEntrada = new Date();
    this.horaEntradaFormateada = this.formatearHora(this.fechaEntrada);
    this.estadoActualTexto = 'En servicio';
    this.textoBotonFichaje = 'Finalizar fichaje';

    this.timerSub?.unsubscribe();
    this.timerSub = interval(1000).subscribe(() => {
      if (!this.fechaEntrada) return;
      this.tiempoTrabajadoActual = this.formatearDuracion(
        Math.floor((Date.now() - this.fechaEntrada.getTime()) / 1000)
      );
    });
  }

  private detenerFichaje(): void {
    this.fichado = false;
    this.estadoActualTexto = 'Fuera de servicio';
    this.textoBotonFichaje = 'Iniciar fichaje';
    this.timerSub?.unsubscribe();
  }

  private cargarResumenesMock(): void {
    this.resumenHoy = {
      serviciosRealizadosHoy: 4
    };

    this.resumenSemana = {
      horasRegistradas: '18:30',
      diasTrabajados: 4,
      serviciosCompletados: 11,
      primaEstimada: '$4,850'
    };

    this.resumenMes = {
      horasTotales: '76:15',
      jornadasCompletadas: 16,
      serviciosRealizados: 41,
      rendimiento: '87%'
    };
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
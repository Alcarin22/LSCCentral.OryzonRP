import { ChangeDetectorRef, Component, NgZone, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import {
  FichajeService,
  FichajeListado,
  FichajeFiltros
} from '../../core/services/fichaje.service';

@Component({
  selector: 'app-fichajes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: 'fichajes.component.html',
  styleUrls: ['fichajes.component.css']
})
export class FichajesComponent implements OnInit {
  fichajes: FichajeListado[] = [];

  loading = false;
  error = '';

  filtros: FichajeFiltros = {
    fechaInicio: '',
    fechaFin: ''
  };

  fichajeAbiertoId: number | null = null;

  constructor(
    private fichajeService: FichajeService,
    private zone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.setSemanaActual();
  }

  async buscar(): Promise<void> {
    this.zone.run(() => {
      this.loading = true;
      this.error = '';
      this.cdr.detectChanges();
    });

    try {
      const response = await firstValueFrom(
        this.fichajeService.listarFichajes(this.filtros)
      );

      this.zone.run(() => {
        this.fichajes = response ?? [];
        this.loading = false;
        this.error = '';
        this.cdr.detectChanges();
      });
    } catch (error) {
      console.error('ERROR CARGANDO FICHAJES:', error);

      this.zone.run(() => {
        this.fichajes = [];
        this.error = 'No se pudieron cargar los fichajes.';
        this.loading = false;
        this.cdr.detectChanges();
      });
    }
  }

  limpiar(): void {
    this.setSemanaActual();
  }

  setSemanaActual(): void {
    const hoy = new Date();
    const lunes = this.getLunesSemana(hoy);

    const domingo = new Date(lunes);
    domingo.setDate(lunes.getDate() + 6);

    this.filtros.fechaInicio = this.toInputDate(lunes);
    this.filtros.fechaFin = this.toInputDate(domingo);

    this.buscar();
  }

  setSemanaAnterior(): void {
    const hoy = new Date();
    const lunes = this.getLunesSemana(hoy);

    lunes.setDate(lunes.getDate() - 7);

    const domingo = new Date(lunes);
    domingo.setDate(lunes.getDate() + 6);

    this.filtros.fechaInicio = this.toInputDate(lunes);
    this.filtros.fechaFin = this.toInputDate(domingo);

    this.buscar();
  }

  toggleDetalle(fichaje: FichajeListado): void {
    this.fichajeAbiertoId = this.fichajeAbiertoId === fichaje.id ? null : fichaje.id;
  }

  isFichajeAbierto(fichaje: FichajeListado): boolean {
    return this.fichajeAbiertoId === fichaje.id;
  }

  get totalFichajes(): number {
    return this.fichajes.length;
  }

  get fichajesActivos(): number {
    return this.fichajes.filter(f => f.activo).length;
  }

  get minutosTotales(): number {
    return this.fichajes.reduce((acc, f) => acc + (f.minutosTrabajados || 0), 0);
  }

  get horasTotales(): string {
    return this.formatearDuracion(this.minutosTotales);
  }

  get promedioFichaje(): string {
    const fichajesCerrados = this.fichajes.filter(f => !f.activo && f.minutosTrabajados);

    if (!fichajesCerrados.length) {
      return '0h 00m';
    }

    const total = fichajesCerrados.reduce((acc, f) => acc + (f.minutosTrabajados || 0), 0);
    const promedio = Math.round(total / fichajesCerrados.length);

    return this.formatearDuracion(promedio);
  }

  formatearFecha(fecha: string | null): Date | null {
    if (!fecha) return null;
    return new Date(fecha);
  }

  formatearDuracion(minutos: number | null | undefined): string {
    const total = minutos ?? 0;
    const horas = Math.floor(total / 60);
    const mins = total % 60;

    return `${horas}h ${String(mins).padStart(2, '0')}m`;
  }

  getEstadoTexto(fichaje: FichajeListado): string {
    return fichaje.activo ? 'Activo' : 'Finalizado';
  }

  private getLunesSemana(fecha: Date): Date {
    const copia = new Date(fecha);
    const dia = copia.getDay();
    const diff = dia === 0 ? -6 : 1 - dia;

    copia.setDate(copia.getDate() + diff);
    copia.setHours(0, 0, 0, 0);

    return copia;
  }

  private toInputDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
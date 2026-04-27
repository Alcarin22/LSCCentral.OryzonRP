import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FacturaService,
  ReparacionDto,
  ItemDto,
  TasacionPrecioDto,
  FullTuningDto,
  TuneoDto
} from '../../../app/services/factura.service';

@Component({
  selector: 'app-precios',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './precios.component.html',
  styleUrls: ['./precios.component.css']
})
export class PreciosComponent implements OnInit {
  reparaciones: ReparacionDto[] = [];
  items: ItemDto[] = [];
  tasaciones: TasacionPrecioDto[] = [];
  fullTuning: FullTuningDto[] = [];
  tuneos: TuneoDto[] = [];

  convenios = [10, 15, 20];

  loading = false;
  error = '';

  constructor(private facturaService: FacturaService) {}

  ngOnInit(): void {
    this.cargarPrecios();
  }

  cargarPrecios(): void {
    this.loading = true;
    this.error = '';

    let peticionesPendientes = 5;

    const finalizar = (): void => {
      peticionesPendientes--;

      if (peticionesPendientes === 0) {
        this.loading = false;
      }
    };

    this.facturaService.getFullTuning().subscribe({
      next: data => {
        this.fullTuning = data ?? [];
        finalizar();
      },
      error: error => {
        console.error('Error cargando full tuning:', error);
        this.error = 'No se pudieron cargar todos los precios.';
        finalizar();
      }
    });

    this.facturaService.getReparaciones().subscribe({
      next: data => {
        this.reparaciones = data ?? [];
        finalizar();
      },
      error: error => {
        console.error('Error cargando reparaciones:', error);
        this.error = 'No se pudieron cargar todos los precios.';
        finalizar();
      }
    });

    this.facturaService.getTuneo().subscribe({
      next: data => {
        this.tuneos = data ?? [];
        finalizar();
      },
      error: error => {
        console.error('Error cargando tuneo:', error);
        this.error = 'No se pudieron cargar todos los precios.';
        finalizar();
      }
    });

    this.facturaService.getItems().subscribe({
      next: data => {
        this.items = data ?? [];
        finalizar();
      },
      error: error => {
        console.error('Error cargando items:', error);
        this.error = 'No se pudieron cargar todos los precios.';
        finalizar();
      }
    });

    this.facturaService.getTasacionPrecios().subscribe({
      next: data => {
        this.tasaciones = data ?? [];
        finalizar();
      },
      error: error => {
        console.error('Error cargando tasaciones:', error);
        this.error = 'No se pudieron cargar todos los precios.';
        finalizar();
      }
    });
  }

  formatearDinero(valor: number | null | undefined): string {
    const precio = valor ?? 0;

    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      maximumFractionDigits: 0
    }).format(precio);
  }

  getPrecioTuneo(tuneo: TuneoDto): string {
    if (tuneo.rendimiento) {
      return `${tuneo.rendimiento}%`;
    }

    return this.formatearDinero(tuneo.precio);
  }
}
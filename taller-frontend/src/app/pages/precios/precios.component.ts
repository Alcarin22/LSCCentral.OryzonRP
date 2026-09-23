import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface PrecioFila {
  tipo: string;
  precio: string;
}

@Component({
  selector: 'app-precios',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './precios.component.html',
  styleUrls: ['./precios.component.css']
})
export class PreciosComponent implements OnInit {
  fullTuning: PrecioFila[] = [];
  reparaciones: PrecioFila[] = [];
  tuneos: PrecioFila[] = [];
  kits: PrecioFila[] = [];

  convenios = ['10%', '15%', '20%'];

  loading = false;
  error = '';

  ngOnInit(): void {
    this.cargarPreciosLocales();
  }

  cargarPreciosLocales(): void {
    this.fullTuning = [
      { tipo: 'Compacto', precio: '$5.500' },
      { tipo: 'Coupe', precio: '$6.500' },
      { tipo: 'Moto', precio: '$5.500' },
      { tipo: 'Muscle', precio: '$6.500' },
      { tipo: 'Offroad', precio: '$10.500' },
      { tipo: 'Sedan', precio: '$6.500' },
      { tipo: 'SUV', precio: '$9.000' },
      { tipo: 'Deportivo', precio: '$13.000' },
      { tipo: 'Deportivo Clasico', precio: '$13.000' },
      { tipo: 'Van', precio: '$5.500' },
      { tipo: 'Super', precio: '$21.000' },
      { tipo: 'VIP', precio: '$10.000' }
    ];

    this.reparaciones = [
      { tipo: 'Basica (1-2)', precio: '$600' },
      { tipo: 'Media (3-4)', precio: '$700' },
      { tipo: 'Avanzada (5-6)', precio: '$800' },
      { tipo: 'LSPD', precio: '$200' },
      { tipo: 'Grua', precio: '$600' }
    ];

    this.tuneos = [
      { tipo: 'Pieza Rendimiento', precio: '30%' },
      { tipo: 'Pieza Estetica', precio: '$1.000' },
      { tipo: 'Livery/P.Ruedas', precio: '$500' },
      { tipo: 'Pintura', precio: '$2.000' }
    ];

    this.kits = [
      { tipo: 'Kit Reparación', precio: '$1.000' },
      { tipo: 'Peluche Torque', precio: '$500' },
      { tipo: 'Peluche Chispa', precio: '$500' },
      { tipo: 'Peluche Remache', precio: '$500' },
      { tipo: 'Peluche Volt', precio: '$500' },
      { tipo: 'Peluche Piston', precio: '$500' },
      { tipo: 'Peluche Nitro', precio: '$500' }
    ];
  }
}
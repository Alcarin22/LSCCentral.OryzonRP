import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  SessionEmpleado,
  SessionService
} from '../../core/services/session.service';

import {
  FacturaService,
  CreateFacturaRequest,
  ReparacionDto,
  ItemDto,
  TasacionPrecioDto,
  FullTuningDto,
  TuneoDto
} from '../../../app/services/factura.service';

import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-factura',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './factura.component.html',
  styleUrls: ['./factura.component.css']
})
export class FacturaComponent implements OnInit {
  empleado: SessionEmpleado | null = null;

  tipoSeleccionado = '';
  total = 0;

  matricula = '';
  modelo = '';
  estado = 'SERIE';
  convenio = false;
  cantidad = 1;
  item = '';
  otros = '';

  categoria = '';
  tuneoPlate = '';
  gravedad = '';
  grua = false;

  tuneoRendimientoOpciones: string[] = [
    'Motor',
    'Frenos',
    'Transmisión',
    'Suspensión',
    'Blindaje',
    'Turbo'
  ];

  tuneoEsteticaOpciones: string[] = [
    'Aleron',
    'Parachoques Delantero',
    'Parachoques Trasero',
    'Falda Lateral',
    'Escape',
    'Jaula Antivuelco',
    'Reja',
    'Capo',
    'Guardabarros Derecho',
    'Guardabarros Izquierdo',
    'Techo',
    'Trim A',
    'Ornamentas',
    'Panel',
    'Marcador',
    'Altavoz de puerta',
    'Asientos',
    'Volante',
    'Palanca de cambios',
    'Placa',
    'Maletero',
    'Hydraulica',
    'Bloque Motor',
    'Filtro de aire',
    'Cubierta de arco',
    'Antena',
    'Trim B',
    'Deposito de combustible',
    'Livery',
    'Claxon',
    'Pintura',
    'Window Tint',
    'Neon',
    'Faro Xenon',
    'Humo Neumatico',
    'Llanta',
    'Pintura Llantas',
    'Old Livery',
    'Plate Index'
  ];

  tuneoSeleccionados: string[] = [];

  reparaciones: ReparacionDto[] = [];
  itemsDisponibles: ItemDto[] = [];
  tasacionPrecios: TasacionPrecioDto[] = [];
  fullTuningDisponibles: FullTuningDto[] = [];
  tuneoDisponibles: TuneoDto[] = [];

  enviando = false;
  cargandoReparaciones = false;
  cargandoItems = false;
  cargandoTasacionPrecios = false;
  cargandoFullTuning = false;
  cargandoTuneo = false;

  constructor(
    private sessionService: SessionService,
    private facturaService: FacturaService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.empleado = this.sessionService.getEmpleado();

    this.cargarReparaciones();
    this.cargarItems();
    this.cargarTasacionPrecios();
    this.cargarFullTuning();
    this.cargarTuneo();

    this.actualizarTotal();
  }

  cargarReparaciones(): void {
    this.cargandoReparaciones = true;

    this.facturaService.getReparaciones().subscribe({
      next: (data) => {
        this.reparaciones = data ?? [];
        this.cargandoReparaciones = false;
        this.actualizarTotal();
      },
      error: (error) => {
        console.error('Error cargando reparaciones:', error);
        this.cargandoReparaciones = false;
        this.toastService.error('No se pudieron cargar las reparaciones.');
      }
    });
  }

  cargarItems(): void {
    this.cargandoItems = true;

    this.facturaService.getItems().subscribe({
      next: (data) => {
        this.itemsDisponibles = data ?? [];
        this.cargandoItems = false;
        this.actualizarTotal();
      },
      error: (error) => {
        console.error('Error cargando items:', error);
        this.cargandoItems = false;
        this.toastService.error('No se pudieron cargar los items.');
      }
    });
  }

  cargarTasacionPrecios(): void {
    this.cargandoTasacionPrecios = true;

    this.facturaService.getTasacionPrecios().subscribe({
      next: (data) => {
        this.tasacionPrecios = data ?? [];
        this.cargandoTasacionPrecios = false;
        this.actualizarTotal();
      },
      error: (error) => {
        console.error('Error cargando precios de tasación:', error);
        this.cargandoTasacionPrecios = false;
        this.toastService.error('No se pudieron cargar los precios de tasación.');
      }
    });
  }

  cargarFullTuning(): void {
    this.cargandoFullTuning = true;

    this.facturaService.getFullTuning().subscribe({
      next: (data) => {
        this.fullTuningDisponibles = data ?? [];
        this.cargandoFullTuning = false;
        this.actualizarTotal();
      },
      error: (error) => {
        console.error('Error cargando full tuning:', error);
        this.cargandoFullTuning = false;
        this.toastService.error('No se pudieron cargar los precios de Full Tuning.');
      }
    });
  }

  cargarTuneo(): void {
    this.cargandoTuneo = true;

    this.facturaService.getTuneo().subscribe({
      next: (data) => {
        this.tuneoDisponibles = data ?? [];
        this.cargandoTuneo = false;
        this.actualizarTotal();
      },
      error: (error) => {
        console.error('Error cargando tuneo:', error);
        this.cargandoTuneo = false;
        this.toastService.error('No se pudieron cargar los precios de tuneo.');
      }
    });
  }

  onTipoFacturaChange(): void {
    this.total = 0;

    if (this.tipoSeleccionado !== 'Reparación') {
      this.gravedad = '';
      this.grua = false;
    }

    if (this.tipoSeleccionado !== 'Items') {
      this.item = '';
      this.cantidad = 1;
    }

    if (this.tipoSeleccionado !== 'Tasación') {
      this.estado = 'SERIE';
      this.otros = '';
      this.modelo = '';
    }

    if (
      this.tipoSeleccionado !== 'Tasación' &&
      this.tipoSeleccionado !== 'Full Tuning'
    ) {
      this.matricula = '';
    }

    if (this.tipoSeleccionado !== 'Full Tuning' && this.tipoSeleccionado !== 'Tuneo') {
      this.categoria = '';
    }

    if (this.tipoSeleccionado !== 'Tuneo') {
      this.tuneoPlate = '';
      this.tuneoSeleccionados = [];
    }

    if (this.tipoSeleccionado === 'Tasación') {
      this.convenio = false;
    }

    this.actualizarTotal();
  }

  isTuneoSelected(opcion: string): boolean {
    return this.tuneoSeleccionados.includes(opcion);
  }

  toggleTuneoItem(opcion: string, checked: boolean): void {
    if (checked) {
      if (!this.tuneoSeleccionados.includes(opcion)) {
        this.tuneoSeleccionados.push(opcion);
      }
    } else {
      this.tuneoSeleccionados = this.tuneoSeleccionados.filter(i => i !== opcion);
    }

    this.actualizarTotal();
  }

  actualizarTotal(): void {
    let base = 0;

    switch (this.tipoSeleccionado) {
      case 'Reparación': {
        const reparacionSeleccionada = this.reparaciones.find(
          r => this.normalizarClave(r.tipo) === this.normalizarClave(this.gravedad)
        );

        base = reparacionSeleccionada?.precio ?? 0;

        if (this.grua) {
          base += 600;
        }

        break;
      }

      case 'Items': {
        const itemSeleccionado = this.itemsDisponibles.find(
          i => this.normalizarClave(i.nombre) === this.normalizarClave(this.item)
        );

        base = (itemSeleccionado?.precio ?? 0) * (this.cantidad || 1);
        break;
      }

      case 'Tasación': {
        const precioSeleccionado = this.tasacionPrecios.find(
          t => this.normalizarClave(t.estado) === this.normalizarClave(this.estado)
        );

        base = precioSeleccionado?.precio ?? 0;
        break;
      }

      case 'Full Tuning': {
        const fullTuningSeleccionado = this.fullTuningDisponibles.find(
          ft => this.normalizarClave(ft.categoria) === this.normalizarClave(this.categoria)
        );

        base = fullTuningSeleccionado?.precio ?? 0;
        break;
      }

      case 'Tuneo': {
        const fullTuningSeleccionado = this.fullTuningDisponibles.find(
          ft => this.normalizarClave(ft.categoria) === this.normalizarClave(this.categoria)
        );

        const precioFullTuning = fullTuningSeleccionado?.precio ?? 0;
        const precioPorRendimiento = precioFullTuning * 0.3;

        const totalRendimiento = this.tuneoSeleccionados
          .filter(pieza => this.tuneoRendimientoOpciones.includes(pieza))
          .length * precioPorRendimiento;

        const totalEstetica = this.tuneoSeleccionados
          .filter(pieza => this.tuneoEsteticaOpciones.includes(pieza))
          .reduce((acc, pieza) => {
            const clavePrecio = this.getClavePrecioTuneo(pieza);

            const tuneo = this.tuneoDisponibles.find(
              t => this.normalizarClave(t.pieza) === this.normalizarClave(clavePrecio)
            );

            return acc + (tuneo?.precio ?? 0);
          }, 0);

        base = totalRendimiento + totalEstetica;
        break;
      }

      default:
        base = 0;
    }

    if (this.convenio && this.tipoSeleccionado !== 'Tasación') {
      base = Math.round(base * 0.8);
    }

    this.total = Math.round(base);
  }

  enviarFactura(): void {
    if (!this.empleado?.discordId) {
      this.toastService.error('No hay sesión de empleado activa.');
      return;
    }

    if (!this.tipoSeleccionado) {
      this.toastService.warning('Debes seleccionar un tipo de factura.');
      return;
    }

    if (this.tipoSeleccionado === 'Reparación' && !this.gravedad) {
      this.toastService.warning('Debes seleccionar un tipo de reparación.');
      return;
    }

    if (this.tipoSeleccionado === 'Items' && !this.item) {
      this.toastService.warning('Debes seleccionar un item.');
      return;
    }

    if (this.tipoSeleccionado === 'Tasación') {
      if (!this.matricula.trim()) {
        this.toastService.warning('Debes indicar la matrícula del vehículo.');
        return;
      }

      if (!this.estado) {
        this.toastService.warning('Debes seleccionar un estado para la tasación.');
        return;
      }

      if (!this.modelo.trim()) {
        this.toastService.warning('Debes indicar el modelo del vehículo en la tasación.');
        return;
      }
    }

    if (this.tipoSeleccionado === 'Full Tuning') {
      if (!this.matricula.trim()) {
        this.toastService.warning('Debes indicar la matrícula del vehículo.');
        return;
      }

      if (!this.categoria) {
        this.toastService.warning('Debes seleccionar una categoría de Full Tuning.');
        return;
      }
    }

    if (this.tipoSeleccionado === 'Tuneo') {
      if (!this.tuneoPlate.trim()) {
        this.toastService.warning('Debes indicar la matrícula del vehículo.');
        return;
      }

      if (!this.categoria) {
        this.toastService.warning('Debes seleccionar la categoría del vehículo.');
        return;
      }

      if (this.tuneoSeleccionados.length === 0) {
        this.toastService.warning('Debes seleccionar al menos una pieza de tuneo.');
        return;
      }
    }

    if (this.enviando) {
      return;
    }

    const payload: CreateFacturaRequest = {
      discordId: this.empleado.discordId,
      matricula: this.obtenerMatriculaParaBackend(),
      tipo: this.tipoSeleccionado,
      total: this.total,
      convenio: this.tipoSeleccionado === 'Tasación' ? false : this.convenio,
      modelo: this.tipoSeleccionado === 'Reparación' || this.tipoSeleccionado === 'Items'
        ? null
        : this.normalizarTexto(this.modelo),
      estado: this.normalizarTexto(this.estado),
      cantidad: this.tipoSeleccionado === 'Items' ? this.cantidad : null,
      item: this.tipoSeleccionado === 'Items' ? this.normalizarTexto(this.item) : null,
      categoria: this.tipoSeleccionado === 'Full Tuning' || this.tipoSeleccionado === 'Tuneo'
        ? this.normalizarTexto(this.categoria)
        : null,
      gravedad: this.tipoSeleccionado === 'Reparación' ? this.normalizarTexto(this.gravedad) : null,
      tuneoPlate: this.tipoSeleccionado === 'Tuneo' ? this.normalizarTexto(this.tuneoPlate) : null,
      tuneoSeleccionados: this.tipoSeleccionado === 'Tuneo'
        ? this.tuneoSeleccionados.join(', ')
        : null,
      grua: this.tipoSeleccionado === 'Reparación' ? this.grua : false,
      otros: this.tipoSeleccionado === 'Tasación' ? this.normalizarTexto(this.otros) : null
    };

    this.enviando = true;

    this.facturaService.crearFactura(payload).subscribe({
      next: (response) => {
        this.toastService.success(`Factura guardada correctamente. Total final: $${response.total ?? this.total}`);
        this.resetFormulario();
        this.enviando = false;
      },
      error: (error) => {
        console.error('Error guardando factura:', error);
        this.toastService.error('No se pudo guardar la factura.');
        this.enviando = false;
      }
    });
  }

  private getClavePrecioTuneo(pieza: string): string {
    switch (pieza) {
      case 'Pintura':
        return 'Pintura';

      case 'Livery':
        return 'Vinilo';

      case 'Pintura Llantas':
        return 'Pintura de ruedas';

      default:
        return 'Parte estetica';
    }
  }

  private obtenerMatriculaParaBackend(): string | null {
    if (this.tipoSeleccionado === 'Tuneo') {
      return this.normalizarTexto(this.tuneoPlate);
    }

    return this.normalizarTexto(this.matricula);
  }

  private normalizarTexto(valor: string | null | undefined): string | null {
    if (!valor) {
      return null;
    }

    const limpio = valor.trim();
    return limpio.length ? limpio : null;
  }

  private normalizarClave(valor: string | null | undefined): string {
    if (!valor) {
      return '';
    }

    return valor
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toLowerCase();
  }

  private resetFormulario(): void {
    this.tipoSeleccionado = '';
    this.total = 0;
    this.matricula = '';
    this.modelo = '';
    this.estado = 'SERIE';
    this.convenio = false;
    this.cantidad = 1;
    this.item = '';
    this.otros = '';
    this.categoria = '';
    this.tuneoPlate = '';
    this.gravedad = '';
    this.grua = false;
    this.tuneoSeleccionados = [];
  }
}
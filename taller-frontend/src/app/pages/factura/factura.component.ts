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
  CreateFacturacionLoteRequest,
  ReparacionDto,
  ItemDto,
  TasacionPrecioDto,
  FullTuningDto,
  TuneoDto
} from '../../../app/services/factura.service';

import { ToastService } from '../../core/services/toast.service';

interface ElementoFacturacion {
  idTemporal: number;
  tipo: string;
  descripcion: string;
  total: number;
  payload: CreateFacturaRequest;
}

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
  lspd = false;
  cantidad = 1;
  item = '';
  otros = '';

  categoria = '';
  tuneoPlate = '';
  gravedad = '';
  grua = false;

  elementosFacturacion: ElementoFacturacion[] = [];
  private siguienteIdTemporal = 1;

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
    const nuevoTipo = this.tipoSeleccionado;
    this.resetCamposFormulario();
    this.tipoSeleccionado = nuevoTipo;
    this.actualizarTotal();
  }

  isTuneoSelected(opcion: string): boolean {
    return this.tuneoSeleccionados.includes(opcion);
  }

  tieneMejorasRendimientoSeleccionadas(): boolean {
    return this.tuneoSeleccionados.some(opcion =>
      this.tuneoRendimientoOpciones.includes(opcion)
    );
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

        const cantidad = Math.max(
          1,
          Number(this.cantidad) || 1
        );

        const nombreItem = this.normalizarClave(this.item);

        const esKitReparacion =
          nombreItem.includes('kit') &&
          nombreItem.includes('reparacion');

        if (this.lspd && esKitReparacion) {
          base = 500 * cantidad;
        } else {
          base = (itemSeleccionado?.precio ?? 0) * cantidad;
        }

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
        const tieneRendimiento = this.tieneMejorasRendimientoSeleccionadas();

        let totalRendimiento = 0;

        if (tieneRendimiento && this.categoria) {
          const fullTuningSeleccionado = this.fullTuningDisponibles.find(
            ft => this.normalizarClave(ft.categoria) === this.normalizarClave(this.categoria)
          );

          const precioFullTuning = fullTuningSeleccionado?.precio ?? 0;
          const precioPorRendimiento = precioFullTuning * 0.3;

          totalRendimiento = this.tuneoSeleccionados
            .filter(pieza => this.tuneoRendimientoOpciones.includes(pieza))
            .length * precioPorRendimiento;
        }

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

  agregarAFacturacion(): void {
    if (!this.empleado?.discordId) {
      this.toastService.error('No hay sesión de empleado activa.');
      return;
    }

    if (!this.validarFormularioActual()) {
      return;
    }

    const payload = this.construirPayloadActual();

    if (this.tipoSeleccionado === 'Items') {
      const existente = this.elementosFacturacion.find(elemento =>
        elemento.tipo === 'Items'
        && this.normalizarClave(elemento.payload.item) === this.normalizarClave(payload.item)
        && Boolean(elemento.payload.lspd) === Boolean(payload.lspd)
      );

      if (existente) {
        const cantidadActual = existente.payload.cantidad ?? 1;
        const cantidadNueva = payload.cantidad ?? 1;

        existente.payload.cantidad = cantidadActual + cantidadNueva;
        existente.total += this.total;
        existente.descripcion = this.getDescripcionElemento(existente.payload);

        this.elementosFacturacion = [...this.elementosFacturacion];
      } else {
        this.anadirElementoNuevo(payload);
      }
    } else {
      this.anadirElementoNuevo(payload);
    }

    this.toastService.success('Elemento añadido a la facturación.');
    this.resetCamposTipoActual();
  }

  eliminarElemento(idTemporal: number): void {
    this.elementosFacturacion = this.elementosFacturacion.filter(
      elemento => elemento.idTemporal !== idTemporal
    );
  }

  vaciarFacturacion(): void {
    if (this.elementosFacturacion.length === 0) {
      return;
    }

    const confirmar = confirm('¿Seguro que quieres vaciar toda la facturación actual?');

    if (!confirmar) {
      return;
    }

    this.elementosFacturacion = [];
  }

  generarFacturacion(): void {
    if (!this.empleado?.discordId) {
      this.toastService.error('No hay sesión de empleado activa.');
      return;
    }

    if (this.elementosFacturacion.length === 0) {
      this.toastService.warning('Añade al menos un elemento antes de generar la facturación.');
      return;
    }

    if (this.enviando) {
      return;
    }

    const payload: CreateFacturacionLoteRequest = {
      discordId: this.empleado.discordId,
      elementos: this.elementosFacturacion.map(elemento => ({
        ...elemento.payload,
        discordId: this.empleado!.discordId
      }))
    };

    this.enviando = true;

    this.facturaService.crearFacturacionLote(payload).subscribe({
      next: (response) => {
        const numeroFacturas = response.totalFacturas ?? 0;
        const totalGeneral = response.totalGeneral ?? this.totalFacturacion;

        this.toastService.success(
          `Facturación generada: ${numeroFacturas} factura${numeroFacturas === 1 ? '' : 's'} · Total $${totalGeneral}`
        );

        this.elementosFacturacion = [];
        this.resetFormularioCompleto();
        this.enviando = false;
      },
      error: (error) => {
        console.error('Error generando facturación:', error);
        this.toastService.error(
          error?.error?.message || error?.error?.error || 'No se pudo generar la facturación.'
        );
        this.enviando = false;
      }
    });
  }

  get totalFacturacion(): number {
    return this.elementosFacturacion.reduce(
      (acc, elemento) => acc + Number(elemento.total || 0),
      0
    );
  }

  get facturasPrevistas(): number {
    const tieneItems = this.elementosFacturacion.some(
      elemento => elemento.tipo === 'Items'
    );

    const independientes = this.elementosFacturacion.filter(
      elemento => elemento.tipo !== 'Items'
    ).length;

    return independientes + (tieneItems ? 1 : 0);
  }

  get cantidadElementos(): number {
    return this.elementosFacturacion.length;
  }

  private anadirElementoNuevo(payload: CreateFacturaRequest): void {
    this.elementosFacturacion = [
      ...this.elementosFacturacion,
      {
        idTemporal: this.siguienteIdTemporal++,
        tipo: payload.tipo,
        descripcion: this.getDescripcionElemento(payload),
        total: this.total,
        payload
      }
    ];
  }

  private validarFormularioActual(): boolean {
    if (!this.tipoSeleccionado) {
      this.toastService.warning('Debes seleccionar un tipo de factura.');
      return false;
    }

    if (this.tipoSeleccionado === 'Reparación' && !this.gravedad) {
      this.toastService.warning('Debes seleccionar un tipo de reparación.');
      return false;
    }

    if (this.tipoSeleccionado === 'Items') {
      if (!this.item) {
        this.toastService.warning('Debes seleccionar un item.');
        return false;
      }

      if (!this.cantidad || Number(this.cantidad) < 1) {
        this.toastService.warning('La cantidad debe ser al menos 1.');
        return false;
      }
    }

    if (this.tipoSeleccionado === 'Tasación') {
      if (!this.matricula.trim()) {
        this.toastService.warning('Debes indicar la matrícula del vehículo.');
        return false;
      }

      if (!this.estado) {
        this.toastService.warning('Debes seleccionar un estado para la tasación.');
        return false;
      }

      if (!this.modelo.trim()) {
        this.toastService.warning('Debes indicar el modelo del vehículo en la tasación.');
        return false;
      }
    }

    if (this.tipoSeleccionado === 'Full Tuning') {
      if (!this.matricula.trim()) {
        this.toastService.warning('Debes indicar la matrícula del vehículo.');
        return false;
      }

      if (!this.categoria) {
        this.toastService.warning('Debes seleccionar una categoría de Full Tuning.');
        return false;
      }
    }

    if (this.tipoSeleccionado === 'Tuneo') {
      if (!this.tuneoPlate.trim()) {
        this.toastService.warning('Debes indicar la matrícula del vehículo.');
        return false;
      }

      if (this.tuneoSeleccionados.length === 0) {
        this.toastService.warning('Debes seleccionar al menos una pieza de tuneo.');
        return false;
      }

      if (this.tieneMejorasRendimientoSeleccionadas() && !this.categoria) {
        this.toastService.warning(
          'Debes seleccionar la categoría del vehículo para mejoras de rendimiento.'
        );
        return false;
      }
    }

    return true;
  }

  private construirPayloadActual(): CreateFacturaRequest {
    return {
      discordId: this.empleado!.discordId,
      matricula: this.obtenerMatriculaParaBackend(),
      tipo: this.tipoSeleccionado,
      total: this.total,
      convenio: this.tipoSeleccionado === 'Tasación' || this.tipoSeleccionado === 'Items'
        ? false
        : this.convenio,
      lspd: this.tipoSeleccionado === 'Items' ? this.lspd : false,
      modelo: this.tipoSeleccionado === 'Reparación' || this.tipoSeleccionado === 'Items'
        ? null
        : this.normalizarTexto(this.modelo),
      estado: this.tipoSeleccionado === 'Tasación'
        ? this.normalizarTexto(this.estado)
        : null,
      cantidad: this.tipoSeleccionado === 'Items' ? Number(this.cantidad) : null,
      item: this.tipoSeleccionado === 'Items' ? this.normalizarTexto(this.item) : null,
      categoria: this.tipoSeleccionado === 'Full Tuning' || this.tipoSeleccionado === 'Tuneo'
        ? this.normalizarTexto(this.categoria)
        : null,
      gravedad: this.tipoSeleccionado === 'Reparación'
        ? this.normalizarTexto(this.gravedad)
        : null,
      tuneoPlate: this.tipoSeleccionado === 'Tuneo'
        ? this.normalizarTexto(this.tuneoPlate)
        : null,
      tuneoSeleccionados: this.tipoSeleccionado === 'Tuneo'
        ? this.tuneoSeleccionados.join(', ')
        : null,
      grua: this.tipoSeleccionado === 'Reparación' ? this.grua : false,
      otros: this.tipoSeleccionado === 'Tasación' ? this.normalizarTexto(this.otros) : null
    };
  }

  private getDescripcionElemento(payload: CreateFacturaRequest): string {
    switch (payload.tipo) {
      case 'Reparación':
        return `${payload.gravedad || 'Reparación'}${payload.grua ? ' · Grúa' : ''}${payload.convenio ? ' · Convenio' : ''}`;

      case 'Items':
        return `${payload.item || 'Item'} x${payload.cantidad || 1}${payload.lspd ? ' · LSPD' : ''}`;

      case 'Tasación':
        return `${payload.modelo || 'Modelo'} · ${payload.estado || 'Estado'} · ${payload.matricula || '-'}`;

      case 'Full Tuning':
        return `${payload.categoria || 'Categoría'} · ${payload.matricula || '-'}${payload.convenio ? ' · Convenio' : ''}`;

      case 'Tuneo':
        return `${payload.tuneoSeleccionados || 'Tuneo'} · ${payload.tuneoPlate || '-'}${payload.convenio ? ' · Convenio' : ''}`;

      default:
        return payload.tipo;
    }
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

  private resetCamposTipoActual(): void {
    const tipoActual = this.tipoSeleccionado;
    this.resetCamposFormulario();
    this.tipoSeleccionado = tipoActual;
    this.actualizarTotal();
  }

  private resetCamposFormulario(): void {
    this.total = 0;
    this.matricula = '';
    this.modelo = '';
    this.estado = 'SERIE';
    this.convenio = false;
    this.lspd = false;
    this.cantidad = 1;
    this.item = '';
    this.otros = '';
    this.categoria = '';
    this.tuneoPlate = '';
    this.gravedad = '';
    this.grua = false;
    this.tuneoSeleccionados = [];
  }

  private resetFormularioCompleto(): void {
    this.tipoSeleccionado = '';
    this.resetCamposFormulario();
  }
}

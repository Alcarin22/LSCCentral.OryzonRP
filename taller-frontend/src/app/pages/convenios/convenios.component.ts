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
  Convenio,
  ConvenioRequest,
  ConvenioService,
  CategoriaConvenio,
  EstadoConvenio
} from '../../core/services/convenio.service';

@Component({
  selector: 'app-convenios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './convenios.component.html',
  styleUrls: ['./convenios.component.css']
})
export class ConveniosComponent implements OnInit {
  empleado: SessionEmpleado | null = null;

  categorias: CategoriaConvenio[] = ['Estado', 'Talleres', 'Ocio', 'Alimentación'];
  estados: EstadoConvenio[] = ['Activo', 'Inactivo'];

  convenios: Convenio[] = [];

  loading = false;
  error = '';

  convenioAbiertoId: number | null = null;
  convenioEditandoId: number | null = null;

  modalNuevoAbierto = false;

  editNombre = '';
  editCategoria: CategoriaConvenio = 'Estado';
  editEstado: EstadoConvenio = 'Activo';
  editCondiciones = '';
  editDocumentoUrl = '';
  editDescuento = '';

  nuevoNombre = '';
  nuevoCategoria: CategoriaConvenio = 'Estado';
  nuevoEstado: EstadoConvenio = 'Activo';
  nuevoCondiciones = '';
  nuevoDocumentoUrl = '';
  nuevoDescuento = '';

  constructor(
    private sessionService: SessionService,
    private convenioService: ConvenioService,
    private zone: NgZone,
    private cdr: ChangeDetectorRef
  ) {
    this.empleado = this.sessionService.getEmpleado();
  }

  ngOnInit(): void {
    this.cargarConvenios();
  }

  cargarConvenios(): void {
    this.zone.run(() => {
      this.loading = true;
      this.error = '';
      this.cdr.detectChanges();
    });

    this.convenioService.listar().subscribe({
      next: (data) => {
        this.zone.run(() => {
          this.convenios = data ?? [];
          this.loading = false;
          this.error = '';
          this.cdr.detectChanges();
        });
      },
      error: (error) => {
        console.error('Error cargando convenios:', error);

        this.zone.run(() => {
          this.convenios = [];
          this.error = 'No se pudieron cargar los convenios.';
          this.loading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  get totalConvenios(): number {
    return this.convenios.length;
  }

  get totalActivos(): number {
    return this.convenios.filter(c => c.estado === 'Activo').length;
  }

  get totalInactivos(): number {
    return this.convenios.filter(c => c.estado === 'Inactivo').length;
  }

  puedeEditar(): boolean {
    return (this.empleado?.rango?.nivel ?? 0) >= 3;
  }

  getConveniosPorCategoria(categoria: CategoriaConvenio): Convenio[] {
    return this.convenios.filter(c => c.categoria === categoria);
  }

  getActivosPorCategoria(categoria: CategoriaConvenio): number {
    return this.getConveniosPorCategoria(categoria).filter(c => c.estado === 'Activo').length;
  }

  toggleConvenio(convenio: Convenio): void {
    if (this.convenioEditandoId === convenio.id) {
      return;
    }

    this.convenioAbiertoId =
      this.convenioAbiertoId === convenio.id ? null : convenio.id;

    if (this.convenioAbiertoId !== convenio.id) {
      this.cancelarEdicion();
    }
  }

  isConvenioAbierto(convenio: Convenio): boolean {
    return this.convenioAbiertoId === convenio.id;
  }

  isEditando(convenio: Convenio): boolean {
    return this.convenioEditandoId === convenio.id;
  }

  iniciarEdicion(event: MouseEvent, convenio: Convenio): void {
    event.stopPropagation();

    if (!this.puedeEditar()) return;

    this.convenioAbiertoId = convenio.id;
    this.convenioEditandoId = convenio.id;

    this.editNombre = convenio.nombre;
    this.editCategoria = convenio.categoria;
    this.editEstado = convenio.estado;
    this.editCondiciones = (convenio.condiciones ?? []).join('\n');
    this.editDocumentoUrl = convenio.documentoUrl ?? '';
    this.editDescuento = convenio.descuento ?? '';
  }

  guardarEdicion(event: MouseEvent, convenio: Convenio): void {
    event.stopPropagation();

    if (!this.puedeEditar()) return;

    const payload: ConvenioRequest = {
      nombre: this.editNombre,
      categoria: this.editCategoria,
      estado: this.editEstado,
      descuento: this.normalizarTexto(this.editDescuento),
      contacto: '',
      descripcion: '',
      documentoUrl: this.normalizarTexto(this.editDocumentoUrl),
      condiciones: this.convertirTextoACondiciones(this.editCondiciones)
    };

    this.convenioService.actualizar(convenio.id, payload).subscribe({
      next: () => {
        this.cargarConvenios();
        this.cancelarEdicion();
      },
      error: () => alert('Error actualizando')
    });
  }

  cancelarEdicion(event?: MouseEvent): void {
    event?.stopPropagation();
    this.convenioEditandoId = null;
  }

  eliminarConvenio(event: MouseEvent, convenio: Convenio): void {
    event.stopPropagation();

    if (!this.puedeEditar()) return;

    if (!confirm(`¿Eliminar "${convenio.nombre}"?`)) return;

    this.convenioService.eliminar(convenio.id).subscribe({
      next: () => this.cargarConvenios(),
      error: () => alert('Error eliminando')
    });
  }

  abrirModalNuevo(): void {
    if (!this.puedeEditar()) return;

    this.modalNuevoAbierto = true;
  }

  cerrarModalNuevo(): void {
    this.modalNuevoAbierto = false;
  }

  crearConvenio(): void {
    const payload: ConvenioRequest = {
      nombre: this.nuevoNombre,
      categoria: this.nuevoCategoria,
      estado: this.nuevoEstado,
      descuento: this.normalizarTexto(this.nuevoDescuento) ?? 'Pendiente',
      contacto: '',
      descripcion: '',
      documentoUrl: this.normalizarTexto(this.nuevoDocumentoUrl),
      condiciones: this.convertirTextoACondiciones(this.nuevoCondiciones)
    };

    this.convenioService.crear(payload).subscribe({
      next: () => {
        this.cargarConvenios();
        this.cerrarModalNuevo();
      },
      error: () => alert('Error creando convenio')
    });
  }

  abrirDocumento(event: MouseEvent, convenio: Convenio): void {
    event.stopPropagation();

    if (!convenio.documentoUrl) {
      alert('Sin documento');
      return;
    }

    window.open(convenio.documentoUrl, '_blank');
  }

  private convertirTextoACondiciones(texto: string): string[] {
    return texto
      .split('\n')
      .map(c => c.trim())
      .filter(Boolean);
  }

  private normalizarTexto(valor: string | null | undefined): string | null {
    if (!valor) return null;
    const limpio = valor.trim();
    return limpio.length ? limpio : null;
  }
}
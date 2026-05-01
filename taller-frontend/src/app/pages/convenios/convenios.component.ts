import { Component, OnInit } from '@angular/core';
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
    private convenioService: ConvenioService
  ) {
    this.empleado = this.sessionService.getEmpleado();
  }

  ngOnInit(): void {
    this.cargarConvenios();
  }

  cargarConvenios(): void {
    this.loading = true;
    this.error = '';

    this.convenioService.listar().subscribe({
      next: (data) => {
        this.convenios = data ?? [];
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando convenios:', error);
        this.convenios = [];
        this.error = 'No se pudieron cargar los convenios.';
        this.loading = false;
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

    const nombre = this.editNombre.trim();

    if (!nombre) {
      alert('Debes indicar el nombre del local.');
      return;
    }

    const payload: ConvenioRequest = {
      nombre,
      categoria: this.editCategoria,
      estado: this.editEstado,
      descuento: this.normalizarTexto(this.editDescuento),
      contacto: convenio.contacto ?? '',
      descripcion: convenio.descripcion ?? '',
      documentoUrl: this.normalizarTexto(this.editDocumentoUrl),
      condiciones: this.convertirTextoACondiciones(this.editCondiciones)
    };

    this.convenioService.actualizar(convenio.id, payload).subscribe({
      next: (actualizado) => {
        this.convenios = this.convenios.map(c =>
          c.id === actualizado.id ? actualizado : c
        );

        this.cancelarEdicion();
      },
      error: (error) => {
        console.error('Error actualizando convenio:', error);
        alert('No se pudo actualizar el convenio.');
      }
    });
  }

  cancelarEdicion(event?: MouseEvent): void {
    event?.stopPropagation();
    this.convenioEditandoId = null;
  }

  eliminarConvenio(event: MouseEvent, convenio: Convenio): void {
    event.stopPropagation();

    if (!this.puedeEditar()) return;

    const confirmar = confirm(`¿Eliminar el convenio "${convenio.nombre}"?`);

    if (!confirmar) return;

    this.convenioService.eliminar(convenio.id).subscribe({
      next: () => {
        this.convenios = this.convenios.filter(c => c.id !== convenio.id);

        if (this.convenioAbiertoId === convenio.id) {
          this.convenioAbiertoId = null;
        }

        if (this.convenioEditandoId === convenio.id) {
          this.convenioEditandoId = null;
        }
      },
      error: (error) => {
        console.error('Error eliminando convenio:', error);
        alert('No se pudo eliminar el convenio.');
      }
    });
  }

  abrirModalNuevo(): void {
    if (!this.puedeEditar()) return;

    this.modalNuevoAbierto = true;
    this.nuevoNombre = '';
    this.nuevoCategoria = 'Estado';
    this.nuevoEstado = 'Activo';
    this.nuevoCondiciones = '';
    this.nuevoDocumentoUrl = '';
    this.nuevoDescuento = '';
  }

  cerrarModalNuevo(): void {
    this.modalNuevoAbierto = false;
  }

  crearConvenio(): void {
    if (!this.puedeEditar()) return;

    const nombre = this.nuevoNombre.trim();

    if (!nombre) {
      alert('Debes indicar el nombre del local.');
      return;
    }

    const payload: ConvenioRequest = {
      nombre,
      categoria: this.nuevoCategoria,
      estado: this.nuevoEstado,
      descuento: this.normalizarTexto(this.nuevoDescuento) ?? (this.nuevoEstado === 'Activo' ? 'Pendiente' : '-'),
      contacto: '',
      descripcion: '',
      condiciones: this.convertirTextoACondiciones(this.nuevoCondiciones),
      documentoUrl: this.normalizarTexto(this.nuevoDocumentoUrl)
    };

    this.convenioService.crear(payload).subscribe({
      next: (creado) => {
        this.convenios = [...this.convenios, creado];
        this.convenioAbiertoId = creado.id;
        this.cerrarModalNuevo();
      },
      error: (error) => {
        console.error('Error creando convenio:', error);
        alert('No se pudo crear el convenio.');
      }
    });
  }

  abrirDocumento(event: MouseEvent, convenio: Convenio): void {
    event.stopPropagation();

    if (!convenio.documentoUrl || convenio.documentoUrl === '#') {
      alert('Este convenio todavía no tiene documento asociado.');
      return;
    }

    window.open(convenio.documentoUrl, '_blank');
  }

  private convertirTextoACondiciones(texto: string): string[] {
    const condiciones = texto
      .split('\n')
      .map(c => c.trim())
      .filter(Boolean);

    return condiciones.length ? condiciones : ['Pendiente de definir condiciones.'];
  }

  private normalizarTexto(valor: string | null | undefined): string | null {
    if (!valor) return null;

    const limpio = valor.trim();
    return limpio.length ? limpio : null;
  }
}
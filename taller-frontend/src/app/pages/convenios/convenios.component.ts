import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  SessionEmpleado,
  SessionService
} from '../../core/services/session.service';

type EstadoConvenio = 'Activo' | 'Inactivo';
type CategoriaConvenio = 'Estado' | 'Talleres' | 'Ocio' | 'Alimentación';

interface Convenio {
  id: number;
  nombre: string;
  categoria: CategoriaConvenio;
  estado: EstadoConvenio;
  descuento: string;
  contacto: string;
  descripcion: string;
  condiciones: string[];
  documentoUrl: string;
}

@Component({
  selector: 'app-convenios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './convenios.component.html',
  styleUrls: ['./convenios.component.css']
})
export class ConveniosComponent {
  empleado: SessionEmpleado | null = null;

  categorias: CategoriaConvenio[] = ['Estado', 'Talleres', 'Ocio', 'Alimentación'];
  estados: EstadoConvenio[] = ['Activo', 'Inactivo'];

  convenioAbiertoId: number | null = null;
  convenioEditandoId: number | null = null;

  modalNuevoAbierto = false;

  editNombre = '';
  editCategoria: CategoriaConvenio = 'Estado';
  editEstado: EstadoConvenio = 'Activo';
  editCondiciones = '';
  editDocumentoUrl = '';

  nuevoNombre = '';
  nuevoCategoria: CategoriaConvenio = 'Estado';
  nuevoEstado: EstadoConvenio = 'Activo';
  nuevoCondiciones = '';
  nuevoDocumentoUrl = '';

  constructor(private sessionService: SessionService) {
    this.empleado = this.sessionService.getEmpleado();
  }

  convenios: Convenio[] = [
    {
      id: 1,
      nombre: 'LSPD',
      categoria: 'Estado',
      estado: 'Activo',
      descuento: '20%',
      contacto: '',
      descripcion: '',
      condiciones: [
        'Aplicable a reparaciones oficiales',
        'Identificación obligatoria'
      ],
      documentoUrl: '#'
    },
    {
      id: 2,
      nombre: 'EMS',
      categoria: 'Estado',
      estado: 'Activo',
      descuento: '20%',
      contacto: '',
      descripcion: '',
      condiciones: [
        'Servicios médicos',
        'Uso exclusivo emergencias'
      ],
      documentoUrl: '#'
    },
    {
      id: 3,
      nombre: 'RaceLand Circuit',
      categoria: 'Talleres',
      estado: 'Activo',
      descuento: '15%',
      contacto: '',
      descripcion: '',
      condiciones: [
        'Convenio activo',
        'Aplicable a clientes asociados'
      ],
      documentoUrl: '#'
    }
  ];

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
    this.editCondiciones = convenio.condiciones.join('\n');
    this.editDocumentoUrl = convenio.documentoUrl;
  }

  guardarEdicion(event: MouseEvent, convenio: Convenio): void {
    event.stopPropagation();

    convenio.nombre = this.editNombre.trim() || convenio.nombre;
    convenio.categoria = this.editCategoria;
    convenio.estado = this.editEstado;
    convenio.condiciones = this.convertirTextoACondiciones(this.editCondiciones);
    convenio.documentoUrl = this.editDocumentoUrl.trim() || '#';

    this.cancelarEdicion();
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

    this.convenios = this.convenios.filter(c => c.id !== convenio.id);

    if (this.convenioAbiertoId === convenio.id) {
      this.convenioAbiertoId = null;
    }

    if (this.convenioEditandoId === convenio.id) {
      this.convenioEditandoId = null;
    }
  }

  abrirModalNuevo(): void {
    if (!this.puedeEditar()) return;

    this.modalNuevoAbierto = true;
    this.nuevoNombre = '';
    this.nuevoCategoria = 'Estado';
    this.nuevoEstado = 'Activo';
    this.nuevoCondiciones = '';
    this.nuevoDocumentoUrl = '';
  }

  cerrarModalNuevo(): void {
    this.modalNuevoAbierto = false;
  }

  crearConvenio(): void {
    const nombre = this.nuevoNombre.trim();

    if (!nombre) {
      alert('Debes indicar el nombre del local.');
      return;
    }

    const nuevoConvenio: Convenio = {
      id: this.generarNuevoId(),
      nombre,
      categoria: this.nuevoCategoria,
      estado: this.nuevoEstado,
      descuento: this.nuevoEstado === 'Activo' ? 'Pendiente' : '-',
      contacto: '',
      descripcion: '',
      condiciones: this.convertirTextoACondiciones(this.nuevoCondiciones),
      documentoUrl: this.nuevoDocumentoUrl.trim() || '#'
    };

    this.convenios = [...this.convenios, nuevoConvenio];

    this.convenioAbiertoId = nuevoConvenio.id;
    this.cerrarModalNuevo();
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

  private generarNuevoId(): number {
    return this.convenios.length
      ? Math.max(...this.convenios.map(c => c.id)) + 1
      : 1;
  }
}
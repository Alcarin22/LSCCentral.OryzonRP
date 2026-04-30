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

  editNombre = '';
  editCategoria: CategoriaConvenio = 'Estado';
  editEstado: EstadoConvenio = 'Activo';
  editCondiciones = '';
  editDocumentoUrl = '';

  constructor(private sessionService: SessionService) {
    this.empleado = this.sessionService.getEmpleado();
  }

  // 🔥 CONTROL APERTURA / CIERRE
  toggleConvenio(convenio: Convenio): void {
    // ❌ NO permitir cerrar si está en edición
    if (this.convenioEditandoId === convenio.id) {
      return;
    }

    this.convenioAbiertoId = this.convenioAbiertoId === convenio.id ? null : convenio.id;

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

  // 🔥 TEMPORAL (luego volvemos a roles)
  puedeEditar(): boolean {
    return true;
  }

  iniciarEdicion(event: MouseEvent, convenio: Convenio): void {
    event.stopPropagation();

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
    convenio.condiciones = this.editCondiciones
      .split('\n')
      .map(c => c.trim())
      .filter(Boolean);
    convenio.documentoUrl = this.editDocumentoUrl.trim() || '#';

    this.cancelarEdicion();
  }

  cancelarEdicion(event?: MouseEvent): void {
    event?.stopPropagation();

    this.convenioEditandoId = null;
  }

  abrirDocumento(event: MouseEvent, convenio: Convenio): void {
    event.stopPropagation();

    if (!convenio.documentoUrl || convenio.documentoUrl === '#') {
      alert('Este convenio todavía no tiene documento asociado.');
      return;
    }

    window.open(convenio.documentoUrl, '_blank');
  }

  // 🔥 DATOS (igual que antes, no los repito para no hacer esto enorme)
  convenios: Convenio[] = [
    {
      id: 1,
      nombre: 'LSPD',
      categoria: 'Estado',
      estado: 'Activo',
      descuento: '20%',
      contacto: 'Pendiente',
      descripcion: '',
      condiciones: ['Aplicable a reparaciones autorizadas.'],
      documentoUrl: '#'
    },
    {
      id: 2,
      nombre: 'EMS',
      categoria: 'Estado',
      estado: 'Activo',
      descuento: '20%',
      contacto: 'Pendiente',
      descripcion: '',
      condiciones: ['Aplicable a servicios médicos.'],
      documentoUrl: '#'
    }
  ];

  getConveniosPorCategoria(categoria: CategoriaConvenio): Convenio[] {
    return this.convenios.filter(c => c.categoria === categoria);
  }
}
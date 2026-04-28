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
    console.log('Empleado sesión:', this.empleado);
  }

  convenios: Convenio[] = [
    {
      id: 1,
      nombre: 'LSPD',
      categoria: 'Estado',
      estado: 'Activo',
      descuento: '20%',
      contacto: 'Pendiente',
      descripcion: 'Convenio para servicios oficiales relacionados con vehículos policiales.',
      condiciones: [
        'Aplicable a reparaciones autorizadas.',
        'Debe identificarse como servicio oficial.',
        'No acumulable con otros descuentos.'
      ],
      documentoUrl: '#'
    },
    {
      id: 2,
      nombre: 'EMS',
      categoria: 'Estado',
      estado: 'Activo',
      descuento: '20%',
      contacto: 'Pendiente',
      descripcion: 'Convenio para vehículos y servicios relacionados con emergencias médicas.',
      condiciones: [
        'Aplicable a reparaciones de servicio.',
        'Requiere identificación del trabajador.',
        'No aplicable a trabajos personales.'
      ],
      documentoUrl: '#'
    },
    {
      id: 3,
      nombre: 'RaceLand Circuit',
      categoria: 'Talleres',
      estado: 'Activo',
      descuento: '15%',
      contacto: 'Pendiente',
      descripcion: 'Convenio comercial con taller o entidad vinculada al motor.',
      condiciones: [
        'Aplicable a servicios acordados.',
        'Consultar condiciones especiales antes de facturar.'
      ],
      documentoUrl: '#'
    },
    {
      id: 4,
      nombre: 'Bullet Haven',
      categoria: 'Talleres',
      estado: 'Activo',
      descuento: '15%',
      contacto: 'Pendiente',
      descripcion: 'Convenio activo con condiciones de colaboración entre negocios.',
      condiciones: [
        'Aplicable según acuerdo vigente.',
        'No acumulable con otros convenios.'
      ],
      documentoUrl: '#'
    },
    {
      id: 5,
      nombre: 'Infernus',
      categoria: 'Talleres',
      estado: 'Activo',
      descuento: '15%',
      contacto: 'Pendiente',
      descripcion: 'Convenio activo para servicios de taller.',
      condiciones: [
        'Aplicable a trabajos facturados.',
        'Debe indicarse convenio en la factura.'
      ],
      documentoUrl: '#'
    },
    {
      id: 6,
      nombre: "Benny's Motorworks",
      categoria: 'Talleres',
      estado: 'Activo',
      descuento: '15%',
      contacto: 'Pendiente',
      descripcion: 'Convenio activo con taller asociado.',
      condiciones: [
        'Aplicable a servicios pactados.',
        'Consultar documento antes de aplicar excepciones.'
      ],
      documentoUrl: '#'
    },
    {
      id: 7,
      nombre: 'Hayes Auto',
      categoria: 'Talleres',
      estado: 'Activo',
      descuento: '15%',
      contacto: 'Pendiente',
      descripcion: 'Convenio activo con negocio de automoción.',
      condiciones: [
        'Aplicable a reparaciones y servicios autorizados.',
        'No acumulable.'
      ],
      documentoUrl: '#'
    },
    {
      id: 8,
      nombre: 'LSC East',
      categoria: 'Talleres',
      estado: 'Activo',
      descuento: '15%',
      contacto: 'Pendiente',
      descripcion: 'Convenio activo con otro punto de taller.',
      condiciones: [
        'Aplicable según documento del convenio.',
        'Registrar correctamente en factura.'
      ],
      documentoUrl: '#'
    },
    {
      id: 9,
      nombre: 'RedLine Performance',
      categoria: 'Talleres',
      estado: 'Inactivo',
      descuento: '-',
      contacto: 'Pendiente',
      descripcion: 'Convenio actualmente inactivo.',
      condiciones: [
        'No aplicar descuento.',
        'Pendiente de renovación o cierre definitivo.'
      ],
      documentoUrl: '#'
    },
    {
      id: 10,
      nombre: 'Romanova',
      categoria: 'Talleres',
      estado: 'Inactivo',
      descuento: '-',
      contacto: 'Pendiente',
      descripcion: 'Convenio actualmente inactivo.',
      condiciones: [
        'No aplicar descuento.',
        'Pendiente de revisión.'
      ],
      documentoUrl: '#'
    },
    {
      id: 11,
      nombre: 'Auto Exotic',
      categoria: 'Talleres',
      estado: 'Inactivo',
      descuento: '-',
      contacto: 'Pendiente',
      descripcion: 'Convenio actualmente inactivo.',
      condiciones: [
        'No aplicar descuento.',
        'Pendiente de renovación.'
      ],
      documentoUrl: '#'
    },
    {
      id: 12,
      nombre: 'Ruta68',
      categoria: 'Talleres',
      estado: 'Inactivo',
      descuento: '-',
      contacto: 'Pendiente',
      descripcion: 'Convenio actualmente inactivo.',
      condiciones: [
        'No aplicar descuento.',
        'Pendiente de revisión.'
      ],
      documentoUrl: '#'
    },
    {
      id: 13,
      nombre: 'Vanilla',
      categoria: 'Ocio',
      estado: 'Inactivo',
      descuento: '-',
      contacto: 'Pendiente',
      descripcion: 'Convenio pendiente o no activo actualmente.',
      condiciones: [
        'No aplicar descuento hasta confirmación.',
        'Pendiente de documento.'
      ],
      documentoUrl: '#'
    },
    {
      id: 14,
      nombre: 'Casino',
      categoria: 'Ocio',
      estado: 'Inactivo',
      descuento: '-',
      contacto: 'Pendiente',
      descripcion: 'Convenio pendiente o no activo actualmente.',
      condiciones: [
        'No aplicar descuento.',
        'Pendiente de revisión.'
      ],
      documentoUrl: '#'
    },
    {
      id: 15,
      nombre: 'Wiwan',
      categoria: 'Ocio',
      estado: 'Inactivo',
      descuento: '-',
      contacto: 'Pendiente',
      descripcion: 'Convenio actualmente inactivo.',
      condiciones: [
        'No aplicar descuento.',
        'Pendiente de renovación.'
      ],
      documentoUrl: '#'
    },
    {
      id: 16,
      nombre: 'Bahamas',
      categoria: 'Ocio',
      estado: 'Inactivo',
      descuento: '-',
      contacto: 'Pendiente',
      descripcion: 'Convenio pendiente o no activo actualmente.',
      condiciones: [
        'No aplicar descuento hasta confirmación.',
        'Pendiente de documento.'
      ],
      documentoUrl: '#'
    },
    {
      id: 17,
      nombre: 'Pacific Bluff',
      categoria: 'Ocio',
      estado: 'Activo',
      descuento: '10%',
      contacto: 'Pendiente',
      descripcion: 'Convenio activo con local de ocio.',
      condiciones: [
        'Aplicable según acuerdo.',
        'Debe indicarse convenio en la factura.'
      ],
      documentoUrl: '#'
    },
    {
      id: 18,
      nombre: 'Tequila',
      categoria: 'Ocio',
      estado: 'Activo',
      descuento: '10%',
      contacto: 'Pendiente',
      descripcion: 'Convenio activo con local de ocio.',
      condiciones: [
        'Aplicable según documento.',
        'No acumulable con otros descuentos.'
      ],
      documentoUrl: '#'
    },
    {
      id: 19,
      nombre: 'Case',
      categoria: 'Ocio',
      estado: 'Activo',
      descuento: '10%',
      contacto: 'Pendiente',
      descripcion: 'Convenio activo con local de ocio.',
      condiciones: [
        'Aplicable a servicios acordados.',
        'Revisar documento antes de excepciones.'
      ],
      documentoUrl: '#'
    },
    {
      id: 20,
      nombre: 'Burger Shot',
      categoria: 'Alimentación',
      estado: 'Activo',
      descuento: '10%',
      contacto: 'Pendiente',
      descripcion: 'Convenio activo con negocio de alimentación.',
      condiciones: [
        'Aplicable según acuerdo vigente.',
        'Debe indicarse convenio en la factura.'
      ],
      documentoUrl: '#'
    },
    {
      id: 21,
      nombre: 'Pizza This',
      categoria: 'Alimentación',
      estado: 'Inactivo',
      descuento: '-',
      contacto: 'Pendiente',
      descripcion: 'Convenio pendiente o inactivo actualmente.',
      condiciones: [
        'No aplicar descuento.',
        'Pendiente de revisión.'
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

  getConveniosPorCategoria(categoria: CategoriaConvenio): Convenio[] {
    return this.convenios.filter(c => c.categoria === categoria);
  }

  getActivosPorCategoria(categoria: CategoriaConvenio): number {
    return this.getConveniosPorCategoria(categoria).filter(c => c.estado === 'Activo').length;
  }

  toggleConvenio(convenio: Convenio): void {
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

  puedeEditar(): boolean {
    return true;
  }

  iniciarEdicion(event: MouseEvent, convenio: Convenio): void {
    event.stopPropagation();

    if (!this.puedeEditar()) {
      return;
    }

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
    this.editNombre = '';
    this.editCategoria = 'Estado';
    this.editEstado = 'Activo';
    this.editCondiciones = '';
    this.editDocumentoUrl = '';
  }

  abrirDocumento(event: MouseEvent, convenio: Convenio): void {
    event.stopPropagation();

    if (!convenio.documentoUrl || convenio.documentoUrl === '#') {
      alert('Este convenio todavía no tiene documento asociado.');
      return;
    }

    window.open(convenio.documentoUrl, '_blank');
  }
}
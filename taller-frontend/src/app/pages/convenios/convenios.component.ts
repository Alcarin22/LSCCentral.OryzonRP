import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  NgZone,
  OnInit,
  ViewChild
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  CategoriaConvenio,
  Convenio,
  ConvenioRequest,
  ConvenioService,
  EstadoConvenio
} from '../../services/convenio.service';

import {
  SessionEmpleado,
  SessionService
} from '../../core/services/session.service';

import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-convenios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './convenios.component.html',
  styleUrls: ['./convenios.component.css']
})
export class ConveniosComponent implements OnInit {
  @ViewChild('archivoInput')
  archivoInput?: ElementRef<HTMLInputElement>;

  empleado: SessionEmpleado | null = null;

  readonly categorias: CategoriaConvenio[] = [
    'Estado',
    'Talleres',
    'Ocio',
    'Alimentación',
    'Otros'
  ];

  readonly estados: EstadoConvenio[] = [
    'Activo',
    'Inactivo'
  ];

  convenios: Convenio[] = [];

  loading = false;
  guardando = false;
  eliminandoId: number | null = null;
  convenioAbiertoId: number | null = null;
  error = '';

  modalAbierto = false;
  convenioEditandoId: number | null = null;
  archivoExistenteNombre: string | null = null;

  nuevoLocal = '';
  nuevaCategoria: CategoriaConvenio = 'Estado';
  nuevoEstado: EstadoConvenio = 'Activo';
  nuevasCondicionesLsc = '';
  nuevasCondicionesLocal = '';
  archivoSeleccionado: File | null = null;

  constructor(
    private convenioService: ConvenioService,
    private sessionService: SessionService,
    private toastService: ToastService,
    private zone: NgZone,
    private cdr: ChangeDetectorRef
  ) {
    this.empleado = this.sessionService.getEmpleado();
  }

  ngOnInit(): void {
    this.cargarConvenios();
  }

  puedeGestionarConvenios(): boolean {
    return (this.empleado?.rango?.nivel ?? 0) >= 3;
  }

  puedeAdministrarConvenios(): boolean {
    return (this.empleado?.rango?.nivel ?? 0) >= 4;
  }

  cargarConvenios(): void {
    this.zone.run(() => {
      this.loading = true;
      this.error = '';
      this.cdr.detectChanges();
    });

    this.convenioService.listar().subscribe({
      next: (data: Convenio[]) => {
        this.zone.run(() => {
          this.convenios = (data ?? []).sort((a, b) =>
            a.local.localeCompare(b.local, 'es', {
              sensitivity: 'base'
            })
          );

          this.loading = false;
          this.error = '';
          this.cdr.detectChanges();
        });
      },
      error: (error: any) => {
        console.error('Error cargando convenios:', error);

        this.zone.run(() => {
          this.convenios = [];
          this.loading = false;
          this.error = 'No se pudieron cargar los convenios.';
          this.cdr.detectChanges();
        });
      }
    });
  }

  getConveniosPorCategoria(
    categoria: CategoriaConvenio
  ): Convenio[] {
    return this.convenios.filter(
      convenio => convenio.categoria === categoria
    );
  }

  getActivosPorCategoria(
    categoria: CategoriaConvenio
  ): number {
    return this.getConveniosPorCategoria(categoria)
      .filter(convenio => convenio.estado === 'Activo')
      .length;
  }

  toggleConvenio(convenio: Convenio): void {
    this.convenioAbiertoId =
      this.convenioAbiertoId === convenio.id
        ? null
        : convenio.id;
  }

  isConvenioAbierto(convenio: Convenio): boolean {
    return this.convenioAbiertoId === convenio.id;
  }

  abrirModal(): void {
    if (!this.puedeGestionarConvenios()) {
      this.toastService.error(
        'No tienes permisos para añadir convenios.'
      );
      return;
    }

    this.resetFormulario();
    this.modalAbierto = true;
  }

  editarConvenio(convenio: Convenio): void {
    if (!this.puedeAdministrarConvenios()) {
      this.toastService.error(
        'No tienes permisos para editar convenios.'
      );
      return;
    }

    this.convenioEditandoId = convenio.id;
    this.nuevoLocal = convenio.local;
    this.nuevaCategoria = convenio.categoria;
    this.nuevoEstado = convenio.estado;
    this.nuevasCondicionesLsc = convenio.condicionesLsc ?? '';
    this.nuevasCondicionesLocal = convenio.condicionesLocal ?? '';
    this.archivoSeleccionado = null;
    this.archivoExistenteNombre = convenio.tieneArchivo
      ? convenio.archivoNombre
      : null;

    if (this.archivoInput?.nativeElement) {
      this.archivoInput.nativeElement.value = '';
    }

    this.modalAbierto = true;
    this.cdr.detectChanges();
  }

  eliminarConvenio(convenio: Convenio): void {
    if (!this.puedeAdministrarConvenios()) {
      this.toastService.error(
        'No tienes permisos para eliminar convenios.'
      );
      return;
    }

    if (this.eliminandoId !== null) {
      return;
    }

    const confirmar = window.confirm(
      `¿Seguro que quieres eliminar el convenio con "${convenio.local}"?\n\nEsta acción eliminará también el archivo asociado y no se puede deshacer.`
    );

    if (!confirmar) {
      return;
    }

    this.eliminandoId = convenio.id;
    this.cdr.detectChanges();

    this.convenioService.eliminar(convenio.id).subscribe({
      next: () => {
        this.zone.run(() => {
          this.eliminandoId = null;
          this.convenios = this.convenios.filter(
            item => item.id !== convenio.id
          );

          if (this.convenioAbiertoId === convenio.id) {
            this.convenioAbiertoId = null;
          }

          this.toastService.success(
            `Convenio con ${convenio.local} eliminado correctamente.`
          );

          this.cdr.detectChanges();
        });
      },
      error: (error: any) => {
        console.error('Error eliminando convenio:', error);

        this.zone.run(() => {
          this.eliminandoId = null;

          this.toastService.error(
            error?.error?.message ||
            error?.error?.error ||
            'No se pudo eliminar el convenio.'
          );

          this.cdr.detectChanges();
        });
      }
    });
  }

  cerrarModal(): void {
    if (this.guardando) {
      return;
    }

    this.modalAbierto = false;
    this.resetFormulario();
  }

  cerrarModalDesdeFondo(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.cerrarModal();
    }
  }

  onArchivoSeleccionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    const archivo = input.files?.[0] ?? null;

    if (archivo && archivo.size > 10 * 1024 * 1024) {
      this.toastService.error(
        'El archivo no puede superar los 10 MB.'
      );

      input.value = '';
      this.archivoSeleccionado = null;
      return;
    }

    this.archivoSeleccionado = archivo;
  }

  quitarArchivo(): void {
    this.archivoSeleccionado = null;

    if (this.archivoInput?.nativeElement) {
      this.archivoInput.nativeElement.value = '';
    }
  }

  guardarConvenio(): void {
    if (this.guardando) {
      return;
    }

    const local = this.nuevoLocal.trim();

    if (!local) {
      this.toastService.error(
        'Debes indicar el nombre del local.'
      );
      return;
    }

    const payload: ConvenioRequest = {
      local,
      categoria: this.nuevaCategoria,
      estado: this.nuevoEstado,
      condicionesLsc: this.normalizarTexto(
        this.nuevasCondicionesLsc
      ),
      condicionesLocal: this.normalizarTexto(
        this.nuevasCondicionesLocal
      )
    };

    this.guardando = true;
    this.cdr.detectChanges();

    const request$ = this.convenioEditandoId !== null
      ? this.convenioService.actualizar(
          this.convenioEditandoId,
          payload,
          this.archivoSeleccionado
        )
      : this.convenioService.crear(
          payload,
          this.archivoSeleccionado
        );

    request$.subscribe({
      next: (convenio: Convenio) => {
        this.zone.run(() => {
          const estabaEditando = this.convenioEditandoId !== null;

          this.guardando = false;
          this.modalAbierto = false;
          this.resetFormulario();

          this.toastService.success(
            estabaEditando
              ? `Convenio con ${convenio.local} actualizado correctamente.`
              : `Convenio con ${convenio.local} añadido correctamente.`
          );

          this.cargarConvenios();
          this.cdr.detectChanges();
        });
      },
      error: (error: any) => {
        console.error('Error guardando convenio:', error);

        this.zone.run(() => {
          this.guardando = false;

          this.toastService.error(
            error?.error?.message ||
            error?.error?.error ||
            'No se pudo guardar el convenio.'
          );

          this.cdr.detectChanges();
        });
      }
    });
  }

  verArchivo(convenio: Convenio): void {
    if (!convenio.tieneArchivo) {
      this.toastService.info(
        'Este convenio no tiene ningún archivo asociado.'
      );
      return;
    }

    const ventana = window.open('', '_blank');

    this.convenioService.obtenerArchivo(convenio.id).subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);

        if (ventana) {
          ventana.location.href = url;
        } else {
          window.open(url, '_blank');
        }

        window.setTimeout(() => {
          URL.revokeObjectURL(url);
        }, 60000);
      },
      error: (error: any) => {
        console.error('Error abriendo archivo de convenio:', error);

        if (ventana) {
          ventana.close();
        }

        this.toastService.error(
          'No se pudo abrir el archivo del convenio.'
        );
      }
    });
  }

  get condicionesLocalLabel(): string {
    const local = this.nuevoLocal.trim();

    return local
      ? `Condiciones ${local}`
      : 'Condiciones [Local]';
  }

  get modalTitulo(): string {
    return this.convenioEditandoId !== null
      ? 'Editar Convenio'
      : 'Añadir Convenio';
  }

  get modalKicker(): string {
    return this.convenioEditandoId !== null
      ? 'Modificar acuerdo'
      : 'Nuevo acuerdo';
  }

  get modalDescripcion(): string {
    return this.convenioEditandoId !== null
      ? 'Modifica la información y las condiciones del convenio.'
      : 'Introduce la información y las condiciones del nuevo convenio.';
  }

  get textoBotonGuardar(): string {
    if (this.guardando) {
      return this.convenioEditandoId !== null
        ? 'Actualizando...'
        : 'Guardando...';
    }

    return this.convenioEditandoId !== null
      ? 'Guardar cambios'
      : 'Guardar convenio';
  }

  private resetFormulario(): void {
    this.convenioEditandoId = null;
    this.nuevoLocal = '';
    this.nuevaCategoria = 'Estado';
    this.nuevoEstado = 'Activo';
    this.nuevasCondicionesLsc = '';
    this.nuevasCondicionesLocal = '';
    this.archivoSeleccionado = null;
    this.archivoExistenteNombre = null;

    if (this.archivoInput?.nativeElement) {
      this.archivoInput.nativeElement.value = '';
    }
  }

  private normalizarTexto(
    valor: string | null | undefined
  ): string | null {
    if (!valor) {
      return null;
    }

    const limpio = valor.trim();
    return limpio.length ? limpio : null;
  }
}

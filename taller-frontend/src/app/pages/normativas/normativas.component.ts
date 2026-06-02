import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

type NormativaId = 'general' | 'locales';

interface Normativa {
  id: NormativaId;
  titulo: string;
  descripcion: string;
  icono: string;
  totalPaginas: number;
  pdfUrl: string;
  getImagenUrl: (pagina: number) => string;
}

@Component({
  selector: 'app-normativas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './normativas.component.html',
  styleUrls: ['./normativas.component.css']
})
export class NormativasComponent {
  normativaSeleccionada: NormativaId | null = null;
  paginaActual = 1;

  normativas: Normativa[] = [
    {
      id: 'general',
      titulo: 'Normativa General',
      descripcion: 'Normativa general del servidor OryzonRP.',
      icono: '📜',
      totalPaginas: 14,
      pdfUrl: '/assets/n-general/NORMATIVA GENERAL ORYZONRP.pdf',
      getImagenUrl: (pagina: number) =>
        `/assets/n-general/pagina_${String(pagina).padStart(2, '0')}.png`
    },
    {
      id: 'locales',
      titulo: 'Normativa Locales',
      descripcion: 'Normativa específica para locales y negocios.',
      icono: '🏪',
      totalPaginas: 10,
      pdfUrl: '/assets/n-locales/NORMATIVA LOCALES WIPE9.pdf',
      getImagenUrl: (pagina: number) =>
        `/assets/n-locales/${pagina}.png`
    }
  ];

  get normativaActual(): Normativa | null {
    return this.normativas.find(n => n.id === this.normativaSeleccionada) ?? null;
  }

  seleccionarNormativa(id: NormativaId): void {
    this.normativaSeleccionada = id;
    this.paginaActual = 1;
  }

  volver(): void {
    this.normativaSeleccionada = null;
    this.paginaActual = 1;
  }

  paginaAnterior(): void {
    if (this.paginaActual > 1) {
      this.paginaActual--;
      this.scrollArriba();
    }
  }

  paginaSiguiente(): void {
    const normativa = this.normativaActual;

    if (normativa && this.paginaActual < normativa.totalPaginas) {
      this.paginaActual++;
      this.scrollArriba();
    }
  }

  irAPagina(pagina: number): void {
    const normativa = this.normativaActual;

    if (!normativa) return;

    if (pagina >= 1 && pagina <= normativa.totalPaginas) {
      this.paginaActual = pagina;
      this.scrollArriba();
    }
  }

  get paginas(): number[] {
    const normativa = this.normativaActual;

    if (!normativa) return [];

    return Array.from(
      { length: normativa.totalPaginas },
      (_, index) => index + 1
    );
  }

  abrirPdf(): void {
    const normativa = this.normativaActual;

    if (!normativa) return;

    window.open(normativa.pdfUrl, '_blank');
  }

  descargarPdf(): void {
    const normativa = this.normativaActual;

    if (!normativa) return;

    const link = document.createElement('a');
    link.href = normativa.pdfUrl;
    link.download = normativa.pdfUrl.split('/').pop() ?? 'normativa.pdf';
    link.click();
  }

  private scrollArriba(): void {
    setTimeout(() => {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      });
    }, 0);
  }
}
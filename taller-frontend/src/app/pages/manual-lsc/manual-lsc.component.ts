import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface ManualLsc {
  titulo: string;
  descripcion: string;
  icono: string;
  totalPaginas: number;
  pdfUrl: string;
  getImagenUrl: (pagina: number) => string;
}

@Component({
  selector: 'app-manual-lsc',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './manual-lsc.component.html',
  styleUrls: ['./manual-lsc.component.css']
})
export class ManualLscComponent {
  paginaActual = 1;

  manual: ManualLsc = {
  titulo: 'Manual LSC',
  descripcion: 'Manual interno de funcionamiento de LSC Central.',
  icono: '📘',
  totalPaginas: 15,
  pdfUrl: '/assets/manual-lsc/Manual_LSCCentral.pdf',
  getImagenUrl: (pagina: number) =>
    `/assets/manual-lsc/pagina_${String(pagina).padStart(2, '0')}.png`
};

  paginaAnterior(): void {
    if (this.paginaActual > 1) {
      this.paginaActual--;
      this.scrollArriba();
    }
  }

  paginaSiguiente(): void {
    if (this.paginaActual < this.manual.totalPaginas) {
      this.paginaActual++;
      this.scrollArriba();
    }
  }

  irAPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.manual.totalPaginas) {
      this.paginaActual = pagina;
      this.scrollArriba();
    }
  }

  get paginas(): number[] {
    return Array.from(
      { length: this.manual.totalPaginas },
      (_, index) => index + 1
    );
  }

  abrirPdf(): void {
    window.open(this.manual.pdfUrl, '_blank');
  }

  descargarPdf(): void {
    const link = document.createElement('a');
    link.href = this.manual.pdfUrl;
    link.download = this.manual.pdfUrl.split('/').pop() ?? 'manual-lsc.pdf';
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
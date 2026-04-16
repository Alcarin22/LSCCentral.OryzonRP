import { CommonModule, CurrencyPipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FacturaService } from '../../services/factura.service';
import { SessionEmpleado, SessionService } from '../../core/services/session.service';

type TipoFactura = '' | 'Full Tuning' | 'Tuneo' | 'Reparación' | 'Items' | 'Tasación';

@Component({
  selector: 'app-factura',
  standalone: true,
  imports: [CommonModule, FormsModule, CurrencyPipe],
  templateUrl: './factura.component.html',
  styleUrl: './factura.component.css'
})
export class FacturaComponent {
  empleado: SessionEmpleado | null = null;

  tipoSeleccionado: TipoFactura = '';

  matricula = '';
  categoria = '';
  gravedad = '';
  item = '';
  cantidad: number | null = null;
  modelo = '';
  estado = '';

  tuneoPlate = '';
  selectedTuneoItems: string[] = [];

  convenio = false;
  total = 0;

  readonly preciosFullTuning: Record<string, number> = {
    Compacto: 5500,
    Sedan: 6500,
    Coupe: 6500,
    Muscle: 6500,
    Van: 5500,
    Offroad: 10500,
    SUV: 9000,
    Moto: 5500,
    Deportivo: 13000,
    'Deportivo Clasico': 130000,
    Super: 21000,
    VIP: 10000
  };

  readonly preciosReparacion: Record<string, number> = {
    'Leve (1-2)': 600,
    'Media (3-4)': 700,
    'Grave (5-6)': 800,
    'LSPD': 200
  };

  readonly tuneoItems: string[] = [
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

  constructor(
    private facturaService: FacturaService,
    private sessionService: SessionService
  ) {
    this.empleado = this.sessionService.getEmpleado();
  }

  onTipoFacturaChange(): void {
    this.resetCamposFactura();
    this.actualizarTotal();
  }

  actualizarTotal(): void {
    let baseTotal = 0;

    switch (this.tipoSeleccionado) {
      case 'Full Tuning':
        baseTotal = this.preciosFullTuning[this.categoria] ?? 0;
        break;

      case 'Tuneo':
        baseTotal = this.calcularTotalTuneo();
        break;

      case 'Reparación':
        baseTotal = this.preciosReparacion[this.gravedad] ?? 0;
        break;

      case 'Items':
        baseTotal = this.item === 'Kit Reparación' ? (this.cantidad ?? 0) * 1000 : 0;
        break;

      case 'Tasación':
        baseTotal =
          this.matricula.trim() && this.modelo.trim() && this.estado.trim() ? 500 : 0;
        break;

      default:
        baseTotal = 0;
    }

    this.total = this.convenio ? Math.round(baseTotal * 0.8) : baseTotal;
  }

  toggleTuneoItem(item: string, checked: boolean): void {
    if (checked) {
      if (!this.selectedTuneoItems.includes(item)) {
        this.selectedTuneoItems = [...this.selectedTuneoItems, item];
      }
    } else {
      this.selectedTuneoItems = this.selectedTuneoItems.filter(i => i !== item);
    }

    this.actualizarTotal();
  }

  isTuneoSelected(item: string): boolean {
    return this.selectedTuneoItems.includes(item);
  }

  private calcularTotalTuneo(): number {
    let total = 0;

    for (const item of this.selectedTuneoItems) {
      if (item === 'Pintura') {
        total += 2000;
      } else if (item === 'Livery' || item === 'Pintura Llantas') {
        total += 500;
      } else {
        total += 1000;
      }
    }

    return total;
  }

  enviarFactura(): void {
    if (!this.empleado?.discordId) {
      alert('No se ha encontrado la sesión del empleado.');
      return;
    }

    if (!this.tipoSeleccionado) {
      alert('Selecciona un tipo de factura.');
      return;
    }

    if (this.tipoSeleccionado === 'Tuneo') {
      if (!this.tuneoPlate.trim()) {
        alert('Introduce la matrícula del vehículo.');
        return;
      }

      if (this.selectedTuneoItems.length === 0) {
        alert('Selecciona al menos un elemento de tuneo.');
        return;
      }
    }

    const matriculaFinal =
      this.tipoSeleccionado === 'Tuneo' ? this.tuneoPlate.trim() : this.matricula.trim();

    const factura = {
      discordId: this.empleado.discordId,
      matricula: matriculaFinal,
      tipo: this.tipoSeleccionado,
      total: this.total,
      convenio: this.convenio
    };

    this.facturaService.crearFactura(factura).subscribe({
      next: (res) => {
        console.log('Factura guardada:', res);
        alert('Factura creada correctamente');
        this.resetFormularioCompleto();
      },
      error: (err) => {
        console.error(err);
        alert('Error al crear factura');
      }
    });
  }

  private resetCamposFactura(): void {
    this.matricula = '';
    this.categoria = '';
    this.gravedad = '';
    this.item = '';
    this.cantidad = null;
    this.modelo = '';
    this.estado = '';
    this.tuneoPlate = '';
    this.selectedTuneoItems = [];
    this.convenio = false;
  }

  private resetFormularioCompleto(): void {
    this.tipoSeleccionado = '';
    this.resetCamposFactura();
    this.total = 0;
  }
}
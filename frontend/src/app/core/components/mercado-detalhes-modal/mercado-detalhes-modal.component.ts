import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-mercado-detalhes-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mercado-detalhes-modal.component.html'
})
export class MercadoDetalhesModalComponent {
  @Input({ required: true }) mercado: any;
  @Output() onFechar = new EventEmitter<void>();

  /**
   * 🚗 Abre o Google Maps real com rota direta para o visitante chegar ao recinto
   */
  abrirRotaNoGoogleMaps() {
    if (!this.mercado || !this.mercado.latitude || !this.mercado.longitude) return;
    const url = `https://www.google.com/maps/search/?api=1&query=${this.mercado.latitude},${this.mercado.longitude}`;
    window.open(url, '_blank');
  }
}

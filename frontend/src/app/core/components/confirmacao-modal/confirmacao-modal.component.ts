import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirmacao-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirmacao-modal.component.html'
})
export class ConfirmacaoModalComponent {
  @Input() titulo = 'Confirmar Operação';
  @Input() mensagem = 'Tem a certeza de que deseja prosseguir com esta ação permanente?';
  @Input() confirmarTexto = 'Confirmar';
  @Input() cancelarTexto = 'Cancelar';

  @Output() onConfirmar = new EventEmitter<void>();
  @Output() onCancelar = new EventEmitter<void>();
}

import { Injectable, signal } from '@angular/core';

export interface ToastData {
  mensagem: string;
  tipo: 'success' | 'error' | 'info';
  titulo?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  // O único sinal que controla a visibilidade do balão na app inteira
  toast = signal<ToastData | null>(null);

  show(mensagem: string, tipo: 'success' | 'error' | 'info' = 'success', titulo?: string) {
    this.toast.set({ mensagem, tipo, titulo });

    // Auto-destruição após 4 segundos
    setTimeout(() => {
      this.clear();
    }, 4000);
  }

  clear() {
    this.toast.set(null);
  }
}

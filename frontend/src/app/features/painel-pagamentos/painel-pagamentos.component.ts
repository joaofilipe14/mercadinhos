import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../core/services/toast.service'; // 🎯 1. IMPORTAR O TOAST SERVICE GLOBAL

@Component({
  selector: 'app-painel-pagamentos',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './painel-pagamentos.component.html'
})
export class PainelPagamentosComponent implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private toastService = inject(ToastService); // 🎯 2. INJETAR O SERVIÇO GLOBAL

  candidaturaId = signal<number | null>(null);
  candidatura = signal<any>(null);
  isLoading = signal<boolean>(true);
  erro = signal<string>('');
  isPagando = signal<boolean>(false);

  metodoSelecionado = signal<'MB' | 'CARD' | 'IBAN'>('MB');
  dadosCartao = { numero: '', validade: '', cvc: '', titular: '' };
  metodosPermitidos = signal<string[]>(['MULTIBANCO', 'CREDIT_CARD', 'TRANSFERENCIA']);

  private pollInterval: any;

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.candidaturaId.set(Number(id));
        this.obterDadosCandidatura(Number(id));
      } else {
        this.isLoading.set(false);
        this.erro.set('Identificador de liquidação em falta na requisição.');
        this.toastService.show('Identificador de liquidação em falta.', 'error', 'Erro de Parâmetros');
      }
    });
  }

  ngOnDestroy() {
    if (this.pollInterval) clearInterval(this.pollInterval);
  }

  obterDadosCandidatura(id: number) {
    this.http.get<any>(`http://localhost:8080/api/candidaturas/${id}`).subscribe({
      next: (dados) => {
        this.candidatura.set(dados);
        this.isLoading.set(false);

        if (dados.mercado?.metodosPagamentoPermitidos) {
          this.metodosPermitidos.set(dados.mercado.metodosPagamentoPermitidos);
          if (!this.verificarPermissao('MULTIBANCO')) {
            this.metodoSelecionado.set(this.verificarPermissao('CREDIT_CARD') ? 'CARD' : 'IBAN');
          }
        }

        if (dados.estado === 'APROVADA') {
          clearInterval(this.pollInterval);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.erro.set('Não foi possível carregar a guia de pagamento eletrónica.');
        this.toastService.show('Não foi possível carregar a guia de pagamento eletrónica.', 'error', 'Erro de Ligação');
      }
    });
  }

  verificarPermissao(metodo: 'MULTIBANCO' | 'CREDIT_CARD' | 'TRANSFERENCIA'): boolean {
    return this.metodosPermitidos().includes(metodo);
  }

  efetuarPagamento() {
    if (!this.candidatura()) return;
    this.isPagando.set(true);

    const payload = {
      candidaturaId: this.candidaturaId(),
      valor: this.candidatura().precoTotal,
      emailFeirante: this.candidatura().feirante?.email || '',
      canalEscolhido: this.metodoSelecionado()
    };

    this.http.post('http://localhost:8080/api/pagamentos/efetuar', payload, {
      responseType: 'text'
    }).subscribe({
      next: () => {
        this.isPagando.set(false);
        this.toastService.show('Pagamento submetido com sucesso! A processar a licença...', 'success', 'Liquidação Efetuada');
        this.obterDadosCandidatura(this.candidaturaId()!);
      },
      error: () => {
        this.isPagando.set(false);
        this.toastService.show('Erro ao processar a liquidação junto do serviço de pagamentos.', 'error', 'Falha no Pagamento');
      }
    });
  }

  gerarReferenciaMb(id: number): string {
    const base = String(id).padStart(3, '0');
    return `305 ${base} 789`;
  }
}

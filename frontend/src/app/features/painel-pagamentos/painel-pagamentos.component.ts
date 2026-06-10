import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-painel-pagamentos',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './painel-pagamentos.component.html'
})
export class PainelPagamentosComponent implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);

  candidaturaId = signal<number | null>(null);
  candidatura = signal<any>(null);
  isLoading = signal<boolean>(true);
  erro = signal<string>('');
  isPagando = signal<boolean>(false);

  private pollInterval: any;

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.candidaturaId.set(Number(id));
        this.obterDadosCandidatura(Number(id));
        this.iniciarVerificacaoAutomatica(Number(id));
      } else {
        this.isLoading.set(false);
        this.erro.set('Identificador de liquidação em falta na requisição.');
      }
    });
  }

  ngOnDestroy() {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
    }
  }

  obterDadosCandidatura(id: number) {
    this.http.get<any>(`http://localhost:8080/api/candidaturas/${id}`).subscribe({
      next: (dados) => {
        this.candidatura.set(dados);
        this.isLoading.set(false);

        if (dados.estado === 'APROVADA') {
          clearInterval(this.pollInterval);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.erro.set('Não foi possível carregar a guia de pagamento eletrónica.');
      }
    });
  }

  iniciarVerificacaoAutomatica(id: number) {
    this.pollInterval = setInterval(() => {
      this.http.get<any>(`http://localhost:8080/api/candidaturas/${id}`).subscribe({
        next: (dados) => {
          this.candidatura.set(dados);
          if (dados.estado === 'APROVADA') {
            clearInterval(this.pollInterval);
          }
        }
      });
    }, 2000);
  }

  efetuarPagamento() {
    if (!this.candidatura()) return;
    this.isPagando.set(true);

    const payload = {
      candidaturaId: this.candidaturaId(),
      valor: this.candidatura().precoTotal,
      emailFeirante: this.candidatura().feirante?.email || ''
    };

    this.http.post('http://localhost:8080/api/pagamentos/efetuar', payload, {
      responseType: 'text'
    }).subscribe({
      next: () => {
        this.isPagando.set(false);
      },
      error: (err) => {
        this.isPagando.set(false);
        alert('Erro ao processar a liquidação junto do serviço de pagamentos.');
      }
    });
  }

  gerarReferenciaMb(id: number): string {
    const base = String(id).padStart(3, '0');
    return `305 ${base} 789`;
  }
}

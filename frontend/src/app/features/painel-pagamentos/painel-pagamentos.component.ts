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
  isPagando = signal<boolean>(false); // Controla o loading do botão de ação

  // Intervalo para verificar o estado reativamente (Polling da Saga)
  private pollInterval: any;

  ngOnInit() {
    // Captura o ?id=X enviado pelo link do e-mail
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

        // Se a Saga já terminou e o estado já for APROVADA, cancelamos o polling imediatamente
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
    // Verifica o estado da base de dados de 2 em 2 segundos para apanhar o veredito do Kafka automaticamente
    this.pollInterval = setInterval(() => {
      this.http.get<any>(`http://localhost:8080/api/candidaturas/${id}`).subscribe({
        next: (dados) => {
          this.candidatura.set(dados);
          if (dados.estado === 'APROVADA') {
            clearInterval(this.pollInterval); // Transação concluída com sucesso!
          }
        }
      });
    }, 2000);
  }

  // 🎯 NOVO MÉTODO: Envia a intenção de pagamento para o microsserviço financeiro dedicado
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
        // Desativa o loading do botão. O ecrã muda para verde sozinho
        // assim que o loop do iniciarVerificacaoAutomatica() ler "APROVADA"
        this.isPagando.set(false);
      },
      error: (err) => {
        this.isPagando.set(false);
        alert('Erro ao processar a liquidação junto do serviço de pagamentos.');
      }
    });
  }

  // Gera uma referência multibanco mockada e bonita com base no ID da candidatura
  gerarReferenciaMb(id: number): string {
    const base = String(id).padStart(3, '0');
    return `305 ${base} 789`;
  }
}

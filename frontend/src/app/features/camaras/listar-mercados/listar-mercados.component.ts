import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Mercado } from '../../../core/models/mercado.model';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service'; // 🎯 1. IMPORTAR O TOAST SERVICE GLOBAL

@Component({
  selector: 'app-listar-mercados',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './listar-mercados.component.html'
})
export class ListarMercadosComponent implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private toastService = inject(ToastService); // 🎯 2. INJETAR O SERVIÇO GLOBAL

  currentUser = this.authService.currentUser;
  mercados = signal<Mercado[]>([]);
  isLoading = signal(true);
  dashboardDados = signal<any>(null);

  ngOnInit() {
    const utilizadorLogado = this.currentUser();

    if (utilizadorLogado && utilizadorLogado.email) {
      const emailCamara = utilizadorLogado.email;
      this.carregarDashboardAutarquico(emailCamara);
      this.carregarMercadosDaCamara(emailCamara);
    } else {
      this.isLoading.set(false);
      // Alerta preventivo caso o token expire ou não haja sessão ativa
      this.toastService.show('Sessão expirada. Por favor, efetue o login novamente.', 'error', 'Autenticação');
    }
  }

  /**
   * 📊 Carrega as métricas financeiras do painel executivo
   */
  carregarDashboardAutarquico(email: string) {
    const headers = new HttpHeaders().set('X-User-Email', email);

    this.http.get<any>('http://localhost:8080/api/mercados/municipio/dashboard', { headers })
      .subscribe({
        next: (dados) => this.dashboardDados.set(dados),
        error: (err) => {
          console.error('Erro ao processar métricas do dashboard:', err);
          // 🎯 TOAST DE ERRO: Feedback visual instantâneo para o Município
          this.toastService.show('Falha ao sincronizar as métricas financeiras da autarquia.', 'error', 'Erro de FinOps');
        }
      });
  }

  /**
   * 🎪 Carrega o catálogo de feiras criadas pela conta municipal
   */
  carregarMercadosDaCamara(email: string) {
    const url = `http://localhost:8080/api/mercados/criados-por/${email}`;

    this.http.get<Mercado[]>(url).subscribe({
      next: (dados) => {
        this.mercados.set(dados);
        this.isLoading.set(false);
        if (dados.length > 0) {
          this.toastService.show('Catálogo municipal atualizado com sucesso.', 'success', 'Sincronização OK');
        }
      },
      error: (erro) => {
        console.error('Erro ao carregar mercados da autarquia:', erro);
        this.isLoading.set(false);
        this.toastService.show('Não foi possível estabelecer ligação com o inventário de feiras.', 'error', 'Falha de Rede');
      }
    });
  }
}

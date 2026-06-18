import { Component, Input, Output, EventEmitter, signal, inject, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { Candidatura } from '../../models/candidatura.model';

@Component({
  selector: 'app-inscricao-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inscricao-modal.component.html'
})
export class InscricaoModalComponent implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  @Input({ required: true }) mercado: any;
  @Output() onFechar = new EventEmitter<void>();
  @Output() onSucesso = new EventEmitter<void>();

  // Configurações reativas escolhidas pelo Feirante
  atividadeSelecionada = signal<string>('ARTESANATO'); // 'ARTESANATO' ou 'STREET_FOOD'
  infraestruturaSelecionada = signal<string>('PROPRIO'); // 'PROPRIO' ou 'ORGANIZACAO'
  dias = signal<number>(1);
  precoTotal = signal<number>(1);
  estado = signal<string>('PENDENTE'); // 'PENDENTE', 'APROVADA', 'REJEITADA'

  // Mapeamento dos ficheiros carregados
  dossieFicheiros = signal<{ [tipoDoc: string]: File }>({});

  isSubmetendo = signal<boolean>(false);
  mensagemSucesso = signal<string>('');
  mensagemErro = signal<string>('');

  ngOnInit() {
    // Garante que os dias começam a 1 caso falte informação do backend
    if (!this.dias() || this.dias() < 1) {
      this.dias.set(1);
    }
  }

  // Helper para ler o preço base da categoria selecionada (Stand Próprio)
  obterPrecoBase(): number {
    if (!this.mercado) return 0;
    return this.atividadeSelecionada() === 'STREET_FOOD'
      ? (this.mercado.precoStreetFoodStandProprio || 0)
      : (this.mercado.precoArtesanatoStandProprio || 0);
  }

  // Incrementa ou decrementa dias respeitando os limites da feira
  alterarDias(valor: number) {
    const total = this.dias() + valor;
    if (total >= 1 && total <= 30) { // Bloqueio de segurança UX
      this.dias.set(total);
    }
  }

  // 🪙 Motor de Cálculo Automatizado em Tempo Real (Computed Signal)
  contaFinal = computed(() => {
    if (!this.mercado) return 0;

    let precoUnitario = 0;

    if (this.atividadeSelecionada() === 'STREET_FOOD') {
      precoUnitario = this.mercado.precoStreetFoodStandProprio || 0;
    } else {
      precoUnitario = this.infraestruturaSelecionada() === 'ORGANIZACAO'
        ? (this.mercado.precoArtesanatoStandOrganizacao || 0)
        : (this.mercado.precoArtesanatoStandProprio || 0);
    }

    // Multiplica pelos dias contratados apenas se for faturação DIÁRIA
    if (this.mercado.tipoPreco === 'DIARIO') {
      return precoUnitario * this.dias();
    }

    return precoUnitario;
  });

  // Validador Automático: Garante que todos os obrigatórios foram carregados
  validarDossieCompleto = computed(() => {
    if (!this.mercado || !this.mercado.documentosExigidos) return true;
    const ficheirosAtuais = this.dossieFicheiros();
    return this.mercado.documentosExigidos.every((doc: string) => !!ficheirosAtuais[doc]);
  });

  onFileSelected(event: any, tipoDoc: string) {
    const file = event.target.files?.[0];
    if (file) {
      this.dossieFicheiros.update(atuais => ({
        ...atuais,
        [tipoDoc]: file
      }));
    }
  }

  enviarCandidatura() {
    if (!this.validarDossieCompleto()) return;

    this.isSubmetendo.set(true);
    this.mensagemErro.set('');
      const payloadCandidatura: Candidatura = {
      mercadoId: this.mercado.id,
      feiranteEmail: this.authService.currentUser()?.email || 'anonimo@feirante.pt',
      opcaoInfraestrutura: this.infraestruturaSelecionada(),
      dias: this.dias(),
      estado: this.estado(),
      precoTotal: this.contaFinal()
    };
    const formData = new FormData();
    formData.append('mercadoId', this.mercado.id.toString());
    formData.append('feiranteEmail', this.authService.currentUser()?.email || 'anonimo@feirante.pt');
    formData.append('opcaoInfraestrutura', this.infraestruturaSelecionada());
    formData.append('dias', this.dias().toString());
    formData.append('valorTotalPago', this.contaFinal().toString());

    const ficheiros = this.dossieFicheiros();
    Object.keys(ficheiros).forEach(key => {
      formData.append('pdfFiles', ficheiros[key], `${key}.pdf`);
    });

    this.http.post('http://localhost:8080/api/candidaturas/submeter', formData, {
      responseType: 'text'
    }).subscribe({
      next: (resposta) => {
        this.mensagemSucesso.set(resposta || 'Inscrição submetida com sucesso!');
        this.onSucesso.emit();
        setTimeout(() => this.onFechar.emit(), 1500);
      },
      error: (err) => {
        this.isSubmetendo.set(false);
        if (err.status === 409) {
          this.mensagemErro.set('Já possui uma inscrição registada para este mercado.');
        } else {
          this.mensagemErro.set('Erro ao processar a submissão do dossiê.');
        }
      }
    });
  }

  extrairNomeLegivel(tipoDoc: string): string {
    const dicionario: { [key: string]: string } = {
      'INICIO_ACTIVIDADE': 'Declaração de Início de Atividade',
      'NAO_DIVIDA_AT': 'Certidão de Não Dívida (Autoridade Tributária)',
      'REGISTO_CRIMINAL': 'Registo Criminal do Empreendedor'
    };
    return dicionario[tipoDoc] || tipoDoc.replace(/_/g, ' ');
  }
}

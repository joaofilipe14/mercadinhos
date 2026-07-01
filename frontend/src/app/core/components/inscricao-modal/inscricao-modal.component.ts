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
  atividadeSelecionada = signal<string>('ARTESANATO');
  infraestruturaSelecionada = signal<string>('PROPRIO');
  dias = signal<number>(1);
  precoTotal = signal<number>(1);
  estado = signal<string>('PENDENTE');

  // 🎯 SIGNALS DE GESTÃO DA PASTA DIGITAL DO PERFIL
  documentosPerfilExistentes = signal<string[]>([]); // Armazena as chaves dos PDFs já existentes no perfil (Ex: ['INICIO_ACTIVIDADE'])
  sincronizarComPerfil = signal<{ [tipoDoc: string]: boolean }>({}); // Monitoriza se deve salvar a alteração no perfil geral

  // Mapeamento dos ficheiros novos carregados localmente no modal
  dossieFicheiros = signal<{ [tipoDoc: string]: File }>({});

  isSubmetendo = signal<boolean>(false);
  mensagemSucesso = signal<string>('');
  mensagemErro = signal<string>('');

  ngOnInit() {
    if (!this.dias() || this.dias() < 1) {
      this.dias.set(1);
    }
    this.carregarDocumentosDoPerfil();
  }

  /**
   * 🔍 Verifica na pasta digital do feirante quais os documentos já arquivados no MinIO
   */
  carregarDocumentosDoPerfil() {
    const emailFeirante = this.authService.currentUser()?.email || 'anonimo@feirante.pt';

    // Consulta os metadados dos documentos do perfil de feirante
    this.http.get<string[]>(`http://localhost:8080/api/feirantes/perfil/documentos-ativos?email=${emailFeirante}`).subscribe({
      next: (docs) => {
        if (docs && docs.length > 0) {
          this.documentosPerfilExistentes.set(docs);
        }
      },
      error: (err) => console.log('ℹ️ O feirante ainda não possui documentos arquivados no perfil geral.')
    });
  }

  obterPrecoBase(): number {
    if (!this.mercado) return 0;
    return this.atividadeSelecionada() === 'STREET_FOOD'
      ? (this.mercado.precoStreetFoodStandProprio || 0)
      : (this.mercado.precoArtesanatoStandProprio || 0);
  }

  alterarDias(valor: number) {
    const total = this.dias() + valor;
    if (total >= 1 && total <= 30) {
      this.dias.set(total);
    }
  }

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

    if (this.mercado.tipoPreco === 'DIARIO') {
      return precoUnitario * this.dias();
    }
    return precoUnitario;
  });

  /**
   * 🪙 VALIDADOR AUTOMÁTICO EVOLUÍDO (Computed):
   * Garante o dossiê completo se o ficheiro estiver carregado LOCALMENTE OU se já existir no PERFIL geral.
   */
  validarDossieCompleto = computed(() => {
    if (!this.mercado || !this.mercado.documentosExigidos) return true;

    const ficheirosLocais = this.dossieFicheiros();
    const ficheirosNoPerfil = this.documentosPerfilExistentes();

    return this.mercado.documentosExigidos.every((doc: string) =>
      !!ficheirosLocais[doc] || ficheirosNoPerfil.includes(doc)
    );
  });

  onFileSelected(event: any, tipoDoc: string) {
    const file = event.target.files?.[0];
    if (file) {
      this.dossieFicheiros.update(atuais => ({
        ...atuais,
        [tipoDoc]: file
      }));

      // 🎯 UX INTELIGENTE: Se o documento já existia no perfil, ativa por defeito o gancho de sincronização automática
      if (this.documentosPerfilExistentes().includes(tipoDoc)) {
        this.sincronizarComPerfil.update(s => ({ ...s, [tipoDoc]: true }));
      }
    }
  }

  toggleSincronizacao(tipoDoc: string) {
    this.sincronizarComPerfil.update(s => ({
      ...s,
      [tipoDoc]: !s[tipoDoc]
    }));
  }

  enviarCandidatura() {
    if (!this.validarDossieCompleto()) return;

    this.isSubmetendo.set(true);
    this.mensagemErro.set('');

    const emailFeirante = this.authService.currentUser()?.email || 'anonimo@feirante.pt';

    // 🚀 EVENTO 1: Trata primeiro da Sincronização Paralela do Perfil (se selecionado)
    const ficheirosLocais = this.dossieFicheiros();
    const ganchosSincronizacao = this.sincronizarComPerfil();

    Object.keys(ficheirosLocais).forEach(docKey => {
      if (ganchosSincronizacao[docKey]) {
        const perfilFormData = new FormData();
        perfilFormData.append('email', emailFeirante);
        perfilFormData.append('tipoDocumento', docKey);
        perfilFormData.append('file', ficheirosLocais[docKey]);

        // Dispara o upload de atualização em background para a pasta digital do feirante
        this.http.post('http://localhost:8080/api/feirantes/perfil/atualizar-documento', perfilFormData).subscribe({
          next: () => console.log(`🟢 Documento ${docKey} sincronizado com sucesso com o perfil do feirante.`),
          error: (err) => console.error(`🔴 Falha na sincronização em background do documento ${docKey}:`, err)
        });
      }
    });

    // 🚀 EVENTO 2: Montagem do Payload Principal da Candidatura
    const formData = new FormData();
    formData.append('mercadoId', this.mercado.id.toString());
    formData.append('feiranteEmail', emailFeirante);
    formData.append('opcaoInfraestrutura', this.infraestruturaSelecionada());
    formData.append('dias', this.dias().toString());
    formData.append('valorTotalPago', this.contaFinal().toString());

    // Agregação mista de ficheiros binários e metadados de reaproveitamento
    const perfilAtivos = this.documentosPerfilExistentes();

    this.mercado.documentosExigidos.forEach((doc: string) => {
      if (ficheirosLocais[doc]) {
        // Envia o ficheiro novo inserido no modal
        formData.append('pdfFiles', ficheirosLocais[doc], `${doc}.pdf`);
      } else if (perfilAtivos.includes(doc)) {
        // Envia uma instrução textual avisando o backend para clonar o ficheiro existente no MinIO do perfil
        formData.append('documentosReutilizadosDoPerfil', doc);
      }
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
      'NAO_DIVIDA_SS': 'Certidão de Não Dívida (Segurança Social)',
      'SEGURO_ACIDENTES': 'Seguro de Acidentes de Trabalho',
      'CARTAO_FEIRANTE': 'Cartão Nacional de Feirante'
    };
    return dicionario[tipoDoc] || tipoDoc.replace(/_/g, ' ');
  }
}

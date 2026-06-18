import { Component, inject, signal, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service'; // 🎯 1. IMPORTAR O TOAST SERVICE
import { ConfirmacaoModalComponent } from '../../../core/components/confirmacao-modal/confirmacao-modal.component'; // 🎯 IMPORT DO NOVO MODAL

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    ConfirmacaoModalComponent
  ],
  templateUrl: './perfil.component.html'
})
export class PerfilComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private toastService = inject(ToastService); // 🎯 2. INJETAR O SERVIÇO GLOBAL

  currentUser = this.authService.currentUser;

  isGuardando = signal<boolean>(false);
  temDocAtividade = signal<boolean>(false);
  temDocFinancas = signal<boolean>(false);
  isUploadingAtividade = signal<boolean>(false);
  isUploadingFinancas = signal<boolean>(false);
  isHoveringAtividade = signal<boolean>(false);
  isHoveringFinancas = signal<boolean>(false);
  isModalAvisoAberto = signal<boolean>(false);
  documentoPendenteSubstituicao = signal<'INICIO_ACTIVIDADE' | 'NAO_DIVIDA_AT' | null>(null);
  meuHistorico = signal<any[]>([]);
  isLoadingHistorico = signal<boolean>(true);

  @ViewChild('fileAtividade') fileAtividadeInput!: ElementRef<HTMLInputElement>;
  @ViewChild('fileFinancas') fileFinancasInput!: ElementRef<HTMLInputElement>;

  perfilForm = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(3)]],
    email: [{ value: '', disabled: true }, [Validators.required, Validators.email]],
    nif: ['', [Validators.pattern(/^\d{9}$/)]],
    telemovel: ['', [Validators.pattern(/^\d{9}$/)]],
    morada: ['']
  });

  ngOnInit(): void {
    const user = this.currentUser();
    if (user) {
      this.perfilForm.patchValue({
        email: user.email
      });

      const userEmail = user.email;
      if (user.role === 'ROLE_FEIRANTE') {
        this.carregarDadosExtraFeirante(userEmail);
      } else if (user.role === 'ROLE_MUNICIPO') {
        this.carregarDadosExtraMunicipio(userEmail);
      }
    }
  }

  carregarDadosExtraFeirante(email: string) {
    this.isLoadingHistorico.set(true);
    this.http.get<any>(`http://localhost:8080/api/feirantes/perfil?email=${email}`)
      .subscribe({
        next: (dados) => {
          this.perfilForm.patchValue({
            nome: dados.nome,
            nif: dados.nif,
            telemovel: dados.telemovel,
            morada: dados.morada
          });
          this.temDocAtividade.set(!!dados.portfolioDocumentos?.INICIO_ACTIVIDADE);
          this.temDocFinancas.set(!!dados.portfolioDocumentos?.NAO_DIVIDA_AT);
          this.meuHistorico.set(dados.candidaturas || []);
          this.isLoadingHistorico.set(false);
        },
        error: () => {
          this.isLoadingHistorico.set(false);
          this.toastService.show('Não foi possível carregar o histórico de inscrições.', 'error', 'Erro de Sincronização');
        }
      });
  }

  carregarDadosExtraMunicipio(email: string) {
    this.http.get<any>(`http://localhost:8080/api/municipios/perfil?email=${email}`)
      .subscribe({
        next: (dados) => {
          this.perfilForm.patchValue({
            nome: dados.nomeCamara,
            nif: dados.nifAutarquia,
            telemovel: dados.telefoneOficial,
            morada: dados.moradaPacosConcelho
          });
        },
        error: () => console.log('A carregar perfil base autárquico por defeito...')
      });
  }

  guardarDadosFiscais() {
    if (this.perfilForm.invalid) return;

    this.isGuardando.set(true);

    const formRaw = this.perfilForm.getRawValue();
    const role = this.currentUser()?.role;

    let url = 'http://localhost:8080/api/utilizadores/atualizar-perfil';
    let payload: any = formRaw;

    if (role === 'ROLE_FEIRANTE') {
      url = 'http://localhost:8080/api/feirantes/atualizar-perfil';
      payload = formRaw;
    } else if (role === 'ROLE_MUNICIPO') {
      url = 'http://localhost:8080/api/municipios/atualizar-perfil';
      payload = {
        email: formRaw.email,
        nomeCamara: formRaw.nome,
        nifAutarquia: formRaw.nif,
        telefoneOficial: formRaw.telemovel,
        moradaPacosConcelho: formRaw.morada
      };
    }

    this.http.post(url, payload).subscribe({
      next: () => {
        this.isGuardando.set(false);
        this.toastService.show('Informações de perfil atualizadas com sucesso!', 'success', 'Alterações Guardadas');
      },
      error: () => {
        this.isGuardando.set(false);
        this.toastService.show('Erro ao salvar as informações fiscais no servidor.', 'error', 'Falha no Perfil');
      }
    });
  }

  manipularCliqueDocumento(tipoDoc: 'INICIO_ACTIVIDADE' | 'NAO_DIVIDA_AT', fileInput: HTMLInputElement) {
    const jaExiste = tipoDoc === 'INICIO_ACTIVIDADE' ? this.temDocAtividade() : this.temDocFinancas();
    if (!jaExiste) {
      fileInput.click();
    } else {
      this.documentoPendenteSubstituicao.set(tipoDoc);
      this.isModalAvisoAberto.set(true);
    }
  }

  /**
   * 🎯 RESPOSTA DO MODAL: Executado quando o utilizador clica em "Sim, Substituir" no componente genérico
   */
  confirmarSubstituicao() {
    const tipo = this.documentoPendenteSubstituicao();
    this.isModalAvisoAberto.set(false);
    this.documentoPendenteSubstituicao.set(null);

    // Dispara o clique programático usando a referência nativa do Angular
    if (tipo === 'INICIO_ACTIVIDADE' && this.fileAtividadeInput) {
      this.fileAtividadeInput.nativeElement.click();
    } else if (tipo === 'NAO_DIVIDA_AT' && this.fileFinancasInput) {
      this.fileFinancasInput.nativeElement.click();
    }
  }

  onDragOver(event: DragEvent, tipoDoc: 'INICIO_ACTIVIDADE' | 'NAO_DIVIDA_AT') {
    event.preventDefault();
    event.stopPropagation();
    if (tipoDoc === 'INICIO_ACTIVIDADE') this.isHoveringAtividade.set(true);
    if (tipoDoc === 'NAO_DIVIDA_AT') this.isHoveringFinancas.set(true);
  }

  onDragLeave(event: DragEvent, tipoDoc: 'INICIO_ACTIVIDADE' | 'NAO_DIVIDA_AT') {
    event.preventDefault();
    event.stopPropagation();
    if (tipoDoc === 'INICIO_ACTIVIDADE') this.isHoveringAtividade.set(false);
    if (tipoDoc === 'NAO_DIVIDA_AT') this.isHoveringFinancas.set(false);
  }

  onDropFile(event: DragEvent, tipoDoc: 'INICIO_ACTIVIDADE' | 'NAO_DIVIDA_AT') {
    event.preventDefault();
    event.stopPropagation();
    if (tipoDoc === 'INICIO_ACTIVIDADE') this.isHoveringAtividade.set(false);
    if (tipoDoc === 'NAO_DIVIDA_AT') this.isHoveringFinancas.set(false);
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      const mockEvent = { target: { files: [files[0]] } };
      // Validação de substituição preventiva também no Drop!
      const jaExiste = tipoDoc === 'INICIO_ACTIVIDADE' ? this.temDocAtividade() : this.temDocFinancas();
      if (jaExiste) {
        this.toastService.show('Para substituir um ficheiro existente utilize o botão dedicado.', 'info', 'Operação Bloqueada');
        return;
      }
      this.uploadDocumentoPortfolio(mockEvent, tipoDoc);
    }
  }

  uploadDocumentoPortfolio(event: any, tipoDoc: 'INICIO_ACTIVIDADE' | 'NAO_DIVIDA_AT') {
    const file = event.target.files[0];
    if (!file) return;

    if (file.type !== 'application/pdf') {
      this.toastService.show('Apenas são permitidos ficheiros em formato PDF.', 'error', 'Formato Inválido');
      return;
    }

    // ⚡ ESTADO INTERNO: Ativa o loading exclusivo do botão pressionado
    if (tipoDoc === 'INICIO_ACTIVIDADE') this.isUploadingAtividade.set(true);
    if (tipoDoc === 'NAO_DIVIDA_AT') this.isUploadingFinancas.set(true);

    const formData = new FormData();
    formData.append('email', this.currentUser()?.email || '');
    formData.append('tipoDocumento', tipoDoc);
    formData.append('file', file);

    this.http.post('http://localhost:8080/api/feirantes/upload-portfolio', formData, {
      responseType: 'text'
    }).subscribe({
      next: () => {
        if (tipoDoc === 'INICIO_ACTIVIDADE') this.temDocAtividade.set(true);
        if (tipoDoc === 'NAO_DIVIDA_AT') this.temDocFinancas.set(true);
        this.toastService.show('Ficheiro associado com sucesso à sua pasta digital!', 'success', 'Documento Guardado');
        event.target.value = ''; // Limpa a cache do input
      },
      error: (err) => {
        console.error('Erro de upload:', err);
        const mensagemServidor = err.error || 'Falha ao submeter o ficheiro para o servidor autárquico.';
        this.toastService.show(mensagemServidor, 'error', 'Erro de Upload');
      },
      complete: () => {
        // ⚡ RESET DO ESTADO: Desliga o loading em ambos os botões após a resposta final
        this.isUploadingAtividade.set(false);
        this.isUploadingFinancas.set(false);
      }
    });
  }

  traduzirEstado(estado: string): string {
    const dicionario: { [key: string]: string } = {
      'PENDENTE': 'Em Análise',
      'A_AGUARDAR_PAGAMENTO': 'Aguardando Pagamento',
      'APROVADA': 'Confirmada / Vaga Garantida',
      'REJEITADA': 'Recusada'
    };
    return dicionario[estado] || estado.replace(/_/g, ' ');
  }
}

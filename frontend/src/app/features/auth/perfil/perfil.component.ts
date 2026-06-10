import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './perfil.component.html'
})
export class PerfilComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  currentUser = this.authService.currentUser;

  isGuardando = signal<boolean>(false);
  mensagemSucesso = signal<string | null>(null);
  mensagemErro = signal<string | null>(null);

  temDocAtividade = signal<boolean>(false);
  temDocFinancas = signal<boolean>(false);

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
    this.http.get<any>(`http://localhost:8080/api/feirantes/perfil?email=${email}`)
      .subscribe({
        next: (dados) => {
          this.perfilForm.patchValue({
            nif: dados.nif,
            telemovel: dados.telemovel,
            morada: dados.morada
          });
          this.temDocAtividade.set(!!dados.portfolioDocumentos?.INICIO_ACTIVIDADE);
          this.temDocFinancas.set(!!dados.portfolioDocumentos?.NAO_DIVIDA_AT);
        },
        error: () => console.log('A carregar perfil base de feirante por defeito...')
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
    this.mensagemSucesso.set(null);
    this.mensagemErro.set(null);

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
        this.mensagemSucesso.set('Dados guardados com sucesso!');
      },
      error: (err) => {
        this.isGuardando.set(false);
        this.mensagemErro.set('Erro ao guardar as informações.');
      }
    });
  }

  uploadDocumentoPortfolio(event: any, tipoDoc: 'INICIO_ACTIVIDADE' | 'NAO_DIVIDA_AT') {
    const file = event.target.files[0];
    if (!file || file.type !== 'application/pdf') {
      this.mensagemErro.set('Só aceitamos ficheiros PDF.');
      return;
    }

    const formData = new FormData();
    formData.append('email', this.currentUser()?.email || '');
    formData.append('tipoDocumento', tipoDoc);
    formData.append('file', file);

    this.http.post('http://localhost:8080/api/feirantes/upload-portfolio', formData)
      .subscribe({
        next: () => {
          if (tipoDoc === 'INICIO_ACTIVIDADE') this.temDocAtividade.set(true);
          if (tipoDoc === 'NAO_DIVIDA_AT') this.temDocFinancas.set(true);
          this.mensagemSucesso.set('Documento guardado com sucesso!');
        },
        error: () => this.mensagemErro.set('Falha no upload do documento.')
      });
  }
}

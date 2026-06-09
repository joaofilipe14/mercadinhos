import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { LogoComponent } from '../../../core/components/logo/logo.component';

@Component({
  selector: 'app-registo',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    LogoComponent
  ],
  templateUrl: './registo.component.html'
})
export class RegistoComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);

  // Signals de Controlo de Estado Reativo
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  // 🎯 Signal que controla visualmente qual o cartão selecionado (Padrão: Feirante)
  perfilSelecionado = signal<string>('ROLE_FEIRANTE');

  // Inicialização do Formulário Reativo
  registoForm = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['', Validators.required] // Guarda o valor interno enviado para o Gateway
  });

  /**
   * 🎯 Função executada ao clicar em qualquer um dos cartões no HTML.
   * Altera o estado visual e sincroniza o campo do formulário em background.
   */
  mudarPerfil(novaRole: string) {
    this.perfilSelecionado.set(novaRole);
    this.registoForm.patchValue({ role: novaRole });
  }

  onSubmit() {
    if (this.registoForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    // Extrai o objeto limpo pronto para transmissão JSON
    const payload = this.registoForm.getRawValue();

    // Rota centralizada de registo no identidade-service (via Gateway na porta 8080)
    this.http.post('http://localhost:8080/api/auth/registar', payload, { responseType: 'text' })
      .subscribe({
        next: () => {
          this.successMessage.set('Perfil registado com sucesso! A redirecionar para o ecrã de login...');

          // Aguarda um pequeno delay para o feirante ler a mensagem de sucesso
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2500);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err.error || 'Não foi possível concluir o registo. Valide os dados introduzidos.');
        },
        complete: () => this.isLoading.set(false)
      });
  }
}

import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { LogoComponent } from '../../../core/components/logo/logo.component';
import { ToastService } from '../../../core/services/toast.service'; // 🎯 1. Importar o serviço global

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
  private toastService = inject(ToastService); // 🎯 2. Injetar o ToastService

  isLoading = signal<boolean>(false);
  perfilSelecionado = signal<string>('ROLE_FEIRANTE');

  // 🎯 3. Otimização: Role inicializada logo como 'ROLE_FEIRANTE' para dar match com o Signal
  registoForm = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['ROLE_FEIRANTE', Validators.required]
  });

  mudarPerfil(novaRole: string) {
    this.perfilSelecionado.set(novaRole);
    this.registoForm.patchValue({ role: novaRole });
  }

  isCampoInvalido(campo: string): boolean {
    const controlo = this.registoForm.get(campo);
    return !!(controlo && controlo.invalid && (controlo.touched || controlo.dirty));
  }

  onSubmit() {
    if (this.registoForm.invalid) return;

    this.isLoading.set(true);

    const payload = this.registoForm.getRawValue();

    this.http.post('http://localhost:8080/api/auth/registar', payload, { responseType: 'text' })
      .subscribe({
        next: () => {
          // 🎯 4. Feedback elegante via Toast de Sucesso
          this.toastService.show('Conta criada! A preparar o seu painel...', 'success', 'Registo Concluído');

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2500);
        },
        error: (err) => {
          this.isLoading.set(false);
          const msgErro = err.error || 'Não foi possível concluir o registo.';

          // 🎯 5. Feedback elegante via Toast de Erro
          this.toastService.show(msgErro, 'error', 'Erro no Registo');
        },
        complete: () => this.isLoading.set(false)
      });
  }
}

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

  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  perfilSelecionado = signal<string>('ROLE_FEIRANTE');

  registoForm = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['', Validators.required]
  });

  mudarPerfil(novaRole: string) {
    this.perfilSelecionado.set(novaRole);
    this.registoForm.patchValue({ role: novaRole });
  }

  onSubmit() {
    if (this.registoForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const payload = this.registoForm.getRawValue();

    this.http.post('http://localhost:8080/api/auth/registar', payload, { responseType: 'text' })
      .subscribe({
        next: () => {
          this.successMessage.set('Registo concluído! A redirecionar...');

          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2500);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err.error || 'Não foi possível concluir o registo.');
        },
        complete: () => this.isLoading.set(false)
      });
  }
}

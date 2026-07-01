import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LogoComponent } from '../../../core/components/logo/logo.component';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    LogoComponent
  ],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  loginForm = this.fb.nonNullable.group({
    email: ['camara@test.com', [Validators.required, Validators.email]],
    password: ['password', Validators.required]
  });

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { email, password } = this.loginForm.getRawValue();

    this.authService.login(email, password).subscribe({
      next: (res) => {
        this.toastService.show('Autenticação confirmada. Bem-vindo de volta!', 'success', 'Sessão Iniciada');
        if (res.role === 'ROLE_MUNICIPIO') {
          this.router.navigate(['/mercados']);
        } else if (res.role === 'ROLE_FEIRANTE') {
          this.router.navigate(['/mercados-vitrine']);
        } else {
          this.router.navigate(['/mercados-vitrine']);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('Credenciais inválidas. Tenta outra vez.');
        this.toastService.show('E-mail ou palavra-passe incorretos.', 'error', 'Falha no Login');
      },
      complete: () => this.isLoading.set(false)
    });
  }
}

import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms'; // 🎯 Importado o RouterModule
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { LogoComponent } from '../../../core/components/logo/logo.component';

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
        // Redirecionamento inteligente baseado nas permissões do utilizador
        if (res.role === 'ROLE_MUNICIPO') {
          // 🏛️ Funcionários das Câmaras vão para o painel de criação/gestão de mercados
          this.router.navigate(['/mercados']);
        } else if (res.role === 'ROLE_FEIRANTE') {
          // 🧑‍🌾 Feirantes vão para a vitrine unificada
          this.router.navigate(['/mercados-vitrine']);
        } else {
          // Fallback seguro caso exista outro perfil no futuro
          this.router.navigate(['/mercados-vitrine']);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('Credenciais inválidas. Tente novamente.');
      },
      complete: () => this.isLoading.set(false)
    });
  }
}

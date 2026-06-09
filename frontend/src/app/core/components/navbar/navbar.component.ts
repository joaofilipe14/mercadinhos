import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service'; // Ajusta o caminho para o teu AuthService
import { LogoComponent } from '../logo/logo.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, LogoComponent],
  templateUrl: './navbar.component.html'
})
export class NavbarComponent {
  private authService = inject(AuthService);
  private http = inject(HttpClient);
  private router = inject(Router);

  // Signals reativos derivados do teu AuthService global
  currentUser = computed(() => this.authService.currentUser());
  isLoggedIn = computed(() => this.currentUser() !== null);

  alertasCount = signal<number>(0);
  isMunicipio = computed(() => this.currentUser()?.role === 'ROLE_MUNICIPO');
  isFeirante = computed(() => this.currentUser()?.role === 'ROLE_FEIRANTE');

  ngOnInit() {
    if (this.isMunicipio()) {
      this.carregarContadorAlertas();
    }
  }

  logout() {
    // 1. Chamar o método de logout no teu serviço (deve limpar o localStorage e o Signal)
    this.authService.logout();

    // 2. Redirecionar imediatamente para a vitrine pública
    this.router.navigate(['/mercados-vitrine']);
  }

  carregarContadorAlertas() {
    this.http.get<any>('http://localhost:8080/api/mercados/municipio/dashboard').subscribe({
      next: (dados) => {
        this.alertasCount.set(dados.feirantesAguardandoAprovacao); // Alimenta o badge do Sino!
      }
    });
  }
}

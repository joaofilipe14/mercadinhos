import { Component, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';
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

  currentUser = computed(() => this.authService.currentUser());
  isLoggedIn = computed(() => this.currentUser() !== null);

  alertasCount = signal<number>(0);
  isMunicipio = computed(() => this.currentUser()?.role === 'ROLE_MUNICIPO');
  isFeirante = computed(() => this.currentUser()?.role === 'ROLE_FEIRANTE');

  ngOnInit() {
    if (this.isMunicipio()) {
      this.carregarContadorAlertas();
    }
    if (this.isFeirante()) {
      this.carregarContadorAlertas();
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/mercados-vitrine']);
  }

  carregarContadorAlertas() {
    this.http.get<any>('http://localhost:8080/api/mercados/municipio/dashboard').subscribe({
      next: (dados) => {
        this.alertasCount.set(dados.feirantesAguardandoAprovacao);
      }
    });
  }

  carregarContadorFeirantesAlertas() {
    this.http.get<any>('http://localhost:8080/api/mercados/municipio/dashboard').subscribe({
      next: (dados) => {
        this.alertasCount.set(dados.feirantesAguardandoAprovacao);
      }
    });
  }
  
  getProfileRoute(): string {
    if (this.isMunicipio()) {
      return '/perfil/municipio';
    } else if (this.isFeirante()) {
      return '/perfil/feirante';
    }
    return '/perfil';
  }
}

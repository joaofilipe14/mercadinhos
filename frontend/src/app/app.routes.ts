import { Routes } from '@angular/router';

export const routes: Routes = [
  // 1. Rota Padrão: Manda o utilizador anónimo direto para a vitrine pública (Sem forçar login!)
  {
    path: '',
    redirectTo: '/mercados-vitrine',
    pathMatch: 'full'
  },

  // 2. Vitrine Unificada (Cidadão vê mercados e notícias / Feirante vê botão de registo)
  {
    path: 'mercados-vitrine',
    loadComponent: () => import('./features/mercados-vitrine/mercados-vitrine.component').then(m => m.MercadosVitrineComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'registo',
    loadComponent: () => import('./features/auth/registo/registo.component').then(m => m.RegistoComponent)
  },
  {
    path: 'perfil',
    loadComponent: () => import('./features/auth/perfil/perfil.component').then(m => m.PerfilComponent)
  },
  {
    path: 'mercados',
    loadComponent: () => import('./features/camaras/listar-mercados/listar-mercados.component').then(m => m.ListarMercadosComponent)
  },
  {
    path: 'mercados/criar',
    loadComponent: () => import('./features/camaras/formulario-mercado/formulario-mercado.component').then(m => m.FormularioMercadoComponent)
  },
  {
    path: 'mercados/editar/:id',
    loadComponent: () => import('./features/camaras/formulario-mercado/formulario-mercado.component').then(m => m.FormularioMercadoComponent)
  },
  {
    path: 'mercados/:id/candidaturas',
    loadComponent: () => import('./features/camaras/gestao-candidaturas/gestao-candidaturas.component').then(m => m.GestaoCandidaturasComponent)
  },
  {
    path: 'painel/pagamentos',
    loadComponent: () => import('./features/painel-pagamentos/painel-pagamentos.component').then(m => m.PainelPagamentosComponent)
  }
];

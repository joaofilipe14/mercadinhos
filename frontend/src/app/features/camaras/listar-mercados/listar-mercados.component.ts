import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Mercado } from '../../../core/models/mercado.model';

@Component({
  selector: 'app-listar-mercados',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './listar-mercados.component.html'
})
export class ListarMercadosComponent implements OnInit {
  private http = inject(HttpClient);

  mercados = signal<Mercado[]>([]);
  isLoading = signal(true);
  dashboardDados = signal<any>(null);

  ngOnInit() {
    this.carregarMercados();
    this.http.get<any>('http://localhost:8080/api/mercados/municipio/dashboard').subscribe(dados => {
      this.dashboardDados.set(dados);
    });
  }

  carregarMercados() {
    this.http.get<Mercado[]>('http://localhost:8080/api/mercados').subscribe({
      next: (dados) => {
        this.mercados.set(dados);
        this.isLoading.set(false);
      },
      error: (erro) => {
        console.error('Erro ao carregar mercados:', erro);
        this.isLoading.set(false);
      }
    });
  }
}

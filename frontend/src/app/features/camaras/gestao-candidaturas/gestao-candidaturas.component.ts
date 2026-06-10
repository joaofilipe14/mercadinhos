import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-gestao-candidaturas',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './gestao-candidaturas.component.html'
})
export class GestaoCandidaturasComponent implements OnInit {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);

  candidaturas = signal<any[]>([]);
  mercadoId = signal<number | null>(null);
  isLoading = signal(true);
  expandedRows = signal<Set<number>>(new Set());

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.mercadoId.set(Number(id));
      this.carregarCandidaturas(Number(id));
    }
  }

  carregarCandidaturas(id: number) {
    this.isLoading.set(true);
    this.http.get<any[]>(`http://localhost:8080/api/candidaturas/mercado/${id}`).subscribe({
      next: (dados) => {
        this.candidaturas.set(dados);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  alterarEstado(candidaturaId: number, novoEstado: string) {
    this.http.put(`http://localhost:8080/api/candidaturas/${candidaturaId}/estado?estado=${novoEstado}`, {}).subscribe({
      next: () => {
        this.carregarCandidaturas(this.mercadoId()!);
      },
      error: () => alert('Erro ao atualizar o fluxo regulamentar da candidatura.')
    });
  }

  toggleRow(id: number) {
    this.expandedRows.update(set => {
      const newSet = new Set(set);
      if (newSet.has(id)) newSet.delete(id);
      else newSet.add(id);
      return newSet;
    });
  }

  isRowExpanded(id: number): boolean {
    return this.expandedRows().has(id);
  }

  getDocumentosKeys(candidatura: any): string[] {
    if (!candidatura || !candidatura.documentosAnexados) return [];
    return Object.keys(candidatura.documentosAnexados);
  }

  traduzirEstado(estado: string): string {
    const dicionario: { [key: string]: string } = {
      'PENDENTE': 'Em Análise',
      'A_AGUARDAR_PAGAMENTO': 'Aguardando Pagamento',
      'APROVADA': 'Confirmada / Paga',
      'REJEITADA': 'Rejeitada'
    };
    return dicionario[estado] || estado.replace(/_/g, ' ');
  }

  extrairNomeLegivel(tipoDoc: string): string {
    const dicionario: { [key: string]: string } = {
      'INICIO_ACTIVIDADE': 'Declaração de Início de Atividade',
      'NAO_DIVIDA_AT': 'Certidão de Não Dívida (AT)',
      'NAO_DIVIDA_SS': 'Certidão de Não Dívida (Segurança Social)',
      'REGISTO_CRIMINAL': 'Registo Criminal',
      'SEGURO_RESP_CIVIL': 'Seguro de Responsabilidade Civil'
    };
    return dicionario[tipoDoc] || tipoDoc.replace(/_/g, ' ');
  }

  descarregarDocumento(candidaturaId: number, tipoDoc: string) {
    this.http.get(`http://localhost:8080/api/candidaturas/${candidaturaId}/documentos/${tipoDoc}`, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        const urlVirtual = window.URL.createObjectURL(blob);
        const linkInvisivel = document.createElement('a');
        linkInvisivel.href = urlVirtual;
        linkInvisivel.download = `${tipoDoc}.pdf`;
        document.body.appendChild(linkInvisivel);
        linkInvisivel.click();
        document.body.removeChild(linkInvisivel);
        window.URL.revokeObjectURL(urlVirtual);
      },
      error: (err) => {
        console.error('Erro ao descarregar:', err);
        alert('Ficheiro físico indisponível no servidor.');
      }
    });
  }
}

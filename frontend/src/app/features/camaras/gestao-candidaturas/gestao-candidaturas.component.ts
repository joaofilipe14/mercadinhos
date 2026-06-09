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
    // Exemplo de estados: APROVADA, REJEITADA, PENDENTE
    this.http.put(`http://localhost:8080/api/candidaturas/${candidaturaId}/estado?estado=${novoEstado}`, {}).subscribe({
      next: () => {
        // Atualiza a lista automaticamente após sucesso
        this.carregarCandidaturas(this.mercadoId()!);
      },
      error: () => alert('Erro ao alterar o estado da candidatura.')
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

  // 🎯 O Java devolve um JSON com as chaves (Map), isto extrai as chaves para iterarmos no HTML
  getDocumentosKeys(candidatura: any): string[] {
    if (!candidatura || !candidatura.documentosAnexados) return [];
    return Object.keys(candidatura.documentosAnexados);
  }

  // 🎯 Traduz o Enum "feio" para um nome bonito na UI
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
    // É OBRIGATÓRIO definir o responseType como 'blob' para o Angular não tentar ler o PDF como JSON!
    this.http.get(`http://localhost:8080/api/candidaturas/${candidaturaId}/documentos/${tipoDoc}`, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        // Cria um URL virtual na memória do browser com o ficheiro recebido
        const urlVirtual = window.URL.createObjectURL(blob);

        // Cria um link invisível, clica nele automaticamente e destroi-o
        const linkInvisivel = document.createElement('a');
        linkInvisivel.href = urlVirtual;
        linkInvisivel.download = `${tipoDoc}.pdf`; // Nome com que o ficheiro vai ser guardado
        document.body.appendChild(linkInvisivel);
        linkInvisivel.click();

        // Limpa a memória
        document.body.removeChild(linkInvisivel);
        window.URL.revokeObjectURL(urlVirtual);
      },
      error: (err) => {
        console.error('Erro ao descarregar:', err);
        alert('Não foi possível descarregar o documento. Verifique se o ficheiro existe no servidor.');
      }
    });
  }
}

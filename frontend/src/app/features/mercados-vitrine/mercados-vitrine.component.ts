import { Component, OnInit, signal, computed, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { InscricaoModalComponent } from '../../core/components/inscricao-modal/inscricao-modal.component';
import { MercadoDetalhesModalComponent } from '../../core/components/mercado-detalhes-modal/mercado-detalhes-modal.component';

import * as L from 'leaflet';

@Component({
  selector: 'app-mercados-vitrine',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    InscricaoModalComponent,
    MercadoDetalhesModalComponent
  ],
  templateUrl: './mercados-vitrine.component.html'
})
export class MercadosVitrineComponent implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);
  private platformId = inject(PLATFORM_ID);

  private latitudeCache: number | null = null;
  private longitudeCache: number | null = null;
  private marcadorCentroUtilizador: L.CircleMarker | null = null;
  private camadaCirculoRaio: L.Circle | null = null;

  // Estado Reativo Principal
  mercados = signal<any[]>([]);
  paginaAtual = signal<number>(0);
  isUltimaPagina = signal<boolean>(false);
  raioKm = signal<number>(25);
  mapa: L.Map | undefined;
  isPesquisandoGps = signal<boolean>(false);

  // 🐾 FILTROS ADICIONAIS DE COMODIDADES
  apenasPetFriendly = signal<boolean>(false);
  apenasComWc = signal<boolean>(false);

  // Gestão de Inscrições / Modais
  isModalAberto = signal<boolean>(false);
  mercadoSelecionado = signal<any>(null);
  inscricoesFeitas = signal<number[]>([]);
  isDetalhesAberto = signal<boolean>(false);
  mercadoDetalhes = signal<any>(null);

  userRole = computed(() => this.authService.currentUser()?.role || '');
  isFeirante = computed(() => this.userRole() === 'ROLE_FEIRANTE');
  isMunicipio = computed(() => this.userRole() === 'ROLE_MUNICIPO');

  noticias = signal([
    { id: 1, titulo: 'Feira das Mercês regressa já este fim de semana', autoria: 'Câmara Municipal' },
    { id: 2, titulo: 'Novas vagas abertas para mercados sazonais de Verão', autoria: 'Junta de Freguesia' }
  ]);

  // 🎯 FILTRAGEM REATIVA EM TEMPO REAL (Para PetFriendly e WC)
  mercadosExibidos = computed(() => {
    const filtrados = this.mercados().filter(m => {
      const cumprePet = !this.apenasPetFriendly() || m.petFriendly;
      const cumpreWc = !this.apenasComWc() || m.temWc;
      return cumprePet && cumpreWc;
    });
    return filtrados.sort((a: any, b: any) => {
      return new Date(a.dataInicio).getTime() - new Date(b.dataInicio).getTime();
    });
  });

  ngOnInit() {
    this.carregarMercadosProximos(true);
    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => this.iniciarMapa(), 100);
    }
  }

  iniciarMapa() {
    this.mapa = L.map('mapa-vitrine').setView([39.3999, -8.2245], 6);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap'
    }).addTo(this.mapa);

    setTimeout(() => {
      this.mapa?.invalidateSize();
      if (this.mercados().length > 0) {
        this.adicionarMarcadoresNoMapa(this.mercados());
      }
    }, 500);
  }

  // 🎯 ATUALIZADO: Nome dos parâmetros unificado com o "respostaPaged"
  carregarMercadosProximos(resetarLista = false) {
    if (resetarLista) {
      this.paginaAtual.set(0);
    }

    const url = `http://localhost:8080/api/mercados?page=${this.paginaAtual()}&size=5`;

    this.http.get<any>(url).subscribe({
      next: (respostaPaged: any) => {
        // Proteção: extrai a lista quer venha como Page do Spring ou como Array nativo
        const novosMercados = respostaPaged.content || respostaPaged;
        const ultimo = respostaPaged.page
          ? (respostaPaged.page.number + 1) >= respostaPaged.page.totalPages
          : (respostaPaged.last !== undefined ? respostaPaged.last : true);

        if (resetarLista) {
          this.mercados.set(novosMercados);
        } else {
          this.mercados.update(listaAntiga => [...listaAntiga, ...novosMercados]);
        }

        this.adicionarMarcadoresNoMapa(this.mercados());
        this.isUltimaPagina.set(ultimo);
      },
      error: (err) => console.error('Erro ao carregar montra de mercados', err)
    });
  }

  carregarProximaPagina() {
    this.paginaAtual.update(p => p + 1);
    this.carregarMercadosProximos(false);
  }

  procurarComGPS() {
    if (!isPlatformBrowser(this.platformId)) return;
    this.isPesquisandoGps.set(true);

    if (this.latitudeCache !== null && this.longitudeCache !== null) {
      this.centralizarMapaNoUtilizador(this.latitudeCache, this.longitudeCache);
      this.carregarMercadosPorRaioGps(this.latitudeCache, this.longitudeCache, true);
      return;
    }

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (posicao) => {
          this.latitudeCache = posicao.coords.latitude;
          this.longitudeCache = posicao.coords.longitude;
          this.centralizarMapaNoUtilizador(this.latitudeCache, this.longitudeCache);
          this.carregarMercadosPorRaioGps(this.latitudeCache, this.longitudeCache, true);
        },
        (erro) => {
          console.error('❌ Erro ou recusa de acesso ao GPS:', erro);
          this.toastService.show('Ative a localização no navegador para ver feiras perto de si.', 'info', 'Geolocalização');
          this.isPesquisandoGps.set(false);
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: Infinity
        }
      );
    } else {
      this.toastService.show('O seu navegador não suporta geolocalização.', 'error', 'Erro de Hardware');
      this.isPesquisandoGps.set(false);
    }
  }

  adicionarMarcadoresNoMapa(mercadosDaApi: any[]) {
    if (!this.mapa) return;
    this.mapa.eachLayer((layer) => { if (layer instanceof L.Marker) this.mapa?.removeLayer(layer); });

    const iconeTenda = L.divIcon({
      html: '<div class="text-3xl drop-shadow-md">🎪</div>',
      className: 'bg-transparent border-0',
      iconSize: [30, 30],
      iconAnchor: [15, 30]
    });

    mercadosDaApi.forEach(m => {
      if (m.latitude && m.longitude) {
        L.marker([m.latitude, m.longitude], { icon: iconeTenda })
          .addTo(this.mapa!)
          .on('click', () => this.abrirDetalhes(m));
      }
    });
  }

  abrirDetalhes(mercado: any) {
    this.mercadoDetalhes.set(mercado);
    this.isDetalhesAberto.set(true);
  }

  fecharDetalhes() {
    this.isDetalhesAberto.set(false);
    this.mercadoDetalhes.set(null);
  }

  abrirInscricao(mercado: any) {
    this.mercadoSelecionado.set(mercado);
    this.isModalAberto.set(true);
  }

  fecharModal() {
    this.isModalAberto.set(false);
    this.mercadoSelecionado.set(null);
  }

  handleSucessoCandidatura() {
    this.fecharModal();
    this.carregarMercadosProximos(true);
    this.toastService.show('Candidatura submetida com sucesso! Aguarde o parecer técnico da autarquia.', 'success', 'Inscrição Efetuada');
  }

  private carregarMercadosPorRaioGps(lat: number, lng: number, resetarLista = false) {
    if (resetarLista) {
      this.paginaAtual.set(0);
    }
    const url = `http://localhost:8080/api/mercados/proximos?lat=${lat}&lng=${lng}&raio=${this.raioKm()}&page=${this.paginaAtual()}&size=5`;

    this.http.get<any>(url).subscribe({
      next: (respostaPaged: any) => {
        const novosMercados = respostaPaged.content || respostaPaged;
        const ultimo = respostaPaged.page
          ? (respostaPaged.page.number + 1) >= respostaPaged.page.totalPages
          : (respostaPaged.last !== undefined ? respostaPaged.last : true);

        if (!novosMercados || novosMercados.length === 0) {
          this.mercados.set([]);

          // 2. Limpa os marcadores (tendas 🎪) antigos do mapa Leaflet
          this.adicionarMarcadoresNoMapa([]);

          // 3. Desliga os controlos de paginação e carregamento
          this.isUltimaPagina.set(true);
          this.isPesquisandoGps.set(false);

          // 4. Lança um aviso explícito e honesto ao utilizador
          this.toastService.show(`Não encontrámos feiras num raio de ${this.raioKm()}km da sua posição.`, 'info', 'Sem Cobertura');
        } else {
          if (resetarLista) {
            this.mercados.set(novosMercados);
          } else {
            this.mercados.update(listaAntiga => [...listaAntiga, ...novosMercados]);
          }
          this.adicionarMarcadoresNoMapa(this.mercados());
          this.isUltimaPagina.set(ultimo);
          this.isPesquisandoGps.set(false);
        }
      },
      error: (err) => {
        console.error('Erro na rota de GPS:', err);
        this.isPesquisandoGps.set(false);
      }
    });
  }

  alterarRaio(event: any) {
    const novoRaio = Number(event.target.value);
    this.raioKm.set(novoRaio);

    // 🐾 UX EM TEMPO REAL: Se o utilizador já tiver as coordenadas GPS ativas,
    // redesenha o círculo verde e recarrega os mercados do servidor imediatamente!
    if (this.latitudeCache !== null && this.longitudeCache !== null) {
      this.centralizarMapaNoUtilizador(this.latitudeCache, this.longitudeCache);
      this.carregarMercadosPorRaioGps(this.latitudeCache, this.longitudeCache, true); // true faz o reset da lista
    }
  }

  private centralizarMapaNoUtilizador(lat: number, lng: number) {
    if (!this.mapa) return;

    if (this.marcadorCentroUtilizador) this.mapa.removeLayer(this.marcadorCentroUtilizador);
    if (this.camadaCirculoRaio) this.mapa.removeLayer(this.camadaCirculoRaio);

    let zoomDinamico = 11;
    if (this.raioKm() > 60) zoomDinamico = 8;
    else if (this.raioKm() > 35) zoomDinamico = 9;
    else if (this.raioKm() > 15) zoomDinamico = 10;

    this.mapa.setView([lat, lng], zoomDinamico);

    this.camadaCirculoRaio = L.circle([lat, lng], {
      radius: this.raioKm() * 1000,
      color: '#10b981',
      weight: 2,
      dashArray: '6, 6',
      fillColor: '#10b981',
      fillOpacity: 0.12
    }).addTo(this.mapa);

    this.marcadorCentroUtilizador = L.circleMarker([lat, lng], {
      radius: 7,
      color: '#ffffff',
      weight: 2,
      fillColor: '#10b981',
      fillOpacity: 1
    }).bindPopup(`<b>A sua localização base</b><br>Pesquisando num raio de ${this.raioKm()} km.`)
      .addTo(this.mapa);
  }
}

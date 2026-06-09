import { Component, OnInit, signal, computed, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/services/auth.service';
import { InscricaoModalComponent } from '../../core/components/inscricao-modal/inscricao-modal.component';
import { MercadoDetalhesModalComponent } from '../../core/components/mercado-detalhes-modal/mercado-detalhes-modal.component';

import * as L from 'leaflet'; // O motor do mapa!

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
  private platformId = inject(PLATFORM_ID); // Para saber se o código está a correr no Browser
  private latitudeCache: number | null = null;
  private longitudeCache: number | null = null;
  private marcadorCentroUtilizador: L.CircleMarker | null = null;
  private camadaCirculoRaio: L.Circle | null = null;
  // 🎯 Estado Reativo
  mercados = signal<any[]>([]);
  raioKm = signal<number>(25); // Começa nos 25 km por defeito
  pesquisa = signal<string>('');
  mapa: L.Map | undefined;
  isPesquisandoGps = signal<boolean>(false);
  // 🎯 Gestão de Inscrições / Modal
  isModalAberto = signal<boolean>(false);
  mercadoSelecionado = signal<any>(null);
  inscricoesFeitas = signal<number[]>([]);
  isDetalhesAberto = signal<boolean>(false); // 🔍 NOVO CONTROLADOR
  mercadoDetalhes = signal<any>(null);
  // 🎯 O Mapa Leaflet
  userRole = computed(() => this.authService.currentUser()?.role || '');
  isFeirante = computed(() => this.userRole() === 'ROLE_FEIRANTE');
  isMunicipio = computed(() => this.userRole() === 'ROLE_MUNICIPO');

  // Dados fictícios para a barra lateral
  noticias = signal([
    { id: 1, titulo: 'Feira das Mercês regressa já este fim de semana', autoria: 'Câmara Municipal' },
    { id: 2, titulo: 'Novas vagas abertas para mercados sazonais de Verão', autoria: 'Junta de Freguesia' }
  ]);

  ngOnInit() {
    this.carregarMercadosProximos();

    if (this.isFeirante()) {
      this.carregarInscricoesExistentes();
    }

    // Só inicia o mapa se estivermos num Browser real (evita erros no Angular SSR)
    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => this.iniciarMapa(), 100);
    }
  }

  // ==========================================
  // 🗺️ MAGIA DO MAPA E GEOLOCALIZAÇÃO
  // ==========================================

  iniciarMapa() {
    // Inicia focado no centro de Portugal
    this.mapa = L.map('mapa-vitrine').setView([39.3999, -8.2245], 6);

    // Carrega o desenho do mapa (estradas, cidades, etc)
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

  carregarMercadosProximos() {
    this.http.get<any[]>('http://localhost:8080/api/mercados').subscribe({
      next: (dados) => {
        this.mercados.set(dados);
        this.adicionarMarcadoresNoMapa(dados);
      },
      error: (err) => console.error('Erro ao carregar montra de mercados', err)
    });
  }

  procurarComGPS() {
    if (!isPlatformBrowser(this.platformId)) return;
    this.isPesquisandoGps.set(true);
    if (this.latitudeCache !== null && this.longitudeCache !== null) {
      this.centralizarMapaNoUtilizador(this.latitudeCache, this.longitudeCache);
      this.carregarMercadosPorRaioGps(this.latitudeCache, this.longitudeCache);
      return;
    }

    // 🔒 PASSO 2: Se é a primeira vez, pedimos autorização formal ao browser
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (posicao) => {
          // Guardamos os dados na cache para as próximas jogadas do slider
          this.latitudeCache = posicao.coords.latitude;
          this.longitudeCache = posicao.coords.longitude;
          // Desenha o pino azul de onde o utilizador está no mapa Leaflet
          this.centralizarMapaNoUtilizador(this.latitudeCache, this.longitudeCache);
          // Faz a chamada ao backend mercados-service
          this.carregarMercadosPorRaioGps(this.latitudeCache, this.longitudeCache);
        },(erro) => {
          console.error('❌ Erro ou recusa de acesso ao GPS:', erro);
          alert('Para pesquisar feiras perto de si, por favor ative a autorização de localização no seu navegador.');
          this.isPesquisandoGps.set(false);
        },{
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: Infinity // 🎯 Permite ao browser reutilizar a última posição conhecida do SO
        }
      );
    } else {
      alert('O seu navegador não suporta geolocalização por GPS.');
      this.isPesquisandoGps.set(false);
    }
  }

  adicionarMarcadoresNoMapa(mercadosDaApi: any[]) {
    if (!this.mapa) return;
    // Limpa os pinos antigos
    this.mapa.eachLayer((layer) => { if (layer instanceof L.Marker) this.mapa?.removeLayer(layer); });

    // Ícone 🎪 personalizado (Emoji)
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
          // 🎯 NOVO FLUXO UX: Clicar no pino do mapa abre diretamente a Ficha de Detalhes Completa!
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
  // ==========================================
  // 📝 MAGIA DAS INSCRIÇÕES E MODAL
  // ==========================================

  carregarInscricoesExistentes() {
    const emailFeirante = this.authService.currentUser()?.email;
    if (!emailFeirante) return;

    this.http.get<number[]>(`http://localhost:8080/api/candidaturas/inscritas?email=${emailFeirante}`)
      .subscribe({
        next: (ids) => this.inscricoesFeitas.set(ids),
        error: (err) => console.error('Erro ao recuperar histórico de inscrições', err)
      });
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
    this.carregarInscricoesExistentes();
    this.carregarMercadosProximos();
    alert('Candidatura efetuada com sucesso! A autarquia irá agora avaliar o seu processo.');
  }

  private carregarMercadosPorRaioGps(lat: number, lng: number) {
    const url = `http://localhost:8080/api/mercados/proximos?lat=${lat}&lng=${lng}&raio=${this.raioKm()}`;

    this.http.get<any[]>(url).subscribe({
      next: (dados) => {
        if (!dados || dados.length === 0) {
          alert(`Nenhuma feira encontrada a ${this.raioKm()}km da sua posição real. A carregar todos os mercados gerais para efeitos de demonstração!`);

          // Fallback automático: Carrega os mercados normais aprovados
          this.http.get<any[]>('http://localhost:8080/api/mercados').subscribe(todos => {
            this.mercados.set(todos);
            this.adicionarMarcadoresNoMapa(todos);
            this.isPesquisandoGps.set(false);
          });
        } else {
          this.mercados.set(dados);
          this.adicionarMarcadoresNoMapa(dados);
          this.isPesquisandoGps.set(false);
        }
      },
      error: (err) => {
        console.error('Erro na rota de GPS:', err);
        this.http.get<any[]>('http://localhost:8080/api/mercados').subscribe({
          next: (todos) => {
            this.mercados.set(todos);
            this.adicionarMarcadoresNoMapa(todos);
            this.isPesquisandoGps.set(false); // 🎯 🔓 Liberta o botão
          },
          error: () => this.isPesquisandoGps.set(false)
        });
      }
    });
  }

  /**
   * 🗺️ Desenha ou move o indicador visual do utilizador no mapa
   */
  private centralizarMapaNoUtilizador(lat: number, lng: number) {
    if (!this.mapa) return;

    // 🧹 PASSO 1: Limpar os desenhos anteriores do mapa para não acumular lixo visual
    if (this.marcadorCentroUtilizador) this.mapa.removeLayer(this.marcadorCentroUtilizador);
    if (this.camadaCirculoRaio) this.mapa.removeLayer(this.camadaCirculoRaio);

    // Ajusta o zoom do mapa automaticamente baseado no tamanho do raio
    let zoomDinamico = 11;
    if (this.raioKm() > 60) zoomDinamico = 8;
    else if (this.raioKm() > 35) zoomDinamico = 9;
    else if (this.raioKm() > 15) zoomDinamico = 10;

    this.mapa.setView([lat, lng], zoomDinamico);

    // 🟢 2. CÍRCULO GRANDE DE COBERTURA (Raio em metros)
    // Multiplicamos por 1000 porque o Leaflet pede o raio estrutural em metros
    this.camadaCirculoRaio = L.circle([lat, lng], {
      radius: this.raioKm() * 1000,
      color: '#10b981',       // Linha de contorno Verde Esmeralda (Tailwind emerald-500)
      weight: 2,              // Espessura da linha
      dashArray: '6, 6',      // Linha tracejada moderna
      fillColor: '#10b981',   // Preenchimento verde
      fillOpacity: 0.12       // Opacidade muito suave para não tapar as estradas do mapa
    }).addTo(this.mapa);

    // 🟢 3. PONTO CENTRAL FIXO (Onde o utilizador está)
    this.marcadorCentroUtilizador = L.circleMarker([lat, lng], {
      radius: 7,
      color: '#ffffff',       // Borda branca pura para dar contraste
      weight: 2,
      fillColor: '#10b981',   // Centro preenchido a verde esmeralda firme
      fillOpacity: 1
    }).bindPopup(`<b>A sua localização base</b><br>Pesquisando num raio de ${this.raioKm()} km.`)
      .addTo(this.mapa);
  }
}

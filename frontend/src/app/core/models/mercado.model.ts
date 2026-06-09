export class Mercado {
  id!: number;
  nome!: string;
  localizacao!: string;
  vagas!: number;
  dataInicio!: string;
  dataFim!: string;
  estado!: string;

  // 🗺️ Coordenadas Geográficas (Leaflet/Photon)
  latitude!: number;
  longitude!: number;
  distancia?: number; // Campo transient calculado pela fórmula de Haversine no Java

  // 🪙 Configurações Regulamentares e Taxas
  tipoPreco!: 'EVENTO' | 'DIARIO';
  aceitaStreetFood!: boolean;
  disponibilizaStandsOrganizacao!: boolean;
  precoArtesanatoStandProprio!: number;
  precoArtesanatoStandOrganizacao!: number;
  precoStreetFoodStandProprio!: number;
  documentosExigidos!: string[];

  // ✨ NOVOS CAMPOS: Ficha de Interesse Público e Lazer (Câmara de Loures)
  descricao?: string;
  petFriendly!: boolean;
  temWc!: boolean;
  imagemCartaz?: string;

  // O construtor recebe um "Partial", permitindo injetar o JSON da API instantaneamente
  constructor(init?: Partial<Mercado>) {
    Object.assign(this, init);

    // Fallbacks de segurança para garantir valores booleanos caso venham nulos do banco
    if (this.petFriendly === undefined) this.petFriendly = true;
    if (this.temWc === undefined) this.temWc = true;
  }

  // 💡 Métodos Utilitários da Classe
  get estaEsgotado(): boolean {
    return this.vagas === 0;
  }

  get dataFormatada(): string {
    return `${this.dataInicio} até ${this.dataFim}`;
  }
}

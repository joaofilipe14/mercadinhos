export class Mercado {
  id!: number;
  nome!: string;
  localizacao!: string;
  vagas!: number;
  dataInicio!: string;
  dataFim!: string;
  estado!: string;
  latitude!: number;
  longitude!: number;
  distancia?: number;
  tipoPreco!: 'EVENTO' | 'DIARIO';
  aceitaStreetFood!: boolean;
  disponibilizaStandsOrganizacao!: boolean;
  precoArtesanatoStandProprio!: number;
  precoArtesanatoStandOrganizacao!: number;
  precoStreetFoodStandProprio!: number;
  documentosExigidos!: string[];
  descricao?: string;
  petFriendly!: boolean;
  aceitaCandidaturas!: boolean;
  temWc!: boolean;
  imagemCartaz?: string;

  constructor(init?: Partial<Mercado>) {
    Object.assign(this, init);
    if (this.petFriendly === undefined) this.petFriendly = true;
    if (this.temWc === undefined) this.temWc = true;
  }

  get estaEsgotado(): boolean {
    return this.vagas === 0;
  }

  get dataFormatada(): string {
    return `${this.dataInicio} até ${this.dataFim}`;
  }
}

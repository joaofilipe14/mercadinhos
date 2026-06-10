import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, switchMap, filter } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-formulario-mercado',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './formulario-mercado.component.html'
})
export class FormularioMercadoComponent implements OnInit {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private programmaticChange = false;

  isEdicao = signal<boolean>(false);
  mercadoId = signal<number | null>(null);
  mensagemSucesso = signal<string | null>(null);
  sugestoesMorada = signal<any[]>([]);
  mostrandoSugestoes = signal<boolean>(false);

  documentosDisponiveis = [
    { value: 'INICIO_ACTIVIDADE', label: 'Declaração de Início de Atividade' },
    { value: 'NAO_DIVIDA_AT', label: 'Não Dívida à Autoridade Tributária' },
    { value: 'REGISTO_CRIMINAL', label: 'Registo Criminal' }
  ];

  mercadoForm = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    localizacao: ['', Validators.required],
    vagas: [50, [Validators.required, Validators.min(1)]],
    dataInicio: ['', Validators.required],
    dataFim: ['', Validators.required],
    estado: ['APROVADO', Validators.required],
    documentosExigidos: [[] as string[]],
    latitude: [38.7223, Validators.required],
    longitude: [-9.1449, Validators.required],

    tipoPreco: ['EVENTO', Validators.required],
    aceitaStreetFood: [true],
    disponibilizaStandsOrganizacao: [true],

    precoArtesanatoStandProprio: [35, [Validators.required, Validators.min(0)]],
    precoArtesanatoStandOrganizacao: [100, [Validators.required, Validators.min(0)]],
    precoStreetFoodStandProprio: [180, [Validators.required, Validators.min(0)]],
    descricao: ['', Validators.required],
    petFriendly: [true],
    temWc: [true],
    imagemCartaz: ['']
  });

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEdicao.set(true);
      this.mercadoId.set(Number(idParam));
      this.carregarDadosMercado(Number(idParam));
    }

    this.mercadoForm.get('disponibilizaStandsOrganizacao')?.valueChanges.subscribe(disponibiliza => {
      const controloStandOrg = this.mercadoForm.get('precoArtesanatoStandOrganizacao');
      if (disponibiliza) {
        controloStandOrg?.enable();
      } else {
        controloStandOrg?.setValue(0);
        controloStandOrg?.disable();
      }
    });

    this.mercadoForm.get('aceitaStreetFood')?.valueChanges.subscribe(aceita => {
      const controloStreetFood = this.mercadoForm.get('precoStreetFoodStandProprio');
      if (aceita) {
        controloStreetFood?.enable();
      } else {
        controloStreetFood?.setValue(0);
        controloStreetFood?.disable();
      }
    });

    this.mercadoForm.get('localizacao')?.valueChanges.pipe(
      filter(() => !this.programmaticChange),
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(termo => {
        if (!termo || termo.length < 3 || typeof termo !== 'string') {
          this.sugestoesMorada.set([]);
          this.mostrandoSugestoes.set(false);
          return of({ features: [] });
        }
        const url = `https://photon.komoot.io/api/?q=${encodeURIComponent(termo)}&limit=5&lat=39.5&lon=-8.0`;
        return this.http.get<any>(url);
      })
    ).subscribe(resposta => {
      if (resposta && resposta.features) {
        this.sugestoesMorada.set(resposta.features);
        this.mostrandoSugestoes.set(true);
      }
    });
  }

  obterSufixoPreco(): string {
    const tipo = this.mercadoForm.get('tipoPreco')?.value;
    return tipo === 'DIARIO' ? '(p/ Dia)' : '(Evento Completo)';
  }

  selecionarMorada(local: any) {
    this.programmaticChange = true;
    this.mercadoForm.patchValue({
      localizacao: local.properties.name,
      latitude: local.geometry.coordinates[1],
      longitude: local.geometry.coordinates[0]
    });
    this.programmaticChange = false;
    this.sugestoesMorada.set([]);
    this.mostrandoSugestoes.set(false);
  }

  carregarDadosMercado(id: number) {
    this.http.get<any>(`http://localhost:8080/api/mercados/${id}`).subscribe({
      next: (mercado) => {
        if (mercado.dataInicio) mercado.dataInicio = mercado.dataInicio.split('T')[0];
        if (mercado.dataFim) mercado.dataFim = mercado.dataFim.split('T')[0];
        this.programmaticChange = true;
        this.mercadoForm.patchValue(mercado);
        this.programmaticChange = false;
      },
      error: (err) => console.error('Erro ao carregar mercado', err)
    });
  }

  onCheckboxChange(event: any) {
    const documentosSelecionados = this.mercadoForm.get('documentosExigidos')?.value || [];
    if (event.target.checked) {
      documentosSelecionados.push(event.target.value);
    } else {
      const index = documentosSelecionados.indexOf(event.target.value);
      if (index > -1) documentosSelecionados.splice(index, 1);
    }
    this.mercadoForm.patchValue({ documentosExigidos: documentosSelecionados });
  }

  isDocumentoSelecionado(docValue: string): boolean {
    const documentos = this.mercadoForm.get('documentosExigidos')?.value || [];
    return documentos.includes(docValue);
  }

  voltar() {
    this.router.navigate(['/mercados']);
  }

  onSubmit() {
    if (this.mercadoForm.invalid) return;

    const payload = this.mercadoForm.getRawValue();

    if (this.isEdicao()) {
      this.http.put(`http://localhost:8080/api/mercados/${this.mercadoId()}`, payload).subscribe({
        next: () => {
          this.mensagemSucesso.set('Regulamento técnico e taxas atualizados com sucesso!');
          setTimeout(() => this.voltar(), 1500);
        },
        error: (err) => alert('Erro ao atualizar dados no servidor.')
      });
    } else {
      this.http.post('http://localhost:8080/api/mercados', payload).subscribe({
        next: () => {
          this.mensagemSucesso.set('Novo mercado e taxas regulamentares publicados com sucesso!');
          this.mercadoForm.reset({
            vagas: 50, estado: 'APROVADO', latitude: 38.7223, longitude: -9.1449,
            tipoPreco: 'EVENTO', aceitaStreetFood: true, disponibilizaStandsOrganizacao: true,
            precoArtesanatoStandProprio: 35, precoArtesanatoStandOrganizacao: 100, precoStreetFoodStandProprio: 180
          });
          setTimeout(() => this.mensagemSucesso.set(null), 3000);
        },
        error: (err) => alert('Erro ao criar mercado no servidor.')
      });
    }
  }
}

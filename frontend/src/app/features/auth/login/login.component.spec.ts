import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest'; // Import obrigatório para o Vitest

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  // Variáveis para os Mocks do Vitest
  let authServiceMock: any;
  let routerMock: any;

  beforeEach(async () => {
    // 1. Criar os Mocks compatíveis com Vitest
    authServiceMock = {
      login: vi.fn()
    };

    routerMock = {
      navigate: vi.fn()
    };

    // 2. Configurar o módulo
    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve inicializar o formulário com o email mockado da câmara', () => {
    const emailControl = component.loginForm.get('username');
    expect(emailControl?.value).toBe('camara@test.com');
    expect(emailControl?.valid).toBe(true); // Vitest usa toBe() com valor booleano
  });

  it('não deve chamar o serviço de login se o formulário for inválido', () => {
    component.loginForm.get('username')?.setValue('');
    component.onSubmit();

    expect(authServiceMock.login).not.toHaveBeenCalled();
  });

  it('deve navegar para /mercados/criar se o login da câmara tiver sucesso', () => {
    const mockResponse = { token: 'jwt123', username: 'camara@test.com', role: 'ROLE_MUNICIPO' };

    // Sintaxe do Vitest para devolver valores mockados num Observable
    authServiceMock.login.mockReturnValue(of(mockResponse));

    component.onSubmit();

    expect(authServiceMock.login).toHaveBeenCalledWith('camara@test.com', 'password');
    expect(routerMock.navigate).toHaveBeenCalledWith(['/mercados/criar']);
    expect(component.isLoading()).toBe(false);
  });

  it('deve mostrar mensagem de erro se as credenciais forem inválidas', () => {
    authServiceMock.login.mockReturnValue(throwError(() => new Error('Unauthorized')));

    component.onSubmit();

    expect(component.errorMessage()).toBe('Credenciais inválidas. Tente novamente.');
    expect(component.isLoading()).toBe(false);
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });
});

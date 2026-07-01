-- =================================================================
-- 1. 🎪 Povoamento do Calendário de Feiras (2º Semestre de 2026)
-- =================================================================
INSERT INTO mercados (
    id, nome, localizacao, latitude, longitude, vagas,
    data_inicio, data_fim, aceita_candidaturas, estado,
    criado_por, pet_friendly, tem_wc, imagem_cartaz,
    tipo_preco, aceita_street_food, disponibiliza_stands_organizacao,
    preco_artesanato_stand_proprio, preco_artesanato_stand_organizacao,
    preco_street_food_stand_proprio
) VALUES
      (1, 'Feira da Ladra - Edição de Verão', 'Campo de Santa Clara, Lisboa', 38.7145, -9.1248, 45, '2026-07-07', '2026-07-08', true, 1, 'geral@cm-lisboa.pt', true, true, NULL, 'EVENTO', true, false, 40.0, NULL, 120.0),
      (2, 'Mercado de Santana - Julho', 'Recinto de Santana, Leiria', 39.7436, -8.8071, 30, '2026-07-12', '2026-07-12', true, 1, 'contacto@cm-leiria.pt', true, true, 'http://localhost:8080/cartazes-bucket/cartaz_mercado_2.png', 'DIARIO', true, true, 15.0, 35.0, 50.0),
      (3, 'Feira de Artesanato de Braga', 'Avenida Central, Braga', 41.5514, -8.4234, 0, '2026-07-15', '2026-07-20', false, 1, 'geral@cm-braga.pt', true, true, NULL, 'EVENTO', false, true, 50.0, 110.0, NULL),
      (4, 'Feira Noturna de Alvalade', 'Lisboa Centro', 38.7523, -9.1432, 20, '2026-08-01', '2026-08-05', true, 1, 'geral@cm-lisboa.pt', true, true, NULL, 'DIARIO', true, true, 25.0, 60.0, 90.0),
      (5, 'Grande Feira de Agosto', 'Mercado Municipal de Leiria', 39.7450, -8.8030, 60, '2026-08-14', '2026-08-18', true, 1, 'contacto@cm-leiria.pt', false, true, NULL, 'EVENTO', false, true, 80.0, 180.0, NULL),
      (6, 'Feira dos Sabores Tradicionais do Porto', 'Praça da Cordoaria, Porto', 41.1456, -8.6167, 15, '2026-08-20', '2026-08-25', false, 1, 'eventos@cm-porto.pt', true, true, NULL, 'EVENTO', true, false, 70.0, NULL, 220.0),
      (7, 'Mercado de Velharias de Sintra', 'Estefânia, Sintra', 38.8028, -9.3784, 25, '2026-09-05', '2026-09-06', true, 1, 'urbanismo@cm-sintra.pt', true, true, NULL, 'DIARIO', false, false, 20.0, NULL, NULL),
      (8, 'Feira Franca de Leiria', 'Parque do Avião, Leiria', 39.7489, -8.8115, 80, '2026-09-11', '2026-09-14', true, 1, 'contacto@cm-leiria.pt', true, true, NULL, 'EVENTO', true, false, 65.0, NULL, 175.0),
      (9, 'Feira de São Miguel', 'Braga Retail Center', 41.5620, -8.4012, 40, '2026-09-26', '2026-09-29', false, 1, 'geral@cm-braga.pt', true, true, NULL, 'DIARIO', true, true, 30.0, 70.0, 110.0),
      (10, 'Feira das Mercês 2026 - Edição Histórica', 'Recinto das Mercês, Sintra', 38.7995, -9.3450, 100, '2026-10-10', '2026-10-18', true, 1, 'urbanismo@cm-sintra.pt', true, true, NULL, 'EVENTO', true, true, 120.0, 300.0, 450.0),
      (11, 'Mercado da Ribeira - Outono Antigo', 'Cais do Sodré, Lisboa', 38.7067, -9.1458, 12, '2026-10-22', '2026-10-24', true, 1, 'geral@cm-lisboa.pt', true, true, NULL, 'DIARIO', true, false, 45.0, NULL, 130.0),
      (12, 'Feira de São Martinho e dos Castanheiros', 'Centro Histórico, Leiria', 39.7412, -8.8092, 35, '2026-11-09', '2026-11-12', true, 1, 'contacto@cm-leiria.pt', true, false, NULL, 'DIARIO', true, true, 18.0, 45.0, 75.0),
      (13, 'Feira do Livro Antigo e Colecionismo', 'Ribeira, Porto', 41.1406, -8.6111, 50, '2026-11-15', '2026-11-20', false, 1, 'eventos@cm-porto.pt', true, true, NULL, 'EVENTO', false, false, 35.0, NULL, NULL),
      (14, 'Wonderland Lisboa - Setor Comercial', 'Parque Eduardo VII, Lisboa', 38.7283, -9.1527, 150, '2026-12-01', '2026-12-23', true, 1, 'geral@cm-lisboa.pt', true, false, NULL, 'EVENTO', true, true, 250.0, 600.0, 900.0),
      (15, 'Leiria Cidade Natal - Mercadinho', 'Praça Rodrigues Lobo, Leiria', 39.7445, -8.8065, 40, '2026-12-05', '2026-12-24', true, 1, 'contacto@cm-leiria.pt', true, true, NULL, 'EVENTO', true, true, 150.0, 350.0, 500.0),
      (16, 'Mercado de Natal de Sintra', 'Terreiro da Rainha, Sintra', 38.7960, -9.3908, 30, '2026-12-06', '2026-12-22', true, 1, 'urbanismo@cm-sintra.pt', true, true, NULL, 'EVENTO', true, true, 90.0, 210.0, 320.0)
    ON CONFLICT (id) DO NOTHING;

-- =================================================================
-- 2. 📂 Povoamento dos Documentos Obrigatórios (Tabela Associativa)
-- =================================================================
DELETE FROM mercado_documentos_exigidos;

INSERT INTO mercado_documentos_exigidos (mercado_id, tipo_documento) VALUES
-- Requisitos Básicos (Apenas Início de Atividade)
(1, 'INICIO_ACTIVIDADE'),
(7, 'INICIO_ACTIVIDADE'),
(11, 'INICIO_ACTIVIDADE'),

-- 🎯 MERCADO DE SANTANA (Alvo Principal)
(2, 'INICIO_ACTIVIDADE'),
(2, 'NAO_DIVIDA_AT'),

-- Mix Regional de Braga e Porto (Seguro de Trabalho e Atividade)
(3, 'INICIO_ACTIVIDADE'),
(3, 'SEGURO_ACIDENTES'),
(6, 'INICIO_ACTIVIDADE'),
(6, 'SEGURO_ACIDENTES'),
(13, 'INICIO_ACTIVIDADE'),

-- Concelho de Leiria - Mix Variado para os restantes mercados
(5, 'INICIO_ACTIVIDADE'),
(5, 'NAO_DIVIDA_AT'),
(5, 'CARTAO_FEIRANTE'), -- Exige cadastro nacional ativo
(8, 'INICIO_ACTIVIDADE'),
(8, 'NAO_DIVIDA_SS'), -- Validação de Segurança Social
(12, 'INICIO_ACTIVIDADE'),
(12, 'SEGURO_ACIDENTES'),
(15, 'INICIO_ACTIVIDADE'),
(15, 'NAO_DIVIDA_AT'),
(15, 'CARTAO_FEIRANTE'),

-- Grandes Certames de Elevada Exigência (Wonderland Lisboa & Feira das Mercês)
(10, 'INICIO_ACTIVIDADE'),
(10, 'NAO_DIVIDA_AT'),
(10, 'NAO_DIVIDA_SS'),
(14, 'INICIO_ACTIVIDADE'),
(14, 'NAO_DIVIDA_AT'),
(14, 'NAO_DIVIDA_SS'),
(14, 'SEGURO_ACIDENTES'),
(14, 'CARTAO_FEIRANTE'), -- Wonderland exige blindagem documental a 100%

-- Sintra Natal e Alvalade Mix
(4, 'INICIO_ACTIVIDADE'),
(4, 'NAO_DIVIDA_SS'),
(9, 'INICIO_ACTIVIDADE'),
(16, 'INICIO_ACTIVIDADE'),
(16, 'NAO_DIVIDA_AT');

-- Sincroniza o indexador autonumérico da tabela mercados
SELECT setval(pg_get_serial_sequence('mercados', 'id'), COALESCE(MAX(id), 1)) FROM mercados;
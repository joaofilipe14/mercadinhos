-- =================================================================
-- 🎪 Povoamento do Calendário de Feiras para o 2º Semestre de 2026
-- =================================================================
INSERT INTO mercados (id, nome, localizacao, latitude, longitude, vagas, data_inicio, data_fim, aceita_candidaturas, estado, criado_por, pet_friendly, tem_wc) VALUES

-- 📍 JULHO 2026
(1 , 'Feira da Ladra - Edição de Verão', 'Campo de Santa Clara, Lisboa', 38.7145, -9.1248, 45, '2026-07-07', '2026-07-08', true, 1, 'geral@cm-lisboa.pt', true, true),
(2, 'Mercado de Santana - Julho', 'Recinto de Santana, Leiria', 39.7436, -8.8071, 30, '2026-07-12', '2026-07-12', true, 1, 'contacto@cm-leiria.pt', true, true),
(3, 'Feira de Artesanato de Braga', 'Avenida Central, Braga', 41.5514, -8.4234, 0, '2026-07-15', '2026-07-20', false, 1, 'geral@cm-braga.pt', true, true),

-- 📍 AGOSTO 2026
(4, 'Feira Noturna de Alvalade', 'Lisboa Centro', 38.7523, -9.1432, 20, '2026-08-01', '2026-08-05', true, 1, 'geral@cm-lisboa.pt', true, true),
(5, 'Grande Feira de Agosto', 'Mercado Municipal de Leiria', 39.7450, -8.8030, 60, '2026-08-14', '2026-08-18', true, 1, 'contacto@cm-leiria.pt', true, true),
(6, 'Feira dos Sabores Tradicionais do Porto', 'Praça da Cordoaria, Porto', 41.1456, -8.6167, 15, '2026-08-20', '2026-08-25', false, 1, 'eventos@cm-porto.pt', true, true),

-- 📍 SETEMBRO 2026
(7, 'Mercado de Velharias de Sintra', 'Estefânia, Sintra', 38.8028, -9.3784, 25, '2026-09-05', '2026-09-06', true, 1, 'urbanismo@cm-sintra.pt', true, true),
(8, 'Feira Franca de Leiria', 'Parque do Avião, Leiria', 39.7489, -8.8115, 80, '2026-09-11', '2026-09-14', true, 1, 'contacto@cm-leiria.pt', true, true),
(9, 'Feira de São Miguel', 'Braga Retail Center', 41.5620, -8.4012, 40, '2026-09-26', '2026-09-29', false, 1, 'geral@cm-braga.pt', true, true),

-- 📍 OUTUBRO 2026
(10, 'Feira das Mercês 2026 - Edição Histórica', 'Recinto das Mercês, Sintra', 38.7995, -9.3450, 100, '2026-10-10', '2026-10-18', true, 1, 'urbanismo@cm-sintra.pt', true, true),
(11, 'Mercado da Ribeira - Outono Antigo', 'Cais do Sodré, Lisboa', 38.7067, -9.1458, 12, '2026-10-22', '2026-10-24', true, 1, 'geral@cm-lisboa.pt', true, true),

-- 📍 NOVEMBRO 2026
(12, 'Feira de São Martinho e dos Castanheiros', 'Centro Histórico, Leiria', 39.7412, -8.8092, 35, '2026-11-09', '2026-11-12', true, 1, 'contacto@cm-leiria.pt', true, true),
(13, 'Feira do Livro Antigo e Colecionismo', 'Ribeira, Porto', 41.1406, -8.6111, 50, '2026-11-15', '2026-11-20', false, 1, 'eventos@cm-porto.pt', true, true),

-- 📍 DEZEMBRO 2026
(14, 'Wonderland Lisboa - Setor Comercial', 'Parque Eduardo VII, Lisboa', 38.7283, -9.1527, 150, '2026-12-01', '2026-12-23', true, 1, 'geral@cm-lisboa.pt', true, false),
(15, 'Leiria Cidade Natal - Mercadinho', 'Praça Rodrigues Lobo, Leiria', 39.7445, -8.8065, 40, '2026-12-05', '2026-12-24', true, 1, 'contacto@cm-leiria.pt', true, true),
(16, 'Mercado de Natal de Sintra', 'Terreiro da Rainha, Sintra', 38.7960, -9.3908, 30, '2026-12-06', '2026-12-22', true, 1, 'urbanismo@cm-sintra.pt', true, true)
    ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('mercados', 'id'), COALESCE(MAX(id), 1)) FROM mercados;
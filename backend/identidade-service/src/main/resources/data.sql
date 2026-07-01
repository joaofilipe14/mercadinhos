INSERT INTO utilizadores (id, email, password, role, ativo) VALUES
                       (1, 'geral@cm-lisboa.pt', '$2a$10$w7L3Ym43sUcH3Eo3Im90C.JqjUcLtcc9NnDrmS3TSG0533xfgSRzu', 'ROLE_MUNICIPIO', true),
                       (2, 'contacto@cm-leiria.pt', '$2a$10$w7L3Ym43sUcH3Eo3Im90C.JqjUcLtcc9NnDrmS3TSG0533xfgSRzu', 'ROLE_MUNICIPIO', true),
                       (3, 'urbanismo@cm-sintra.pt', '$2a$10$w7L3Ym43sUcH3Eo3Im90C.JqjUcLtcc9NnDrmS3TSG0533xfgSRzu', 'ROLE_MUNICIPIO', true),
                       (4, 'eventos@cm-porto.pt', '$2a$10$w7L3Ym43sUcH3Eo3Im90C.JqjUcLtcc9NnDrmS3TSG0533xfgSRzu', 'ROLE_MUNICIPIO', true),
                       (5, 'geral@cm-braga.pt', '$2a$10$w7L3Ym43sUcH3Eo3Im90C.JqjUcLtcc9NnDrmS3TSG0533xfgSRzu', 'ROLE_MUNICIPIO', true)
    ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('utilizadores', 'id'), COALESCE(MAX(id), 1)) FROM utilizadores;
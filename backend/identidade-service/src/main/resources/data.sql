INSERT INTO utilizadores (id, email, password, role) VALUES
                       (1, 'geral@cm-lisboa.pt', '$2a$10$EblZqNptyYvcLm/SwDChtozv6UrEILe8nTW76PAfLN1WZt66B6Sde', 'ROLE_MUNICIPO'),
                       (2, 'contacto@cm-leiria.pt', '$2a$10$EblZqNptyYvcLm/SwDChtozv6UrEILe8nTW76PAfLN1WZt66B6Sde', 'ROLE_MUNICIPO'),
                       (3, 'urbanismo@cm-sintra.pt', '$2a$10$EblZqNptyYvcLm/SwDChtozv6UrEILe8nTW76PAfLN1WZt66B6Sde', 'ROLE_MUNICIPO'),
                       (4, 'eventos@cm-porto.pt', '$2a$10$EblZqNptyYvcLm/SwDChtozv6UrEILe8nTW76PAfLN1WZt66B6Sde', 'ROLE_MUNICIPO'),
                       (5, 'geral@cm-braga.pt', '$2a$10$EblZqNptyYvcLm/SwDChtozv6UrEILe8nTW76PAfLN1WZt66B6Sde', 'ROLE_MUNICIPO')
    ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('utilizadores', 'id'), COALESCE(MAX(id), 1)) FROM utilizadores;
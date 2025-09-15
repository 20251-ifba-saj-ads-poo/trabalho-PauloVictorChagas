MERGE INTO Usuario (id, login, senha, email) KEY(login) VALUES (RANDOM_UUID(), 'admin', 'admin', 'admin@admin.com');

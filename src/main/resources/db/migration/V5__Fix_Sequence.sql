SELECT setval('users_id_seq', COALESCE((SELECT MAX(id)+1 FROM users), 1), false);

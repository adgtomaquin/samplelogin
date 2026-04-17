-- =============================================================================
--  SEED DATA
-- =============================================================================

-- Passwords: Admin@1234 and User@1234 (bcrypt strength 10)
INSERT INTO users (id, email, password_hash, name, avatar, department, status, enabled, joined, created_at, updated_at)
VALUES
    ('a0000000-0000-0000-0000-000000000001',
     'admin@example.com',
     '$2b$10$y8xijkPHgdkNfFUw0lcKWeIaTXFT12ajRveLIFs1kPEuY3fnc/1Cu',
     'Alex Rivera', 'AR', 'Engineering', 'active', TRUE,
     '2025-01-10 00:00:00+00', '2026-04-15 09:00:00+00', '2026-04-15 09:00:00+00'),

    ('a0000000-0000-0000-0000-000000000002',
     'user@example.com',
     '$2b$10$1i2OABYQYzMR71UBxIJFyOcnIASiN57qz8IuoFFl0fKgp9G77cVqy',
     'Sam Chen', 'SC', 'Marketing', 'active', TRUE,
     '2025-03-05 00:00:00+00', '2026-04-15 09:00:00+00', '2026-04-15 09:00:00+00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'USER'),
    ('a0000000-0000-0000-0000-000000000001', 'ADMIN'),
    ('a0000000-0000-0000-0000-000000000002', 'USER')
ON CONFLICT DO NOTHING;

-- Default user preferences
INSERT INTO user_preferences (user_id)
VALUES
    ('a0000000-0000-0000-0000-000000000001'),
    ('a0000000-0000-0000-0000-000000000002')
ON CONFLICT (user_id) DO NOTHING;

-- Default system settings singleton
INSERT INTO system_settings (id) VALUES (1)
ON CONFLICT (id) DO NOTHING;

-- Seed notifications (broadcast to all)
INSERT INTO notifications (id, type, title, message, read, user_id)
VALUES
    ('b0000000-0000-0000-0000-000000000001',
     'security',
     'New login from unknown device',
     'A login was detected from Chrome on Windows in Makati, PH.',
     FALSE, NULL),
    ('b0000000-0000-0000-0000-000000000002',
     'system',
     'Scheduled maintenance',
     'The system will undergo maintenance on April 20, 2026 from 2:00–4:00 AM UTC.',
     FALSE, NULL)
ON CONFLICT (id) DO NOTHING;

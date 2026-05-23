-- Seed data for local development
-- Password for all users: "password123" (bcrypt hashed)

INSERT INTO users (id, email, password_hash, display_name, role) VALUES
  ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'alice@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice Johnson', 'ADMIN'),
  ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'bob@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob Smith', 'MEMBER'),
  ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'carol@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Carol Williams', 'MEMBER'),
  ('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'dave@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Dave Brown', 'MEMBER')
ON CONFLICT (id) DO NOTHING;

INSERT INTO teams (id, name, description, created_by) VALUES
  ('e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'Engineering', 'Core engineering team', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
  ('f5eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'Design', 'Product design team', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33')
ON CONFLICT (id) DO NOTHING;

INSERT INTO team_members (team_id, user_id, role) VALUES
  ('e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'OWNER'),
  ('e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'MEMBER'),
  ('e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'd3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'MEMBER'),
  ('f5eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'OWNER'),
  ('f5eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'MEMBER')
ON CONFLICT (team_id, user_id) DO NOTHING;

INSERT INTO boards (id, name, description, team_id, created_by) VALUES
  ('aa11bc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'Product Roadmap', 'Q3 product planning board', 'e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
  ('bb22bc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'Sprint 42', 'Current sprint tasks', 'e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
  ('cc33bc99-9c0b-4ef8-bb6d-6bb9bd380a99', 'Design System', 'UI component library work', 'f5eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33')
ON CONFLICT (id) DO NOTHING;

INSERT INTO columns (id, board_id, name, position, color) VALUES
  -- Product Roadmap columns
  ('col1bc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'aa11bc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'Backlog', 0, '#94a3b8'),
  ('col2bc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'aa11bc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'In Progress', 1, '#f59e0b'),
  ('col3bc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'aa11bc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'Review', 2, '#6366f1'),
  ('col4bc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'aa11bc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'Done', 3, '#10b981'),
  -- Sprint 42 columns
  ('col5bc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'bb22bc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'To Do', 0, '#6366f1'),
  ('col6bc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'bb22bc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'In Progress', 1, '#f59e0b'),
  ('col7bc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'bb22bc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'Done', 2, '#10b981'),
  -- Design System columns
  ('col8bc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'cc33bc99-9c0b-4ef8-bb6d-6bb9bd380a99', 'To Do', 0, '#6366f1'),
  ('col9bc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'cc33bc99-9c0b-4ef8-bb6d-6bb9bd380a99', 'In Progress', 1, '#f59e0b'),
  ('col0bc99-9c0b-4ef8-bb6d-6bb9bd380a00', 'cc33bc99-9c0b-4ef8-bb6d-6bb9bd380a99', 'Done', 2, '#10b981')
ON CONFLICT (id) DO NOTHING;

INSERT INTO tasks (id, title, description, board_id, column_id, assignee_id, created_by, priority, position) VALUES
  -- Product Roadmap tasks
  ('tsk1bc99-9c0b-4ef8-bb6d-6bb9bd380b01', 'User authentication redesign', 'Improve the login and signup flow for better UX', 'aa11bc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'col2bc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'HIGH', 0),
  ('tsk2bc99-9c0b-4ef8-bb6d-6bb9bd380b02', 'Performance optimization pass', 'Reduce initial page load time by 40%', 'aa11bc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'col1bc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'd3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MEDIUM', 0),
  ('tsk3bc99-9c0b-4ef8-bb6d-6bb9bd380b03', 'Mobile responsive layouts', 'Ensure all pages work on mobile devices', 'aa11bc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'col3bc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'HIGH', 0),
  ('tsk4bc99-9c0b-4ef8-bb6d-6bb9bd380b04', 'Dark mode support', 'Add system-level dark mode detection', 'aa11bc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'col4bc99-9c0b-4ef8-bb6d-6bb9bd380a04', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'LOW', 0),
  -- Sprint 42 tasks
  ('tsk5bc99-9c0b-4ef8-bb6d-6bb9bd380b05', 'Fix null pointer in task service', 'NPE when assignee is null on task creation', 'bb22bc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'col6bc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'URGENT', 0),
  ('tsk6bc99-9c0b-4ef8-bb6d-6bb9bd380b06', 'Add rate limiting to API', 'Prevent abuse of public endpoints', 'bb22bc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'col5bc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'd3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MEDIUM', 0),
  ('tsk7bc99-9c0b-4ef8-bb6d-6bb9bd380b07', 'Write integration tests for boards', 'Cover all CRUD operations on boards endpoint', 'bb22bc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'col5bc99-9c0b-4ef8-bb6d-6bb9bd380a05', NULL, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'MEDIUM', 1),
  ('tsk8bc99-9c0b-4ef8-bb6d-6bb9bd380b08', 'Deploy to staging environment', 'Set up staging infrastructure and CI/CD', 'bb22bc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'col7bc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'HIGH', 0),
  -- Design System tasks
  ('tsk9bc99-9c0b-4ef8-bb6d-6bb9bd380b09', 'Button component variants', 'Design primary, secondary, ghost, and danger variants', 'cc33bc99-9c0b-4ef8-bb6d-6bb9bd380a99', 'col9bc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'HIGH', 0),
  ('tsk0bc99-9c0b-4ef8-bb6d-6bb9bd380b00', 'Typography scale documentation', 'Document font sizes and usage guidelines', 'cc33bc99-9c0b-4ef8-bb6d-6bb9bd380a99', 'col8bc99-9c0b-4ef8-bb6d-6bb9bd380a08', NULL, 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'LOW', 0)
ON CONFLICT (id) DO NOTHING;

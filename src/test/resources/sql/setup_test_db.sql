create role tombolo_test with password 'tombolo_test' LOGIN;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO tombolo_test;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO tombolo_test;

CREATE EXTENSION postgis;
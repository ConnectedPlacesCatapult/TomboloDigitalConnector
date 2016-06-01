create role tombolo_test with password 'tombolo_test' LOGIN;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO tombolo_test;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO tombolo_test;

insert into geography_type(label, name) values
('unknown','Unknown Geography Type'),
('lsoa', 'Lower Layer Super Output Area'),
('msoa', 'Middle Layer Super Output Area'),
('localAuthority', 'Local Authority'),
('sensor', 'Sensor'),
('poi', 'Point of interest');
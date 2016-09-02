drop table if exists timed_value;

drop table if exists subject;
drop sequence if exists subject_id_sequence;
drop table if exists subject_type;

drop table if exists attribute;
drop sequence if exists attribute_id_sequence;

drop table if exists provider;

-- Provider
create table provider (
	label	VARCHAR(63) NOT NULL,
	name	VARCHAR(255) NOT NULL,
	PRIMARY KEY(label)	
);

-- Subject Type
create table subject_type (
	label	VARCHAR(15) NOT NULL,
	name	VARCHAR(255),
	PRIMARY KEY(label)
);

-- Subject
create sequence subject_id_sequence;
create table subject (
	id                   integer NOT NULL DEFAULT nextval('subject_id_sequence'),
	subject_type_label VARCHAR(15) NOT NULL REFERENCES subject_type(label) DEFAULT 'unknown',
	label				 VARCHAR(63) NOT NULL UNIQUE,
	name				 VARCHAR(255),
	shape				 geometry,
	PRIMARY KEY(id)
);
create index subject_label on subject (label);

-- Attribute
create sequence attribute_id_sequence;
create table attribute (
	id 				integer NOT NULL DEFAULT nextval('attribute_id_sequence'),
	provider_label	VARCHAR(63) NOT NULL REFERENCES provider(label),
	label			VARCHAR(63) NOT NULL,
	name			VARCHAR(255) NOT NULL,
	description		VARCHAR(511),
	UNIQUE (provider_label, label),
	PRIMARY KEY(id)
);

-- Timed Value
create table timed_value (
	subject_id		integer NOT NULL REFERENCES subject(id),
	attribute_id 	integer NOT NULL REFERENCES attribute(id),
	timestamp		TIMESTAMP WITH TIME ZONE NOT NULL,
	value			DOUBLE PRECISION NOT NULL,
	PRIMARY KEY(subject_id,attribute_id,timestamp)
);

create table fixed_value (
	subject_id		integer NOT NULL REFERENCES subject(id),
	attribute_id	integer NOT NULL REFERENCES attribute(id),
	value			DOUBLE PRECISION NOT NULL,
	PRIMARY KEY(subject_id,attribute_id)
);

-- Database Journal
create sequence database_journal_id_sequence;
create table database_journal (
    id          integer NOT NULL DEFAULT nextval('database_journal_id_sequence'),
	class_name	VARCHAR(255) NOT NULL,
	key			VARCHAR(255) NOT NULL
);

-- Insert default subject types
insert into subject_type(label, name) values
('unknown','Unknown Subject Type'),
('sensor', 'Sensor'),
('poi', 'Point of interest');

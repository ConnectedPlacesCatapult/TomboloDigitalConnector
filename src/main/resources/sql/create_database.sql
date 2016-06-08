CREATE EXTENSION postgis;

drop table timed_value;

drop table subject;
drop sequence subject_id_sequence;
drop table subject_type;

drop table attribute;
drop sequence attribute_id_sequence;

drop table provider;


-- Provider
create table provider (
	label	VARCHAR(63) NOT NULL,
	name	VARCHAR(255) NOT NULL,
	PRIMARY KEY(label)	
);

-- Data Source
-- FIXME: Add data source table


-- Subject
create table subject_type (
	label	VARCHAR(15) NOT NULL,
	name	VARCHAR(255),
	PRIMARY KEY(label)
);

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

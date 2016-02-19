drop table area;
drop sequence area_id_sequence;
drop table area_type;

create table area_type (
	id 	integer NOT NULL,
	label	VARCHAR(15),
	name	VARCHAR(255),
	PRIMARY KEY(id)
);

create sequence area_id_sequence;
create table area (
	id				integer NOT NULL DEFAULT nextval('area_id_sequence'),
	area_type_id	integer NOT NULL REFERENCES area_type(id),
	label			VARCHAR(63),
	name			VARCHAR(255),
	shape			geometry,
	PRIMARY KEY(id)
);

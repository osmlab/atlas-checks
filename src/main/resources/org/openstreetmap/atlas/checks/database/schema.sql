DROP SCHEMA IF EXISTS {schema} CASCADE;
CREATE SCHEMA IF NOT EXISTS {schema};
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS hstore;


DROP TABLE IF EXISTS checks CASCADE;
CREATE TABLE checks (
	id serial primary key,
	name text,
	date_created timestamp,
	date_modified timestamp,
	active boolean default true
);

DROP TABLE IF EXISTS flag CASCADE;
CREATE TABLE flag (
	id serial primary key,
	flag_id text not null,
	check_name text not null,
	instructions text not null,
	run_id text,
	integrity_version varchar(8)
);

DROP TABLE IF EXISTS feature CASCADE;
CREATE TABLE feature (
  id serial primary key,
  flag_id bigint references flag(id),
  geom geometry not null,
  osm_id bigint,
  atlas_id bigint,
  iso_country_code text,
  tags hstore,
  date_created timestamp,
  date_modified timestamp,
  active boolean default true
);



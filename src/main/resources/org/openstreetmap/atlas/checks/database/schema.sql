CREATE SCHEMA IF NOT EXISTS {schema};
SET search_path TO {schema},public;

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS hstore;

CREATE TABLE IF NOT EXISTS flag (
	id serial primary key,
	flag_id text not null,
	check_name text not null,
	instructions text not null,
	run_uri text,
	software_version varchar(8),
	date_created timestamp
);

CREATE TABLE IF NOT EXISTS feature (
  id serial primary key,
  flag_id integer references flag(id),
  geom geometry not null,
  osm_id bigint not null,
  atlas_id bigint not null,
  iso_country_code text,
  item_type text not null,
  tags hstore,
  date_created timestamp
);

CREATE INDEX IF NOT EXISTS feature_geom_idx
  ON feature
  USING GIST (geom);

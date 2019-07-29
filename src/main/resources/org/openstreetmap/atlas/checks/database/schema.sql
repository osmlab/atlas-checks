DROP SCHEMA IF EXISTS {schema} CASCADE;
CREATE SCHEMA IF NOT EXISTS {schema};
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS hstore;

DROP TABLE IF EXISTS flag CASCADE;
CREATE TABLE flag (
	id serial primary key,
	flag_id text not null,
	check_name text not null,
	instructions text not null,
	run_uri text,
	integrity_version varchar(8),
	date_created timestamp
);

DROP TABLE IF EXISTS feature CASCADE;
CREATE TABLE feature (
  id serial primary key,
  flag_id text not null,
  geom geometry not null,
  osm_id bigint not null,
  atlas_id bigint not null,
  iso_country_code text,
  item_type text not null,
  tags hstore,
  date_created timestamp
);

DROP INDEX IF EXISTS feature_geom_idx;
CREATE INDEX feature_geom_idx
  ON feature
  USING GIST (geom);

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
	run_id text,
	integrity_version varchar(8)
);

DROP TABLE IF EXISTS feature CASCADE;
CREATE TABLE feature (
  id serial primary key,
  flag_id bigint,
  geom geometry not null,
  osm_id bigint,
  atlas_id bigint,
  iso_country_code text,
  tags hstore,
  date_created timestamp,
  date_modified timestamp,
  active boolean default true
);

DROP INDEX IF EXISTS feature_geom_idx;
CREATE INDEX feature_geom_idx
  ON feature
  USING GIST (geom);

-- Get all OSM features that exist in multiple checks
DROP MATERIALIZED VIEW IF EXISTS multiple_checks_osm_id;
CREATE MATERIALIZED VIEW multiple_checks_osm_id AS
SELECT osm_id, iso_country_code, array_agg(DISTINCT check_name) AS checks, cardinality(array_agg(DISTINCT check_name)) AS checks_count, feature.geom
FROM feature, flag
WHERE feature.flag_id = flag.id
GROUP BY osm_id, geom, iso_country_code
HAVING cardinality(array_agg(DISTINCT check_name)) > 1
ORDER BY cardinality(array_agg(DISTINCT check_name)) DESC;

-- Get all Atlas features that exist in multiple checks
DROP MATERIALIZED VIEW IF EXISTS multiple_checks_atlas_id;
CREATE MATERIALIZED VIEW multiple_checks_atlas_id AS
SELECT atlas_id, iso_country_code, array_agg(DISTINCT check_name) AS checks, cardinality(array_agg(DISTINCT check_name)) AS checks_count, feature.geom
FROM feature, flag
WHERE feature.flag_id = flag.id
GROUP BY atlas_id, geom, iso_country_code
HAVING cardinality(array_agg(DISTINCT check_name)) > 1
ORDER BY cardinality(array_agg(DISTINCT check_name)) DESC;




REFRESH MATERIALIZED VIEW multiple_checks_atlas_id;
REFRESH MATERIALIZED VIEW multiple_checks_osm_id;

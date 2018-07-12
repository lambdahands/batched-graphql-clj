CREATE TABLE IF NOT EXISTS people (
  id serial PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS friends (
  person_id integer REFERENCES people (id) NOT NULL,
  friend_id integer REFERENCES people (id) NOT NULL,
  UNIQUE (person_id, friend_id)
);

CREATE OR REPLACE FUNCTION random_between(low INT ,high INT)
   RETURNS INT AS $$
BEGIN
   RETURN floor(random()* (high-low + 1) + low);
END
$$ language 'plpgsql' STRICT;

INSERT INTO people (id)
SELECT *
FROM generate_series(1, 100)
ON CONFLICT DO NOTHING;

INSERT INTO friends (person_id, friend_id)
SELECT random_between(1, 100), random_between(1, 100)
FROM generate_series(1,1000)
ON CONFLICT DO NOTHING;

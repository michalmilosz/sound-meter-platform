-- PostgreSQL

-- DROP TABLE public."user";

CREATE TABLE public."user" (
	login varchar(255) NOT NULL,
	"password" varchar(255) NULL,
	phone varchar(255) NULL,
	calibration int4 NULL,
	CONSTRAINT user_pkey PRIMARY KEY (login)
);

-- DROP TABLE public.measurement;

CREATE TABLE public.measurement (
	id serial NOT NULL DEFAULT nextval('measurement_id_seq'::regclass),
	min numeric NULL,
	max numeric NULL,
	avg numeric NULL,
	gps_longitude numeric NULL,
	gps_latitude numeric NULL,
	user_login varchar(255) NULL,
	CONSTRAINT measurement_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE public.measurement_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807;
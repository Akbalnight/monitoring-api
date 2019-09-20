CREATE TABLE public.mg_services (
	id serial PRIMARY KEY,
	service_name character varying,
	service_url character varying,
	service_key character varying,
	service_version character varying,
	service_status boolean,
	update_time timestamp with time zone
);
ALTER TABLE public.mg_services ADD COLUMN server_id character varying;
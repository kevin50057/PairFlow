-- Richer personal profile: self-described gender + a short bio.
ALTER TABLE public.users ADD COLUMN gender character varying(16);
ALTER TABLE public.users ADD COLUMN bio character varying(200);

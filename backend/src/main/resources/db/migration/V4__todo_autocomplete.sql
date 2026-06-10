-- Todos can be bound to the calendar and auto-complete when their due time passes.
ALTER TABLE public.todos ADD COLUMN auto_complete boolean NOT NULL DEFAULT false;

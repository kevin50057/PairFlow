-- "加到行事曆": a wish can be scheduled; it shows on the calendar and auto-completes when the time passes.
ALTER TABLE public.wishes ADD COLUMN scheduled_at timestamp(6) with time zone;

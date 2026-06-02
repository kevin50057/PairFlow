-- PairFlow initial schema (generated from JPA entities via Hibernate, dumped from PostgreSQL 16).


CREATE TABLE public.albums (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id character varying(36) NOT NULL,
    title character varying(200) NOT NULL,
    description character varying(1000),
    couple_id character varying(255) NOT NULL,
    cover_photo_url character varying(255),
    created_by character varying(255) NOT NULL
);

CREATE TABLE public.anniversaries (
    date date NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    repeat_type character varying(16) NOT NULL,
    id character varying(36) NOT NULL,
    reminder_days_before_csv character varying(100),
    title character varying(200) NOT NULL,
    description character varying(1000),
    couple_id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    CONSTRAINT anniversaries_repeat_type_check CHECK (((repeat_type)::text = ANY ((ARRAY['NONE'::character varying, 'YEARLY'::character varying, 'MONTHLY'::character varying])::text[])))
);

CREATE TABLE public.audit_logs (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id character varying(36) NOT NULL,
    action character varying(40) NOT NULL,
    ip_address character varying(64),
    actor_id character varying(255) NOT NULL,
    couple_id character varying(255),
    target_id character varying(255),
    target_type character varying(255),
    CONSTRAINT audit_logs_action_check CHECK (((action)::text = ANY ((ARRAY['REGISTER'::character varying, 'LOGIN'::character varying, 'LOGOUT'::character varying, 'COUPLE_JOIN'::character varying, 'COUPLE_BREAKUP_INITIATE'::character varying, 'COUPLE_BREAKUP_CONFIRM'::character varying, 'COUPLE_BREAKUP_CANCEL'::character varying, 'DATA_EXPORT'::character varying, 'TODO_DELETE'::character varying, 'PHOTO_DELETE'::character varying, 'NOTE_DELETE'::character varying])::text[])))
);

CREATE TABLE public.couple_invites (
    created_at timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    code character varying(16) NOT NULL,
    status character varying(16) NOT NULL,
    id character varying(36) NOT NULL,
    accepted_by_user_id character varying(255),
    couple_id character varying(255),
    inviter_user_id character varying(255) NOT NULL,
    CONSTRAINT couple_invites_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'CANCELLED'::character varying, 'EXPIRED'::character varying])::text[])))
);

CREATE TABLE public.couples (
    relationship_start_date date,
    created_at timestamp(6) with time zone NOT NULL,
    ended_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone NOT NULL,
    data_handling character varying(16),
    status character varying(16) NOT NULL,
    id character varying(36) NOT NULL,
    useraid character varying(255) NOT NULL,
    userbid character varying(255) NOT NULL,
    CONSTRAINT couples_data_handling_check CHECK (((data_handling)::text = ANY ((ARRAY['ARCHIVE'::character varying, 'DELETE'::character varying, 'KEEP_PERSONAL'::character varying, 'EXPORT'::character varying])::text[]))),
    CONSTRAINT couples_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'ENDED'::character varying])::text[])))
);

CREATE TABLE public.daily_questions (
    date date NOT NULL,
    is_favorite boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id character varying(36) NOT NULL,
    couple_id character varying(255) NOT NULL,
    question_card_id character varying(255) NOT NULL
);

CREATE TABLE public.date_candidates (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id character varying(36) NOT NULL,
    title character varying(200) NOT NULL,
    description character varying(1000),
    added_by character varying(255) NOT NULL,
    location character varying(255),
    plan_id character varying(255) NOT NULL
);

CREATE TABLE public.date_plans (
    duration_hours integer,
    budget_level character varying(8),
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    date_type character varying(16) NOT NULL,
    status character varying(16) NOT NULL,
    id character varying(36) NOT NULL,
    title character varying(200) NOT NULL,
    area character varying(255),
    chosen_candidate_id character varying(255),
    couple_id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    scheduled_event_id character varying(255),
    CONSTRAINT date_plans_budget_level_check CHECK (((budget_level)::text = ANY ((ARRAY['LOW'::character varying, 'MEDIUM'::character varying, 'HIGH'::character varying])::text[]))),
    CONSTRAINT date_plans_date_type_check CHECK (((date_type)::text = ANY ((ARRAY['FOOD'::character varying, 'MOVIE'::character varying, 'EXHIBITION'::character varying, 'WALK'::character varying, 'TRIP'::character varying, 'INDOOR'::character varying, 'SPORT'::character varying, 'CAFE'::character varying, 'RELAX'::character varying, 'ANNIVERSARY'::character varying])::text[]))),
    CONSTRAINT date_plans_status_check CHECK (((status)::text = ANY ((ARRAY['PLANNING'::character varying, 'DECIDED'::character varying, 'DONE'::character varying])::text[])))
);

CREATE TABLE public.date_votes (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    vote character varying(8) NOT NULL,
    id character varying(36) NOT NULL,
    candidate_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL,
    CONSTRAINT date_votes_vote_check CHECK (((vote)::text = ANY ((ARRAY['WANT'::character varying, 'NEUTRAL'::character varying, 'NO'::character varying, 'LATER'::character varying])::text[])))
);

CREATE TABLE public.events (
    budget double precision,
    created_at timestamp(6) with time zone NOT NULL,
    end_time timestamp(6) with time zone,
    reminder_time timestamp(6) with time zone,
    start_time timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    event_type character varying(16) NOT NULL,
    id character varying(36) NOT NULL,
    title character varying(200) NOT NULL,
    description character varying(2000),
    couple_id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    dress_code character varying(255),
    location_address character varying(255),
    location_name character varying(255),
    related_album_id character varying(255),
    related_todo_id character varying(255),
    reservation_info character varying(255),
    transport character varying(255),
    CONSTRAINT events_event_type_check CHECK (((event_type)::text = ANY ((ARRAY['DATE'::character varying, 'TRAVEL'::character varying, 'ANNIVERSARY'::character varying, 'HOUSEWORK'::character varying, 'PERSONAL'::character varying, 'OTHER'::character varying])::text[])))
);

CREATE TABLE public.expenses (
    amount double precision NOT NULL,
    custom_payer_ratio double precision,
    created_at timestamp(6) with time zone NOT NULL,
    spent_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    split_type character varying(16) NOT NULL,
    category character varying(32) NOT NULL,
    id character varying(36) NOT NULL,
    note character varying(500),
    couple_id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    paid_by_user_id character varying(255) NOT NULL,
    related_event_id character varying(255),
    CONSTRAINT expenses_split_type_check CHECK (((split_type)::text = ANY ((ARRAY['NONE'::character varying, 'EQUAL'::character varying, 'I_PAID'::character varying, 'PARTNER_PAID'::character varying, 'CUSTOM'::character varying])::text[])))
);

CREATE TABLE public.mood_entries (
    need_response boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    mood character varying(16) NOT NULL,
    id character varying(36) NOT NULL,
    note character varying(500),
    couple_id character varying(255) NOT NULL,
    emoji character varying(255),
    user_id character varying(255) NOT NULL,
    CONSTRAINT mood_entries_mood_check CHECK (((mood)::text = ANY ((ARRAY['VERY_HAPPY'::character varying, 'HAPPY'::character varying, 'NORMAL'::character varying, 'TIRED'::character varying, 'STRESSED'::character varying, 'WANT_COMPANY'::character varying, 'WANT_QUIET'::character varying, 'ANGRY'::character varying, 'SAD'::character varying, 'MISS_YOU'::character varying])::text[])))
);

CREATE TABLE public.mood_reactions (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    reaction character varying(16) NOT NULL,
    id character varying(36) NOT NULL,
    mood_entry_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL,
    CONSTRAINT mood_reactions_reaction_check CHECK (((reaction)::text = ANY ((ARRAY['HUG'::character varying, 'HERE'::character varying, 'LATER'::character varying, 'THANKS'::character varying, 'LISTEN'::character varying, 'ILL_HANDLE'::character varying])::text[])))
);

CREATE TABLE public.notes (
    is_favorite boolean NOT NULL,
    is_read boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    read_at timestamp(6) with time zone,
    unlock_time timestamp(6) with time zone,
    updated_at timestamp(6) with time zone NOT NULL,
    note_type character varying(24) NOT NULL,
    id character varying(36) NOT NULL,
    title character varying(200),
    content character varying(4000) NOT NULL,
    background_style character varying(255),
    couple_id character varying(255) NOT NULL,
    image_url character varying(255),
    receiver_id character varying(255) NOT NULL,
    sender_id character varying(255) NOT NULL,
    CONSTRAINT notes_note_type_check CHECK (((note_type)::text = ANY ((ARRAY['NOTE'::character varying, 'LETTER'::character varying, 'FUTURE'::character varying, 'APOLOGY'::character varying, 'THANKS'::character varying, 'ENCOURAGE'::character varying, 'ANNIVERSARY_CARD'::character varying])::text[])))
);

CREATE TABLE public.notification_preferences (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id character varying(36) NOT NULL,
    disabled_types_csv character varying(500),
    user_id character varying(255) NOT NULL
);

CREATE TABLE public.notifications (
    is_read boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    read_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone NOT NULL,
    type character varying(24) NOT NULL,
    id character varying(36) NOT NULL,
    title character varying(200) NOT NULL,
    body character varying(500),
    couple_id character varying(255) NOT NULL,
    recipient_id character varying(255) NOT NULL,
    related_id character varying(255),
    related_type character varying(255),
    CONSTRAINT notifications_type_check CHECK (((type)::text = ANY ((ARRAY['TODO_DUE'::character varying, 'TODO_CREATED'::character varying, 'TODO_COMPLETED'::character varying, 'ANNIVERSARY'::character varying, 'NOTE'::character varying, 'LETTER_UNLOCK'::character varying, 'MOOD'::character varying, 'EVENT'::character varying, 'DAILY_QUESTION'::character varying, 'AI_NUDGE'::character varying, 'BREAKUP_REQUESTED'::character varying, 'BREAKUP_CONFIRMED'::character varying, 'BREAKUP_CANCELLED'::character varying])::text[])))
);

CREATE TABLE public.pending_breakups (
    cancelled boolean NOT NULL,
    confirmed boolean NOT NULL,
    cancelled_at timestamp(6) with time zone,
    confirmed_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    data_handling character varying(16),
    id character varying(36) NOT NULL,
    confirmed_by_id character varying(255),
    couple_id character varying(255) NOT NULL,
    initiator_id character varying(255) NOT NULL,
    CONSTRAINT pending_breakups_data_handling_check CHECK (((data_handling)::text = ANY ((ARRAY['ARCHIVE'::character varying, 'DELETE'::character varying, 'KEEP_PERSONAL'::character varying, 'EXPORT'::character varying])::text[])))
);

CREATE TABLE public.photos (
    is_favorite boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    taken_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone NOT NULL,
    id character varying(36) NOT NULL,
    caption character varying(500),
    tags_csv character varying(500),
    album_id character varying(255),
    couple_id character varying(255) NOT NULL,
    image_url character varying(255) NOT NULL,
    location_name character varying(255),
    storage_key character varying(255) NOT NULL,
    thumbnail_url character varying(255),
    uploader_id character varying(255) NOT NULL
);

CREATE TABLE public.question_answers (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id character varying(36) NOT NULL,
    answer character varying(2000) NOT NULL,
    daily_question_id character varying(255) NOT NULL,
    user_id character varying(255) NOT NULL
);

CREATE TABLE public.question_cards (
    created_at timestamp(6) with time zone NOT NULL,
    sensitivity character varying(8) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    category character varying(32) NOT NULL,
    id character varying(36) NOT NULL,
    text character varying(500) NOT NULL,
    CONSTRAINT question_cards_sensitivity_check CHECK (((sensitivity)::text = ANY ((ARRAY['LOW'::character varying, 'MEDIUM'::character varying, 'HIGH'::character varying])::text[])))
);

CREATE TABLE public.refresh_tokens (
    revoked boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    expires_at timestamp(6) with time zone NOT NULL,
    revoked_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone NOT NULL,
    id character varying(36) NOT NULL,
    token_hash character varying(64) NOT NULL,
    user_id character varying(255) NOT NULL
);

CREATE TABLE public.repair_sessions (
    flagged boolean NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    responded_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone NOT NULL,
    response_type character varying(16),
    status character varying(16) NOT NULL,
    state character varying(24) NOT NULL,
    id character varying(36) NOT NULL,
    key_points character varying(1000),
    response_note character varying(1000),
    feelings character varying(2000),
    softened_message character varying(2000),
    couple_id character varying(255) NOT NULL,
    initiator_id character varying(255) NOT NULL,
    responder_id character varying(255),
    CONSTRAINT repair_sessions_response_type_check CHECK (((response_type)::text = ANY ((ARRAY['I_HEAR_YOU'::character varying, 'NEED_TIME'::character varying, 'LETS_TALK'::character varying, 'IM_SORRY'::character varying, 'THANK_YOU'::character varying])::text[]))),
    CONSTRAINT repair_sessions_state_check CHECK (((state)::text = ANY ((ARRAY['NEED_CALM'::character varying, 'WANT_APOLOGIZE'::character varying, 'WANT_UNDERSTOOD'::character varying, 'DONT_KNOW'::character varying])::text[]))),
    CONSTRAINT repair_sessions_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SENT'::character varying, 'RESPONDED'::character varying, 'CLOSED'::character varying])::text[])))
);

CREATE TABLE public.todo_checklist_items (
    completed boolean NOT NULL,
    sort_order integer NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id character varying(36) NOT NULL,
    title character varying(300) NOT NULL,
    todo_id character varying(255) NOT NULL
);

CREATE TABLE public.todo_comments (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id character varying(36) NOT NULL,
    content character varying(1000) NOT NULL,
    author_id character varying(255) NOT NULL,
    todo_id character varying(255) NOT NULL
);

CREATE TABLE public.todos (
    assigned_to_both boolean NOT NULL,
    goal_current double precision,
    goal_target double precision,
    secret boolean NOT NULL,
    completed_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone NOT NULL,
    due_date timestamp(6) with time zone,
    priority character varying(8) NOT NULL,
    reminder_time timestamp(6) with time zone,
    secret_unlock_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone NOT NULL,
    status character varying(16) NOT NULL,
    type character varying(16) NOT NULL,
    id character varying(36) NOT NULL,
    title character varying(200) NOT NULL,
    description character varying(2000),
    assignee_user_id character varying(255),
    couple_id character varying(255) NOT NULL,
    created_by character varying(255) NOT NULL,
    goal_unit character varying(255),
    related_anniversary_id character varying(255),
    related_event_id character varying(255),
    repeat_rule character varying(255),
    CONSTRAINT todos_priority_check CHECK (((priority)::text = ANY ((ARRAY['LOW'::character varying, 'MEDIUM'::character varying, 'HIGH'::character varying])::text[]))),
    CONSTRAINT todos_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'DONE'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT todos_type_check CHECK (((type)::text = ANY ((ARRAY['GENERAL'::character varying, 'DATE'::character varying, 'TRAVEL'::character varying, 'HOUSEWORK'::character varying, 'SHOPPING'::character varying, 'GOAL'::character varying, 'ANNIVERSARY'::character varying, 'SURPRISE'::character varying])::text[])))
);

CREATE TABLE public.user_devices (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    platform character varying(16),
    id character varying(36) NOT NULL,
    fcm_token character varying(512) NOT NULL,
    user_id character varying(255) NOT NULL
);

CREATE TABLE public.users (
    birthday date,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id character varying(36) NOT NULL,
    avatar_url character varying(255),
    display_name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    timezone character varying(255) NOT NULL
);

CREATE TABLE public.wishes (
    estimated_cost double precision,
    completed_at timestamp(6) with time zone,
    created_at timestamp(6) with time zone NOT NULL,
    priority character varying(8) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    category character varying(16) NOT NULL,
    status character varying(16) NOT NULL,
    id character varying(36) NOT NULL,
    title character varying(200) NOT NULL,
    description character varying(1000),
    added_by character varying(255) NOT NULL,
    converted_todo_id character varying(255),
    couple_id character varying(255) NOT NULL,
    link character varying(255),
    location character varying(255),
    CONSTRAINT wishes_category_check CHECK (((category)::text = ANY ((ARRAY['PLACE'::character varying, 'FOOD'::character varying, 'MOVIE'::character varying, 'BUY'::character varying, 'DO'::character varying, 'LEARN'::character varying, 'OTHER'::character varying])::text[]))),
    CONSTRAINT wishes_priority_check CHECK (((priority)::text = ANY ((ARRAY['LOW'::character varying, 'MEDIUM'::character varying, 'HIGH'::character varying])::text[]))),
    CONSTRAINT wishes_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'COMPLETED'::character varying, 'ARCHIVED'::character varying])::text[])))
);

ALTER TABLE ONLY public.albums
    ADD CONSTRAINT albums_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.anniversaries
    ADD CONSTRAINT anniversaries_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.couple_invites
    ADD CONSTRAINT couple_invites_code_key UNIQUE (code);

ALTER TABLE ONLY public.couple_invites
    ADD CONSTRAINT couple_invites_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.couples
    ADD CONSTRAINT couples_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.daily_questions
    ADD CONSTRAINT daily_questions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.date_candidates
    ADD CONSTRAINT date_candidates_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.date_plans
    ADD CONSTRAINT date_plans_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.date_votes
    ADD CONSTRAINT date_votes_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.events
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.expenses
    ADD CONSTRAINT expenses_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT idx_rt_hash UNIQUE (token_hash);

ALTER TABLE ONLY public.mood_entries
    ADD CONSTRAINT mood_entries_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.mood_reactions
    ADD CONSTRAINT mood_reactions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.notes
    ADD CONSTRAINT notes_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.notification_preferences
    ADD CONSTRAINT notification_preferences_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pending_breakups
    ADD CONSTRAINT pending_breakups_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.photos
    ADD CONSTRAINT photos_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.question_answers
    ADD CONSTRAINT question_answers_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.question_cards
    ADD CONSTRAINT question_cards_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.repair_sessions
    ADD CONSTRAINT repair_sessions_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.todo_checklist_items
    ADD CONSTRAINT todo_checklist_items_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.todo_comments
    ADD CONSTRAINT todo_comments_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.todos
    ADD CONSTRAINT todos_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.question_answers
    ADD CONSTRAINT uk_answer_dq_user UNIQUE (daily_question_id, user_id);

ALTER TABLE ONLY public.daily_questions
    ADD CONSTRAINT uk_daily_couple_date UNIQUE (couple_id, date);

ALTER TABLE ONLY public.notification_preferences
    ADD CONSTRAINT uk_notifpref_user UNIQUE (user_id);

ALTER TABLE ONLY public.date_votes
    ADD CONSTRAINT uk_vote_candidate_user UNIQUE (candidate_id, user_id);

ALTER TABLE ONLY public.user_devices
    ADD CONSTRAINT user_devices_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.wishes
    ADD CONSTRAINT wishes_pkey PRIMARY KEY (id);

CREATE INDEX idx_album_couple ON public.albums USING btree (couple_id);

CREATE INDEX idx_anniv_couple ON public.anniversaries USING btree (couple_id);

CREATE INDEX idx_answer_dq ON public.question_answers USING btree (daily_question_id);

CREATE INDEX idx_audit_actor ON public.audit_logs USING btree (actor_id);

CREATE INDEX idx_audit_couple ON public.audit_logs USING btree (couple_id);

CREATE INDEX idx_candidate_plan ON public.date_candidates USING btree (plan_id);

CREATE INDEX idx_checklist_todo ON public.todo_checklist_items USING btree (todo_id);

CREATE INDEX idx_comment_todo ON public.todo_comments USING btree (todo_id);

CREATE INDEX idx_daily_couple ON public.daily_questions USING btree (couple_id);

CREATE INDEX idx_dateplan_couple ON public.date_plans USING btree (couple_id);

CREATE INDEX idx_device_user ON public.user_devices USING btree (user_id);

CREATE INDEX idx_event_couple ON public.events USING btree (couple_id);

CREATE INDEX idx_event_start ON public.events USING btree (start_time);

CREATE INDEX idx_expense_couple ON public.expenses USING btree (couple_id);

CREATE INDEX idx_mood_couple_user ON public.mood_entries USING btree (couple_id, user_id);

CREATE INDEX idx_note_couple ON public.notes USING btree (couple_id);

CREATE INDEX idx_notif_recipient ON public.notifications USING btree (recipient_id);

CREATE INDEX idx_pb_couple ON public.pending_breakups USING btree (couple_id);

CREATE INDEX idx_photo_album ON public.photos USING btree (album_id);

CREATE INDEX idx_photo_couple ON public.photos USING btree (couple_id);

CREATE INDEX idx_reaction_mood ON public.mood_reactions USING btree (mood_entry_id);

CREATE INDEX idx_repair_couple ON public.repair_sessions USING btree (couple_id);

CREATE INDEX idx_rt_user ON public.refresh_tokens USING btree (user_id);

CREATE INDEX idx_todo_couple ON public.todos USING btree (couple_id);

CREATE INDEX idx_todo_due ON public.todos USING btree (due_date);

CREATE INDEX idx_vote_candidate ON public.date_votes USING btree (candidate_id);

CREATE INDEX idx_wish_couple ON public.wishes USING btree (couple_id);


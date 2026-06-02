-- Free-text "when do we want to do this" note for wishes (e.g. "2026 冬天完成").
ALTER TABLE wishes ADD COLUMN target_note varchar(100);

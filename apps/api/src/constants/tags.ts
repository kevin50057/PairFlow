// Predefined review tags for the MVP. Codes are stable identifiers used by the
// API + clients; labels are the user-facing Traditional Chinese text.

export interface TagDef {
  code: string;
  label: string;
  sortOrder: number;
}

export const REVIEW_TAGS: TagDef[] = [
  { code: 'BAD_TASTE', label: '難吃', sortOrder: 1 },
  { code: 'TOO_EXPENSIVE', label: '太貴', sortOrder: 2 },
  { code: 'SERVICE_BAD', label: '服務差', sortOrder: 3 },
  { code: 'DIRTY', label: '環境髒亂', sortOrder: 4 },
  { code: 'LONG_WAIT', label: '等太久', sortOrder: 5 },
  { code: 'PHOTO_FRAUD', label: '照片詐欺', sortOrder: 6 },
  { code: 'SMALL_PORTION', label: '份量太少', sortOrder: 7 },
  { code: 'COLD_FOOD', label: '冷掉', sortOrder: 8 },
  { code: 'HYGIENE', label: '衛生疑慮', sortOrder: 9 },
  { code: 'NEVER_AGAIN', label: '不會再去', sortOrder: 10 },
];

export const REVIEW_TAG_CODES = REVIEW_TAGS.map((t) => t.code);

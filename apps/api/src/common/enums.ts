// Allowed values for the string-typed status/role columns. Centralised so the
// service layer and validators share one source of truth. (Stored as strings so
// the same schema runs on SQLite in dev and Postgres in prod.)

export const UserRole = { USER: 'USER', ADMIN: 'ADMIN' } as const;
export type UserRole = (typeof UserRole)[keyof typeof UserRole];

export const UserStatus = { ACTIVE: 'ACTIVE', SUSPENDED: 'SUSPENDED' } as const;
export type UserStatus = (typeof UserStatus)[keyof typeof UserStatus];

export const RestaurantStatus = { ACTIVE: 'ACTIVE', HIDDEN: 'HIDDEN', CLOSED: 'CLOSED' } as const;
export type RestaurantStatus = (typeof RestaurantStatus)[keyof typeof RestaurantStatus];

export const ReviewStatus = {
  PUBLISHED: 'PUBLISHED',
  HIDDEN: 'HIDDEN',
  REPORTED: 'REPORTED',
  DELETED: 'DELETED',
} as const;
export type ReviewStatus = (typeof ReviewStatus)[keyof typeof ReviewStatus];

export const ReportStatus = { PENDING: 'PENDING', RESOLVED: 'RESOLVED', REJECTED: 'REJECTED' } as const;
export type ReportStatus = (typeof ReportStatus)[keyof typeof ReportStatus];

export const ReportReason = {
  ABUSE: 'ABUSE',
  SPAM: 'SPAM',
  FAKE: 'FAKE',
  SEXUAL_VIOLENT: 'SEXUAL_VIOLENT',
  IRRELEVANT: 'IRRELEVANT',
} as const;
export type ReportReason = (typeof ReportReason)[keyof typeof ReportReason];

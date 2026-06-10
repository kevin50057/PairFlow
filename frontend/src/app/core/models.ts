// API response shapes (mirror the Spring DTOs).

export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export interface User {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
  birthday?: string;
  gender?: Gender;
  bio?: string;
  timezone: string;
  createdAt: string;
}

export interface AuthResponse { token: string; refreshToken: string; user: User; }

export interface Couple {
  id: string;
  partner: User | null;
  relationshipStartDate?: string;
  daysTogether?: number;
  status: string;
  createdAt: string;
}

export interface ChecklistItem { id: string; title: string; completed: boolean; sortOrder: number; }
export interface Comment { id: string; authorId: string; content: string; createdAt: string; }

export interface Todo {
  id: string;
  title: string;
  description?: string;
  type: string;
  status: string;
  priority: string;
  assignee: 'me' | 'partner' | 'both' | 'unassigned';
  assigneeUserId?: string;
  assignedToBoth: boolean;
  dueDate?: string;
  reminderTime?: string;
  autoComplete?: boolean;
  isSecret: boolean;
  relatedEventId?: string;
  goalTarget?: number;
  goalCurrent?: number;
  goalUnit?: string;
  checklist: ChecklistItem[];
  comments?: Comment[];
  createdBy: string;
  createdAt: string;
  completedAt?: string;
}

export interface Reaction { id: string; userId: string; reaction: string; createdAt: string; }
export interface Mood {
  id: string; userId: string; mood: string; emoji?: string; note?: string;
  needResponse: boolean; reactions: Reaction[]; createdAt: string;
}
export interface TodayMood { me: Mood | null; partner: Mood | null; }

export interface Anniversary {
  id: string; title: string; date: string; repeatType: string;
  reminderDaysBefore: number[]; description?: string;
  nextOccurrence: string; daysLeft: number; createdAt: string;
}

export interface EventItem {
  id: string; title: string; description?: string; eventType: string;
  startTime: string; endTime?: string; locationName?: string; locationAddress?: string;
  budget?: number; transport?: string; dressCode?: string; reservationInfo?: string; createdAt: string;
}

export interface NoteItem {
  id: string; senderId: string; receiverId: string; title?: string; content: string;
  noteType: string; backgroundStyle?: string; imageUrl?: string; unlockTime?: string;
  locked: boolean; isRead: boolean; isFavorite: boolean; createdAt: string; readAt?: string;
}

export interface Album { id: string; title: string; description?: string; coverPhotoUrl?: string; photoCount: number; createdAt: string; }
export interface Photo { id: string; albumId?: string; uploaderId: string; imageUrl: string; thumbnailUrl?: string; caption?: string; takenAt?: string; locationName?: string; tags: string[]; isFavorite: boolean; createdAt: string; }

export interface Wish {
  id: string; title: string; description?: string; category: string; priority: string;
  estimatedCost?: number; location?: string; link?: string; targetNote?: string; status: string;
  convertedTodoId?: string; scheduledAt?: string; completedAt?: string; createdAt: string;
}

export interface Expense {
  id: string; amount: number; category: string; paidByUserId: string; splitType: string;
  customPayerRatio?: number; note?: string; spentAt: string; createdAt: string;
}
export interface ExpenseSummary { total: number; count: number; byCategory: Record<string, number>; byPayer: Record<string, number>; }

export interface DailyQuestion {
  id: string; date: string; questionText: string; category: string; sensitivity: string;
  myAnswer?: string; partnerAnswer?: string; myAnswered: boolean; partnerAnswered: boolean;
  bothAnswered: boolean; isFavorite: boolean;
}

export interface VoteView { userId: string; vote: string; }
export interface Candidate { id: string; title: string; description?: string; location?: string; addedBy: string; votes: VoteView[]; myVote?: string; createdAt: string; }
export interface DatePlan {
  id: string; title: string; dateType: string; budgetLevel?: string; area?: string;
  durationHours?: number; status: string; chosenCandidateId?: string; scheduledEventId?: string;
  candidates: Candidate[]; createdBy: string; createdAt: string;
}

export interface Repair {
  id: string; initiatorId: string; state: string; feelings?: string; keyPoints?: string;
  softenedMessage?: string; status: string; flagged: boolean; notice?: string;
  responderId?: string; responseType?: string; responseNote?: string; respondedAt?: string; createdAt: string;
}

export interface Notification {
  id: string; type: string; title: string; body?: string; isRead: boolean;
  relatedType?: string; relatedId?: string; createdAt: string; readAt?: string;
}

export interface Home {
  couple: { daysTogether?: number; relationshipStartDate?: string };
  partnerMood: Mood | null;
  todayTodos: Todo[];
  nextAnniversary: { title: string; daysLeft: number; date: string } | null;
  todayEvents: EventItem[];
  memory: { title: string; description: string; photoCount: number } | null;
}

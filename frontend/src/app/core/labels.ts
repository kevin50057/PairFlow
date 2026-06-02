// Display labels for backend enum codes (kept in one place for reuse in templates).

export const MOOD: Record<string, { label: string; emoji: string }> = {
  VERY_HAPPY: { label: '很開心', emoji: '😄' },
  HAPPY: { label: '開心', emoji: '🙂' },
  NORMAL: { label: '普通', emoji: '😌' },
  TIRED: { label: '有點累', emoji: '😮‍💨' },
  STRESSED: { label: '壓力大', emoji: '😣' },
  WANT_COMPANY: { label: '想被陪', emoji: '🥺' },
  WANT_QUIET: { label: '想安靜', emoji: '🤫' },
  ANGRY: { label: '生氣', emoji: '😠' },
  SAD: { label: '難過', emoji: '😢' },
  MISS_YOU: { label: '想你', emoji: '❤️' },
};

export const REACTION: Record<string, string> = {
  HUG: '抱抱你', HERE: '我在', LATER: '晚點陪你', THANKS: '辛苦了', LISTEN: '想聽你說', ILL_HANDLE: '今天我來處理',
};

export const TODO_TYPE: Record<string, string> = {
  GENERAL: '一般', DATE: '約會', TRAVEL: '旅行', HOUSEWORK: '家務',
  SHOPPING: '採買', GOAL: '目標', ANNIVERSARY: '紀念日', SURPRISE: '驚喜',
};

export const PRIORITY: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高' };

export const ASSIGNEE: Record<string, string> = { me: '我', partner: '對方', both: '一起', unassigned: '未指派' };

export const WISH_CATEGORY: Record<string, string> = {
  PLACE: '想去的地方', FOOD: '想吃', MOVIE: '想看', BUY: '想買', DO: '想完成', LEARN: '想學', OTHER: '其他',
};

export const VOTE: Record<string, string> = { WANT: '想去', NEUTRAL: '普通', NO: '不想去', LATER: '下次再說' };

export const DATE_TYPE: Record<string, string> = {
  FOOD: '美食', MOVIE: '電影', EXHIBITION: '展覽', WALK: '散步', TRIP: '小旅行',
  INDOOR: '室內', SPORT: '運動', CAFE: '咖啡廳', RELAX: '放鬆', ANNIVERSARY: '紀念日約會',
};

export const REPAIR_STATE: Record<string, string> = {
  NEED_CALM: '我需要冷靜', WANT_APOLOGIZE: '我想道歉', WANT_UNDERSTOOD: '我想被理解', DONT_KNOW: '我不知道怎麼說',
};

export const RESPONSE_TYPE: Record<string, string> = {
  I_HEAR_YOU: '我聽到了', NEED_TIME: '需要一點時間', LETS_TALK: '一起聊聊', IM_SORRY: '對不起', THANK_YOU: '謝謝你',
};

export function initial(name?: string | null): string {
  return name?.trim()?.charAt(0)?.toUpperCase() ?? '?';
}

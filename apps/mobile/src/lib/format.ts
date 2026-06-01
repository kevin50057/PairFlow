export function formatDistance(meters: number | null): string {
  if (meters == null) return '';
  if (meters < 1000) return `${meters} m`;
  return `${(meters / 1000).toFixed(1)} km`;
}

export function formatDate(iso: string | null): string {
  return iso ? iso.slice(0, 10) : '';
}

export function timeAgo(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const day = 86_400_000;
  if (diffMs < day) return '今天';
  const days = Math.floor(diffMs / day);
  if (days < 30) return `${days} 天前`;
  const months = Math.floor(days / 30);
  if (months < 12) return `${months} 個月前`;
  return `${Math.floor(months / 12)} 年前`;
}

/** Short descriptor for a 雷度 band, used as a caption next to the score. */
export function thunderLabel(score: number): string {
  if (score >= 4.5) return '重大地雷';
  if (score >= 3.5) return '非常雷';
  if (score >= 2.5) return '普通偏雷';
  if (score >= 1.5) return '小失望';
  if (score > 0) return '勉強可以';
  return '尚無評價';
}

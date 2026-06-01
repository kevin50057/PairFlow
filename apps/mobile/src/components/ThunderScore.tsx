import { IonIcon } from '@ionic/react';
import { flash, flashOutline } from 'ionicons/icons';
import { thunderLabel } from '../lib/format';

// Colour bands shared with the rest of the UI (higher 雷度 = redder = worse).
const BANDS: { min: number; color: string }[] = [
  { min: 4.5, color: '#b3001b' },
  { min: 3.5, color: '#e4572e' },
  { min: 2.5, color: '#f3a712' },
  { min: 1.5, color: '#f4b740' },
  { min: 0.000001, color: '#86b817' },
];
const NEUTRAL = '#b8b8c0';

export function colorForScore(score: number): string {
  return BANDS.find((b) => score >= b.min)?.color ?? NEUTRAL;
}

const SIZES = { sm: 15, md: 20, lg: 32 } as const;

interface Props {
  score: number;
  size?: keyof typeof SIZES;
  showNumber?: boolean;
  showLabel?: boolean;
}

export default function ThunderScore({ score, size = 'md', showNumber = true, showLabel = false }: Props) {
  const filled = Math.round(score);
  const color = score > 0 ? colorForScore(score) : NEUTRAL;
  const px = SIZES[size];

  return (
    <div className="thunder-score">
      <span className="thunder-icons" style={{ fontSize: px }}>
        {[1, 2, 3, 4, 5].map((i) => (
          <IonIcon key={i} icon={i <= filled ? flash : flashOutline} style={{ color }} />
        ))}
      </span>
      {showNumber && (
        <span className="thunder-number" style={{ color }}>
          {score > 0 ? score.toFixed(1) : '—'}
        </span>
      )}
      {showLabel && <span className="thunder-caption">{thunderLabel(score)}</span>}
    </div>
  );
}

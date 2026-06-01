import { IonChip, IonIcon, IonLabel } from '@ionic/react';
import { checkmarkCircle } from 'ionicons/icons';

interface TagOption {
  code: string;
  label: string;
  count?: number;
}

interface Props {
  options: TagOption[];
  selected?: string[];
  onToggle?: (code: string) => void;
  readonly?: boolean;
  max?: number;
}

export default function TagChips({ options, selected = [], onToggle, readonly = false, max }: Props) {
  return (
    <div className="tag-chips">
      {options.map((o) => {
        const isSelected = selected.includes(o.code);
        const blocked = !readonly && !isSelected && !!max && selected.length >= max;
        return (
          <IonChip
            key={o.code}
            className={`tag-chip${isSelected ? ' selected' : ''}${blocked ? ' disabled' : ''}`}
            outline={!isSelected}
            onClick={() => !readonly && !blocked && onToggle?.(o.code)}
          >
            {isSelected && !readonly && <IonIcon icon={checkmarkCircle} />}
            <IonLabel>
              {o.label}
              {typeof o.count === 'number' ? ` ${o.count}` : ''}
            </IonLabel>
          </IonChip>
        );
      })}
    </div>
  );
}

import { IonCard, IonIcon } from '@ionic/react';
import { chatbubbleEllipsesOutline, locationOutline } from 'ionicons/icons';
import type { RestaurantCard as RestaurantCardModel } from '../api/types';
import { formatDistance } from '../lib/format';
import ThunderScore, { colorForScore } from './ThunderScore';

interface Props {
  restaurant: RestaurantCardModel;
}

export default function RestaurantCard({ restaurant: r }: Props) {
  const cover = r.coverImageUrl ?? `https://picsum.photos/seed/r${r.id}/640/400`;
  const meta = [r.category, r.district].filter(Boolean).join(' · ');

  return (
    <IonCard className="rcard" button routerLink={`/restaurant/${r.id}`}>
      <div className="rcard-cover" style={{ backgroundImage: `url(${cover})` }}>
        <span className="rcard-score" style={{ borderColor: colorForScore(r.averageThunderScore) }}>
          <ThunderScore score={r.averageThunderScore} size="sm" showNumber />
        </span>
      </div>
      <div className="rcard-body">
        <h2 className="rcard-name">{r.name}</h2>
        {meta && <p className="rcard-sub">{meta}</p>}
        <div className="rcard-foot">
          <span>
            <IonIcon icon={chatbubbleEllipsesOutline} /> {r.reviewCount} 則雷評
          </span>
          {r.distanceMeters != null && (
            <span>
              <IonIcon icon={locationOutline} /> {formatDistance(r.distanceMeters)}
            </span>
          )}
        </div>
      </div>
    </IonCard>
  );
}

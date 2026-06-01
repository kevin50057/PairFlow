import {
  IonAlert,
  IonBackButton,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonPage,
  IonSpinner,
  IonTitle,
  IonToolbar,
  useIonRouter,
  useIonViewWillEnter,
} from '@ionic/react';
import { createOutline, trashOutline } from 'ionicons/icons';
import { useState } from 'react';
import { getMyReviews } from '../api/auth';
import { deleteReview } from '../api/restaurants';
import type { MyReviewItem } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import ThunderScore from '../components/ThunderScore';
import { timeAgo } from '../lib/format';

export default function MyReviewsPage() {
  const router = useIonRouter();
  const { user, ready } = useAuth();
  const [items, setItems] = useState<MyReviewItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const load = () => {
    setLoading(true);
    getMyReviews()
      .then((r) => setItems(r.items))
      .finally(() => setLoading(false));
  };

  useIonViewWillEnter(() => {
    if (ready && !user) {
      router.push('/login?redirect=/profile/reviews', 'root', 'replace');
      return;
    }
    load();
  });

  const confirmDelete = async () => {
    if (deleteId == null) return;
    await deleteReview(deleteId);
    setDeleteId(null);
    load();
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonButtons slot="start">
            <IonBackButton defaultHref="/profile" text="" />
          </IonButtons>
          <IonTitle>我的雷評</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="home-content">
        {loading && (
          <div className="center-pad">
            <IonSpinner name="crescent" />
          </div>
        )}
        {!loading && items.length === 0 && (
          <div className="empty-state">
            <p>你還沒有發表過雷評，去踩個雷吧！</p>
          </div>
        )}

        <div className="card-list">
          {items.map((rv) => (
            <div key={rv.id} className="myreview-card">
              <div className="myreview-top" onClick={() => router.push(`/restaurant/${rv.restaurant.id}`, 'forward')}>
                <div
                  className="myreview-thumb"
                  style={{ backgroundImage: `url(${rv.restaurant.coverImageUrl ?? ''})` }}
                />
                <div className="myreview-info">
                  <h3>{rv.restaurant.name}</h3>
                  <ThunderScore score={rv.thunderScore} size="sm" />
                  <span className="myreview-time">
                    {timeAgo(rv.createdAt)}
                    {rv.status === 'HIDDEN' ? ' · 已被管理員隱藏' : ''}
                  </span>
                </div>
              </div>
              <p className="myreview-content">{rv.content}</p>
              {rv.imageUrls.length > 0 && (
                <div className="review-images">
                  {rv.imageUrls.map((u, i) => (
                    <div key={i} className="review-image" style={{ backgroundImage: `url(${u})` }} />
                  ))}
                </div>
              )}
              <div className="myreview-actions">
                <IonButton size="small" fill="clear" onClick={() => router.push(`/reviews/${rv.id}/edit`, 'forward')}>
                  <IonIcon icon={createOutline} slot="start" />
                  編輯
                </IonButton>
                <IonButton size="small" fill="clear" color="danger" onClick={() => setDeleteId(rv.id)}>
                  <IonIcon icon={trashOutline} slot="start" />
                  刪除
                </IonButton>
              </div>
            </div>
          ))}
        </div>

        <IonAlert
          isOpen={deleteId != null}
          header="刪除雷評"
          message="確定要刪除這則雷評嗎？此動作無法復原。"
          buttons={[
            { text: '取消', role: 'cancel' },
            { text: '刪除', role: 'destructive', handler: confirmDelete },
          ]}
          onDidDismiss={() => setDeleteId(null)}
        />
      </IonContent>
    </IonPage>
  );
}

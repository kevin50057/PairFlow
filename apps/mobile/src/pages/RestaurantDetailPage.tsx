import {
  IonActionSheet,
  IonBackButton,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonPage,
  IonSpinner,
  IonText,
  IonTitle,
  IonToast,
  IonToolbar,
  useIonRouter,
  useIonViewWillEnter,
} from '@ionic/react';
import { callOutline, flag, flash, locationOutline, navigateOutline } from 'ionicons/icons';
import { useState } from 'react';
import { useParams } from 'react-router';
import { ApiError } from '../api/client';
import { getRestaurant, reportReview } from '../api/restaurants';
import { REPORT_REASONS, type RestaurantDetail } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import TagChips from '../components/TagChips';
import ThunderScore from '../components/ThunderScore';
import { thunderLabel, timeAgo } from '../lib/format';

function osmEmbed(lat: number, lng: number): string {
  const d = 0.004;
  const bbox = `${lng - d},${lat - d},${lng + d},${lat + d}`;
  return `https://www.openstreetmap.org/export/embed.html?bbox=${bbox}&layer=mapnik&marker=${lat},${lng}`;
}

export default function RestaurantDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useIonRouter();
  const { user } = useAuth();
  const [data, setData] = useState<RestaurantDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reportId, setReportId] = useState<number | null>(null);
  const [toast, setToast] = useState<{ message: string; color: string } | null>(null);

  useIonViewWillEnter(() => {
    let active = true;
    setLoading(true);
    setError(null);
    getRestaurant(id)
      .then((d) => active && setData(d))
      .catch((e) => active && setError(e instanceof Error ? e.message : '載入失敗'))
      .finally(() => active && setLoading(false));
  });

  const onFlag = (reviewId: number) => {
    if (!user) {
      router.push(`/login?redirect=/restaurant/${id}`, 'forward');
      return;
    }
    setReportId(reviewId);
  };

  const doReport = async (reasonCode: string) => {
    if (reportId == null) return;
    try {
      await reportReview(reportId, reasonCode);
      setToast({ message: '已送出檢舉，感謝你的回報', color: 'success' });
    } catch (e) {
      setToast({ message: e instanceof ApiError ? e.message : '檢舉失敗', color: 'danger' });
    }
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonButtons slot="start">
            <IonBackButton defaultHref="/home" text="" />
          </IonButtons>
          <IonTitle>{data?.name ?? '餐廳'}</IonTitle>
        </IonToolbar>
      </IonHeader>

      <IonContent>
        {loading && (
          <div className="center-pad">
            <IonSpinner name="crescent" />
          </div>
        )}
        {!loading && error && (
          <div className="center-pad">
            <IonText color="danger">{error}</IonText>
          </div>
        )}

        {data && (
          <>
            <div className="detail-cover" style={{ backgroundImage: `url(${data.coverImageUrl ?? ''})` }}>
              <div className="detail-cover-overlay">
                <h1>{data.name}</h1>
                <p>{[data.category, data.district].filter(Boolean).join(' · ')}</p>
              </div>
            </div>

            <div className="detail-scorebar">
              <ThunderScore score={data.averageThunderScore} size="lg" showNumber={false} />
              <div className="detail-scoretext">
                <span className="big">
                  {data.averageThunderScore > 0 ? data.averageThunderScore.toFixed(1) : '—'}
                </span>
                <span className="cap">
                  {thunderLabel(data.averageThunderScore)} · {data.reviewCount} 則雷評
                </span>
              </div>
            </div>

            <div className="detail-section">
              <div className="detail-row">
                <IonIcon icon={locationOutline} />
                <span>{data.address}</span>
              </div>
              {data.phone && (
                <div className="detail-row">
                  <IonIcon icon={callOutline} />
                  <a href={`tel:${data.phone}`}>{data.phone}</a>
                </div>
              )}
            </div>

            <div className="detail-map">
              <iframe title={`${data.name} 地圖`} src={osmEmbed(data.latitude, data.longitude)} loading="lazy" />
              <a
                className="map-open"
                href={`https://www.openstreetmap.org/?mlat=${data.latitude}&mlon=${data.longitude}#map=17/${data.latitude}/${data.longitude}`}
                target="_blank"
                rel="noreferrer"
              >
                <IonIcon icon={navigateOutline} /> 在地圖開啟
              </a>
            </div>

            {data.tagDistribution.length > 0 && (
              <div className="detail-section">
                <h3 className="detail-h3">大家踩到的雷</h3>
                <TagChips
                  readonly
                  options={data.tagDistribution.map((t) => ({ code: t.code, label: t.label, count: t.count }))}
                />
              </div>
            )}

            <div className="detail-section">
              <h3 className="detail-h3">最新雷評</h3>
              {data.latestReviews.length === 0 && <p className="muted">還沒有人評論，當第一個踩雷的人吧！</p>}
              {data.latestReviews.map((rv) => (
                <div key={rv.id} className="review-item">
                  <div className="review-head">
                    <ThunderScore score={rv.thunderScore} size="sm" showNumber={false} />
                    <span className="review-author">{rv.author.nickname}</span>
                    <span className="review-time">{timeAgo(rv.createdAt)}</span>
                    <IonIcon icon={flag} className="review-flag" onClick={() => onFlag(rv.id)} title="檢舉" />
                  </div>
                  {rv.title && <p className="review-title">{rv.title}</p>}
                  <p className="review-content">{rv.content}</p>
                  {rv.imageUrls.length > 0 && (
                    <div className="review-images">
                      {rv.imageUrls.map((u, i) => (
                        <div key={i} className="review-image" style={{ backgroundImage: `url(${u})` }} />
                      ))}
                    </div>
                  )}
                  {rv.tags.length > 0 && (
                    <div className="review-tags">
                      {rv.tags.map((t) => (
                        <span key={t.code} className="mini-tag">
                          {t.label}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>

            <div style={{ height: 96 }} />
          </>
        )}
      </IonContent>

      <div className="cta-bar">
        <IonButton expand="block" className="cta-btn" routerLink={`/restaurant/${id}/review`}>
          <IonIcon icon={flash} slot="start" />
          我要踩雷
        </IonButton>
      </div>

      <IonActionSheet
        isOpen={reportId != null}
        header="檢舉這則雷評"
        buttons={[
          ...REPORT_REASONS.map((r) => ({ text: r.label, handler: () => doReport(r.code) })),
          { text: '取消', role: 'cancel' },
        ]}
        onDidDismiss={() => setReportId(null)}
      />
      <IonToast
        isOpen={!!toast}
        message={toast?.message}
        color={toast?.color}
        duration={1500}
        onDidDismiss={() => setToast(null)}
      />
    </IonPage>
  );
}

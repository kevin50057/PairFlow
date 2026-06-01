import {
  IonBackButton,
  IonBadge,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonLabel,
  IonPage,
  IonSegment,
  IonSegmentButton,
  IonSpinner,
  IonTitle,
  IonToast,
  IonToolbar,
  useIonRouter,
  useIonViewWillEnter,
} from '@ionic/react';
import { eyeOffOutline, eyeOutline, checkmarkCircleOutline, closeCircleOutline, openOutline } from 'ionicons/icons';
import { useCallback, useEffect, useState } from 'react';
import { getAdminReports, hideReview, resolveReport, restoreReview } from '../api/admin';
import { ApiError } from '../api/client';
import { AdminReportItem, reportReasonLabel } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { timeAgo } from '../lib/format';

const FILTERS = [
  { value: 'PENDING', label: '待處理' },
  { value: 'RESOLVED', label: '已處理' },
  { value: 'REJECTED', label: '已駁回' },
  { value: 'ALL', label: '全部' },
];

const REPORT_STATUS_LABEL: Record<string, string> = {
  PENDING: '待處理',
  RESOLVED: '已處理',
  REJECTED: '已駁回',
};

export default function AdminReportsPage() {
  const router = useIonRouter();
  const { user, ready } = useAuth();
  const [status, setStatus] = useState('PENDING');
  const [items, setItems] = useState<AdminReportItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState<{ message: string; color: string } | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await getAdminReports(status === 'ALL' ? undefined : status);
      setItems(res.items);
    } catch (e) {
      setToast({ message: e instanceof ApiError ? e.message : '載入失敗', color: 'danger' });
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [status]);

  useIonViewWillEnter(() => {
    if (ready && (!user || user.role !== 'ADMIN')) {
      router.push('/home', 'root', 'replace');
      return;
    }
    void load();
  });

  useEffect(() => {
    if (user?.role === 'ADMIN') void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status]);

  const act = async (fn: () => Promise<unknown>, okMessage: string) => {
    try {
      await fn();
      setToast({ message: okMessage, color: 'success' });
      await load();
    } catch (e) {
      setToast({ message: e instanceof ApiError ? e.message : '操作失敗', color: 'danger' });
    }
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonButtons slot="start">
            <IonBackButton defaultHref="/profile" text="" />
          </IonButtons>
          <IonTitle>管理後台 · 檢舉審核</IonTitle>
        </IonToolbar>
        <IonToolbar>
          <IonSegment value={status} scrollable onIonChange={(e) => setStatus(String(e.detail.value ?? 'PENDING'))}>
            {FILTERS.map((f) => (
              <IonSegmentButton key={f.value} value={f.value}>
                <IonLabel>{f.label}</IonLabel>
              </IonSegmentButton>
            ))}
          </IonSegment>
        </IonToolbar>
      </IonHeader>

      <IonContent className="admin-content">
        <div className="admin-wrap">
          {loading && (
            <div className="center-pad">
              <IonSpinner name="crescent" />
            </div>
          )}
          {!loading && items.length === 0 && (
            <div className="empty-state">
              <IonIcon icon={checkmarkCircleOutline} />
              <p>目前沒有{status === 'ALL' ? '' : REPORT_STATUS_LABEL[status]}的檢舉</p>
            </div>
          )}

          {items.map((r) => {
            const review = r.target;
            const hidden = review?.status === 'HIDDEN';
            return (
              <div key={r.id} className="admin-card">
                <div className="admin-card-head">
                  <span className="admin-reason">{reportReasonLabel(r.reasonCode)}</span>
                  <IonBadge color={r.status === 'PENDING' ? 'warning' : r.status === 'RESOLVED' ? 'success' : 'medium'}>
                    {REPORT_STATUS_LABEL[r.status] ?? r.status}
                  </IonBadge>
                  <span className="admin-time">{timeAgo(r.createdAt)}</span>
                </div>

                <p className="admin-meta">
                  檢舉者：使用者 #{r.reporterUserId}
                  {r.note ? ` · 補充：${r.note}` : ''}
                </p>

                {review ? (
                  <div className={`admin-review${hidden ? ' hidden' : ''}`}>
                    <div className="admin-review-head">
                      <strong>{review.author.nickname}</strong>
                      <IonBadge color={hidden ? 'danger' : 'success'}>{hidden ? '已隱藏' : '顯示中'}</IonBadge>
                      <IonButton
                        fill="clear"
                        size="small"
                        onClick={() => router.push(`/restaurant/${review.restaurantId}`, 'forward')}
                      >
                        <IonIcon icon={openOutline} slot="icon-only" />
                      </IonButton>
                    </div>
                    <p className="admin-review-content">{review.content}</p>
                  </div>
                ) : (
                  <p className="admin-meta muted">（評論已不存在）</p>
                )}

                <div className="admin-actions">
                  {review &&
                    (hidden ? (
                      <IonButton size="small" fill="outline" onClick={() => act(() => restoreReview(review.id), '已復原評論')}>
                        <IonIcon icon={eyeOutline} slot="start" />
                        復原評論
                      </IonButton>
                    ) : (
                      <IonButton size="small" color="danger" onClick={() => act(() => hideReview(review.id), '已隱藏評論')}>
                        <IonIcon icon={eyeOffOutline} slot="start" />
                        隱藏評論
                      </IonButton>
                    ))}

                  {r.status === 'PENDING' && (
                    <>
                      <IonButton size="small" fill="clear" color="success" onClick={() => act(() => resolveReport(r.id, 'RESOLVED'), '已標記為處理完成')}>
                        <IonIcon icon={checkmarkCircleOutline} slot="start" />
                        標記已處理
                      </IonButton>
                      <IonButton size="small" fill="clear" color="medium" onClick={() => act(() => resolveReport(r.id, 'REJECTED'), '已駁回此檢舉')}>
                        <IonIcon icon={closeCircleOutline} slot="start" />
                        駁回
                      </IonButton>
                    </>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </IonContent>

      <IonToast
        isOpen={!!toast}
        message={toast?.message}
        color={toast?.color}
        duration={1400}
        onDidDismiss={() => setToast(null)}
      />
    </IonPage>
  );
}

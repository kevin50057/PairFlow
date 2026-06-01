import {
  IonBackButton,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonInput,
  IonPage,
  IonSpinner,
  IonTextarea,
  IonTitle,
  IonToast,
  IonToolbar,
  useIonRouter,
} from '@ionic/react';
import { cameraOutline, closeCircle, flash, flashOutline } from 'ionicons/icons';
import { ChangeEvent, useEffect, useRef, useState } from 'react';
import { useLocation, useParams } from 'react-router';
import { ApiError } from '../api/client';
import { createReview, getReview, getReviewTags, updateReview, uploadReviewImage } from '../api/restaurants';
import type { ReviewTag } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import TagChips from '../components/TagChips';
import { colorForScore } from '../components/ThunderScore';

const SCORE_DESC: Record<number, string> = {
  1: '勉強可以',
  2: '有點失望',
  3: '普通偏雷',
  4: '非常雷',
  5: '重大地雷',
};

const MAX_TAGS = 5;
const MAX_IMAGES = 3;
const MIN_CONTENT = 10;
const MAX_CONTENT = 1000;

export default function WriteReviewPage({ mode }: { mode: 'create' | 'edit' }) {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const router = useIonRouter();
  const { user, ready } = useAuth();
  const editing = mode === 'edit';

  const [restaurantId, setRestaurantId] = useState(editing ? '' : id);
  const [tags, setTags] = useState<ReviewTag[]>([]);
  const [score, setScore] = useState(0);
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [content, setContent] = useState('');
  const [title, setTitle] = useState('');
  const [images, setImages] = useState<string[]>([]);
  const [loadingExisting, setLoadingExisting] = useState(editing);
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [toast, setToast] = useState<{ message: string; color: string } | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Auth gate + initial data load.
  useEffect(() => {
    if (!ready) return;
    if (!user) {
      router.push(`/login?redirect=${encodeURIComponent(location.pathname)}`, 'root', 'replace');
      return;
    }
    getReviewTags()
      .then(setTags)
      .catch(() => undefined);

    if (editing) {
      setLoadingExisting(true);
      getReview(id)
        .then((rv) => {
          setRestaurantId(String(rv.restaurantId));
          setScore(rv.thunderScore);
          setSelectedTags(rv.tags.map((t) => t.code));
          setContent(rv.content);
          setTitle(rv.title ?? '');
          setImages(rv.imageUrls);
        })
        .catch((e) => setToast({ message: e instanceof ApiError ? e.message : '載入失敗', color: 'danger' }))
        .finally(() => setLoadingExisting(false));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ready, user, editing, id]);

  const toggleTag = (code: string) =>
    setSelectedTags((prev) =>
      prev.includes(code) ? prev.filter((c) => c !== code) : prev.length < MAX_TAGS ? [...prev, code] : prev,
    );

  const onPickImages = async (e: ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files ?? []);
    e.target.value = '';
    const room = MAX_IMAGES - images.length;
    if (room <= 0) return;
    setUploading(true);
    try {
      for (const file of files.slice(0, room)) {
        const { url } = await uploadReviewImage(file);
        setImages((prev) => [...prev, url]);
      }
    } catch (err) {
      setToast({ message: err instanceof ApiError ? err.message : '圖片上傳失敗', color: 'danger' });
    } finally {
      setUploading(false);
    }
  };

  const trimmed = content.trim();
  const canSubmit =
    score >= 1 &&
    selectedTags.length >= 1 &&
    trimmed.length >= MIN_CONTENT &&
    trimmed.length <= MAX_CONTENT &&
    !submitting &&
    !uploading;

  const submit = async () => {
    if (!canSubmit) return;
    setSubmitting(true);
    const payload = {
      thunderScore: score,
      content: trimmed,
      title: title.trim() || undefined,
      tagCodes: selectedTags,
      imageUrls: images,
    };
    try {
      if (editing) {
        await updateReview(id, payload);
        setToast({ message: '已更新雷評！', color: 'success' });
        window.setTimeout(() => router.push('/profile/reviews', 'back', 'pop'), 700);
      } else {
        await createReview(restaurantId, payload);
        setToast({ message: '雷評已送出，感謝你的避雷貢獻！', color: 'success' });
        window.setTimeout(() => router.push(`/restaurant/${restaurantId}`, 'back', 'pop'), 700);
      }
    } catch (e) {
      setToast({ message: e instanceof ApiError ? e.message : '送出失敗，請稍後再試', color: 'danger' });
      setSubmitting(false);
    }
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonButtons slot="start">
            <IonBackButton defaultHref={editing ? '/profile/reviews' : `/restaurant/${id}`} text="取消" />
          </IonButtons>
          <IonTitle>{editing ? '編輯雷評' : '我要踩雷'}</IonTitle>
        </IonToolbar>
      </IonHeader>

      <IonContent className="write-content">
        {loadingExisting ? (
          <div className="center-pad">
            <IonSpinner name="crescent" />
          </div>
        ) : (
          <>
            <section className="write-block">
              <h3>這家有多雷？</h3>
              <div className="score-picker">
                {[1, 2, 3, 4, 5].map((n) => {
                  const active = score >= n;
                  return (
                    <button
                      key={n}
                      type="button"
                      className={`score-btn${score === n ? ' current' : ''}`}
                      onClick={() => setScore(n)}
                      aria-label={`${n} 雷`}
                    >
                      <IonIcon icon={active ? flash : flashOutline} style={{ color: active ? colorForScore(score) : '#c9c9d0' }} />
                      <span>{n}</span>
                    </button>
                  );
                })}
              </div>
              <p className="score-desc" style={{ color: score ? colorForScore(score) : '#8a8a92' }}>
                {score ? `${score} 雷 · ${SCORE_DESC[score]}` : '點選 1～5 雷'}
              </p>
            </section>

            <section className="write-block">
              <h3>
                踩到什麼雷？ <span className="muted">（{selectedTags.length}/{MAX_TAGS}）</span>
              </h3>
              <TagChips options={tags} selected={selectedTags} onToggle={toggleTag} max={MAX_TAGS} />
            </section>

            <section className="write-block">
              <h3>說說你的經驗</h3>
              <IonTextarea
                className="write-textarea"
                value={content}
                onIonInput={(e) => setContent(e.detail.value ?? '')}
                placeholder="食物、服務、環境、價格…分享真實踩雷經驗（至少 10 字）"
                autoGrow
                rows={4}
                maxlength={MAX_CONTENT}
                counter
              />
            </section>

            <section className="write-block">
              <h3>
                照片 <span className="muted">（選填，最多 {MAX_IMAGES} 張）</span>
              </h3>
              <div className="image-row">
                {images.map((url, i) => (
                  <div key={i} className="image-thumb" style={{ backgroundImage: `url(${url})` }}>
                    <IonIcon
                      icon={closeCircle}
                      className="image-remove"
                      onClick={() => setImages((prev) => prev.filter((_, idx) => idx !== i))}
                    />
                  </div>
                ))}
                {images.length < MAX_IMAGES && (
                  <button type="button" className="image-add" onClick={() => fileInputRef.current?.click()} disabled={uploading}>
                    {uploading ? <IonSpinner name="dots" /> : <IonIcon icon={cameraOutline} />}
                  </button>
                )}
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png,image/webp"
                multiple
                hidden
                onChange={onPickImages}
              />
            </section>

            <section className="write-block">
              <h3>
                標題 <span className="muted">（選填）</span>
              </h3>
              <IonInput
                className="write-input"
                value={title}
                onIonInput={(e) => setTitle(e.detail.value ?? '')}
                placeholder="例如：大雷、避雷、地雷店"
                maxlength={100}
              />
            </section>

            <div style={{ height: 100 }} />
          </>
        )}
      </IonContent>

      <div className="cta-bar">
        <IonButton expand="block" className="cta-btn" disabled={!canSubmit} onClick={submit}>
          {submitting ? (
            <IonSpinner name="dots" />
          ) : (
            <>
              <IonIcon icon={flash} slot="start" />
              {editing ? '儲存修改' : '送出雷評'}
            </>
          )}
        </IonButton>
      </div>

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

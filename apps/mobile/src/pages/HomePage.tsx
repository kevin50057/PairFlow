import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonIcon,
  IonPage,
  IonRefresher,
  IonRefresherContent,
  IonSearchbar,
  IonSpinner,
  IonText,
  IonTitle,
  IonToolbar,
} from '@ionic/react';
import { flashOutline, personCircleOutline, warningOutline } from 'ionicons/icons';
import { useEffect, useState } from 'react';
import { getNearby, searchRestaurants } from '../api/restaurants';
import type { RestaurantCard as RestaurantCardModel } from '../api/types';
import RestaurantCard from '../components/RestaurantCard';
import { useGeolocation } from '../hooks/useGeolocation';

const NEARBY_RADIUS_M = 1500;

export default function HomePage() {
  const { coords, status, usingFallback, locate } = useGeolocation();
  const [query, setQuery] = useState('');
  const [debounced, setDebounced] = useState('');
  const [items, setItems] = useState<RestaurantCardModel[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  // Debounce the search box.
  useEffect(() => {
    const t = window.setTimeout(() => setDebounced(query.trim()), 300);
    return () => window.clearTimeout(t);
  }, [query]);

  // Load whenever the search term, location, or refresh trigger changes.
  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError(null);
      try {
        const res = debounced
          ? await searchRestaurants(debounced)
          : await getNearby(coords.lat, coords.lng, NEARBY_RADIUS_M);
        if (!cancelled) setItems(res.items);
      } catch (e) {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : '載入失敗');
          setItems([]);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [debounced, coords.lat, coords.lng, refreshKey]);

  const searching = debounced.length > 0;

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>
            <span className="brand">⚡ 雷評</span>
          </IonTitle>
          <IonButtons slot="end">
            <IonButton routerLink="/profile" aria-label="會員中心">
              <IonIcon slot="icon-only" icon={personCircleOutline} />
            </IonButton>
          </IonButtons>
        </IonToolbar>
        <IonToolbar>
          <IonSearchbar
            value={query}
            debounce={0}
            placeholder="搜尋餐廳名稱…"
            onIonInput={(e) => setQuery(e.detail.value ?? '')}
            onIonClear={() => setQuery('')}
          />
        </IonToolbar>
      </IonHeader>

      <IonContent className="home-content">
        <IonRefresher
          slot="fixed"
          onIonRefresh={async (e) => {
            if (!debounced) await locate();
            setRefreshKey((k) => k + 1);
            e.detail.complete();
          }}
        >
          <IonRefresherContent />
        </IonRefresher>

        {usingFallback && !searching && (
          <div className="banner banner-warn">
            <IonIcon icon={warningOutline} />
            無法取得定位，已顯示台北市中心，建議改用搜尋
          </div>
        )}

        <p className="section-hint">
          {searching
            ? `搜尋「${debounced}」`
            : status === 'loading'
              ? '定位中…'
              : usingFallback
                ? '附近餐廳（預設位置）'
                : '你附近的踩雷名單'}
        </p>

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

        {!loading && !error && items.length === 0 && (
          <div className="empty-state">
            <IonIcon icon={flashOutline} />
            <p>{searching ? '找不到符合的餐廳' : '附近目前沒有資料，試試搜尋吧'}</p>
          </div>
        )}

        <div className="card-list">
          {items.map((r) => (
            <RestaurantCard key={r.id} restaurant={r} />
          ))}
        </div>
      </IonContent>
    </IonPage>
  );
}

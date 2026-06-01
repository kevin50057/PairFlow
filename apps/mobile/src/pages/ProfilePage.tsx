import {
  IonBadge,
  IonButton,
  IonContent,
  IonHeader,
  IonIcon,
  IonInput,
  IonPage,
  IonSpinner,
  IonTitle,
  IonToolbar,
  useIonRouter,
} from '@ionic/react';
import { createOutline, listOutline, logOutOutline, personCircleOutline, shieldCheckmarkOutline } from 'ionicons/icons';
import { useState } from 'react';
import { useAuth } from '../auth/AuthContext';

export default function ProfilePage() {
  const router = useIonRouter();
  const { user, ready, logout, updateProfile } = useAuth();
  const [editing, setEditing] = useState(false);
  const [nickname, setNickname] = useState('');
  const [busy, setBusy] = useState(false);

  if (!ready) {
    return (
      <IonPage>
        <IonContent>
          <div className="center-pad">
            <IonSpinner name="crescent" />
          </div>
        </IonContent>
      </IonPage>
    );
  }

  if (!user) {
    return (
      <IonPage>
        <IonHeader>
          <IonToolbar>
            <IonTitle>我的</IonTitle>
          </IonToolbar>
        </IonHeader>
        <IonContent>
          <div className="profile-guest">
            <IonIcon icon={personCircleOutline} />
            <h2>尚未登入</h2>
            <p>登入後即可踩雷、檢舉與管理你的評論</p>
            <IonButton expand="block" onClick={() => router.push('/login', 'forward')}>
              登入 / 註冊
            </IonButton>
          </div>
        </IonContent>
      </IonPage>
    );
  }

  const startEdit = () => {
    setNickname(user.nickname);
    setEditing(true);
  };
  const save = async () => {
    setBusy(true);
    try {
      await updateProfile({ nickname: nickname.trim() });
      setEditing(false);
    } finally {
      setBusy(false);
    }
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>我的</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent className="profile-content">
        <div className="profile-card">
          <div className="profile-avatar">{user.nickname.slice(0, 1)}</div>
          {editing ? (
            <div className="profile-edit">
              <IonInput
                className="auth-input"
                value={nickname}
                onIonInput={(e) => setNickname(e.detail.value ?? '')}
                maxlength={30}
              />
              <div className="profile-edit-actions">
                <IonButton size="small" fill="clear" onClick={() => setEditing(false)}>
                  取消
                </IonButton>
                <IonButton size="small" disabled={busy || !nickname.trim()} onClick={save}>
                  {busy ? <IonSpinner name="dots" /> : '儲存'}
                </IonButton>
              </div>
            </div>
          ) : (
            <h2 className="profile-name">
              {user.nickname}
              <IonIcon icon={createOutline} onClick={startEdit} />
            </h2>
          )}
          <p className="profile-email">{user.email}</p>
          {user.role === 'ADMIN' && <IonBadge color="secondary">管理員</IonBadge>}
        </div>

        <div className="profile-actions">
          <IonButton expand="block" fill="outline" onClick={() => router.push('/profile/reviews', 'forward')}>
            <IonIcon icon={listOutline} slot="start" />
            我的雷評
          </IonButton>
          {user.role === 'ADMIN' && (
            <IonButton
              expand="block"
              color="secondary"
              onClick={() => router.push('/admin/reports', 'forward')}
            >
              <IonIcon icon={shieldCheckmarkOutline} slot="start" />
              管理後台
            </IonButton>
          )}
          <IonButton expand="block" color="medium" fill="clear" onClick={logout}>
            <IonIcon icon={logOutOutline} slot="start" />
            登出
          </IonButton>
        </div>
      </IonContent>
    </IonPage>
  );
}

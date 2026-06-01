import {
  IonBackButton,
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonInput,
  IonLabel,
  IonPage,
  IonSegment,
  IonSegmentButton,
  IonSpinner,
  IonTitle,
  IonToast,
  IonToolbar,
  useIonRouter,
} from '@ionic/react';
import { useState } from 'react';
import { useLocation } from 'react-router';
import { ApiError } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export default function LoginPage() {
  const router = useIonRouter();
  const location = useLocation();
  const { login, register } = useAuth();

  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [busy, setBusy] = useState(false);
  const [toast, setToast] = useState<string | null>(null);

  const redirect = new URLSearchParams(location.search).get('redirect') || '/profile';
  const isLogin = mode === 'login';
  const canSubmit =
    /\S+@\S+\.?\S*/.test(email) &&
    password.length >= (isLogin ? 1 : 8) &&
    (isLogin || nickname.trim().length >= 1) &&
    !busy;

  const submit = async () => {
    if (!canSubmit) return;
    setBusy(true);
    try {
      if (isLogin) await login(email.trim(), password);
      else await register(email.trim(), password, nickname.trim());
      router.push(redirect, 'root', 'replace');
    } catch (e) {
      setToast(e instanceof ApiError ? e.message : '操作失敗，請稍後再試');
      setBusy(false);
    }
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonButtons slot="start">
            <IonBackButton defaultHref="/home" text="" />
          </IonButtons>
          <IonTitle>會員</IonTitle>
        </IonToolbar>
      </IonHeader>

      <IonContent className="auth-content">
        <div className="auth-hero">
          <div className="auth-logo">⚡</div>
          <h1>雷評</h1>
          <p>登入後即可踩雷、檢舉與管理你的評論</p>
        </div>

        <IonSegment
          className="auth-segment"
          value={mode}
          onIonChange={(e) => setMode((e.detail.value as 'login' | 'register') ?? 'login')}
        >
          <IonSegmentButton value="login">
            <IonLabel>登入</IonLabel>
          </IonSegmentButton>
          <IonSegmentButton value="register">
            <IonLabel>註冊</IonLabel>
          </IonSegmentButton>
        </IonSegment>

        <div className="auth-form">
          <IonInput
            className="auth-input"
            label="Email"
            labelPlacement="stacked"
            type="email"
            autocomplete="email"
            value={email}
            onIonInput={(e) => setEmail(e.detail.value ?? '')}
            placeholder="you@example.com"
          />
          {!isLogin && (
            <IonInput
              className="auth-input"
              label="暱稱"
              labelPlacement="stacked"
              value={nickname}
              onIonInput={(e) => setNickname(e.detail.value ?? '')}
              placeholder="你的顯示名稱"
              maxlength={30}
            />
          )}
          <IonInput
            className="auth-input"
            label="密碼"
            labelPlacement="stacked"
            type="password"
            value={password}
            onIonInput={(e) => setPassword(e.detail.value ?? '')}
            placeholder={isLogin ? '輸入密碼' : '至少 8 碼，含英文與數字'}
          />

          <IonButton expand="block" className="auth-submit" disabled={!canSubmit} onClick={submit}>
            {busy ? <IonSpinner name="dots" /> : isLogin ? '登入' : '建立帳號'}
          </IonButton>

          {isLogin && <p className="auth-hint">測試帳號：user1@thunder.test / Password123!</p>}
        </div>
      </IonContent>

      <IonToast isOpen={!!toast} message={toast ?? ''} color="danger" duration={1800} onDidDismiss={() => setToast(null)} />
    </IonPage>
  );
}

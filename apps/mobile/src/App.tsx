import { IonApp, IonRouterOutlet } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import { Redirect, Route } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import AdminReportsPage from './pages/AdminReportsPage';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import MyReviewsPage from './pages/MyReviewsPage';
import ProfilePage from './pages/ProfilePage';
import RestaurantDetailPage from './pages/RestaurantDetailPage';
import WriteReviewPage from './pages/WriteReviewPage';

export default function App() {
  return (
    <IonApp>
      <AuthProvider>
        <IonReactRouter>
          <IonRouterOutlet>
            <Route exact path="/home" component={HomePage} />
            <Route exact path="/login" component={LoginPage} />
            <Route exact path="/profile" component={ProfilePage} />
            <Route exact path="/profile/reviews" component={MyReviewsPage} />
            <Route exact path="/admin/reports" component={AdminReportsPage} />
            <Route exact path="/restaurant/:id" component={RestaurantDetailPage} />
            <Route exact path="/restaurant/:id/review" render={() => <WriteReviewPage mode="create" />} />
            <Route exact path="/reviews/:id/edit" render={() => <WriteReviewPage mode="edit" />} />
            <Route exact path="/">
              <Redirect to="/home" />
            </Route>
          </IonRouterOutlet>
        </IonReactRouter>
      </AuthProvider>
    </IonApp>
  );
}

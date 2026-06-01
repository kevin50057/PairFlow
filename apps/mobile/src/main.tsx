import { setupIonicReact } from '@ionic/react';
import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';

/* Core Ionic CSS — required for components to work */
import '@ionic/react/css/core.css';
import '@ionic/react/css/normalize.css';
import '@ionic/react/css/structure.css';
import '@ionic/react/css/typography.css';

/* Optional Ionic utilities */
import '@ionic/react/css/flex-utils.css';
import '@ionic/react/css/padding.css';
import '@ionic/react/css/text-alignment.css';

/* App theme */
import './theme/variables.css';
import './theme/app.css';

setupIonicReact({ mode: 'ios' });

const container = document.getElementById('root');
if (!container) throw new Error('Root element #root not found');

createRoot(container).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);

import React from 'react';
import ReactDOM from 'react-dom/client';
import { TradingProvider } from './context/TradingContext';
import { AppContent } from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <TradingProvider>
      <AppContent />
    </TradingProvider>
  </React.StrictMode>
);

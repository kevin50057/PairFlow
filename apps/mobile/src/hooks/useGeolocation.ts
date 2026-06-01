import { Geolocation } from '@capacitor/geolocation';
import { useCallback, useEffect, useState } from 'react';

export interface Coords {
  lat: number;
  lng: number;
}

// Fallback location (Taipei 101) used when permission is denied or unavailable.
export const DEFAULT_COORDS: Coords = { lat: 25.033, lng: 121.5654 };

export type GeoStatus = 'idle' | 'loading' | 'granted' | 'denied';

export interface GeolocationState {
  coords: Coords;
  status: GeoStatus;
  usingFallback: boolean;
  locate: () => Promise<void>;
}

/**
 * Resolves the device location via Capacitor Geolocation (works on web + iOS).
 * If permission is denied or times out, falls back to DEFAULT_COORDS and flags
 * `usingFallback` so the UI can prompt the user to search instead.
 */
export function useGeolocation(): GeolocationState {
  const [coords, setCoords] = useState<Coords>(DEFAULT_COORDS);
  const [status, setStatus] = useState<GeoStatus>('idle');
  const [usingFallback, setUsingFallback] = useState(false);

  const locate = useCallback(async () => {
    setStatus('loading');
    try {
      const position = await Geolocation.getCurrentPosition({
        enableHighAccuracy: true,
        timeout: 8000,
        maximumAge: 60_000,
      });
      setCoords({ lat: position.coords.latitude, lng: position.coords.longitude });
      setUsingFallback(false);
      setStatus('granted');
    } catch {
      setCoords(DEFAULT_COORDS);
      setUsingFallback(true);
      setStatus('denied');
    }
  }, []);

  useEffect(() => {
    void locate();
  }, [locate]);

  return { coords, status, usingFallback, locate };
}

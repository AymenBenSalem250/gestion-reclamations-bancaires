/***************************************************************************************************
 * Polyfills pour compatibilité Node libs (SockJS / STOMP)
 **************************************************************************************************/

// ✅ Fix pour "global is not defined"
(window as any).global = window;

// ✅ Optionnel (si erreur process plus tard)
(window as any).process = {
  env: { DEBUG: undefined }
};
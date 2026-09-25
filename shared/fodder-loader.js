// Shared Fodder bootstrap used by the desktop and Android shells.
// The loader stays small on purpose: Fodder's current client is fetched from
// the official endpoint so fixes do not require a new shell build.
(async () => {
  if (window.__fcToolsFodderLoaded) return;
  window.__fcToolsFodderLoaded = true;

  const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
  const backend = "https://fodder.gg";

  try {
    const hash = String(location.hash || "");
    const token = hash.match(/(?:^#|&)token=([^&]+)/)?.[1];
    if (token) {
      localStorage.setItem("fodder_gg_token", decodeURIComponent(token));
      history.replaceState(null, "", location.pathname + location.search);
    }
  } catch (_) {}

  for (let attempt = 0; attempt < 120; attempt += 1) {
    if (window.services && window.repositories) break;
    await sleep(500);
  }

  if (!window.services || !window.repositories) return;

  try {
    const source = await fetch(`${backend}/client.core.js`, { cache: "no-store" }).then((response) => {
      if (!response.ok) throw new Error(`Fodder client returned ${response.status}`);
      return response.text();
    });
    (0, eval)(source);
    console.info("[fc-tools] Fodder client loaded from the official endpoint");
  } catch (error) {
    console.warn("[fc-tools] Fodder client could not be loaded", error);
  }
})();

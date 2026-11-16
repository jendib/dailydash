export function createDeploymentChecker(currentVersion, {
  fetchVersion = globalThis.fetch,
  reload = () => window.location.reload(),
} = {}) {
  let checking = false
  let reloading = false
  return async function checkDeployment() {
    if (checking || reloading) return
    checking = true
    try {
      // Same origin as the frontend, independently of the private API's base URL.
      const response = await fetchVersion(`/version.json?t=${Date.now()}`, {
        cache: 'no-store', signal: AbortSignal.timeout(10000),
      })
      if (!response.ok) return
      const { version } = await response.json()
      if (typeof version !== 'string' || !/^[0-9a-f-]{36}$/i.test(version)) return
      if (version !== currentVersion) {
        reloading = true
        reload()
      }
    } catch {
      // A deployment or network interruption must not discard the working dashboard.
      // The next scheduled check retries automatically.
    } finally {
      checking = false
    }
  }
}

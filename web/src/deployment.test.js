import test from 'node:test'
import assert from 'node:assert/strict'
import { createDeploymentChecker } from './deployment.js'

const current = '11111111-1111-4111-8111-111111111111'
const next = '22222222-2222-4222-8222-222222222222'

test('keeps the current build, then reloads exactly once when a new version is deployed', async () => {
  let version = current
  let reloads = 0
  const check = createDeploymentChecker(current, {
    fetchVersion: async (url, options) => {
      assert.ok(url.startsWith('/version.json?'))
      assert.equal(options.cache, 'no-store')
      return { ok: true, json: async () => ({ version }) }
    },
    reload: () => reloads++,
  })
  await check()
  assert.equal(reloads, 0)
  version = next
  await check()
  await check()
  assert.equal(reloads, 1)
})

test('ignores outages and invalid responses, and retries on the next check', async () => {
  const responses = [
    () => { throw new Error('offline') },
    () => ({ ok: false }),
    () => ({ ok: true, json: async () => { throw new Error('HTML fallback') } }),
    () => ({ ok: true, json: async () => ({ version: 'invalid' }) }),
    () => ({ ok: true, json: async () => ({ version: next }) }),
  ]
  let reloads = 0
  const check = createDeploymentChecker(current, { fetchVersion: async () => responses.shift()(), reload: () => reloads++ })
  for (let i = 0; i < 4; i++) { await check(); assert.equal(reloads, 0) }
  await check()
  assert.equal(reloads, 1)
})

test('does not overlap pending checks', async () => {
  let finish
  let requests = 0
  const check = createDeploymentChecker(current, { fetchVersion: () => { requests++; return new Promise(resolve => { finish = resolve }) } })
  const pending = check()
  await check()
  assert.equal(requests, 1)
  finish({ ok: false })
  await pending
})

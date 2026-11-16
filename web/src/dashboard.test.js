import test from 'node:test'
import assert from 'node:assert/strict'
import { formatEventRange, hourlyRainForecast, weatherCondition } from './dashboard.js'

test('hourly rain uses the matching accumulation interval, including midnight and missing data', () => {
  const end = Date.parse('2026-11-16T23:00:00Z') / 1000
  const hourly = { time: [end - 3600, end, end + 3600], rain: [0, 0.4, null], showers: [0, 0.2, 0] }
  const result = hourlyRainForecast(hourly, Date.parse('2026-11-16T22:30:00Z'))
  assert.equal(result.length, 2)
  assert.equal(result[0].label, '23h')
  assert.equal(result[0].range, '23:00–00:00')
  assert.ok(Math.abs(result[0].amount - 0.6) < 0.00001)
  assert.equal(result[1].label, '00h')
  assert.equal(result[1].amount, null)
  assert.equal(hourlyRainForecast(hourly, end * 1000, 1)[0].label, '00h')
  assert.deepEqual(hourlyRainForecast(null), [])
})

test('hourly rain stays aligned across daylight saving time and limits the displayed hours', () => {
  const end = Date.parse('2026-10-25T01:00:00Z') / 1000
  const result = hourlyRainForecast({ time: [end, end + 3600], rain: [0, 1], showers: [0, 0] }, (end - 1800) * 1000, 1)
  assert.equal(result.length, 1)
  assert.equal(result[0].range, '02:00–02:00')
  assert.equal(result[0].amount, 0)
})

test('calendar handles inclusive all-day dates and timed ranges in Paris', () => {
  assert.equal(formatEventRange('2026-11-15', '2026-11-15'), '15 Nov · All day')
  assert.equal(formatEventRange('2026-11-15', '2026-11-17'), '15 Nov – 17 Nov · All day')
  assert.equal(formatEventRange('2026-11-15T14:00:00Z', '2026-11-15T15:30:00Z'), '15 Nov · 15:00–16:30')
  assert.equal(formatEventRange('2026-07-15T23:00:00Z', null), '16 Jul · 01:00')
  assert.equal(formatEventRange(null, null), 'Date to be confirmed')
  assert.equal(formatEventRange('invalid', null), 'Date to be confirmed')
})

test('weather distinguishes clear days and nights and all WMO condition groups', () => {
  assert.deepEqual(weatherCondition(0, 0), { kind: 'clear', label: 'Clear night', daylight: false })
  assert.equal(weatherCondition(0, 1).label, 'Clear skies')
  for (const [codes, kind] of [ [[1, 2], 'partly'], [[3], 'cloud'], [[45, 48], 'fog'], [[51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82], 'rain'], [[71, 73, 75, 77, 85, 86], 'snow'], [[95, 96, 99], 'storm'] ]) {
    for (const code of codes) assert.equal(weatherCondition(code, 0).kind, kind)
  }
  assert.equal(weatherCondition(null, null).kind, 'unknown')
  assert.equal(weatherCondition(100, 1).kind, 'unknown')
})

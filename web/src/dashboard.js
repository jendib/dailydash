const zone = 'Europe/Paris'
const dayFormat = new Intl.DateTimeFormat('en-GB', { day: 'numeric', month: 'short', timeZone: zone })
const timeFormat = new Intl.DateTimeFormat('en-GB', { hour: '2-digit', minute: '2-digit', timeZone: zone })

export function hourlyRainForecast(hourly, now = Date.now(), count = 6) {
  if (!Array.isArray(hourly?.time)) return []
  return hourly.time.flatMap((end, index) => {
    // Open-Meteo hourly accumulations describe the hour ENDING at this timestamp.
    const endTime = Number(end) * 1000
    if (!Number.isFinite(endTime) || endTime <= now) return []
    const start = new Date(endTime - 3600000)
    const rain = hourly.rain?.[index]
    const showers = hourly.showers?.[index]
    const amount = rain == null || showers == null ? null : rain + showers
    return [{
      time: endTime,
      label: `${new Intl.DateTimeFormat('en-GB', { hour: '2-digit', hourCycle: 'h23', timeZone: zone }).format(start)}h`,
      range: `${timeFormat.format(start)}–${timeFormat.format(new Date(endTime))}`,
      amount,
    }]
  }).slice(0, count)
}

export function parseEventDate(value) {
  if (!value) return null
  // Date-only values are calendar dates, not UTC instants.
  const date = new Date(/^\d{4}-\d{2}-\d{2}$/.test(value) ? `${value}T12:00:00+01:00` : value)
  return Number.isNaN(date.getTime()) ? null : date
}

export function formatEventRange(start, end) {
  const first = parseEventDate(start)
  const last = parseEventDate(end)
  if (!first) return 'Date to be confirmed'
  const allDay = /^\d{4}-\d{2}-\d{2}$/.test(start)
  const firstDay = dayFormat.format(first)
  const sameDay = last && new Intl.DateTimeFormat('en-CA', { timeZone: zone }).format(first) === new Intl.DateTimeFormat('en-CA', { timeZone: zone }).format(last)
  if (allDay) return !last || sameDay ? `${firstDay} · All day` : `${firstDay} – ${dayFormat.format(last)} · All day`
  const from = `${firstDay} · ${timeFormat.format(first)}`
  if (!last || first.getTime() === last.getTime()) return from
  return sameDay ? `${from}–${timeFormat.format(last)}` : `${from} – ${dayFormat.format(last)} · ${timeFormat.format(last)}`
}

// Open-Meteo WMO interpretation codes: https://open-meteo.com/en/docs
export function weatherCondition(code, isDay) {
  const daylight = Number(isDay) === 1
  if (code == null) return { kind: 'unknown', label: 'Weather unavailable', daylight }
  const value = Number(code)
  if (value === 0) return { kind: 'clear', label: daylight ? 'Clear skies' : 'Clear night', daylight }
  if (value === 1 || value === 2) return { kind: 'partly', label: value === 1 ? 'Mostly clear' : 'Partly cloudy', daylight }
  if (value === 3) return { kind: 'cloud', label: 'Overcast', daylight }
  if ([45, 48].includes(value)) return { kind: 'fog', label: 'Foggy', daylight }
  if ([51, 53, 55, 56, 57].includes(value)) return { kind: 'rain', label: 'Drizzle', daylight }
  if ([61, 63, 65, 66, 67, 80, 81, 82].includes(value)) return { kind: 'rain', label: 'Rain', daylight }
  if ([71, 73, 75, 77, 85, 86].includes(value)) return { kind: 'snow', label: 'Snow', daylight }
  if ([95, 96, 99].includes(value)) return { kind: 'storm', label: 'Thunderstorms', daylight }
  return { kind: 'unknown', label: 'Weather unavailable', daylight }
}

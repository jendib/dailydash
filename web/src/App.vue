<template>
  <main class="dashboard">
    <header class="masthead">
      <div><h1>{{ greeting }}<span>.</span></h1></div>
      <div class="clock"><time>{{ clock }}</time><p>{{ today }}</p></div>
    </header>

    <div class="dashboard-grid">
      <div class="column conditions-column">
        <section class="panel weather-panel" aria-labelledby="weather-title">
          <div class="panel-heading"><h2 id="weather-title">Outside today</h2><span class="tag"><component :is="currentWeather?.is_day === 1 ? SunIcon : MoonIcon" />{{ currentWeather ? (currentWeather.is_day === 1 ? 'Daytime' : 'Nighttime') : 'Waiting' }}</span></div>
          <div class="weather-hero"><div><p class="temperature">{{ number(currentWeather?.temperature_2m) }}<span>°</span></p><p class="weather-description">{{ condition.label }}</p><p class="weather-inline">Wind <strong>{{ number(currentWeather?.wind_speed_10m) }} km/h</strong><span>Balcony <strong>{{ number(homeAssistant?.outdoorTemperature, 1) }}°C</strong></span></p></div><WeatherIcon :condition="condition" class="weather-icon" /></div>
          <div class="rain-forecast" aria-label="Hourly rain forecast">
            <span class="rain-caption">Rain<small>mm / hour</small></span>
            <div v-for="hour in rainForecast" :key="hour.time" class="rain-hour" :class="{ wet: hour.amount > 0, heavy: hour.amount >= 2 }" :title="`${hour.range}: ${rainLabel(hour.amount)}${hour.amount == null ? '' : ' · ' + hour.amount.toFixed(1) + ' mm'}`">
              <span class="rain-hour-label">{{ hour.label }}<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" role="img" :aria-label="rainLabel(hour.amount)">
                <path v-if="hour.amount == null" d="M8 12h8" />
                <circle v-else-if="hour.amount <= 0" cx="12" cy="12" r="4" />
                <template v-else><path d="M6 14a4 4 0 1 1 0-8 6 6 0 0 1 11 1 3.5 3.5 0 1 1 1 7Z" /><path d="m12 18-1 3" /><path v-if="hour.amount >= 2" d="m6 18-1 3m13-3-1 3" /></template>
              </svg></span><strong>{{ number(hour.amount, 1) }}</strong>
            </div>
            <span v-if="!rainForecast.length" class="rain-unavailable">Hourly forecast unavailable</span>
          </div>
          <p v-if="errors.weather" class="status">{{ errors.weather }}</p>
        </section>

        <section class="panel home-panel" aria-labelledby="home-title">
          <div class="panel-heading"><h2 id="home-title"><HomeIcon />At home</h2><span class="live-dot" :class="{ muted: !homeAssistant || errors.home }"></span></div>
          <div class="home-readings"><div><p class="label">Indoor</p><p class="reading">{{ number(homeAssistant?.indoorTemperature, 1) }}<small> °C</small></p></div><div><p class="label">Air quality · CO₂</p><p class="reading" :class="co2Class">{{ number(homeAssistant?.co2Level) }}<small> ppm</small></p></div></div>
          <div class="tempo-row"><div><span class="label">Tempo today</span><span class="tempo" :class="tempoClass(homeAssistant?.tempoToday)">{{ homeAssistant?.tempoToday || '—' }}</span></div><div><span class="label">Tomorrow</span><span class="tempo" :class="tempoClass(homeAssistant?.tempoTomorrow)">{{ homeAssistant?.tempoTomorrow || '—' }}</span></div></div>
          <p v-if="errors.home" class="status">{{ errors.home }}</p>
        </section>

        <section class="panel shopping-panel" aria-labelledby="shopping-title">
          <div class="panel-heading"><h2 id="shopping-title"><ShoppingBagIcon />Pick up</h2><span class="count">{{ shoppingList.length }}</span></div>
          <ul class="shopping-list"><li v-for="(item, index) in shoppingList" :key="`${index}-${item}`"><span class="check-circle"></span>{{ item }}</li></ul>
          <p v-if="!shoppingList.length" class="empty">{{ loaded.shopping ? 'All stocked up.' : 'Loading your list…' }}</p>
          <p v-if="errors.shopping" class="status">{{ errors.shopping }}</p>
        </section>
      </div>

      <section class="panel agenda-panel" aria-labelledby="agenda-title">
        <div class="panel-heading"><h2 id="agenda-title"><CalendarDaysIcon />Coming up</h2><span class="tag">Family agenda</span></div>
        <div class="agenda-list"><article v-for="(event, index) in calendar" :key="`${event.startDate}-${event.title}-${index}`" class="event">
          <div class="date-tile"><span>{{ eventMonth(event.startDate) }}</span><strong>{{ eventDay(event.startDate) }}</strong></div>
          <div class="event-content"><p class="event-date">{{ formatEventRange(event.startDate, event.endDate) }}</p><h3>{{ event.title || 'Untitled event' }}</h3><p v-if="event.description" class="event-description">{{ event.description }}</p><p v-if="event.location" class="event-location"><MapPinIcon />{{ event.location }}</p></div>
        </article></div>
        <p v-if="!calendar.length" class="empty">{{ loaded.calendar ? 'A little room to slow down. No upcoming events.' : 'Loading your agenda…' }}</p>
        <p v-if="errors.calendar" class="status">{{ errors.calendar }}</p>
      </section>

      <section class="panel photos-panel" aria-labelledby="photos-title">
        <div class="panel-heading"><div><p class="eyebrow">The good stuff</p><h2 id="photos-title">Life lately</h2></div><PhotoIcon class="photo-heading-icon" /></div>
        <div v-if="photos.length" class="photo-wall"><figure v-for="(id, index) in photos.slice(0, 9)" :key="id" :class="{ featured: index === 0 }"><img :src="photoUrl(id)" :alt="`Recent family photo ${index + 1}`" @error="onPhotoError" /></figure></div>
        <div v-else class="photo-empty"><PhotoIcon /><p>{{ loaded.photos ? 'New memories will appear here.' : 'Gathering your memories…' }}</p></div>
        <p v-if="errors.photos" class="status">{{ errors.photos }}</p>
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import axios from 'axios'
import { CalendarDaysIcon, HomeIcon, MapPinIcon, MoonIcon, PhotoIcon, ShoppingBagIcon, SunIcon } from '@heroicons/vue/24/outline'
import WeatherIcon from './components/WeatherIcon.vue'
import { createDeploymentChecker } from './deployment.js'
import { formatEventRange, hourlyRainForecast, parseEventDate, weatherCondition } from './dashboard.js'

const zone = 'Europe/Paris'
const now = ref(new Date())
const homeAssistant = ref(null)
const shoppingList = ref([])
const calendar = ref([])
const weather = ref(null)
const photos = ref([])
const errors = reactive({})
const loaded = reactive({})
const refreshing = ref(false)
const currentWeather = computed(() => weather.value?.current)
const rainForecast = computed(() => hourlyRainForecast(weather.value?.hourly, now.value.getTime()))
// Compact visual buckets, in mm/hour; exact amounts remain visible below.
const rainLabel = amount => amount == null ? 'Forecast unavailable' : amount <= 0 ? 'No rain' : amount < 2 ? 'Light rain' : 'Heavier rain'
const condition = computed(() => weatherCondition(currentWeather.value?.weather_code, currentWeather.value?.is_day))
const clock = computed(() => now.value.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit', timeZone: zone }))
const today = computed(() => now.value.toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long', timeZone: zone }))
const greeting = computed(() => {
  const hour = Number(new Intl.DateTimeFormat('en-GB', { hour: '2-digit', hourCycle: 'h23', timeZone: zone }).format(now.value))
  return hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening'
})
const co2Class = computed(() => homeAssistant.value?.co2Level >= 1500 ? 'air-high' : homeAssistant.value?.co2Level >= 1000 ? 'air-medium' : '')
const number = (value, digits = 0) => value == null || !Number.isFinite(Number(value)) ? '—' : Number(value).toFixed(digits)
const tempoClass = value => ({ bleu: 'tempo-blue', blanc: 'tempo-white', rouge: 'tempo-red' })[value?.toLowerCase()] || ''
const eventMonth = value => { const date = parseEventDate(value); return date ? date.toLocaleDateString('en-GB', { month: 'short', timeZone: zone }) : 'TBC' }
const eventDay = value => { const date = parseEventDate(value); return date ? date.toLocaleDateString('en-GB', { day: '2-digit', timeZone: zone }) : '—' }
const photoUrl = id => `${(window.API_URL || '').replace(/\/$/, '')}/photos/${encodeURIComponent(id)}/preview`
const onPhotoError = event => { event.target.style.visibility = 'hidden' }
const controller = new AbortController()
let refreshTimer, clockTimer
let deploymentTimer
const checkDeployment = import.meta.env.PROD ? createDeploymentChecker(__APP_VERSION__) : null

async function load() {
  if (refreshing.value) return
  refreshing.value = true
  const sources = [
    ['shopping', 'shopping-list', shoppingList], ['calendar', 'calendar', calendar],
    ['home', 'home-assistant', homeAssistant], ['photos', 'photos/latest', photos],
    ['weather', 'https://api.open-meteo.com/v1/forecast?latitude=45.7485&longitude=4.8467&current=precipitation,weather_code,temperature_2m,wind_speed_10m,is_day&hourly=rain,showers&forecast_days=2&timeformat=unixtime&timezone=Europe%2FParis', weather],
  ]
  await Promise.allSettled(sources.map(async ([key, url, target]) => {
    try {
      const { data } = await axios.get(url, { timeout: 20000, signal: controller.signal })
      target.value = data
      loaded[key] = true
      errors[key] = ''
    } catch (error) {
      if (!axios.isCancel(error)) errors[key] = loaded[key] ? 'Connection interrupted · showing last available data' : 'Temporarily unavailable · retrying automatically'
    }
  }))
  refreshing.value = false
}
onMounted(() => {
  load()
  if (checkDeployment) {
    checkDeployment()
    deploymentTimer = setInterval(checkDeployment, 60000)
  }
  refreshTimer = setInterval(load, 600000)
  clockTimer = setInterval(() => { now.value = new Date() }, 1000)
})
onUnmounted(() => { clearInterval(refreshTimer); clearInterval(clockTimer); clearInterval(deploymentTimer); controller.abort() })
</script>

<style scoped>
.weather-inline { display: flex; flex-wrap: wrap; gap: 4px 12px; color: #a6bac5; font-size: 10px; line-height: 18px; margin-top: 2px; }
.weather-inline strong { color: #dbe7ed; font-weight: 500; }
.rain-forecast { display: grid; grid-template-columns: 38px repeat(6, minmax(0, 1fr)); gap: 3px; padding-top: 16px; border-top: 1px solid #ffffff12; align-items: center; }
.rain-caption { color: #c5d6df; font-size: 11px; }
.rain-caption small { display: block; font-size: 8px; color: #9eb0bc; white-space: nowrap; margin-top: 4px; }
.rain-hour { text-align: center; font-variant-numeric: tabular-nums; }
.rain-hour > .rain-hour-label { display: flex; justify-content: center; align-items: center; gap: 2px; color: #a6bac5; font-size: 10px; line-height: 15px; }
.rain-hour-label svg { width: 12px; height: 12px; flex-shrink: 0; }
.rain-hour.wet .rain-hour-label svg { color: #8fd1ff; }
.rain-hour.heavy .rain-hour-label svg { color: #b4a5ff; }
.rain-hour strong { display: block; color: #b3c4ce; font-size: 14px; line-height: 20px; font-weight: 500; margin-top: 4px; }
.rain-hour.wet strong { color: #8fd1ff; }
.rain-unavailable { grid-column: 2 / -1; color: #9eb0bc; font-size: 11px; line-height: 36px; text-align: center; }
small { font-size: 11px; color: #9eb0bc; font-weight: 400; letter-spacing: 0; }
.dashboard { min-height: 100dvh; padding: clamp(14px, 1.5vw, 28px); color: #edf1f5; background: radial-gradient(ellipse at 0 0, #203138 0, transparent 45%), #10171d; font-family: 'Segoe UI', system-ui, sans-serif; }
.masthead { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.eyebrow { color: #a8bdbe; text-transform: uppercase; letter-spacing: .2em; font-size: 11px; font-weight: 600; margin: 0 0 8px; }
h1 { font-size: clamp(28px, 2.5vw, 46px); font-weight: 500; letter-spacing: -.045em; line-height: 1.15; } h1 span { color: #b5d8b6; }
.clock { text-align: right; } .clock time { font-size: clamp(30px, 3vw, 52px); font-weight: 300; letter-spacing: -.04em; font-variant-numeric: tabular-nums; line-height: 1; } .clock p { color: #a5b2bd; margin-top: 9px; font-size: 14px; }
.dashboard-grid { display: grid; grid-template-columns: minmax(270px, 1fr) minmax(290px, 1.12fr) minmax(400px, 1.8fr); gap: 16px; align-items: start; }
.column { display: grid; gap: 16px; } .panel { background: #1b252e; border: 1px solid #ffffff0c; border-radius: 22px; padding: 18px; overflow: hidden; }
.panel-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 18px; } h2 { display: flex; align-items: center; gap: 10px; font-size: 16px; font-weight: 600; letter-spacing: -.02em; } h2 svg { width: 20px; height: 20px; color: #acc4bd; flex-shrink: 0; }
.tag { display: inline-flex; align-items: center; gap: 5px; color: #aebec8; font-size: 11px; white-space: nowrap; } .tag svg { width: 14px; height: 14px; }
.weather-panel { background: linear-gradient(135deg, #293d47, #20303b); } .weather-hero { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; } .temperature { font-size: clamp(56px, 4.4vw, 84px); letter-spacing: -.07em; font-weight: 300; line-height: 1; } .temperature span { color: #b2c6cd; } .weather-description { color: #ccd9df; margin-top: 10px; font-size: 16px; } .weather-icon { width: 92px; height: 92px; flex-shrink: 0; }

.live-dot { width: 7px; height: 7px; border-radius: 50%; background: #aad5ab; box-shadow: 0 0 0 4px #aad5ab10; } .live-dot.muted { background: #a7adb3; }
.home-readings { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; } .label { color: #9eafbb; font-size: 11px; } .reading { font-size: 30px; font-weight: 400; letter-spacing: -.04em; margin-top: 7px; white-space: nowrap; } .reading.air-medium { color: #efcc87; } .reading.air-high { color: #ffa292; }
.tempo-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; border-top: 1px solid #ffffff0d; margin-top: 22px; padding-top: 18px; } .tempo-row > div { display: flex; flex-direction: column; gap: 9px; } .tempo { display: inline-flex; align-items: center; gap: 8px; font-size: 13px; } .tempo::before { content: ''; width: 8px; height: 8px; border-radius: 50%; background: #81929e; } .tempo-blue::before { background: #83b7ff; } .tempo-white::before { background: #eff2f4; } .tempo-red::before { background: #f7938d; }
.count { background: #ffffff09; border-radius: 8px; padding: 3px 8px; font-size: 11px; color: #b9c9c1; } .shopping-list { list-style: none; display: grid; gap: 13px; } .shopping-list li { display: flex; align-items: start; gap: 12px; font-size: 15px; color: #dbe3e8; line-height: 1.4; overflow-wrap: anywhere; } .check-circle { width: 13px; height: 13px; border: 1px solid #61756f; border-radius: 4px; margin-top: 4px; flex-shrink: 0; }
.agenda-panel { min-height: 390px; } .agenda-list { display: grid; } .event { display: flex; align-items: start; gap: 16px; padding: 20px 0; border-bottom: 1px solid #ffffff0b; } .event:first-child { padding-top: 0; } .event:last-child { border: 0; padding-bottom: 0; } .date-tile { background: #ffffff06; border: 1px solid #ffffff09; border-radius: 12px; width: 51px; min-width: 51px; text-align: center; padding: 8px 4px; } .date-tile span { display: block; font-size: 10px; color: #a8c4b8; text-transform: uppercase; letter-spacing: .1em; } .date-tile strong { font-size: 25px; font-weight: 400; line-height: 1.3; } .event-content { min-width: 0; } .event-date { font-size: 11px; color: #aec8ba; margin: 1px 0 7px; line-height: 1.5; } h3 { font-size: 17px; font-weight: 500; line-height: 1.4; overflow-wrap: anywhere; } .event-description { color: #a7b5c0; font-size: 13px; line-height: 1.6; margin-top: 7px; white-space: pre-line; overflow-wrap: anywhere; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; } .event-location { display: flex; align-items: center; gap: 4px; color: #8fa2b0; font-size: 11px; margin-top: 9px; overflow-wrap: anywhere; } .event-location svg { width: 13px; height: 13px; flex-shrink: 0; }
.photos-panel { background: #19232a; } .photos-panel h2 { font-size: 27px; font-weight: 400; letter-spacing: -.04em; } .photo-heading-icon { width: 24px; height: 24px; color: #a0b4af; } .photo-wall { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; } .photo-wall figure { aspect-ratio: 1; border-radius: 12px; overflow: hidden; background: #26343c; margin: 0; } .photo-wall figure.featured { grid-column: span 2; grid-row: span 2; } .photo-wall img { width: 100%; height: 100%; object-fit: cover; } .photo-empty { min-height: 350px; display: flex; flex-direction: column; justify-content: center; align-items: center; gap: 16px; color: #9aafb7; text-align: center; font-size: 14px; } .photo-empty svg { width: 48px; height: 48px; opacity: .4; }
.empty { color: #9bafb9; font-size: 14px; line-height: 1.6; } .status { color: #e6bc86; font-size: 11px; line-height: 1.5; margin-top: 16px; }
@media (min-width: 1100px) {
  .dashboard { height: 100dvh; display: flex; flex-direction: column; overflow: hidden; }
  .masthead { flex-shrink: 0; }
  .dashboard-grid { flex: 1; min-height: 0; align-items: stretch; }
  .column { grid-template-rows: auto auto minmax(0, 1fr); min-height: 0; }
  .agenda-panel { min-height: 0; overflow-y: auto; }
  .shopping-panel { min-height: 0; display: flex; flex-direction: column; }
  .shopping-list { overflow-y: auto; min-height: 0; }
  .shopping-panel .panel-heading { flex-shrink: 0; }
  .photos-panel { display: flex; flex-direction: column; min-height: 0; }
  .photo-wall { flex: 1; min-height: 0; grid-template-rows: repeat(4, minmax(0, 1fr)); }
  .photo-wall figure { aspect-ratio: auto; min-height: 0; }
  .agenda-panel, .shopping-list { scrollbar-width: thin; scrollbar-color: #42535c transparent; }
}
@media (min-width: 1100px) and (max-height: 850px) {
  .dashboard { padding: 14px 18px; }
  .masthead { margin-bottom: 14px; }
  .panel { padding: 14px; border-radius: 18px; }
  .column, .dashboard-grid { gap: 12px; }
  .panel-heading { margin-bottom: 10px; }
  .weather-hero { margin-bottom: 0; }
  .temperature { font-size: 48px; }
  .weather-description { margin-top: 4px; }
  .weather-icon { width: 56px; height: 56px; }

  .tempo-row { margin-top: 8px; padding-top: 8px; }
  .tempo-row > div { gap: 5px; }
  .reading { font-size: 24px; margin-top: 3px; }

}
@media (min-width: 1800px) { .dashboard-grid { gap: 18px; } .column { gap: 18px; } .panel { padding: 22px; } h2 { font-size: 19px; } .label, .event-date { font-size: 13px; } h3 { font-size: 20px; } .event-description, .shopping-list li { font-size: 16px; } }
@media (max-width: 1099px) { .dashboard-grid { grid-template-columns: 1fr 1.2fr; } .photos-panel { grid-column: 1 / -1; } .photo-wall { grid-template-columns: repeat(5, 1fr); } }
@media (max-width: 650px) { .dashboard { padding: 14px 12px; } .masthead { align-items: start; gap: 12px; } .clock p { font-size: 11px; max-width: 130px; } .eyebrow { font-size: 9px; } .dashboard-grid { grid-template-columns: minmax(0, 1fr); } .panel { padding: 16px; } .photo-wall { grid-template-columns: repeat(3, 1fr); } }
</style>

<template>
  <div class="h-screen text-white">
    <div class="h-screen">
      <section class="flex flex-col h-full p-6">
        <div class="grid grid-cols-6 grid-rows-1 gap-6 grow overflow-auto">
          <div class="bg-gray-800 rounded-lg shadow-lg p-4 row-span-full">
            <h2 class="text-lg text-center font-semibold mb-6"><ShoppingCartIcon class="h-6 w-6 inline"/> Shopping List</h2>
            <ul>
              <li
                  v-for="(item, index) in shoppingList"
                  :key="index"
                  class="py-2 text-center">
                <span class="text-sm">{{ item }}</span>
              </li>
            </ul>
          </div>
          
          <div class="bg-gray-800 rounded-lg shadow-lg p-4" v-if="homeAssistant && weather">
            <h2 class="text-lg text-center font-semibold mb-6"><SunIcon class="h-6 w-6 inline"/> Today {{new Date().toISOString().split('T')[0]}}</h2>

            <div class="grid grid-cols-2 gap-4">
              <div class="col-span-2 text-center bg-gray-700 rounded-lg p-2">
                <p class="font-bold text-gray-300 mb-1">Indoor</p>
              </div>
              
              <div class="text-center">
                <p class="font-bold text-gray-300 mb-1">Temperature</p>
                <span class="text-lg">{{ homeAssistant.indoorTemperature.toFixed() }}</span>
                <span class="text-sm text-gray-400">°C</span>
              </div>
              <div class="text-center">
                <p class="font-bold text-gray-300 mb-1">CO2</p>
                <span class="text-lg">{{ homeAssistant.co2Level }}</span>
                <span class="text-sm text-gray-400">ppm</span>
              </div>
              <div class="text-center">
                <p class="font-bold text-gray-300 mb-1">Tempo today</p>
                <span class="text-lg">{{ homeAssistant.tempoToday }}</span>
              </div>
              <div class="text-center">
                <p class="font-bold text-gray-300 mb-1">Tempo tmrrw</p>
                <span class="text-lg">{{ homeAssistant.tempoTomorrow }}</span>
              </div>

              <div class="col-span-2 text-center bg-gray-700 rounded-lg mt-4 p-2">
                <p class="font-bold text-gray-300 mb-1">Outdoor</p>
              </div>

              <div class="text-center">
                <p class="font-bold text-gray-300 mb-1">Balcony</p>
                <span class="text-lg">{{ homeAssistant.outdoorTemperature.toFixed() }}</span>
                <span class="text-sm text-gray-400">°C</span>
              </div>
              <div class="text-center">
                <p class="font-bold text-gray-300 mb-1">Temperature</p>
                <span class="text-lg">{{ weather.current.temperature_2m.toFixed() }}</span>
                <span class="text-sm text-gray-400">°C</span>
              </div>
              <div class="text-center">
                <p class="font-bold text-gray-300 mb-1">Weather</p>
                <span class="text-lg">{{ weather.current.weather_code }}</span>
              </div>
              <div class="text-center">
                <p class="font-bold text-gray-300 mb-1">{{ weather.current.is_day ? 'Day' : 'Night' }}</p>
                <span class="text-lg">{{ weather.current.is_day }}</span>
              </div>
              <div class="text-center">
                <p class="font-bold text-gray-300 mb-1">Wind</p>
                <span class="text-lg">{{ weather.current.wind_speed_10m.toFixed() }}</span>
                <span class="text-sm text-gray-400">km/h</span>
              </div>
              <div class="text-center">
                <p class="font-bold text-gray-300 mb-1">Rain</p>
                <span class="text-lg">{{ weather.current.precipitation.toFixed() }}</span>
                <span class="text-sm text-gray-400">mm</span>
              </div>
            </div>
          </div>
          
          <div class="bg-gray-800 rounded-lg shadow-lg p-4 flex flex-col">
            <h2 class="text-lg text-center font-semibold mb-6"><CalendarIcon class="h-6 w-6 inline"/> Next events</h2>

            <ul>
              <li v-for="event in calendar" class="bg-gray-700 rounded-lg p-2 mb-2">
                <p class="font-bold">{{event.title}}</p>
                <p>{{event.description}}</p>
                <p>{{event.startDate}}<span v-if="event.startDate !== event.endDate"> to {{event.endDate}}</span></p>
              </li>
            </ul>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import {onBeforeMount, ref} from "vue";
import axios from "axios";
import {CalendarIcon, ShoppingCartIcon, SunIcon, HomeIcon} from "@heroicons/vue/24/solid";

const homeAssistant = ref(null)
const shoppingList = ref([])
const calendar = ref(null)
const weather = ref(null)

const load = async () => {
  let response = await axios.get('shopping-list')
  shoppingList.value = response.data

  response = await axios.get('calendar')
  calendar.value = response.data

  response = await axios.get('home-assistant')
  homeAssistant.value = response.data
  
  response = await axios.get('https://api.open-meteo.com/v1/forecast?latitude=45.7485&longitude=4.8467&daily=weather_code,temperature_2m_min,temperature_2m_max,precipitation_sum,wind_speed_10m_max,wind_gusts_10m_max&hourly=temperature_2m,precipitation,weather_code,wind_speed_10m,wind_gusts_10m,temperature_80m&models=best_match&current=precipitation,weather_code,temperature_2m,wind_speed_10m,is_day&minutely_15=precipitation&timezone=Europe%2FBerlin&forecast_minutely_15=24')
  weather.value = response.data
}

onBeforeMount(async () => {
  await load()
  setInterval(async () => load(), 600000)
})
</script>

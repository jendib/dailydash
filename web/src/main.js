import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import axios from 'axios';

axios.defaults.baseURL = window.API_URL;

const app = createApp(App)

app.mount('#app')

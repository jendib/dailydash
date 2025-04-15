import './assets/main.css'

import {createApp} from 'vue'
import App from './App.vue'
import axios from 'axios';

axios.defaults.baseURL = window.API_URL;

const app = createApp(App)

app.config.globalProperties.$apiUrl = window.API_URL
app.mount('#app')

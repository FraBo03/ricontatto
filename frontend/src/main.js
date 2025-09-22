import { createApp } from 'vue'
import App from './App.vue'
import axios from 'axios'
import router from './router'

import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

import '@mdi/font/css/materialdesignicons.css'
import { aliases, mdi } from 'vuetify/iconsets/mdi'

import VueTelInput from 'vue-tel-input'
import 'vue-tel-input/vue-tel-input.css' // ✅ nuovo percorso

// Axios
axios.defaults.baseURL = 'http://localhost:8080'

// Vuetify
const vuetify = createVuetify({
  components,
  directives,
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: { mdi },
  },
})

const app = createApp(App)

app.config.globalProperties.$axios = axios

app.use(router)
app.use(vuetify)
app.use(VueTelInput, {
  mode: 'international',
  preferredCountries: ['IT', 'US', 'GB'],
  autoDefaultCountry: false, // ⛔ blocca geolocalizzazione via IP
  defaultCountry: 'IT'       // ✅ imposta IT come default
})

app.mount('#app')

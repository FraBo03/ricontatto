// src/plugins/vuetify.js

import 'vuetify/styles'               // Stili base di Vuetify
import '@mdi/font/css/materialdesignicons.css'  // Icone Material Design

import { createVuetify } from 'vuetify'

// Importa i componenti Vuetify che userai
import {
  VApp,
  VAppBar,
  VBtn,
  VContainer,
  VDatePicker,
  VForm,
  VMenu,
  VSelect,
  VTextField,
  VToolbar,
  VIcon
} from 'vuetify/components'

import { aliases, mdi } from 'vuetify/iconsets/mdi'

// Crea l'istanza di Vuetify
export default createVuetify({
  ssr: true,  // se usi SSR (es. Nuxt, vite-ssr, ecc)
  components: {
    VApp,
    VAppBar,
    VBtn,
    VContainer,
    VDatePicker,
    VForm,
    VMenu,
    VSelect,
    VTextField,
    VToolbar,
    VIcon
  },
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi,
    },
  },
})

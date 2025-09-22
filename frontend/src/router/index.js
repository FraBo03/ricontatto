import { createRouter, createWebHistory } from 'vue-router'
import AdminPrenotazioni from '../views/AdminPrenotazioni.vue'
import PrenotazioneForm from '../components/PrenotazioneForm.vue'
import AdminReperibilita from '@/views/AdminReperibilita.vue'
import Form2 from '@/views/Form2.vue'
import Utente from '@/views/Utente.vue'
import ListaUtenti from '@/views/ListaUtenti.vue'

const routes = [
  {
    path: '/admin-prenotazioni',
    name: 'AdminPrenotazioni',
    component: AdminPrenotazioni,
    meta: { requiresAuth: true }
  },
  {
    path: '/prenotazione-form',
    name: 'PrenotazioneForm',
    component: PrenotazioneForm
  },
  {
    path: '/form2',
    name: 'Form2',
    component: Form2
  },
  {
    path: '/admin-reperibilita',
    name: 'AdminReperibilita',
    component: AdminReperibilita,
    meta: { requiresAuth: true }
  },
  {
    path: '/users/:email',
    name: 'Utente',
    component: Utente,
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: '/users',
    name: 'ListaUtenti',
    component: ListaUtenti,
    props: true,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Password fissa per l’admin (puoi spostarla in .env se vuoi nasconderla meglio)
const ADMIN_PASSWORD = "admin123"

// Guardia globale per proteggere le rotte admin
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const isAdmin = sessionStorage.getItem("isAdmin")
    if (isAdmin === "true") {
      next()
    } else {
      const pwd = prompt("Inserisci la password admin:")
      if (pwd === ADMIN_PASSWORD) {
        sessionStorage.setItem("isAdmin", "true")
        next()
      } else {
        alert("Password errata")
        next(false)
      }
    }
  } else {
    next()
  }
})

export default router

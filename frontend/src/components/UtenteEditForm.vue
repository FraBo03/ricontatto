<template>
  <h3>Anagrafica Utente</h3>
  <v-container>
    <div v-if="loading">Caricamento in corso...</div>

    <v-alert type="error" v-if="errore">Errore nel caricamento o salvataggio</v-alert>

    <v-alert type="info" v-if="!loading && !utente && !errore">Utente non trovato</v-alert>

    <v-form @submit.prevent="salvaUtente" v-if="utente && !loading">
      <v-text-field v-model="utente.nome" label="Nome" required />
      <v-text-field v-model="utente.cognome" label="Cognome" required />
      <v-select v-model="utente.regione" :items="regioni" label="Regione" required />
      <v-select v-model="utente.provincia" :items="province" label="Provincia" required :disabled="!utente.regione" />
      <v-text-field v-model="utente.citta" label="Città" />
      <v-text-field v-model="utente.indirizzo" label="Indirizzo" />
      <v-text-field v-model="utente.cap" label="CAP" />

      <v-btn color="primary" type="submit">Salva</v-btn>
    </v-form>
    <v-btn color="error" prepend-icon="mdi-delete" style="margin-top: 10px" @click="eliminaUtente">
      Elimina utente
    </v-btn>
  </v-container>
</template>

<script>
import axios from 'axios'

export default {
  name: 'UtenteEditForm',
  props: {
    email: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      utente: null,
      errore: false,
      loading: true,
      regioni: [],
      province: [],
    }
  },
  watch: {
    'utente.regione': {
        handler(nuovaRegione, vecchiaRegione) {
            if (nuovaRegione) {
                fetch(`/territorio/province?regione=${encodeURIComponent(nuovaRegione)}`)
                .then(res => res.json())
                .then(data => {
                    this.province = data
                    // Resetta provincia solo se la regione è stata modificata dall'utente
                    if (vecchiaRegione && nuovaRegione !== vecchiaRegione) {
                    this.utente.provincia = ''
                    }
                })
            } else {
                this.province = []
                this.utente.provincia = ''
            }
            },
            immediate: false // non far partire il watcher all'inizializzazione
        }
    },
  mounted() {
    this.caricaUtente()
    this.caricaRegioni()
  },
  methods: {
    async caricaUtente() {
      this.loading = true
      this.errore = false
      try {
        const res = await fetch(`/utenti/${encodeURIComponent(this.email)}`)
        if (!res.ok) {
          this.utente = null
          throw new Error('Utente non trovato')
        }
        this.utente = await res.json()
      } catch {
        this.errore = true
      } finally {
        this.loading = false
      }
    },
    async caricaRegioni() {
      const res = await fetch('/territorio/regioni')
      this.regioni = await res.json()
    },
    async salvaUtente() {
      this.errore = false
      try {
        const res = await fetch(`/utenti/${encodeURIComponent(this.email)}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.utente)
        })
        if (!res.ok) throw new Error()
        alert('Utente salvato con successo')
      } catch {
        this.errore = true
      }
    },
    async eliminaUtente() {
      try {
        const email = this.utente.email
        if (!email) {
          console.warn('Email non specificata')
          return
        }

        await axios.delete(`/utenti/${encodeURIComponent(email)}`)
        this.$router.push({
  path: '/users', // ✅ CORRETTO: path della tua pagina ListaUtenti.vue
  query: {
    messaggio: `Utente ${email} eliminato con successo`
  }
})
      } catch (error) {
        console.error('Errore durante l\'eliminazione:', error)
      }
    }
  }
}
</script>

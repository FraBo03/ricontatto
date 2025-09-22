<template>
  <v-container>
    <v-card>
      <v-card-title>Lista Utenti</v-card-title>
      <v-card-text>
        <v-progress-circular
          v-if="loading"
          indeterminate
          color="primary"
          class="ma-4"
        />
        <div v-else>
          <v-row class="mb-4" dense style="gap: 8px">
            <v-col
                v-for="f in filtersConfig"
                :key="f.key"
                class="d-flex"
                style="width: 200px"
            >
                <v-text-field
                v-model="filters[f.key]"
                :label="`Filtro ${f.label}`"
                dense
                clearable
                />
            </v-col>

            <!-- Pulsante Reset filtri -->
            <v-col style="width: 200px; margin-top: 10px">
                <v-btn
                color="primary"
                @click="resetFiltri"
                prepend-icon="mdi-filter-remove"
                class="ma-0"
                >
                Reset filtri
                </v-btn>
            </v-col>
            </v-row>

          <v-data-table
            :headers="headers"
            :items="utentiFiltrati"
            class="elevation-1"
            item-value="email"
            :items-per-page="10"
          >

            <!-- Email cliccabile -->
            <template #item.email="{ item }">
              <RouterLink :to="`/users/${encodeURIComponent(item.email)}`" class="text-primary">
                {{ item.email }}
              </RouterLink>
            </template>

            <template #item.cap="{ item }">
              {{ item.cap || '-' }}
            </template>
          </v-data-table>
        </div>
      </v-card-text>
    </v-card>

    <!-- Snackbar -->
    <v-snackbar
      v-model="snackbar"
      color="success"
      timeout="3000"
      top
    >
      {{ messaggioSnackbar }}
    </v-snackbar>
  </v-container>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ListaUtenti',
  data() {
    return {
      utenti: [],
      loading: true,
      filters: {
        email: '',
        nome: '',
        cognome: '',
        regione: '',
        provincia: '',
        citta: '',
        cap: ''
      },
      snackbar: false,
      messaggioSnackbar: '',
      headers: [
        { title: 'Email', key: 'email' },
        { title: 'Nome', key: 'nome' },
        { title: 'Cognome', key: 'cognome' },
        { title: 'Regione', key: 'regione' },
        { title: 'Provincia', key: 'provincia' },
        { title: 'Città', key: 'citta' },
        { title: 'Indirizzo', key: 'indirizzo' },
        { title: 'CAP', key: 'cap' }
      ],
      filtersConfig: [
        { key: 'email', label: 'Email' },
        { key: 'nome', label: 'Nome' },
        { key: 'cognome', label: 'Cognome' },
        { key: 'regione', label: 'Regione' },
        { key: 'provincia', label: 'Provincia' },
        { key: 'citta', label: 'Città' },
        { key: 'cap', label: 'CAP' }
      ]
    }
  },
  computed: {
    utentiFiltrati() {
      return this.utenti.filter((utente) => {
        return Object.keys(this.filters).every((key) => {
          const filtro = (this.filters[key] || '').toLowerCase()
          const rawVal = utente[key]
          const valore = (rawVal !== null && rawVal !== undefined) ? String(rawVal).toLowerCase() : ''
          return valore.includes(filtro)
        })
      })
    }
  },
  methods: {
    async caricaUtenti() {
      this.loading = true
      try {
        const response = await axios.get('/utenti')
        this.utenti = response.data
      } catch (error) {
        console.error('Errore durante il caricamento degli utenti:', error)
      } finally {
        this.loading = false
      }
    },
    resetFiltri() {
      Object.keys(this.filters).forEach(k => this.filters[k] = '')
    }
  },
  mounted() {
    this.caricaUtenti()

    const messaggio = this.$route.query.messaggio
    if (messaggio) {
      this.messaggioSnackbar = messaggio
      this.snackbar = true
      this.$router.replace({ query: {} })
    }
  }
}
</script>

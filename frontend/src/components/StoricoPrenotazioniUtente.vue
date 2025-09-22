<template>
  <v-card>
    <h3>Storico Prenotazioni</h3>
    <v-card-text>
      <v-data-table
        :headers="headers"
        :items="prenotazioni"
        :loading="loading"
        no-data-text="Nessuna prenotazione trovata"
        loading-text="Caricamento in corso..."
        class="elevation-1"
        items-per-page="5"
      >
        <template #item.fasciaOraria="{ item }">
            {{ formatFasciaOraria(item.fasciaOraria?.id) }}
        </template>

        <template #item.actions="{ item }">
            <v-icon small class="me-2" color="primary" @click="apriDialogModifica(item)">
                mdi-pencil
            </v-icon>
            <v-icon small color="error" @click="eliminaPrenotazione(item)">
                mdi-delete
            </v-icon>
        </template>

        <template #item.giorno="{ item }">
            {{ formattaData(item.giorno) }}
        </template>

        <template #item.motivazioniDescrizioni="{ item }">
          <ul style="padding-left: 16px; margin: 0;">
            <li v-for="(mot, index) in item.motivazioni" :key="index">
              {{ mot.descrizione }}
            </li>
          </ul>
        </template>
      </v-data-table>

       <!-- DIALOG MODIFICA -->
    <v-dialog v-model="dialogModifica" max-width="500px">
      <v-card>
        <v-card-title>Modifica Prenotazione</v-card-title>
        <v-card-text>
          <v-form ref="modificaForm">
            <v-text-field
              v-model="prenotazioneDaModificare.nome"
              label="Nome"
              :rules="[v => !!v || 'Campo obbligatorio']"
              required
            />
            <v-text-field v-model="prenotazioneDaModificare.email" label="Email" />

            <v-text-field
              v-model="prenotazioneDaModificare.cellulare"
              label="Cellulare"
              type="tel"
            />

            <v-text-field
              v-model="prenotazioneDaModificare.giorno"
              label="Giorno"
              type="date"
              :rules="[v => !!v || 'Seleziona una data']"
              required
            />
            <v-select
              v-model="prenotazioneDaModificare.fasciaOraria.id"
              :items="fasceOrarie"
              item-title="label"
              item-value="id"
              label="Fascia Oraria"
              :rules="[v => !!v || 'Seleziona una fascia']"
              required
            />

            <!-- CAMPO MOTIVAZIONI (MULTIPLA SELEZIONE) -->
            <v-select
              v-model="prenotazioneDaModificare.motivazioniDescrizioni"
              :items="motivazioniDisponibili"
              item-title="descrizione"
              item-value="descrizione"
              multiple
              chips
              label="Motivazioni"
            />

<!-- CAMPO NOTE -->
<v-textarea
  v-model="prenotazioneDaModificare.note"
  label="Note"
/>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn text @click="dialogModifica = false">Annulla</v-btn>
          <v-btn color="primary" @click="salvaModifica">Salva</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

      <!-- Snackbar -->
        <v-snackbar
        v-model="snackbar"
        color="success"
        timeout="3000"
        top
        >
        {{ messaggioSnackbar }}
        </v-snackbar>
    </v-card-text>
  </v-card>
</template>

<script>
import axios from 'axios'

export default {
  name: 'StoricoPrenotazioniUtente',
  props: {
    email: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      loading: false,
      prenotazioni: [],
      fasceOrarie: [],
      snackbar: false,
      messaggioSnackbar: '',  
       dialogModifica: false,
    prenotazioneDaModificare: {
      id: null,
      nome: '',
      email: '',
      cellulare: '',
      giorno: '',
      fasciaOraria: { id: null },
      motivazioniDescrizioni: [],
      note: ''
    },
    motivazioniDisponibili: [],
      headers: [
        { title: 'Cellulare', key: 'cellulare' },
        { title: 'Giorno', key: 'giorno' },
        { title: 'Fascia oraria', key: 'fasciaOraria' },
        { title: 'Motivazioni', key: 'motivazioniDescrizioni' },
        { title: 'Azioni', key: 'actions', sortable: false }
      ]
    }
  },
  mounted() {
    this.caricaFasceOrarie()
    this.caricaPrenotazioni()
    this.caricaMotivazioni()
  },
  methods: {
    async caricaPrenotazioni() {
      try {
        this.loading = true
        const response = await axios.get(`/api/prenotazioni?email=${encodeURIComponent(this.email)}`)
        this.prenotazioni = response.data
      } catch (error) {
        console.error('Errore nel caricamento delle prenotazioni:', error)
      } finally {
        this.loading = false
      }
    },

    async caricaFasceOrarie() {
      try {
        const response = await axios.get('/api/fasce-orarie')
        this.fasceOrarie = response.data.map(f => ({
          id: f.id,
          label: `${f.oraInizio?.substring(0, 5)} - ${f.oraFine?.substring(0, 5)}`
        }))
      } catch (err) {
        console.error('Errore nel caricamento delle fasce orarie:', err)
      }
    },

    formattaData(dataStr) {
      if (!dataStr) return ''
      return new Date(dataStr).toLocaleDateString('it-IT')
    },

    formatFasciaOraria(id) {
    const f = this.fasceOrarie.find(f => String(f.id) === String(id))
    return f ? f.label : ''
    },

    async eliminaPrenotazione(prenotazione) {
    try {
        const conferma = confirm(`Sei sicuro di voler eliminare la prenotazione del ${this.formattaData(prenotazione.giorno)}?`)
        if (!conferma) return

        await this.$axios.delete(`/api/prenotazioni/${prenotazione.id}`)

        // Rimuovi la prenotazione localmente dalla lista
        this.prenotazioni = this.prenotazioni.filter(p => p.id !== prenotazione.id)

        // Snackbar di successo
        this.messaggioSnackbar = 'Prenotazione eliminata con successo'
        this.snackbar = true
    } catch (error) {
        console.error('Errore durante l\'eliminazione:', error)
        this.messaggioSnackbar = 'Errore durante l\'eliminazione'
        this.snackbar = true
        }
    },
    async apriDialogModifica(prenotazione) {
  try {
    await this.caricaMotivazioni()

    this.prenotazioneDaModificare = {
      ...prenotazione,
      fasciaOraria: prenotazione.fasciaOraria || { id: prenotazione.fasciaOrariaId },
      motivazioniDescrizioni: prenotazione.motivazioni ? prenotazione.motivazioni.map(m => m.descrizione) : [],
      note: prenotazione.note || ''
    }

    this.dialogModifica = true
  } catch (error) {
    console.error('Errore apertura dialogo modifica:', error)
  }
},

    async salvaModifica() {
  try {
    const payload = {
      id: this.prenotazioneDaModificare.id,
      nome: this.prenotazioneDaModificare.nome,
      email: this.prenotazioneDaModificare.email,
      cellulare: this.prenotazioneDaModificare.cellulare,
      giorno: this.prenotazioneDaModificare.giorno,
      fasciaOrariaId: this.prenotazioneDaModificare.fasciaOraria.id,
      motivazioniDescrizioni: this.prenotazioneDaModificare.motivazioniDescrizioni,
      note: this.prenotazioneDaModificare.note
    }

    await axios.put(`/api/prenotazioni/${payload.id}`, payload)

    this.dialogModifica = false
    this.messaggioSnackbar = 'Prenotazione modificata con successo'
    this.snackbar = true

    // Aggiorna la lista
    await this.caricaPrenotazioni()
  } catch (error) {
    console.error('Errore durante la modifica:', error)
    this.messaggioSnackbar = 'Errore durante la modifica'
    this.snackbar = true
  }
},

    async caricaMotivazioni() {
    try {
        const response = await axios.get('/api/motivazioni')
        this.motivazioniDisponibili = response.data
    } catch (error) {
        console.error('Errore durante il fetch delle motivazioni:', error)
    }
    }

  },
  
}
</script>

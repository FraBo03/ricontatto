<template>

  <v-container>
    <v-row align="center" justify="space-between">
      <v-col cols="auto">
        <h2>Gestione Reperibilità Giornaliera</h2>
      </v-col>
      <v-col cols="auto" class="text-right">
        <PageSelector />
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="12" md="4">
        <v-menu
          v-model="menu"
          :close-on-content-click="false"
          transition="scale-transition"
          offset-y
          min-width="auto"
        >
          <template #activator="{ props }">
            <v-text-field
              v-bind="props"
              :model-value="formattaGiorno(giornoSelezionato)"
              label="Seleziona Giorno"
              prepend-icon="mdi-calendar"
              readonly
            />
          </template>

          <v-date-picker
            v-model="giornoSelezionato"
            @update:modelValue="onDateSelected"
          />
        </v-menu>
      </v-col>
    </v-row>

    <v-row class="mb-4" align="center" justify="start">
      <v-col cols="12" md="4">
        <v-btn
          color="secondary"
          @click="toggleFasce"
          :aria-pressed="mostraFasceExtra"
        >
          {{ mostraFasceExtra ? 'Nascondi fasce extra' : 'Configura fasce extra' }}
        </v-btn>
      </v-col>
    </v-row>

    <div v-if="fasceOrarie.length === 0">
      <v-alert type="info">Caricamento fasce orarie in corso...</v-alert>
    </div>

    <div v-else>
      <v-alert v-if="nessunaDisponibilita" type="warning" class="mb-4">
        Nessuna disponibilità salvata per il giorno selezionato. Slot inizializzati a 0.
      </v-alert>

      <v-row class="mt-4 mb-2">
        <v-col cols="12" md="3">
          <v-checkbox
            v-model="selectAll"
            label="Seleziona tutte le fasce"
            @change="toggleSelezioneTutte"
          />
        </v-col>

        <v-col cols="12" md="3">
          <v-text-field
            v-model.number="valoreComune"
            label="Valore slot da assegnare"
            type="number"
            min="0"
          />
        </v-col>

        <v-col cols="12" md="3">
          <v-btn color="success" @click="applicaValoreComune" style="margin-top: 10px">
            Applica valore
          </v-btn>
        </v-col>
      </v-row>

      <div
  :style="{
    maxHeight: fasceOrarie.length > 9 ? '550px' : 'auto',
    overflowY: fasceOrarie.length > 9 ? 'auto' : 'visible',
    overflowX: fasceOrarie.length > 9 ? 'hidden' : 'visible',
    padding: '8px'
  }"
>
  <v-row>
    <v-col
      v-for="fascia in fasceOrarie"
      :key="fascia.id"
      cols="12"
      md="3"
    >
      <div style="display: flex; align-items: center; gap: 8px;">
        <v-checkbox
          v-model="fasceSelezionate"
          :value="fascia.id"
          hide-details
          density="compact"
          class="ma-0 pa-0"
          style="margin-right: 0;"
        />
        <span>{{ `${fascia.oraInizio} - ${fascia.oraFine}` }}</span>
      </div>
      <v-text-field
        type="number"
        min="0"
        v-model.number="disponibilitaSlot[fascia.id]"
        :error-messages="avvisiSlot[fascia.id] ? [avvisiSlot[fascia.id]] : []"
        @blur="controllaLimitePrenotazioni(fascia.id)"
      />
    </v-col>
  </v-row>
</div>
      <v-btn :loading="salvataggioInCorso" color="primary" class="mt-4" @click="salvaDisponibilita">
        Salva Disponibilità
      </v-btn>
    </div>
  </v-container>
</template>

<script>
import axios from 'axios';
import PageSelector from '../components/PageSelector.vue';

export default {
  components: {
    PageSelector
  },
  name: 'AdminReperibilita',
  data() {
    return {
      giornoSelezionato: null,
      menu: false,
      fasceOrarie: [],
      mostraFasceExtra: false,
      disponibilitaSlot: {},
      prenotazioniPerFascia: {},
      avvisiSlot: {},
      nessunaDisponibilita: false,
      salvataggioInCorso: false,
      fasceSelezionate: [],
      valoreComune: 1,
      selectAll: false,
    };
  },
  methods: {
    // ✅ Corretto formatter per evitare problemi di fuso orario
    formattaDataLocale(date) {
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    formattaGiorno(date) {
      if (!date) return '';
      return new Date(date).toDateString(); // Esempio: "Fri Jun 27 2025"
    },

    toggleSelezioneTutte() {
      this.fasceSelezionate = this.selectAll
        ? this.fasceOrarie.map(f => f.id)
        : [];
    },

    applicaValoreComune() {
      this.fasceSelezionate.forEach(id => {
        this.disponibilitaSlot[id] = this.valoreComune;
        this.controllaLimitePrenotazioni(id);
      });
    },

    async fetchFasceOrarie(extra = false) {
      try {
        const endpoint = extra
          ? '/api/fasce-orarie'
          : '/api/fasce-orarie-default';
        const res = await axios.get(endpoint);
        this.fasceOrarie = res.data;
      } catch (error) {
        console.error("Errore nel caricamento fasce orarie", error);
      }
    },

    toggleFasce() {
      this.mostraFasceExtra = !this.mostraFasceExtra;
      this.fetchFasceOrarie(this.mostraFasceExtra);
    },

    async fetchFasceOrarieDisponibili(giorno) {
      if (!giorno) return;
      const giornoISO = this.formattaDataLocale(giorno); // ✅ uso funzione locale

      this.fasceOrarie.forEach(fascia => {
        this.disponibilitaSlot[fascia.id] = 0;
        this.prenotazioniPerFascia[fascia.id] = 0;
        this.avvisiSlot[fascia.id] = '';
      });

      try {
        const resDisponibilita = await axios.get(`/api/disponibilita/${giornoISO}`);
        if (resDisponibilita.data && Object.keys(resDisponibilita.data).length > 0) {
          this.nessunaDisponibilita = false;
          for (const fasciaId in resDisponibilita.data) {
            this.disponibilitaSlot[fasciaId] = resDisponibilita.data[fasciaId];
          }
        } else {
          this.nessunaDisponibilita = true;
        }

        const resPrenotazioni = await axios.get(`/api/prenotazioni/count-giorno/${giornoISO}`);
        this.prenotazioniPerFascia = resPrenotazioni.data || {};

      } catch (error) {
        console.error("Errore nel caricamento", error);
        this.nessunaDisponibilita = true;
      }
    },

    controllaLimitePrenotazioni(fasciaId) {
      const prenotazioni = this.prenotazioniPerFascia[fasciaId] || 0;
      const slotImpostato = this.disponibilitaSlot[fasciaId];

      if (slotImpostato < prenotazioni) {
        this.avvisiSlot[fasciaId] = `Sono già presenti ${prenotazioni} prenotazioni.`;
        this.disponibilitaSlot[fasciaId] = prenotazioni;
      } else {
        this.avvisiSlot[fasciaId] = '';
      }
    },

    async salvaDisponibilita() {
      if (!this.giornoSelezionato) {
        alert("Seleziona un giorno prima di salvare.");
        return;
      }

      const giornoISO = this.formattaDataLocale(this.giornoSelezionato); // ✅ uso corretto

      const payload = this.fasceOrarie.map(fascia => ({
        giorno: giornoISO,
        fasciaOrariaId: fascia.id,
        disponibilita: this.disponibilitaSlot[fascia.id] || 0,
      }));

      this.salvataggioInCorso = true;

      try {
        await axios.post(`/api/disponibilita/${giornoISO}`, payload);
        this.nessunaDisponibilita = false;
        alert("Disponibilità salvata correttamente");
      } catch (error) {
        console.error("Errore salvataggio disponibilità", error);
        alert("Errore durante il salvataggio");
      } finally {
        this.salvataggioInCorso = false;
      }
    },

    onDateSelected(value) {
      this.menu = false;
      this.fetchFasceOrarieDisponibili(value);
    }
  },
  mounted() {
    this.fetchFasceOrarie();
  }
};
</script>

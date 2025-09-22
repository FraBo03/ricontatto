<template>
  <v-container>
    <!-- TABELLA -->
    <v-data-table
      :headers="headers"
      :items="prenotazioniFiltrate"
      v-model="selectedPrenotazioni"
      :item-value="item => item"
      :loading="eliminazioneInCorso || loading"
      class="elevation-1"
      :items-per-page="10"
      show-select
    >
  <template v-slot:top>
    <v-row align="center" justify="space-between" class="px-4 pt-2">
      <v-col cols="auto" class="text-h6 font-weight-medium">
        Lista Prenotazioni
      </v-col>
      <v-col cols="auto">
        <PageSelector />
      </v-col>
    </v-row>

    <div style="margin-top: 10px;"></div>

    <!-- Filtri -->
    <v-row class="px-4" dense style="margin-bottom: 10px;"> 
      <v-col style="max-width: 200px; margin-right: 10px">
        <v-text-field
          v-model="filters.nome"
          label="Filtro Nome"
          dense
          hide-details
          clearable
        />
      </v-col>

      <v-col style="max-width: 200px; margin-right: 10px">
        <v-text-field
          v-model="filters.email"
          label="Filtro Email"
          dense
          hide-details
          clearable
        />
      </v-col>

      <v-col style="max-width: 200px; margin-right: 10px">
        <v-menu v-model="menuInizio" :close-on-content-click="false">
          <template v-slot:activator="{ props }">
            <v-text-field v-model="filters.dataInizio" label="Da (YYYY-MM-DD)" readonly clearable v-bind="props" />
          </template>
          <v-date-picker v-model="tempInizio" @update:model-value="val => onDateChange('dataInizio', val, 'menuInizio')" />
        </v-menu>
      </v-col>

      <v-col style="max-width: 200px; margin-right: 10px">
        <v-menu v-model="menuFine" :close-on-content-click="false">
          <template v-slot:activator="{ props }">
            <v-text-field v-model="filters.dataFine" label="A (YYYY-MM-DD)" readonly clearable v-bind="props" />
          </template>
          <v-date-picker v-model="tempFine" @update:model-value="val => onDateChange('dataFine', val, 'menuFine')" />
        </v-menu>
      </v-col>

      <v-col style="max-width: 200px; margin-right: 10px">
        <v-text-field
          v-model="filters.fasciaOraria"
          label="Filtro Fascia"
          dense
          hide-details
          clearable
        />
      </v-col>

      <!-- Pulsanti Reset e Filtra -->
<v-col class="d-flex justify-end" cols="auto">
  <v-btn color="primary" prepend-icon="mdi-magnify" @click="filtraPrenotazioni" style="margin-top: 10px" dense>
    Filtra
  </v-btn>
  <v-btn color="primary" prepend-icon="mdi-filter-remove" @click="resetFilters" style="margin-top: 10px; margin-left: 8px" dense>
    Reset filtri
  </v-btn>
</v-col>
    </v-row>
  </template>


  <template v-slot:item.fasciaOraria="{ item }">
    {{ formatFasciaOraria(item.fasciaOraria) }}
  </template>

  <!-- Header Azioni personalizzato -->
  <template #header.actions>
    <div style="display: flex; align-items: center; justify-content: space-between;">
      <span>Azioni</span>
      <v-btn
        icon
        color="red"
        :disabled="selectedPrenotazioni.length === 0"
        @click="eliminaSelezionate"
        title="Elimina selezionate"
      >
        <v-icon>mdi-delete</v-icon>
      </v-btn>
    </div>
  </template>

    <template v-slot:item.actions="{ item }">
      <v-icon small class="mr-2" @click="modificaPrenotazione(item)">mdi-pencil</v-icon>
      <v-icon small @click="eliminaPrenotazione(item.id)">mdi-delete</v-icon>
    </template>

    <template #item.email="{ item }">
      <span v-if="emailEsistenti[item.email]">
        <router-link :to="`/users/${encodeURIComponent(item.email)}`">
          {{ item.email }}
        </router-link>
      </span>
      <span v-else>
        {{ item.email }}
      </span>
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
  </v-container>
</template>

<script>
import PageSelector from '../components/PageSelector.vue'

export default {
  components: {
    PageSelector
  },
  name: 'TabellaPrenotazioni',
  data() {
    const oggi = new Date().toISOString().split('T')[0];
    return {
      loading: false,
      search: '',
      filters: {
        nome: '',
        email: '',
        cellulare: '',
        dataInizio: oggi,
        dataFine: oggi, 
        fasciaOraria: '',
      },
      tempInizio: '',
      tempFine: '',
      emailEsistenti: {},
      menuInizio: false,
      menuFine: false,
      dataTemporanea: oggi,
      datePickerMenu: false,
      eliminazioneInCorso: false,
      selectedPrenotazioni: [],
      prenotazioniFiltrate: [],
      prenotazioni: [],
      dialogModifica: false,
      motivazioniDisponibili: [],
      prenotazioneDaModificare: {
        id: null,
        nome: '',
        email: '',
        giorno: '',
        fasciaOraria: { id: null },
        note: '',
        motivazioniDescrizioni: [],
      },
      fasceOrarie: [],
      headers: [
        { title: 'ID', key: 'id', sortable: true },
        { title: 'Nome', key: 'nome', sortable: true },
        { title: 'Email', key: 'email', sortable: true },
        { title: 'Cellulare', key: 'cellulare', sortable: true },
        { title: 'Giorno', key: 'giorno', sortable: true },
        { title: 'Fascia Oraria', key: 'fasciaOraria', sortable: true },
        { title: 'Azioni', key: 'actions', sortable: false }
      ]
    };
  },
  watch: {
    prenotazioniFiltrate() {
      this.verificaEmailUtenti();
    }
  },
  mounted() {
    this.fetchPrenotazioni();
    this.caricaFasceOrarie();
    this.filtraPrenotazioni();
    this.caricaMotivazioni();
    this.verificaEmailUtenti();
  },
  methods: {
    async eliminaSelezionate() {
  if (!confirm(`Vuoi eliminare ${this.selectedPrenotazioni.length} prenotazioni selezionate?`)) {
    return;
  }

  this.eliminazioneInCorso = true;

  try {
    for (const prenotazione of this.selectedPrenotazioni) {
      await this.$axios.delete(`/api/prenotazioni/${prenotazione.id}`);
    }

    alert('Prenotazioni eliminate con successo!');
    this.selectedPrenotazioni = [];
    await this.fetchPrenotazioni();  // aspetta il refresh

  } catch (error) {
    console.error('Errore durante eliminazione multipla:', error);
    alert('Errore durante l\'eliminazione.');
  } finally {
    this.eliminazioneInCorso = false;
  }
},

    async fetchPrenotazioni() {
      this.loading = true;
      try {
        const response = await this.$axios.get('/api/prenotazioni');
        this.prenotazioni = response.data;
        this.filtraPrenotazioni();  // aggiorna la lista filtrata
      } catch (error) {
        console.error('Errore caricamento prenotazioni:', error);
      } finally {
        this.loading = false;
      }
    },

    onDateChange(campo, value, menuName) {
    const d = new Date(value);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    this.filters[campo] = `${yyyy}-${mm}-${dd}`;
    this[menuName] = false;
    this.filtraPrenotazioni();
  },

    onDataSelezionata(value) {
      const data = new Date(value);
      const yyyy = data.getFullYear();
      const mm = String(data.getMonth() + 1).padStart(2, '0');
      const dd = String(data.getDate()).padStart(2, '0');
      this.filters.giorno = `${yyyy}-${mm}-${dd}`;
      this.datePickerMenu = false;
      this.filtraPrenotazioni();
    },

    filtraPrenotazioni() {
      this.prenotazioniFiltrate = this.prenotazioni.filter(item => {
        const nomeMatch = !this.filters.nome || String(item.nome || '').toLowerCase().includes(this.filters.nome.toLowerCase());
        const emailMatch = !this.filters.email || String(item.email || '').toLowerCase().includes(this.filters.email.toLowerCase());
        const fasciaMatch = !this.filters.fasciaOraria || this.formatFasciaOraria(item.fasciaOraria).toLowerCase().includes(this.filters.fasciaOraria.toLowerCase());

        const giorno = item.giorno; // 'YYYY-MM-DD'

        const intervalloMatch =
          (!this.filters.dataInizio || giorno >= this.filters.dataInizio) &&
          (!this.filters.dataFine || giorno <= this.filters.dataFine);

        return nomeMatch && emailMatch && fasciaMatch && intervalloMatch;
      });
    },

    resetFilters() {
      this.filters.nome = ''
      this.filters.email = ''
      this.filters.dataInizio = ''
      this.filters.dataFine = ''
      this.filters.fasciaOraria = ''
      this.filtraPrenotazioni()
    },

    async caricaFasceOrarie() {
      try {
        const response = await this.$axios.get('/api/fasce-orarie');
        this.fasceOrarie = response.data.map(f => ({
          id: f.id,
          label: `${f.oraInizio.substring(0, 5)} - ${f.oraFine.substring(0, 5)}`
        }));
      } catch (err) {
        console.error('Errore fasce orarie:', err);
      }
    },

    formatFasciaOraria(fascia) {
      if (!fascia) return '';
      return `${fascia.oraInizio?.substring(0, 5)} - ${fascia.oraFine?.substring(0, 5)}`;
    },

    modificaPrenotazione(prenotazione) {
      this.prenotazioneDaModificare = {
        id: prenotazione.id,
        nome: prenotazione.nome,
        email: prenotazione.email,
        cellulare: prenotazione.cellulare,
        giorno: prenotazione.giorno,
        fasciaOraria: { id: prenotazione.fasciaOraria?.id || null },
        motivazioniDescrizioni: prenotazione.motivazioni
          ? prenotazione.motivazioni.map(m => m.descrizione)
          : [],
        note: prenotazione.note
      };
      this.dialogModifica = true;
    },

    async salvaModifica() {
      const form = this.$refs.modificaForm;
      const valido = await form.validate?.();
      if (valido === false) return;

      try {
        await this.$axios.put(`/api/prenotazioni/${this.prenotazioneDaModificare.id}`, {
          nome: this.prenotazioneDaModificare.nome,
          email: this.prenotazioneDaModificare.email,
          cellulare: this.prenotazioneDaModificare.cellulare,
          giorno: this.prenotazioneDaModificare.giorno,
          fasciaOrariaId: this.prenotazioneDaModificare.fasciaOraria.id,
          motivazioniDescrizioni: this.prenotazioneDaModificare.motivazioniDescrizioni,
          note: this.prenotazioneDaModificare.note
        });
        this.dialogModifica = false;
        this.fetchPrenotazioni();
        alert('Prenotazione modificata con successo!');
      } catch (error) {
        console.error('Errore salvataggio:', error);
        alert('Errore durante il salvataggio.');
      }
    },

    async eliminaPrenotazione(id) {
      if (!confirm('Vuoi eliminare questa prenotazione?')) return;
      try {
        await this.$axios.delete(`/api/prenotazioni/${id}`);
        alert('Prenotazione eliminata!');
        this.fetchPrenotazioni();
      } catch (error) {
        console.error('Errore eliminazione:', error);
        alert('Errore durante eliminazione.');
      }
    },

    async caricaMotivazioni() {
      try {
        const response = await this.$axios.get('/api/motivazioni');
        this.motivazioniDisponibili = response.data;
      } catch (error) {
        console.error('Errore durante il fetch delle motivazioni:', error);
      }
    },

    async verificaEmailUtenti() {
      for (const item of this.prenotazioniFiltrate) {
        const email = item.email;
        // Se non ancora verificata
        if (!(email in this.emailEsistenti)) {
          try {
            const res = await fetch(`/utenti/email-gia-presente?email=${encodeURIComponent(email)}`);
            const presente = await res.json();
            this.emailEsistenti[email] = presente; // reattivo
          } catch (error) {
            console.error(`Errore durante la verifica per l'email ${email}`, error);
            this.emailEsistenti[email] = false;
          }
        }
      }
    },

    customSort(items, sortBy, sortDesc) {
      if (!sortBy.length) return items;
      const sortKey = sortBy[0];
      const desc = sortDesc[0];

      return items.slice().sort((a, b) => {
        let valA = a[sortKey];
        let valB = b[sortKey];

        if (sortKey === 'fasciaOraria') {
          valA = a.fasciaOraria?.oraInizio || '';
          valB = b.fasciaOraria?.oraInizio || '';
        }

        if (valA == null) valA = '';
        if (valB == null) valB = '';
        if (typeof valA === 'string') valA = valA.toLowerCase();
        if (typeof valB === 'string') valB = valB.toLowerCase();

        return (valA < valB ? -1 : valA > valB ? 1 : 0) * (desc ? -1 : 1);
      });
    }
  }
};
</script>

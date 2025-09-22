<template>
  <v-container>
    <v-row align="center" justify="space-between">
    <v-col cols="3"><!-- spazio vuoto per bilanciare --></v-col>

    <v-col cols="6" class="text-center">
      <h2>Form Prenotazione</h2>
    </v-col>

    <v-col cols="3" class="text-right">
      <PageSelector />
    </v-col>
  </v-row>
    <!-- STEP 1: Email e scelta salvataggio -->
<div style="max-width: 30%; margin: auto;" v-if="!stepEmailCompletato">
  <v-text-field
    label="Email"
    v-model="form.email"
    required
    type="email"
    :rules="[v => !!v || 'Email richiesta', v => isValidEmail(v) || 'Email non valida']"
  />

  <v-radio-group v-model="salvaEmail" row>
    <p>Salvare la mail per una migliore esperienza?</p>
    <v-radio label="Sì" value="si" />
    <v-radio label="No" value="no" />
  </v-radio-group>

  <v-btn
    color="primary"
    :disabled="!form.email || !isValidEmail(form.email) || !salvaEmail"
    @click="vaiAvantiDopoEmail"
  >
    Avanti
  </v-btn>
</div>

<div style="max-width: 30%; margin: auto;" v-if="stepEmailCompletato">

  <!-- ✅ Mostra form creazione utente SOLO se necessario -->
  <div v-if="salvaEmail === 'si' && emailVerificata && !emailGiaPresente && !utenteSalvato">

    <v-text-field v-model="formUtente.nome" label="Nome" required 
      :rules="[v => !!v || 'Campo obbligatorio']"
    />
    <v-text-field v-model="formUtente.cognome" label="Cognome" required 
      :rules="[v => !!v || 'Campo obbligatorio']"
    />
    <v-select
      v-model="formUtente.regione"
      :items="regioni"
      label="Regione"
      :rules="[v => !!v || 'Seleziona una regione']"
      @update:model-value="caricaProvince"
      required
    />
    <v-select
      v-model="formUtente.provincia"
      :items="province"
      label="Provincia"
      :rules="[v => !!v || 'Seleziona una provincia']"
      :disabled="province.length === 0"
      required
    />
    <v-text-field v-model="formUtente.citta" label="Città" required 
      :rules="[v => !!v || 'Campo obbligatorio']"
    />
    <v-text-field v-model="formUtente.indirizzo" label="Indirizzo" required 
      :rules="[v => !!v || 'Campo obbligatorio']"
    />
    <v-text-field v-model="formUtente.cap" label="CAP" 
      :rules="[
        v => !v || /^[0-9]{1,5}$/.test(v) || 'Inserisci solo cifre numeriche, massimo 5'
      ]"
      maxlength="5"
      type="text"
      inputmode="numeric"
    />

    <v-btn
      color="primary"
      class="mt-2"
      @click="salvaNuovoUtente"
      :disabled="!isUtenteFormCompleto"
    >
      Salva utente
    </v-btn>
  </div>

  <!-- ✅ Mostra il form prenotazione SOLO se:
       - l’utente non vuole salvare
       - oppure la mail è già presente
       - oppure ha appena salvato -->
  <v-form
    v-if="salvaEmail === 'no' || emailGiaPresente || utenteSalvato"
    ref="formRef"
    @submit.prevent="inviaPrenotazione"
    v-model="isFormValid"
  >
    <!-- Campo Nome solo se salvaEmail === 'no' -->
    <v-text-field
      v-if="salvaEmail === 'no'"
      v-model="form.nome"
      label="Nome"
      required
      :rules="[v => !!v || 'Nome richiesto']"
    />
  
    <!-- Campo telefono -->
    <p style="margin-top: 10px; margin-left: 15px;">Telefono</p>
    <vue-tel-input
      v-model="form.cellulare"
      mode="international"
      :input-options="{ showDialCode: true }"
      :preferred-countries="['IT']"
      required
    />
    <p v-if="form.cellulare && !isValidPhone(form.cellulare)" class="error-message">
      Numero non valido
    </p>

    <!-- Campo giorno -->
    <v-menu
      v-model="menu"
      :close-on-content-click="false"
      transition="scale-transition"
      offset-y
      max-width="290"
      min-width="auto"
    >
      <template #activator="{ props }">
        <v-text-field
          :model-value="formattaGiorno(form.giorno)"
          label="Giorno"
          prepend-icon="mdi-calendar"
          readonly
          v-bind="props"
          :rules="[v => !!v || 'Seleziona un giorno']"
          required
        />
      </template>
      <v-date-picker
        v-model="form.giorno"
        :min="oggi"
        :allowed-dates="isGiornoDisponibile"
        @update:model-value="onGiornoChange"
        color="primary"
      />
    </v-menu>

    <!-- Fascia oraria -->
    <v-select
      v-model="form.fasciaOrariaId"
      :items="fasceDisponibili"
      item-title="label"
      item-value="id"
      label="Fascia Oraria"
      required
      :disabled="fasceDisponibili.length === 0"
      :rules="[v => !!v || 'Seleziona una fascia oraria']"
    />

    <v-select
      v-model="form.motivazioniDescrizioni"
      :items="motivazioni.map(m => m.descrizione)"
      label="Motivazioni"
      multiple
      chips
      clearable
      :rules="[v => v.length > 0 || 'Seleziona almeno una motivazione']"
    />

    <v-textarea
      v-model="form.note"
      label="Note"
      rows="3"
      auto-grow
      clearable
    />

    <v-btn type="submit" color="primary" :disabled="!isFormCompleto">
      Prenota
    </v-btn>
  </v-form>
</div>

    <v-dialog v-model="dialogConflitto" max-width="500">
      <v-card>
        <v-card-title class="text-h6">Attenzione</v-card-title>
        <v-card-text>{{ messaggioConflitto }}</v-card-text>
        <v-card-actions class="justify-end">
          <v-btn text @click="dialogConflitto = false">Annulla</v-btn>
          <v-btn color="red" @click="eliminaPrenotazioneConflitto">Elimina appuntamento</v-btn>
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
  name: 'PrenotazioneForm',
  data() {
    return {
      form: {
        nome: '',
        email: '',
        cellulare: '',
        giorno: '',
        fasciaOrariaId: '',
        motivazioniDescrizioni: [],
        note: ''
      },

      formUtente: {
        nome: '',
        cognome: '',
        regione: '',
        provincia: '',
        citta: '',
        indirizzo: '',
        cap: ''
      },
      utenteSalvato: false,
      regioni: [],
      province: [],
      motivazioni: [],
      salvaEmail: null,
      emailVerificata: false,
      emailGiaPresente: false,
      stepEmailCompletato: false,

      fasceDisponibili: [],
      giorniDisponibili: [],
      menu: false,
      oggi: this.getLocalDateString(new Date()),
      isFormValid: false,

      // stati per il dialog di conflitto
      dialogConflitto: false,
      messaggioConflitto: '',
      prenotazioneIdConflitto: null
    }
  },
  async mounted() {
    this.caricaGiorniDisponibili();
    this.caricaRegioni();
    this.caricaMotivazioni();
  },

  computed: {
    isFormCompleto() {
      return (
        (this.salvaEmail === 'no' ? this.form.nome : true) &&  // se no, nome è obbligatorio
        this.isValidEmail(this.form.email) &&
        this.form.cellulare &&
        this.form.giorno &&
        this.form.fasciaOrariaId &&
        this.isValidPhone(this.form.cellulare) &&
        this.form.motivazioniDescrizioni.length > 0
      )
    },

    isUtenteFormCompleto() {
      return (
        this.formUtente.nome &&
        this.formUtente.cognome &&
        this.formUtente.regione &&
        this.formUtente.provincia &&
        this.formUtente.citta &&
        this.formUtente.indirizzo
      );
    }
  },

  methods: {
    getLocalDateString(date) {
      if (!(date instanceof Date)) date = new Date(date)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    formattaGiorno(date) {
      if (!date) return ''
      return new Date(date).toDateString()
    },

    async caricaGiorniDisponibili() {
      try {
        const res = await this.$axios.get('/api/giorni-disponibili')
        this.giorniDisponibili = res.data
      } catch (e) {
        console.error('Errore caricamento giorni disponibili:', e)
      }
    },
    
    isGiornoDisponibile(date) {
      const giornoStr = this.getLocalDateString(date)
      return this.giorniDisponibili.includes(giornoStr)
    },
    
    async caricaFasceDisponibili() {
      if (!this.form.giorno) {
        this.fasceDisponibili = []
        this.form.fasciaOrariaId = ''
        return
      }

      const giornoFormattato = this.getLocalDateString(this.form.giorno)

      try {
        const res = await this.$axios.get('/api/fasce-disponibili', {
          params: { giorno: giornoFormattato }
        })

        this.fasceDisponibili = res.data.map(f => ({
          id: f.id,
          label: `${f.oraInizio} - ${f.oraFine}`
        }))
        this.form.fasciaOrariaId = ''
      } catch (err) {
        console.error('Errore nel caricamento delle fasce disponibili:', err)
        this.fasceDisponibili = []
        this.form.fasciaOrariaId = ''
      }
    },

    onGiornoChange(val) {
      this.menu = false
      this.form.giorno = val
      this.caricaFasceDisponibili()
    },

    isValidEmail(email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(email);
    },

  isValidPhone(phone) {
    if (typeof phone !== 'string') return false;
    // Rimuovi spazi e trattini o altri caratteri non numerici eccetto '+'
    const cleaned = phone.replace(/[\s\-()]/g, '');
    return /^\+?[1-9]\d{7,14}$/.test(cleaned);
  },

  

  async inviaPrenotazione() {
  const formVuetifyValido = await this.$refs.formRef.validate();

  if (!formVuetifyValido) {
    alert('Controlla i campi obbligatori.');
    return;
  }

  if (!this.isValidPhone(this.form.cellulare)) {
    alert('Numero di telefono non valido. Deve essere nel formato E.164 (es. +393331112233).');
    return;
  }

  let nomeDaInviare = '';

  try {
    // Verifica se l'email è già presente
    const verificaRes = await this.$axios.get(`/utenti/email-gia-presente?email=${encodeURIComponent(this.form.email)}`);
    const emailGiaPresente = verificaRes.data;

    if (this.salvaEmail === 'si') {
      if (emailGiaPresente) {
        // Se l'utente esiste, recupera i suoi dati
        try {
          const utenteRes = await this.$axios.get(`/utenti/${encodeURIComponent(this.form.email)}`);
          const utente = utenteRes.data;
          nomeDaInviare = utente?.nome || '';
        } catch (err) {
          console.error('Errore durante il caricamento dell’utente esistente:', err);
          alert('Errore durante il recupero dei dati utente.');
          return;
        }
      } else {
        // Se l'utente NON esiste, validiamo i dati anagrafici per crearlo
        const campiRichiesti = ['nome', 'cognome', 'regione', 'provincia', 'citta', 'indirizzo'];
        const mancanoCampi = campiRichiesti.some(campo => !this.formUtente[campo]);

        if (mancanoCampi) {
          alert('Compila tutti i campi del modulo utente prima di prenotare.');
          return;
        }

        try {
          const utentePayload = {
            ...this.formUtente,
            email: this.form.email // copiamo l’email da `form`
          };
          await this.$axios.post('/utenti', utentePayload);
          nomeDaInviare = this.formUtente.nome;
        } catch (error) {
          console.error('Errore durante la creazione dell’utente:', error);
          alert('Errore durante la creazione dell’utente.');
          return;
        }
      }
    } else {
      // Se NON voglio salvare l'email, uso il nome inserito manualmente
      nomeDaInviare = this.form.nome;
    }
  } catch (err) {
    console.error('Errore durante la verifica dell’email:', err);
    alert('Errore durante la verifica dell’email.');
    return;
  }

  if (!nomeDaInviare) {
    alert('Il nome non è disponibile. Impossibile completare la prenotazione.');
    return;
  }

  const cleanedNumber = this.form.cellulare.replace(/[\s\-()]/g, '');

  const payloadPrenotazione = {
    nome: nomeDaInviare,
    email: this.form.email,
    cellulare: cleanedNumber,
    giorno: this.getLocalDateString(this.form.giorno),
    fasciaOrariaId: this.form.fasciaOrariaId,
    motivazioniDescrizioni: Array.isArray(this.form.motivazioniDescrizioni)
      ? [...this.form.motivazioniDescrizioni]
      : [],
    note: this.form.note
  };

  console.log('✅ Payload da inviare:', payloadPrenotazione);

  try {
    await this.$axios.post('/api/prenotazioni/light', payloadPrenotazione);
    alert('Prenotazione effettuata con successo!');
    this.resetForm();
  } catch (error) {
    console.error('Errore durante la prenotazione:', error);

    if (error.response && error.response.status === 409) {
      const data = error.response.data;
      this.messaggioConflitto = data.errore || 'Cellulare già in lista.';
      this.prenotazioneIdConflitto = data.id || null;
      this.dialogConflitto = true;
    } else {
      alert('Errore durante la prenotazione.');
    }
  }
},

eliminaPrenotazioneConflitto() {
  if (!this.prenotazioneIdConflitto) return;

  this.$axios.delete(`/api/prenotazioni/${this.prenotazioneIdConflitto}`)
    .then(() => {
      alert('Appuntamento eliminato. Ora puoi riprovare.');
      this.dialogConflitto = false;
    })
    .catch(() => {
      alert("Errore durante l'eliminazione.");
    });
},

async controllaEmail() {
  if (!this.isValidEmail(this.form.email)) return;

  try {
    const res = await this.$axios.get(`/utenti/email-gia-presente`, {
      params: { email: this.form.email }
    });

    this.emailGiaPresente = res.data;
    this.emailVerificata = true;

    // Se la mail è nuova e voglio salvarla, precompilo il formUtente con email
    if (!res.data && this.salvaEmail === 'si') {
      this.formUtente.email = this.form.email;
    }
  } catch (e) {
    console.error('Errore durante il controllo email:', e);
    this.emailVerificata = false;
  }
},

async vaiAvantiDopoEmail() {
  if (!this.isValidEmail(this.form.email)) return;

  try {
    const res = await this.$axios.get(`/utenti/email-gia-presente`, {
      params: { email: this.form.email }
    });

    this.emailGiaPresente = res.data;
    this.emailVerificata = true;
    this.stepEmailCompletato = true;

    if (!res.data && this.salvaEmail === 'si') {
      this.formUtente.email = this.form.email;
    }
  } catch (e) {
    console.error('Errore durante la verifica email:', e);
    alert('Errore durante il controllo email.');
  }
},

async caricaRegioni() {
  try {
    const res = await this.$axios.get('/territorio/regioni');
    this.regioni = res.data;
  } catch (e) {
    console.error('Errore caricamento regioni:', e);
  }
},

caricaProvince(regioneSelezionata) {
  this.formUtente.provincia = ''; // 🔁 reset provincia selezionata
  this.province = [];

  if (!regioneSelezionata) return;

  this.$axios
    .get(`/territorio/province?regione=${encodeURIComponent(regioneSelezionata)}`)
    .then(res => {
      this.province = res.data;
    })
    .catch(err => {
      console.error('Errore nel caricamento province:', err);
    });
},

async salvaNuovoUtente() {
  const campiRichiesti = ['nome', 'cognome', 'regione', 'provincia', 'citta', 'indirizzo'];
  const mancanoCampi = campiRichiesti.some(campo => !this.formUtente[campo]);

  if (mancanoCampi) {
    alert('Compila tutti i campi anagrafici.');
    return;
  }

  try {
    const payload = {
      ...this.formUtente,
      email: this.form.email
    };

    await this.$axios.post('/utenti', payload);
    alert('Utente salvato con successo!');
    this.utenteSalvato = true;
  } catch (error) {
    console.error('Errore durante il salvataggio dell’utente:', error);
    alert('Errore durante il salvataggio.');
  }
},

async caricaMotivazioni() {
  try {
    const res = await this.$axios.get('/api/motivazioni');
    this.motivazioni = res.data;
  } catch (e) {
    console.error('Errore caricamento motivazioni:', e);
  }
},

    resetForm() {
      this.form = {
        nome: '',
        email: '',
        cellulare: '',
        giorno: '',
        fasciaOrariaId: ''
      }
      this.fasceDisponibili = []
      this.isFormValid = false
    }
  }
}
</script>

<style>
.error-message {
  color: #B00020; 
  font-size: 12px;
  margin-top: 4px;
  margin-left: 15px;
  font-weight: 500;
}
</style>
<template>
  <v-container>
    <v-row>
      <!-- Colonna sinistra: form -->
      <v-col :style="{ width: '30%', maxWidth: '30%' }">
        <utente-edit-form :email="email" @utenteAggiornato="caricaPrenotazioni" />
      </v-col>

      <!-- Colonna destra: tabella -->
      <v-col :style="{ width: '70%', maxWidth: '70%' }">
        <storico-prenotazioni-utente :email="email" :key="email" />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import UtenteEditForm from '@/components/UtenteEditForm.vue'
import StoricoPrenotazioniUtente from '@/components/StoricoPrenotazioniUtente.vue'

export default {
  name: 'Utente',
  components: {
    UtenteEditForm,
    StoricoPrenotazioniUtente
  },
  props: {},
  data() {
    return {
      email: this.$route.params.email
    }
  },
  methods: {
    caricaPrenotazioni() {
      // opzionale: puoi usarlo per forzare il refresh del figlio
    }
  }
}
</script>

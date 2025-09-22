<template>
  <v-container>
    <h2>Test VueTelInput con messaggio di errore semplice</h2>

    <vue-tel-input
      v-model="telefono"
      mode="international"
      :preferred-countries="['IT', 'US', 'GB']"
      :input-options="{ showDialCode: true }"
      @blur="controllaNumero"
      :class="{'error-input': erroreTelefono}"
    />

    <!-- Messaggio di errore semplice -->
    <p v-if="erroreTelefono" style="color: #B00020; margin-top: 4px; font-size: 12px;">
      {{ erroreTelefono }}
    </p>

    <p>📞 Numero inserito: {{ telefono }}</p>
  </v-container>
</template>

<script>
export default {
  data() {
    return {
      telefono: '',
      erroreTelefono: '',
    }
  },
  methods: {
    isValidPhone(phone) {
      if (typeof phone !== 'string') return false
      const cleaned = phone.replace(/[\s\-()]/g, '')
      return /^\+?[1-9]\d{7,14}$/.test(cleaned)
    },
    controllaNumero() {
      if (!this.telefono) {
        this.erroreTelefono = 'Cellulare richiesto'
      } else if (!this.isValidPhone(this.telefono)) {
        this.erroreTelefono = 'Numero non valido (formato E.164)'
      } else {
        this.erroreTelefono = ''
      }
    }
  }
}
</script>

<style>
.error-input input {
  border-color: red !important;
}
</style>

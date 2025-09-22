<template>
  <v-menu offset-y bottom left>
    <template #activator="{ props }">
      <v-btn
        v-bind="props"
        icon
        class="ma-2"
        aria-label="Seleziona pagina"
      >
        <v-icon>mdi-menu</v-icon>
      </v-btn>
    </template>

    <v-list>
      <v-list-item
        v-for="page in pages"
        :key="page.name"
        @click="navigate(page.path)"
        :class="{ 'font-weight-bold': currentRoute === page.path }"
      >
        <v-list-item-title>{{ page.label }}</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ref, computed } from 'vue'

const router = useRouter()
const route = useRoute()

const pages = ref([
  { path: '/admin-prenotazioni', name: 'AdminPrenotazioni', label: 'Admin Prenotazioni' },
  { path: '/prenotazione-form', name: 'PrenotazioneForm', label: 'Prenotazione Form' },
  { path: '/admin-reperibilita', name: 'AdminReperibilita', label: 'Admin Reperibilità' },
])

const currentRoute = computed(() => route.path)

function navigate(path) {
  if (path !== route.path) {
    router.push(path)
  }
}
</script>

<style scoped>
.v-list-item {
  cursor: pointer;
}

.v-list-item.font-weight-bold {
  background-color: #E0E0E0;
}
</style>

<script setup lang="ts">
import { useUserStore } from '~/stores/user'

const router = useRouter()
const user = useUserStore()
</script>

<template>
  <v-toolbar>
    <v-toolbar
      color="primary"
      :dark="true"
    >
      <v-toolbar-title font-ark>HeMing Fast</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn
        v-if="user.roleCode === 'platform-super-admin' || user.roleCode === 'platform-admin' || user.roleCode === 'platform-self-provisioner' || user.roleCode === 'platform-view'"
        icon="mdi-view-dashboard"
        @click="router.push('/admin')"
      />
      <DarkToggle />
    </v-toolbar>
  </v-toolbar>
</template>

<style scoped>

</style>
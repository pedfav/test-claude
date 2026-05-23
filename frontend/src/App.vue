<template>
  <router-view />
</template>

<script setup lang="ts">
import { useThemeStore } from '@/store/theme'
useThemeStore() // initializes on first access, applies data-theme to <html>
</script>

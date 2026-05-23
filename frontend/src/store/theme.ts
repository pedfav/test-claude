import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const stored = localStorage.getItem('theme')
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  const dark = ref(stored ? stored === 'dark' : prefersDark)

  function apply() {
    document.documentElement.setAttribute('data-theme', dark.value ? 'dark' : 'light')
    localStorage.setItem('theme', dark.value ? 'dark' : 'light')
  }

  function toggle() {
    dark.value = !dark.value
    apply()
  }

  apply()

  return { dark, toggle }
})

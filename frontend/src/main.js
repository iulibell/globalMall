import './assets/main.css'

import { createApp, watch } from 'vue'
import App from './App.vue'
import { router } from './router'
import { preloadDictionaries } from '@/api/dict.js'
import { useUiLang } from '@/composables/useUiLang.js'

const MALL_DICT_TYPES = ['page_mall', 'page_mall_profile', 'ui_lang_option', 'ui_lang_panel']
const { uiLang } = useUiLang()

function warmupMallDictionaries() {
  return preloadDictionaries(MALL_DICT_TYPES).catch(() => undefined)
}

router.beforeResolve(async () => {
  await warmupMallDictionaries()
})

watch(
  () => uiLang.value,
  () => {
    warmupMallDictionaries()
  },
  { immediate: true },
)

createApp(App).use(router).mount('#app')

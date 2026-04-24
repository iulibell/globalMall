import { ref } from 'vue'

const STORAGE_KEY = 'mall_ui_lang'

function readStoredLang() {
  try {
    return localStorage.getItem(STORAGE_KEY) || 'zh-CN'
  } catch {
    return 'zh-CN'
  }
}

const uiLang = ref(typeof localStorage !== 'undefined' ? readStoredLang() : 'zh-CN')

/** 站点界面语言：zh-CN | en | ru，与 dictionary.lang 映射见 {@link ../constants/dictLang.js} */
export function useUiLang() {
  function setUiLang(v) {
    const next = v === 'en' || v === 'ru' || v === 'zh-CN' ? v : 'zh-CN'
    uiLang.value = next
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      /* ignore */
    }
  }

  return { uiLang, setUiLang }
}

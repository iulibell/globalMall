<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { useUiLang } from '@/composables/useUiLang.js'
import { useMultiDictionary } from '@/composables/useMultiDictionary.js'
import { useSortedDictionaryOptions } from '@/composables/useSortedDictionaryOptions.js'
import { pageDictFallback } from '@/utils/pageDictionaryFallback.js'

const { uiLang, setUiLang } = useUiLang()

const open = ref(false)
const root = ref(null)

const FALLBACK_LANG_OPTIONS = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'en', label: 'English' },
  { value: 'ru', label: 'Русский' },
]

const LANG_CODES = ['zh-CN', 'en', 'ru']

const { options: langOptions } = useSortedDictionaryOptions('ui_lang_option', uiLang, FALLBACK_LANG_OPTIONS)

const displayLangOptions = computed(() => {
  const labelByValue = new Map(langOptions.value.map((o) => [o.value, o.label]))
  return LANG_CODES.map((value) => ({
    value,
    label:
      labelByValue.get(value) ??
      FALLBACK_LANG_OPTIONS.find((f) => f.value === value)?.label ??
      value,
  }))
})

const { t: dictT } = useMultiDictionary(['ui_lang_panel'], uiLang)

const helpTranslateLabel = computed(() =>
  dictT('ui_lang_panel', 'help_translate', pageDictFallback('ui_lang_panel', 'help_translate', uiLang.value)),
)

const currentLabel = computed(() => {
  const v = uiLang.value
  return (
    displayLangOptions.value.find((l) => l.value === v)?.label ??
    (v === 'en' ? 'English' : v === 'ru' ? 'Русский' : '简体中文')
  )
})

function selectLang(v) {
  setUiLang(v)
  open.value = false
}

function onDocClick(e) {
  const el = root.value
  if (!el || el.contains(e.target)) return
  open.value = false
}

watch(open, (isOpen) => {
  if (!isOpen) {
    document.removeEventListener('click', onDocClick, true)
    return
  }
  nextTick(() => {
    document.addEventListener('click', onDocClick, true)
  })
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocClick, true)
})
</script>

<template>
  <div ref="root" class="lang-dropdown">
    <button
      type="button"
      class="lang-trigger"
      :aria-expanded="open"
      aria-haspopup="listbox"
      @click.stop="open = !open"
    >
      <span class="lang-trigger-text">{{ currentLabel }}</span>
      <span class="lang-trigger-caret" aria-hidden="true" />
    </button>

    <div v-show="open" class="lang-panel" role="listbox" @click.stop>
      <span class="lang-panel-top">{{ helpTranslateLabel }}</span>
      <div class="lang-divider" />
      <button
        v-for="opt in displayLangOptions"
        :key="opt.value"
        type="button"
        class="lang-option"
        :class="{ active: uiLang === opt.value }"
        role="option"
        :aria-selected="uiLang === opt.value"
        @click="selectLang(opt.value)"
      >
        {{ opt.label }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.lang-dropdown {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.lang-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 0;
  min-height: 38px;
  padding: 6px 10px;
  box-sizing: border-box;
  border: none;
  background: transparent;
  font-size: 0.8rem;
  font-weight: 500;
  font-family: inherit;
  color: var(--mall-text-muted);
  cursor: pointer;
  line-height: 1.25;
}

.lang-trigger:hover {
  color: var(--mall-orange-bright);
}

.lang-trigger:focus {
  outline: none;
}

.lang-trigger:focus-visible {
  border-radius: 4px;
  box-shadow: 0 0 0 2px var(--mall-orange-glow);
}

.lang-trigger-caret {
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 5px solid var(--mall-text-muted);
  flex-shrink: 0;
  margin-top: 2px;
}

.lang-panel {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 200px;
  max-height: 280px;
  overflow-y: auto;
  padding: 0;
  margin: 0;
  background: var(--mall-surface-elevated);
  border: 1px solid var(--mall-border);
  border-radius: 8px;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.45);
  z-index: 100;
  text-align: left;
}

.lang-panel-top {
  display: block;
  padding: 12px 16px;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--mall-text-muted);
}

.lang-divider {
  height: 1px;
  margin: 0;
  background: var(--mall-border);
  border: none;
}

.lang-option {
  display: block;
  width: 100%;
  margin: 0;
  padding: 12px 16px;
  border: none;
  background: transparent;
  font-size: 0.875rem;
  font-weight: 500;
  font-family: inherit;
  color: var(--mall-text);
  text-align: left;
  cursor: pointer;
  line-height: 1.45;
}

.lang-option:hover {
  background: rgba(255, 138, 0, 0.1);
}

.lang-option.active {
  font-weight: 700;
  background: rgba(255, 138, 0, 0.16);
}
</style>

import { shallowRef, watch } from 'vue'
import { fetchDictionary } from '@/api/dict.js'
import { uiLangToDictLang } from '@/constants/dictLang.js'
import { dictLangSwitchBegin, dictLangSwitchEnd } from '@/composables/useShellLoading.js'

/**
 * 并行拉取多组 dict_type，按当前界面语言聚合成 dictValue -> dictName。
 * @param {string[]} dictTypes
 * @param {import('vue').Ref<string>} uiLangRef
 */
export function useMultiDictionary(dictTypes, uiLangRef) {
  /** @type {import('vue').ShallowRef<Record<string, Record<string, string>>>} */
  const maps = shallowRef({})
  const loading = shallowRef(false)

  async function load() {
    loading.value = true
    const lang = uiLangToDictLang(uiLangRef.value)
    const next = {}
    try {
      await Promise.all(
        dictTypes.map(async (dt) => {
          const res = await fetchDictionary(dt)
          if (res.code !== 200) return
          const list = Array.isArray(res.data) ? res.data : []
          const m = Object.create(null)
          for (const row of list) {
            if (String(row.lang) !== lang) continue
            m[row.dictValue] = row.dictName
          }
          next[dt] = m
        }),
      )
      maps.value = next
    } finally {
      loading.value = false
    }
  }

  let firstLangWatch = true
  watch(
    () => uiLangRef.value,
    () => {
      const trackShell = !firstLangWatch
      firstLangWatch = false
      if (trackShell) dictLangSwitchBegin()
      Promise.resolve(load()).finally(() => {
        if (trackShell) dictLangSwitchEnd()
      })
    },
    { immediate: true },
  )

  /**
   * @param {string} dictType
   * @param {string} dictValue
   * @param {string} [fallback]
   */
  function t(dictType, dictValue, fallback = '') {
    return maps.value[dictType]?.[dictValue] ?? fallback
  }

  return { maps, loading, load, t }
}

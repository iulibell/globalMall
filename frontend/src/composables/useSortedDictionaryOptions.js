import { ref, watch } from 'vue'
import { fetchDictionary } from '@/api/dict.js'
import { uiLangToDictLang } from '@/constants/dictLang.js'
import { dictLangSwitchBegin, dictLangSwitchEnd } from '@/composables/useShellLoading.js'

/**
 * 拉取某一 dict_type，按当前界面语言过滤并按 sort 排序，得到下拉选项列表。
 * @param {string} dictType
 * @param {import('vue').Ref<string>} uiLangRef
 * @param {{ value: string, label: string }[]} fallbackList
 */
export function useSortedDictionaryOptions(dictType, uiLangRef, fallbackList) {
  const options = ref([...fallbackList])

  async function load() {
    try {
      const res = await fetchDictionary(dictType)
      if (res.code !== 200) {
        options.value = [...fallbackList]
        return
      }
      const lang = uiLangToDictLang(uiLangRef.value)
      const list = (Array.isArray(res.data) ? res.data : [])
        .filter((r) => String(r.lang) === lang)
        .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
      const mapped = list.map((r) => ({ value: r.dictValue, label: r.dictName }))
      options.value = mapped.length ? mapped : [...fallbackList]
    } catch {
      options.value = [...fallbackList]
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

  return { options, load }
}

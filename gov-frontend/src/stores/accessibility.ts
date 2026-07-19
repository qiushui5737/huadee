import { ref } from 'vue'
import { defineStore } from 'pinia'
import * as OpenCC from 'opencc-js'

type Language = 'zh-CN' | 'zh-TW'
type FontScale = 100 | 120 | 140

interface AccessibilitySettings {
  language: Language
  fontScale: FontScale
  highContrast: boolean
  highlightLinks: boolean
  reduceMotion: boolean
}

const STORAGE_KEY = 'gov_accessibility_settings'
const toTraditional = OpenCC.Converter({ from: 'cn', to: 'tw' })
const textOriginals = new WeakMap<Text, string>()
const attributeOriginals = new WeakMap<Element, Map<string, string>>()
const translatedAttributes = ['placeholder', 'title', 'aria-label']
let observer: MutationObserver | undefined

function readSettings(): AccessibilitySettings {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
    return {
      language: saved.language === 'zh-TW' ? 'zh-TW' : 'zh-CN',
      fontScale: [100, 120, 140].includes(saved.fontScale) ? saved.fontScale : 100,
      highContrast: Boolean(saved.highContrast),
      highlightLinks: Boolean(saved.highlightLinks),
      reduceMotion: Boolean(saved.reduceMotion)
    }
  } catch {
    return { language: 'zh-CN', fontScale: 100, highContrast: false, highlightLinks: false, reduceMotion: false }
  }
}

export const useAccessibilityStore = defineStore('accessibility', () => {
  const initial = readSettings()
  const language = ref<Language>(initial.language)
  const fontScale = ref<FontScale>(initial.fontScale)
  const highContrast = ref(initial.highContrast)
  const highlightLinks = ref(initial.highlightLinks)
  const reduceMotion = ref(initial.reduceMotion)
  const toolbarOpen = ref(false)
  const reading = ref(false)

  function persist() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      language: language.value,
      fontScale: fontScale.value,
      highContrast: highContrast.value,
      highlightLinks: highlightLinks.value,
      reduceMotion: reduceMotion.value
    }))
  }

  function translateTextNode(node: Text, dynamic = false) {
    const parent = node.parentElement
    if (!parent || ['SCRIPT', 'STYLE', 'NOSCRIPT', 'TEXTAREA'].includes(parent.tagName)) return
    const current = node.nodeValue || ''
    let original = textOriginals.get(node)
    if (original === undefined || (dynamic && (
      (language.value === 'zh-CN' && current !== original)
      || (language.value === 'zh-TW' && current !== toTraditional(original))
    ))) {
      original = current
      textOriginals.set(node, original)
    }
    node.nodeValue = language.value === 'zh-TW' ? toTraditional(original) : original
  }

  function translateAttributes(element: Element, dynamic = false) {
    let originals = attributeOriginals.get(element)
    if (!originals) {
      originals = new Map()
      attributeOriginals.set(element, originals)
    }
    for (const name of translatedAttributes) {
      if (!element.hasAttribute(name)) continue
      const current = element.getAttribute(name) || ''
      let original = originals.get(name)
      if (original === undefined || (dynamic && (
        (language.value === 'zh-CN' && current !== original)
        || (language.value === 'zh-TW' && current !== toTraditional(original))
      ))) {
        original = current
        originals.set(name, original)
      }
      element.setAttribute(name, language.value === 'zh-TW' ? toTraditional(original) : original)
    }
  }

  function translateTree(root: Node, dynamic = false) {
    if (root.nodeType === Node.TEXT_NODE) translateTextNode(root as Text, dynamic)
    if (root.nodeType === Node.ELEMENT_NODE) {
      const element = root as Element
      translateAttributes(element, dynamic)
      const walker = document.createTreeWalker(element, NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT)
      let node = walker.nextNode()
      while (node) {
        if (node.nodeType === Node.TEXT_NODE) translateTextNode(node as Text, dynamic)
        else translateAttributes(node as Element, dynamic)
        node = walker.nextNode()
      }
    }
  }

  function refreshLanguage() {
    observer?.disconnect()
    document.documentElement.lang = language.value
    document.title = language.value === 'zh-TW' ? toTraditional(document.title) : '政府网站集约化平台'
    translateTree(document.body)
    observer?.observe(document.body, { childList: true, subtree: true, characterData: true, attributes: true, attributeFilter: translatedAttributes })
  }

  function observeDynamicContent() {
    observer?.disconnect()
    observer = new MutationObserver((mutations) => {
      observer?.disconnect()
      for (const mutation of mutations) {
        if (mutation.type === 'characterData') translateTextNode(mutation.target as Text, true)
        else if (mutation.type === 'attributes') translateAttributes(mutation.target as Element, true)
        else mutation.addedNodes.forEach((node) => translateTree(node, true))
      }
      observer?.observe(document.body, { childList: true, subtree: true, characterData: true, attributes: true, attributeFilter: translatedAttributes })
    })
    refreshLanguage()
  }

  function applyVisualSettings() {
    const root = document.documentElement
    root.dataset.a11yFont = String(fontScale.value)
    root.classList.toggle('a11y-high-contrast', highContrast.value)
    root.classList.toggle('a11y-highlight-links', highlightLinks.value)
    root.classList.toggle('a11y-reduce-motion', reduceMotion.value)
    persist()
  }

  function setLanguage(value: Language) {
    language.value = value
    persist()
    refreshLanguage()
  }

  function toggleLanguage() {
    setLanguage(language.value === 'zh-CN' ? 'zh-TW' : 'zh-CN')
  }

  function setFontScale(value: FontScale) {
    fontScale.value = value
    applyVisualSettings()
  }

  function toggleHighContrast() {
    highContrast.value = !highContrast.value
    applyVisualSettings()
  }

  function toggleHighlightLinks() {
    highlightLinks.value = !highlightLinks.value
    applyVisualSettings()
  }

  function toggleReduceMotion() {
    reduceMotion.value = !reduceMotion.value
    applyVisualSettings()
  }

  function startReading() {
    if (!('speechSynthesis' in window)) return false
    window.speechSynthesis.cancel()
    const content = document.querySelector('#app-main')?.textContent?.replace(/\s+/g, ' ').trim() || ''
    if (!content) return false
    const utterance = new SpeechSynthesisUtterance(content)
    utterance.lang = language.value
    utterance.rate = 0.9
    utterance.onend = () => { reading.value = false }
    utterance.onerror = () => { reading.value = false }
    reading.value = true
    window.speechSynthesis.speak(utterance)
    return true
  }

  function stopReading() {
    window.speechSynthesis?.cancel()
    reading.value = false
  }

  function reset() {
    language.value = 'zh-CN'
    fontScale.value = 100
    highContrast.value = false
    highlightLinks.value = false
    reduceMotion.value = false
    stopReading()
    applyVisualSettings()
    refreshLanguage()
  }

  function initialize() {
    applyVisualSettings()
    observeDynamicContent()
  }

  return {
    language, fontScale, highContrast, highlightLinks, reduceMotion, toolbarOpen, reading,
    initialize, setLanguage, toggleLanguage, setFontScale, toggleHighContrast,
    toggleHighlightLinks, toggleReduceMotion, startReading, stopReading, reset
  }
})

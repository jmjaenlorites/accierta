import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import enUS from './locales/en-US.json'
import esES from './locales/es-ES.json'

const supportedLocales = ['es-ES', 'en-US'] as const
const storedLocale = localStorage.getItem('accierta.frontend.locale')
const browserLocale = navigator.language

const initialLocale =
  storedLocale && supportedLocales.includes(storedLocale as (typeof supportedLocales)[number])
    ? storedLocale
    : supportedLocales.includes(browserLocale as (typeof supportedLocales)[number])
      ? browserLocale
      : 'es-ES'

void i18n.use(initReactI18next).init({
  resources: {
    'es-ES': { translation: esES },
    'en-US': { translation: enUS }
  },
  lng: initialLocale,
  fallbackLng: 'es-ES',
  interpolation: {
    escapeValue: false
  }
})

document.documentElement.lang = i18n.language
i18n.on('languageChanged', (language) => {
  localStorage.setItem('accierta.frontend.locale', language)
  document.documentElement.lang = language
})

export default i18n

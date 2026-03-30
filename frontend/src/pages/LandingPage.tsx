import { useEffect, useRef, useState, useCallback } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, Navigate } from 'react-router-dom'
import { useSession } from '../lib/useSession'
import {
  Target,
  Coins,
  Trophy,
  Users,
  UserPlus,
  Dices,
  ShieldCheck,
  Zap,
  Heart,
  ChevronDown,
} from 'lucide-react'

function useScrollReveal() {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const container = ref.current
    if (!container) return

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('revealed')
            observer.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.15 }
    )

    const elements = container.querySelectorAll('[data-reveal]')
    elements.forEach((el) => observer.observe(el))

    return () => observer.disconnect()
  }, [])

  return ref
}

function FaqItem({ question, answer }: { question: string; answer: string }) {
  const [open, setOpen] = useState(false)

  return (
    <div className="landing-faq-item">
      <button
        className="landing-faq-question"
        onClick={() => setOpen(!open)}
        aria-expanded={open}
      >
        <span>{question}</span>
        <ChevronDown
          size={20}
          className={`landing-faq-chevron${open ? ' open' : ''}`}
        />
      </button>
      <div className={`landing-faq-answer${open ? ' open' : ''}`}>
        <p className="landing-faq-answer-text">{answer}</p>
      </div>
    </div>
  )
}

const FEATURES = [
  { key: 'predictions', Icon: Target, color: 'var(--primary)', bg: 'rgba(108, 92, 231, 0.12)' },
  { key: 'points', Icon: Coins, color: 'var(--secondary)', bg: 'rgba(0, 206, 201, 0.12)' },
  { key: 'leaderboard', Icon: Trophy, color: '#d4a017', bg: 'rgba(253, 203, 110, 0.2)' },
  { key: 'groups', Icon: Users, color: 'var(--win)', bg: 'rgba(0, 184, 148, 0.12)' },
] as const

const STEPS = [
  { key: '1', Icon: UserPlus },
  { key: '2', Icon: Dices },
  { key: '3', Icon: Trophy },
] as const

const TRUST = [
  { key: 'private', Icon: ShieldCheck, color: 'var(--win)', bg: 'rgba(0, 184, 148, 0.12)' },
  { key: 'realtime', Icon: Zap, color: 'var(--primary)', bg: 'rgba(108, 92, 231, 0.12)' },
  { key: 'free', Icon: Heart, color: 'var(--danger)', bg: 'rgba(225, 112, 85, 0.12)' },
] as const

export function LandingPage() {
  const { session, ready } = useSession()
  const { t } = useTranslation()
  const pageRef = useScrollReveal()
  const [scrolled, setScrolled] = useState(false)
  const heroRef = useRef<HTMLDivElement>(null)

  const handleScroll = useCallback(() => {
    if (heroRef.current) {
      const rect = heroRef.current.getBoundingClientRect()
      setScrolled(rect.bottom < 56)
    }
  }, [])

  useEffect(() => {
    window.addEventListener('scroll', handleScroll, { passive: true })
    return () => window.removeEventListener('scroll', handleScroll)
  }, [handleScroll])

  if (!ready) return null
  if (session) return <Navigate to="/groups" replace />

  return (
    <div className="landing-page" ref={pageRef}>
      <main>
      <div className={`landing-nav-wrapper${scrolled ? ' scrolled' : ''}`}>
        <nav className="landing-nav">
          <div className="landing-nav-brand">
            <div className="brand-mark" aria-label="Accierta">A</div>
            <span className="landing-nav-brand-text">Accierta</span>
          </div>
          <Link className="btn primary sm" to="/register">
            {t('landing.nav.cta')}
          </Link>
        </nav>
      </div>

      {/* Hero */}
      <section className="landing-hero" ref={heroRef}>
        <div className="anim-scale-in">
          <div className="brand-mark xl" aria-label="Accierta">A</div>
        </div>
        <div className="anim-fade-in" style={{ animationDelay: '200ms' }}>
          <h1 className="landing-hero-title">{t('landing.hero.title')}</h1>
          <p className="landing-hero-subtitle" style={{ marginTop: 'var(--space-3)' }}>
            {t('landing.hero.subtitle')}
          </p>
        </div>
        <div className="landing-hero-actions anim-fade-in" style={{ animationDelay: '400ms' }}>
          <Link className="btn primary full" to="/register">
            {t('landing.hero.cta')}
          </Link>
          <p className="landing-hero-login">
            {t('landing.hero.login')}{' '}
            <Link to="/login">{t('landing.hero.loginLink')}</Link>
          </p>
        </div>
        <ChevronDown
          size={24}
          className={`landing-scroll-hint${scrolled ? ' hidden' : ''}`}
          style={{ opacity: scrolled ? 0 : 0.5 }}
        />
      </section>

      {/* Features */}
      <section className="landing-section" data-reveal>
        <div className="landing-section-header">
          <p className="eyebrow">{t('landing.features.eyebrow')}</p>
          <h2 className="section-title">{t('landing.features.title')}</h2>
        </div>
        <div className="landing-features-grid stagger">
          {FEATURES.map(({ key, Icon, color, bg }) => (
            <div className="landing-feature-card anim-fade-in" key={key}>
              <div
                className="landing-feature-icon"
                style={{ background: bg }}
              >
                <Icon size={24} style={{ color }} />
              </div>
              <h3 className="landing-feature-title">
                {t(`landing.features.${key}.title`)}
              </h3>
              <p className="landing-feature-desc">
                {t(`landing.features.${key}.description`)}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* How it works */}
      <section className="landing-section tinted" data-reveal>
        <div className="landing-section-header">
          <p className="eyebrow">{t('landing.steps.eyebrow')}</p>
          <h2 className="section-title">{t('landing.steps.title')}</h2>
        </div>
        <div className="landing-steps">
          {STEPS.map(({ key, Icon }) => (
            <div className="landing-step" key={key}>
              <div className="landing-step-rail">
                <div className="landing-step-number">{key}</div>
                <div className="landing-step-line" />
              </div>
              <div className="landing-step-content">
                <Icon size={28} className="landing-step-icon" />
                <h3 className="landing-step-title">
                  {t(`landing.steps.${key}.title`)}
                </h3>
                <p className="landing-step-desc">
                  {t(`landing.steps.${key}.description`)}
                </p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Trust */}
      <section className="landing-section" data-reveal>
        <div className="landing-section-header">
          <p className="eyebrow">{t('landing.trust.eyebrow')}</p>
          <h2 className="section-title">{t('landing.trust.title')}</h2>
          <p className="page-subtitle">{t('landing.trust.subtitle')}</p>
        </div>
        <div className="landing-trust-list stagger">
          {TRUST.map(({ key, Icon, color, bg }) => (
            <div className="landing-trust-card anim-fade-in" key={key}>
              <div
                className="landing-trust-icon"
                style={{ background: bg }}
              >
                <Icon size={22} style={{ color }} />
              </div>
              <div>
                <h3 className="landing-trust-title">
                  {t(`landing.trust.${key}.title`)}
                </h3>
                <p className="landing-trust-detail">
                  {t(`landing.trust.${key}.detail`)}
                </p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* FAQ */}
      <section className="landing-section tinted" data-reveal>
        <div className="landing-section-header">
          <p className="eyebrow">{t('landing.faq.eyebrow')}</p>
          <h2 className="section-title">{t('landing.faq.title')}</h2>
        </div>
        <div className="landing-faq-list">
          {[1, 2, 3, 4].map((n) => (
            <FaqItem
              key={n}
              question={t(`landing.faq.${n}.question`)}
              answer={t(`landing.faq.${n}.answer`)}
            />
          ))}
        </div>
      </section>

      {/* Footer CTA */}
      <section className="landing-section" data-reveal>
        <div className="landing-footer">
          <div className="brand-mark" aria-label="Accierta">A</div>
          <h2 className="landing-footer-title">{t('landing.footer.title')}</h2>
          <p className="page-subtitle">{t('landing.footer.subtitle')}</p>
          <div className="landing-footer-actions">
            <Link className="btn primary full" to="/register">
              {t('landing.footer.cta')}
            </Link>
            <p className="landing-hero-login">
              {t('landing.footer.login')}{' '}
              <Link to="/login">{t('landing.footer.loginLink')}</Link>
            </p>
          </div>
          <div className="landing-footer-fine">
            <span>{t('landing.footer.copyright', { year: new Date().getFullYear() })}</span>
            <span>·</span>
            <Link to="/privacy">{t('landing.footer.privacy')}</Link>
            <span>·</span>
            <Link to="/terms">{t('landing.footer.terms')}</Link>
          </div>
        </div>
      </section>
      </main>
    </div>
  )
}

<template>
  <div class="min-h-screen bg-white text-slate-900">
    <header class="sticky top-0 z-40 border-b border-slate-200/80 bg-white/95 backdrop-blur">
      <div class="mx-auto flex max-w-6xl items-center justify-between px-6 py-5">
        <a href="#hero" class="flex items-center gap-3">
          <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-100 text-lg font-semibold text-emerald-600 shadow-sm">
            NV
          </div>
          <span class="text-lg font-semibold tracking-tight">NutriVision AI</span>
        </a>
        <nav class="hidden items-center gap-10 text-sm font-medium text-slate-600 lg:flex">
          <a href="#features" class="transition-colors hover:text-emerald-600">{{ t('nav.features') }}</a>
          <a href="#demo" class="transition-colors hover:text-emerald-600">{{ t('nav.demo') }}</a>
          <a href="#how" class="transition-colors hover:text-emerald-600">{{ t('nav.how') }}</a>
          <a href="#contact" class="transition-colors hover:text-emerald-600">{{ t('nav.contact') }}</a>
        </nav>
        <div class="flex items-center gap-3">
          <div class="hidden items-center gap-1 rounded-full border border-slate-200 bg-slate-50 p-1 text-xs font-semibold lg:flex">
            <button
              type="button"
              class="flex items-center gap-1 rounded-full px-3 py-1 transition-all"
              :class="locale === 'pt' ? 'bg-white text-emerald-600 shadow-sm' : 'text-slate-500 hover:text-emerald-600'"
              @click="changeLocale('pt')"
            >
              🇧🇷 <span>PT</span>
            </button>
            <button
              type="button"
              class="flex items-center gap-1 rounded-full px-3 py-1 transition-all"
              :class="locale === 'en' ? 'bg-white text-emerald-600 shadow-sm' : 'text-slate-500 hover:text-emerald-600'"
              @click="changeLocale('en')"
            >
              🇬🇧 <span>EN</span>
            </button>
          </div>
          <RouterLink
            to="/login"
            class="hidden rounded-full border border-emerald-500 px-4 py-2 text-sm font-semibold text-emerald-600 transition-all hover:bg-emerald-500 hover:text-white lg:inline-flex"
          >
            {{ t('nav.login') }}
          </RouterLink>
          <button
            type="button"
            class="inline-flex h-10 w-10 items-center justify-center rounded-full border border-slate-200 text-slate-600 transition-all hover:border-emerald-300 hover:text-emerald-600 lg:hidden"
            @click="isMenuOpen = !isMenuOpen"
          >
            <span class="sr-only">Toggle menu</span>
            <svg v-if="!isMenuOpen" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="h-6 w-6">
              <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
            </svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="h-6 w-6">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
      <div v-if="isMenuOpen" class="border-t border-slate-200 bg-white lg:hidden">
        <div class="space-y-2 px-6 py-4 text-sm font-medium text-slate-600">
          <a href="#features" class="block rounded-xl px-3 py-2 transition-all hover:bg-slate-100">{{ t('nav.features') }}</a>
          <a href="#demo" class="block rounded-xl px-3 py-2 transition-all hover:bg-slate-100">{{ t('nav.demo') }}</a>
          <a href="#how" class="block rounded-xl px-3 py-2 transition-all hover:bg-slate-100">{{ t('nav.how') }}</a>
          <a href="#contact" class="block rounded-xl px-3 py-2 transition-all hover:bg-slate-100">{{ t('nav.contact') }}</a>
          <div class="flex items-center gap-2 pt-2">
            <button
              type="button"
              class="flex-1 rounded-xl border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-600 transition-all hover:text-emerald-600"
              :class="locale === 'pt' ? 'border-emerald-500 text-emerald-600' : ''"
              @click="changeLocale('pt')"
            >
              🇧🇷 PT
            </button>
            <button
              type="button"
              class="flex-1 rounded-xl border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-600 transition-all hover:text-emerald-600"
              :class="locale === 'en' ? 'border-emerald-500 text-emerald-600' : ''"
              @click="changeLocale('en')"
            >
              🇬🇧 EN
            </button>
          </div>
          <RouterLink
            to="/login"
            class="mt-2 block rounded-xl border border-emerald-500 px-3 py-2 text-center text-sm font-semibold text-emerald-600 transition-all hover:bg-emerald-500 hover:text-white"
          >
            {{ t('nav.login') }}
          </RouterLink>
        </div>
      </div>
    </header>

    <main>
      <section id="hero" class="relative overflow-hidden bg-gradient-to-br from-emerald-50 via-white to-slate-50">
        <div class="mx-auto grid max-w-6xl gap-12 px-6 py-16 lg:grid-cols-2 lg:items-center lg:py-28">
          <div class="space-y-6">
            <span class="inline-flex items-center gap-2 rounded-full bg-emerald-100 px-4 py-1 text-sm font-semibold text-emerald-700">
              <span class="h-2 w-2 rounded-full bg-emerald-500"></span>
              {{ t('hero.badge') }}
            </span>
            <h1 class="text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl">
              {{ t('hero.title') }}
            </h1>
            <p class="max-w-xl text-lg leading-relaxed text-slate-600">
              {{ t('hero.subtitle') }}
            </p>
            <div class="flex flex-col gap-4 sm:flex-row">
              <a
                href="https://wa.me/"
                target="_blank"
                rel="noreferrer"
                class="inline-flex items-center justify-center rounded-full bg-emerald-500 px-6 py-3 text-sm font-semibold text-white shadow-sm transition-all hover:scale-105 hover:shadow-md"
              >
                {{ t('hero.demo') }}
              </a>
              <a
                href="#demo"
                class="inline-flex items-center justify-center rounded-full border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-700 transition-all hover:border-emerald-300 hover:text-emerald-600"
              >
                {{ t('hero.watch') }}
              </a>
              <RouterLink
                to="/login"
                class="inline-flex items-center justify-center rounded-full border border-transparent bg-slate-900 px-6 py-3 text-sm font-semibold text-white shadow-sm transition-all hover:scale-105 hover:bg-slate-800"
              >
                {{ t('hero.loginCta') }}
              </RouterLink>
            </div>
          </div>
          <div class="relative rounded-3xl bg-white p-8 shadow-xl">
            <div class="absolute -left-10 -top-10 hidden h-40 w-40 rounded-3xl bg-emerald-100 opacity-70 blur-2xl lg:block"></div>
            <div class="absolute -bottom-12 -right-10 hidden h-40 w-40 rounded-full bg-emerald-200 opacity-70 blur-3xl lg:block"></div>
            <div class="relative space-y-6">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-medium text-slate-500">{{ t('hero.snapshot.title') }}</p>
                  <p class="text-2xl font-semibold text-slate-900">{{ t('hero.snapshot.totalCalories') }}</p>
                </div>
                <span class="rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold text-emerald-700">{{ t('hero.snapshot.balance') }}</span>
              </div>
              <div class="space-y-4">
                <div v-for="item in heroSnapshot" :key="item.label" class="flex items-center justify-between rounded-2xl bg-slate-50 px-4 py-3">
                  <div>
                    <p class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ item.label }}</p>
                    <p class="text-lg font-semibold text-slate-900">{{ item.value }}</p>
                  </div>
                  <span class="text-xs font-medium text-emerald-600">{{ item.delta }}</span>
                </div>
              </div>
              <div class="rounded-2xl border border-dashed border-emerald-200 p-4 text-sm text-slate-500">
                {{ t('hero.snapshot.footer') }}
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="features" class="bg-slate-50 py-20">
        <div class="mx-auto max-w-6xl px-6">
          <div class="mx-auto max-w-2xl text-center">
            <h2 class="text-3xl font-semibold text-slate-900 sm:text-4xl">{{ t('features.title') }}</h2>
            <p class="mt-4 text-base text-slate-600">{{ t('features.subtitle') }}</p>
          </div>
          <div class="mt-12 grid gap-6 md:grid-cols-2">
            <article
              v-for="card in featureCards"
              :key="card.title"
              class="group rounded-3xl border border-slate-100 bg-white p-8 shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg"
            >
              <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-100 text-emerald-600 shadow-sm">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="h-6 w-6">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 class="mt-6 text-xl font-semibold text-slate-900">{{ card.title }}</h3>
              <p class="mt-3 text-sm leading-relaxed text-slate-600">{{ card.text }}</p>
            </article>
          </div>
        </div>
      </section>

      <section id="how" class="bg-white py-20">
        <div class="mx-auto max-w-6xl px-6">
          <div class="flex flex-col gap-12 lg:flex-row lg:items-center">
            <div class="lg:w-1/3">
              <h2 class="text-3xl font-semibold text-slate-900 sm:text-4xl">{{ t('how.title') }}</h2>
              <p class="mt-4 text-base text-slate-600">{{ t('how.subtitle') }}</p>
            </div>
            <div class="grid flex-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
              <div
                v-for="(step, index) in howSteps"
                :key="step.step"
                class="group rounded-3xl border border-slate-100 bg-slate-50/60 p-6 transition-all hover:-translate-y-1 hover:bg-white hover:shadow-lg"
              >
                <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-white text-lg font-semibold text-emerald-600 shadow-sm">0{{ index + 1 }}</div>
                <h3 class="mt-5 text-lg font-semibold text-slate-900">{{ step.step }}</h3>
                <p class="mt-2 text-sm leading-relaxed text-slate-600">{{ step.desc }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="demo" class="bg-slate-50 py-20">
        <div class="mx-auto grid max-w-6xl gap-12 px-6 lg:grid-cols-2 lg:items-center">
          <div class="rounded-3xl bg-white p-8 shadow-lg">
            <p class="text-sm font-semibold uppercase tracking-wide text-emerald-600">{{ t('demo.badge') }}</p>
            <h2 class="mt-3 text-3xl font-semibold text-slate-900">{{ t('demo.title') }}</h2>
            <p class="mt-3 text-sm text-slate-600">{{ t('demo.subtitle') }}</p>
            <div class="mt-8 space-y-4">
              <div
                v-for="(message, index) in demoChat"
                :key="index"
                class="flex items-start gap-3"
                :class="message.from === 'ai' ? 'flex-row' : 'flex-row-reverse text-right'
              "
              >
                <div
                  class="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl"
                  :class="message.from === 'ai' ? 'bg-emerald-100 text-emerald-600' : 'bg-slate-900 text-white'"
                >
                  {{ message.from === 'ai' ? 'AI' : 'You' }}
                </div>
                <div
                  class="max-w-[75%] rounded-2xl p-4 text-sm leading-relaxed shadow-sm transition-all"
                  :class="message.from === 'ai' ? 'bg-emerald-50 text-slate-700' : 'bg-slate-900 text-white'"
                >
                  {{ message.text }}
                </div>
              </div>
            </div>
          </div>
          <div class="rounded-3xl border border-slate-100 bg-white p-8 shadow-sm">
            <h3 class="text-2xl font-semibold text-slate-900">{{ t('demo.form.title') }}</h3>
            <p class="mt-2 text-sm text-slate-600">{{ t('demo.form.subtitle') }}</p>
            <form class="mt-6 space-y-4" @submit.prevent="submitDemo">
              <div>
                <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('demo.form.name') }}</label>
                <input
                  v-model="demoForm.name"
                  type="text"
                  class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm focus:border-emerald-500 focus:bg-white focus:outline-none"
                  :placeholder="t('demo.form.namePlaceholder')"
                />
              </div>
              <div>
                <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('demo.form.email') }}</label>
                <input
                  v-model="demoForm.email"
                  type="email"
                  class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm focus:border-emerald-500 focus:bg-white focus:outline-none"
                  :placeholder="t('demo.form.emailPlaceholder')"
                />
              </div>
              <div>
                <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('demo.form.whatsapp') }}</label>
                <input
                  v-model="demoForm.whatsapp"
                  type="tel"
                  class="mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm focus:border-emerald-500 focus:bg-white focus:outline-none"
                  :placeholder="t('demo.form.whatsappPlaceholder')"
                />
              </div>
              <button
                type="submit"
                class="w-full rounded-full bg-emerald-500 px-6 py-3 text-sm font-semibold text-white shadow-sm transition-all hover:scale-105 hover:shadow-md"
              >
                {{ t('demo.form.send') }}
              </button>
            </form>
          </div>
        </div>
      </section>

      <section id="contact" class="bg-white py-20">
        <div class="mx-auto max-w-6xl px-6">
          <div class="grid gap-12 lg:grid-cols-[1.1fr_0.9fr] lg:items-start">
            <div class="rounded-3xl border border-slate-100 bg-slate-50 p-8 shadow-sm">
              <h2 class="text-3xl font-semibold text-slate-900">{{ t('contact.title') }}</h2>
              <p class="mt-2 text-sm text-slate-600">{{ t('contact.subtitle') }}</p>
              <form class="mt-8 space-y-5" @submit.prevent="submitContact">
                <div>
                  <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('contact.name') }}</label>
                  <input
                    v-model="contactForm.name"
                    type="text"
                    class="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm focus:border-emerald-500 focus:outline-none"
                    :placeholder="t('contact.namePlaceholder')"
                  />
                </div>
                <div>
                  <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('contact.email') }}</label>
                  <input
                    v-model="contactForm.email"
                    type="email"
                    class="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm focus:border-emerald-500 focus:outline-none"
                    :placeholder="t('contact.emailPlaceholder')"
                  />
                </div>
                <div>
                  <label class="text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('contact.message') }}</label>
                  <textarea
                    v-model="contactForm.message"
                    rows="4"
                    class="mt-2 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm focus:border-emerald-500 focus:outline-none"
                    :placeholder="t('contact.messagePlaceholder')"
                  ></textarea>
                </div>
                <button
                  type="submit"
                  class="w-full rounded-full bg-slate-900 px-6 py-3 text-sm font-semibold text-white shadow-sm transition-all hover:scale-105 hover:bg-slate-800"
                >
                  {{ t('contact.send') }}
                </button>
              </form>
            </div>
            <div class="space-y-6">
              <div class="rounded-3xl border border-slate-100 bg-slate-50 p-8 shadow-sm">
                <h3 class="text-xl font-semibold text-slate-900">{{ t('contact.reach') }}</h3>
                <div class="mt-6 space-y-5 text-sm text-slate-600">
                  <a
                    href="https://wa.me/"
                    target="_blank"
                    rel="noreferrer"
                    class="flex items-center justify-between rounded-2xl bg-white px-4 py-3 transition-all hover:scale-[1.01] hover:shadow-md"
                  >
                    <span>{{ t('contact.whatsapp') }}</span>
                    <span class="text-emerald-600">WhatsApp</span>
                  </a>
                  <a
                    href="mailto:contact@nutrivision.ai"
                    class="flex items-center justify-between rounded-2xl bg-white px-4 py-3 transition-all hover:scale-[1.01] hover:shadow-md"
                  >
                    <span>{{ t('contact.emailLabel') }}</span>
                    <span class="text-emerald-600">Email</span>
                  </a>
                  <a
                    href="#"
                    class="flex items-center justify-between rounded-2xl bg-white px-4 py-3 transition-all hover:scale-[1.01] hover:shadow-md"
                  >
                    <span>{{ t('contact.schedule') }}</span>
                    <span class="text-emerald-600">Calendly</span>
                  </a>
                </div>
              </div>
              <div class="rounded-3xl border border-emerald-100 bg-emerald-50/60 p-8 text-sm text-emerald-700 shadow-sm">
                {{ t('contact.note') }}
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <footer class="border-t border-slate-200 bg-slate-50 py-6">
      <div class="mx-auto flex max-w-6xl flex-col items-center justify-between gap-4 px-6 text-xs text-slate-500 sm:flex-row">
        <p>{{ t('footer') }}</p>
        <div class="flex gap-6">
          <a href="#features" class="transition-colors hover:text-emerald-600">{{ t('nav.features') }}</a>
          <a href="#demo" class="transition-colors hover:text-emerald-600">{{ t('nav.demo') }}</a>
          <a href="#contact" class="transition-colors hover:text-emerald-600">{{ t('nav.contact') }}</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { useI18n } from 'vue-i18n';

const messages = {
  en: {
    nav: {
      features: 'Features',
      demo: 'Demo',
      how: 'How it works',
      contact: 'Contact',
      login: 'Login',
    },
    hero: {
      badge: 'AI-powered nutrition',
      title: 'NutriVision AI',
      subtitle:
        'Turn any meal photo into instant, trustworthy nutrition insights. Designed for seamless WhatsApp conversations and holistic guidance.',
      demo: 'WhatsApp Demo',
      watch: 'Watch Demo',
      loginCta: 'Log in',
      snapshot: {
        title: 'Daily balance',
        totalCalories: '1,845 kcal',
        balance: 'Goal aligned',
        macros: [
          { label: 'Protein', value: '95g', delta: '+12% vs goal' },
          { label: 'Carbs', value: '220g', delta: '-8% vs goal' },
          { label: 'Fats', value: '65g', delta: '+2% vs goal' },
        ],
        footer: 'NutriVision keeps every conversation contextual, adapting recommendations to real habits.',
      },
    },
    features: {
      title: 'What makes NutriVision different',
      subtitle: 'Purpose-built to deliver clarity and accountability in every conversation.',
      cards: [
        {
          title: 'Instant Insights',
          text: 'Snap a photo and get nutritional breakdowns, calories, and smarter swaps.',
        },
        {
          title: 'WhatsApp Native',
          text: 'No app installs—just chat and get personalized feedback instantly.',
        },
        {
          title: 'Smart Learning',
          text: 'Adapts to your preferences, restrictions, and habits over time.',
        },
        {
          title: 'Holistic Health',
          text: 'Goes beyond calories: sleep, hydration, and movement-friendly choices.',
        },
      ],
    },
    how: {
      title: 'From photo to insights in seconds',
      subtitle: 'A guided, conversational workflow that feels human while scaling your nutrition practice.',
      steps: [
        { step: 'Capture your meal', desc: 'Take a photo or paste a link directly in WhatsApp.' },
        { step: 'AI analyzes instantly', desc: 'The model estimates calories, macros, and allergens.' },
        { step: 'Get actionable insights', desc: 'Receive improvement tips while keeping your food tasty.' },
        { step: 'Track over time', desc: 'Daily and weekly summaries show your progress.' },
        { step: 'Stay engaged', desc: 'Ask follow-ups or generate grocery lists directly in chat.' },
      ],
    },
    demo: {
      badge: 'Interactive demo',
      title: 'See NutriVision in conversation',
      subtitle: 'Preview the WhatsApp experience customers love.',
      chat: [
        { from: 'user', text: 'Here is my lunch today! Quinoa bowl with salmon and veggies.' },
        {
          from: 'ai',
          text: 'Looks nourishing! Estimated 540 kcal · Protein 36g · Carbs 48g · Fats 22g. Want to balance dinner with lighter carbs?',
        },
        {
          from: 'ai',
          text: 'Tip: add leafy greens tonight to keep fiber high without overshooting calories.',
        },
        { from: 'user', text: "Sounds good. Can you suggest a 500 kcal dinner?" },
        {
          from: 'ai',
          text: 'Try grilled chicken with roasted vegetables and a citrus salad. I’ll log it and remind you at 7pm.',
        },
      ],
      form: {
        title: 'Request your personalised demo link',
        subtitle: 'Share your details and receive a guided walkthrough via WhatsApp.',
        name: 'Full name',
        namePlaceholder: 'Jane Doe',
        email: 'Work email',
        emailPlaceholder: 'jane@company.com',
        whatsapp: 'WhatsApp number',
        whatsappPlaceholder: '+55 11 90000-0000',
        send: 'Send me the demo',
      },
    },
    contact: {
      title: 'Talk to our team',
      subtitle: 'We’ll tailor NutriVision to your workflows and objectives.',
      name: 'Full name',
      namePlaceholder: 'Your name',
      email: 'Work email',
      emailPlaceholder: 'you@company.com',
      message: 'Tell us a bit about what you need...',
      messagePlaceholder: 'Share goals, team size, timelines, integrations…',
      send: 'Send message',
      reach: 'Reach us directly',
      whatsapp: 'Open chat',
      emailLabel: 'contact@nutrivision.ai',
      schedule: 'Schedule',
      note: 'Prefer a hands-on session? We’ll craft a custom onboarding that blends AI assistance with your coaching style.',
    },
    footer: '© 2025 NutriVision AI · All rights reserved',
  },
  pt: {
    nav: {
      features: 'Funcionalidades',
      demo: 'Demonstração',
      how: 'Como funciona',
      contact: 'Contato',
      login: 'Entrar',
    },
    hero: {
      badge: 'Nutrição com Inteligência Artificial',
      title: 'NutriVision AI',
      subtitle:
        'Transforme qualquer foto de refeição em uma análise nutricional instantânea e confiável, direto pelo WhatsApp.',
      demo: 'Demo no WhatsApp',
      watch: 'Ver Demonstração',
      loginCta: 'Fazer login',
      snapshot: {
        title: 'Balanço diário',
        totalCalories: '1.845 kcal',
        balance: 'Meta alinhada',
        macros: [
          { label: 'Proteínas', value: '95g', delta: '+12% vs meta' },
          { label: 'Carboidratos', value: '220g', delta: '-8% vs meta' },
          { label: 'Gorduras', value: '65g', delta: '+2% vs meta' },
        ],
        footer: 'O NutriVision mantém cada conversa contextual, adaptando recomendações aos seus hábitos reais.',
      },
    },
    features: {
      title: 'Por que escolher o NutriVision AI',
      subtitle: 'Inteligência que entrega clareza e acompanhamento em cada mensagem.',
      cards: [
        {
          title: 'Análises Instantâneas',
          text: 'Tire uma foto e receba calorias, macros e dicas inteligentes em segundos.',
        },
        {
          title: 'Integração WhatsApp',
          text: 'Sem precisar instalar nada — converse e receba recomendações personalizadas.',
        },
        {
          title: 'Aprendizado Inteligente',
          text: 'Adapta-se às suas preferências, restrições e hábitos com o tempo.',
        },
        {
          title: 'Saúde Holística',
          text: 'Vai além das calorias: sono, hidratação e bem-estar.',
        },
      ],
    },
    how: {
      title: 'Da foto ao insight em segundos',
      subtitle: 'Um fluxo conversacional que combina tecnologia e atendimento humano.',
      steps: [
        { step: 'Capture sua refeição', desc: 'Envie uma foto ou link direto no WhatsApp.' },
        { step: 'A IA analisa instantaneamente', desc: 'O modelo estima calorias, macros e detecta alérgenos.' },
        { step: 'Receba insights práticos', desc: 'Dicas de melhoria mantendo o sabor e o equilíbrio.' },
        { step: 'Acompanhe sua evolução', desc: 'Relatórios diários e semanais mostram seu progresso.' },
        { step: 'Mantenha-se engajado', desc: 'Peça alternativas ou gere listas de compras direto no chat.' },
      ],
    },
    demo: {
      badge: 'Demonstração interativa',
      title: 'Veja o NutriVision em conversa',
      subtitle: 'Antecipe a experiência no WhatsApp que os clientes adoram.',
      chat: [
        { from: 'user', text: 'Olha meu almoço de hoje! Tigela de quinoa com salmão e legumes.' },
        {
          from: 'ai',
          text: 'Super equilibrado! Estimativa 540 kcal · Proteínas 36g · Carboidratos 48g · Gorduras 22g. Quer equilibrar o jantar com menos carboidratos?',
        },
        {
          from: 'ai',
          text: 'Dica: inclua folhas verdes no jantar para manter fibras altas sem extrapolar calorias.',
        },
        { from: 'user', text: 'Perfeito. Sugere um jantar de 500 kcal?' },
        {
          from: 'ai',
          text: 'Experimente frango grelhado com legumes assados e salada cítrica. Vou registrar e lembrar você às 19h.',
        },
      ],
      form: {
        title: 'Peça um link de demonstração personalizado',
        subtitle: 'Envie seus dados e receba um tour guiado pelo WhatsApp.',
        name: 'Nome completo',
        namePlaceholder: 'Ana Silva',
        email: 'E-mail corporativo',
        emailPlaceholder: 'ana@empresa.com',
        whatsapp: 'Número do WhatsApp',
        whatsappPlaceholder: '+55 11 90000-0000',
        send: 'Quero receber a demo',
      },
    },
    contact: {
      title: 'Fale com nossa equipe',
      subtitle: 'Vamos adaptar o NutriVision aos seus processos e metas.',
      name: 'Nome completo',
      namePlaceholder: 'Seu nome',
      email: 'E-mail corporativo',
      emailPlaceholder: 'voce@empresa.com',
      message: 'Conte-nos um pouco sobre o que você precisa...',
      messagePlaceholder: 'Compartilhe objetivos, tamanho da equipe, prazos, integrações…',
      send: 'Enviar mensagem',
      reach: 'Contatos diretos',
      whatsapp: 'Abrir chat',
      emailLabel: 'contact@nutrivision.ai',
      schedule: 'Agendar reunião',
      note: 'Prefere algo prático? Criamos uma jornada personalizada que combina o toque humano com o apoio da IA.',
    },
    footer: '© 2025 NutriVision AI · Todos os direitos reservados',
  },
};

const isMenuOpen = ref(false);
const { t, locale, tm } = useI18n({ legacy: false, locale: 'en', fallbackLocale: 'en', messages });

const heroSnapshot = computed(() => tm('hero.snapshot.macros'));
const featureCards = computed(() => tm('features.cards'));
const howSteps = computed(() => tm('how.steps'));
const demoChat = computed(() => tm('demo.chat'));

const demoForm = reactive({
  name: '',
  email: '',
  whatsapp: '',
});

const contactForm = reactive({
  name: '',
  email: '',
  message: '',
});

const changeLocale = (value) => {
  locale.value = value;
};

const submitDemo = () => {
  console.log('Demo form submitted', { ...demoForm });
};

const submitContact = () => {
  console.log('Contact form submitted', { ...contactForm });
};
</script>

<style scoped>
:global(body) {
  font-family: 'Inter', 'Poppins', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
</style>

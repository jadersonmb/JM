<template>
  <div class="min-h-screen bg-gray-50 text-slate-900">
    <header class="fixed inset-x-0 top-0 z-50 border-b border-slate-200 bg-white/95 shadow-sm backdrop-blur">
      <div class="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <a href="#hero" class="flex items-center gap-3" @click.prevent="scrollTo('hero')">
          <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-100 text-lg font-semibold text-emerald-600 shadow-sm">
            🌿
          </div>
          <span class="text-lg font-semibold tracking-tight">NutriVision AI</span>
        </a>
        <nav class="hidden items-center gap-10 text-sm font-medium text-slate-600 lg:flex">
          <button type="button" class="transition-colors hover:text-emerald-600" @click="scrollTo('features')">
            {{ t('nav.features') }}
          </button>
          <button type="button" class="transition-colors hover:text-emerald-600" @click="scrollTo('pricing')">
            {{ t('nav.pricing') }}
          </button>
          <button type="button" class="transition-colors hover:text-emerald-600" @click="scrollTo('demo')">
            {{ t('nav.demo') }}
          </button>
          <button type="button" class="transition-colors hover:text-emerald-600" @click="scrollTo('how')">
            {{ t('nav.how') }}
          </button>
          <button type="button" class="transition-colors hover:text-emerald-600" @click="scrollTo('contact')">
            {{ t('nav.contact') }}
          </button>
        </nav>
        <div class="flex items-center gap-4">
          <button
            type="button"
            class="hidden rounded-full bg-emerald-500 px-4 py-2 text-sm font-semibold text-white shadow-sm transition-all hover:scale-105 hover:shadow-md lg:inline-flex"
            @click="openWhatsApp"
          >
            {{ t('nav.chat') }}
          </button>
          <RouterLink
            to="/login"
            class="hidden rounded-full border border-emerald-200 px-4 py-2 text-sm font-semibold text-emerald-600 shadow-sm transition-all hover:border-emerald-300 hover:text-emerald-700 lg:inline-flex"
          >
            {{ t('nav.login') }}
          </RouterLink>
          <div class="flex items-center gap-1 rounded-full border border-slate-200 bg-slate-50 p-1 text-xs font-semibold">
            <button
              type="button"
              class="flex items-center gap-1 rounded-full px-3 py-1 transition-all"
              :class="locale === 'en' ? 'bg-white text-emerald-600 shadow-sm' : 'text-slate-500 hover:text-emerald-600'"
              @click="setLocale('en')"
            >
              <span>EN</span>
            </button>
            <button
              type="button"
              class="flex items-center gap-1 rounded-full px-3 py-1 transition-all"
              :class="locale === 'pt' ? 'bg-white text-emerald-600 shadow-sm' : 'text-slate-500 hover:text-emerald-600'"
              @click="setLocale('pt')"
            >
              <span>PT</span>
            </button>
          </div>
          <button
            type="button"
            class="inline-flex h-10 w-10 items-center justify-center rounded-full border border-slate-200 text-slate-600 transition-all hover:border-emerald-300 hover:text-emerald-600 lg:hidden"
            @click="isMenuOpen = !isMenuOpen"
          >
            <span class="sr-only">Toggle navigation</span>
            <svg
              v-if="!isMenuOpen"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke-width="1.5"
              stroke="currentColor"
              class="h-6 w-6"
            >
              <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
            </svg>
            <svg
              v-else
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke-width="1.5"
              stroke="currentColor"
              class="h-6 w-6"
            >
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
      <div v-if="isMenuOpen" class="border-t border-slate-200 bg-white lg:hidden">
        <div class="space-y-3 px-6 py-4 text-sm font-medium text-slate-600">
          <button type="button" class="block w-full rounded-xl px-3 py-2 text-left transition-all hover:bg-slate-100" @click="handleMobileNav('features')">
            {{ t('nav.features') }}
          </button>
          <button type="button" class="block w-full rounded-xl px-3 py-2 text-left transition-all hover:bg-slate-100" @click="handleMobileNav('pricing')">
            {{ t('nav.pricing') }}
          </button>
          <button type="button" class="block w-full rounded-xl px-3 py-2 text-left transition-all hover:bg-slate-100" @click="handleMobileNav('demo')">
            {{ t('nav.demo') }}
          </button>
          <button type="button" class="block w-full rounded-xl px-3 py-2 text-left transition-all hover:bg-slate-100" @click="handleMobileNav('how')">
            {{ t('nav.how') }}
          </button>
          <button type="button" class="block w-full rounded-xl px-3 py-2 text-left transition-all hover:bg-slate-100" @click="handleMobileNav('contact')">
            {{ t('nav.contact') }}
          </button>
          <button
            type="button"
            class="flex w-full items-center justify-center rounded-xl bg-emerald-500 px-3 py-2 text-sm font-semibold text-white shadow-sm transition-all hover:scale-105 hover:shadow-md"
            @click="openWhatsApp"
          >
            {{ t('nav.chat') }}
          </button>
          <RouterLink
            to="/login"
            class="block w-full rounded-xl border border-emerald-200 px-3 py-2 text-center text-sm font-semibold text-emerald-600 transition-all hover:border-emerald-300 hover:text-emerald-700"
            @click="isMenuOpen = false"
          >
            {{ t('nav.login') }}
          </RouterLink>
        </div>
      </div>
    </header>

    <main class="pt-24">
      <section id="hero" class="relative overflow-hidden bg-gradient-to-br from-emerald-50 via-white to-slate-50">
        <div class="mx-auto grid max-w-6xl items-center gap-12 px-6 py-20 lg:grid-cols-2 lg:py-28">
            <div class="space-y-6">
            <span class="inline-flex items-center gap-2 rounded-full bg-emerald-100 px-4 py-1 text-sm font-semibold text-emerald-700">
              <span class="h-2 w-2 rounded-full bg-emerald-500"></span>
              {{ t('hero.badge') }}
            </span>
            <h1 class="text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl">{{ t('hero.title') }}</h1>
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
                href="https://www.youtube.com/results?search_query=nutrivision+ai"
                target="_blank"
                rel="noreferrer"
                class="inline-flex items-center justify-center rounded-full border border-slate-200 px-6 py-3 text-sm font-semibold text-slate-700 transition-all hover:border-emerald-300 hover:text-emerald-600"
              >
                {{ t('hero.watch') }}
              </a>
            </div>
          </div>
          <div class="relative">
            <div class="absolute -left-12 -top-16 hidden h-48 w-48 rounded-3xl bg-emerald-200 opacity-40 blur-3xl lg:block"></div>
            <div class="absolute -bottom-16 -right-12 hidden h-56 w-56 rounded-full bg-emerald-100 opacity-40 blur-3xl lg:block"></div>
            <div class="relative overflow-hidden rounded-3xl border border-emerald-100 bg-white shadow-xl">
              <img
                src="https://cdn.abrahao.com.br/base/178/254/5ac/fotografando-comida-celular.jpg"
                alt="Healthy meal"
                class="h-full w-full object-cover"
                loading="lazy"
              />
            </div>
          </div>
        </div>
      </section>

      <section id="features" class="bg-gray-50">
        <div class="mx-auto max-w-6xl px-6 py-20">
          <div class="mx-auto max-w-2xl text-center">
            <h2 class="text-3xl font-semibold text-slate-900 sm:text-4xl">{{ t('features.title') }}</h2>
            <p class="mt-4 text-base text-slate-600">{{ t('features.subtitle') }}</p>
          </div>
          <div class="mt-12 grid gap-6 lg:grid-cols-4">
            <article
              v-for="card in featureCards"
              :key="card.title"
              class="group flex flex-col gap-3 rounded-2xl border border-emerald-100 bg-emerald-50 p-6 text-left shadow-sm transition-all hover:-translate-y-1 hover:shadow-md"
            >
              <div class="text-3xl">{{ card.icon }}</div>
              <h3 class="text-xl font-semibold text-slate-900">{{ card.title }}</h3>
              <p class="text-sm leading-relaxed text-slate-600">{{ card.description }}</p>
            </article>
          </div>
        </div>
      </section>

      <section id="pricing" class="bg-white py-20">
        <div class="mx-auto max-w-6xl px-6 text-center">
          <h2 class="mb-12 text-3xl font-bold text-slate-900 sm:text-4xl">{{ t('pricing.title') }}</h2>

          <div v-if="plansLoading" class="grid gap-6 md:grid-cols-3">
            <div
              v-for="index in 3"
              :key="index"
              class="rounded-2xl border border-slate-100 bg-slate-50 p-8 shadow-sm"
            >
              <div class="h-6 w-1/2 animate-pulse rounded bg-slate-200"></div>
              <div class="mt-4 h-10 w-2/3 animate-pulse rounded bg-slate-200"></div>
              <div class="mt-6 h-4 w-full animate-pulse rounded bg-slate-200"></div>
              <div class="mt-2 h-4 w-5/6 animate-pulse rounded bg-slate-200"></div>
              <div class="mt-8 h-10 w-full animate-pulse rounded bg-slate-200"></div>
            </div>
          </div>

          <div
            v-else-if="planError"
            class="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700"
          >
            {{ planError }}
          </div>

          <div v-else class="grid gap-6 md:grid-cols-3">
            <div
              v-for="plan in plans"
              :key="plan.id"
              class="rounded-2xl border border-slate-100 p-8 text-left shadow-sm transition hover:shadow-lg"
            >
              <h3 class="mb-2 text-xl font-semibold text-slate-900">{{ plan.name }}</h3>
              <p class="mb-4 text-3xl font-bold text-emerald-600">
                {{ formatPlanPrice(plan) }}
                <span class="text-sm font-normal text-slate-500">/{{ intervalLabel(plan) }}</span>
              </p>
              <p class="mb-6 text-sm text-slate-600">{{ plan.description }}</p>
              <button
                type="button"
                class="w-full rounded-lg bg-emerald-600 px-6 py-3 font-semibold text-white transition hover:bg-emerald-700"
                @click="openRegisterModal(plan)"
              >
                {{ t('pricing.subscribe') }}
              </button>
            </div>
          </div>

          <p v-if="!plansLoading && !planError && !plans.length" class="mt-6 text-sm text-slate-500">
            {{ t('pricing.empty') }}
          </p>
        </div>

        <RegisterUserPaymentModal
          v-if="showModal"
          :selected-plan="selectedPlan"
          @close="showModal = false"
          @success="handleSubscriptionSuccess"
        />
      </section>

      <section id="how" class="bg-white">
        <div class="mx-auto max-w-6xl px-6 py-20">
          <div class="mx-auto max-w-3xl text-center">
            <h2 class="text-3xl font-semibold text-slate-900 sm:text-4xl">{{ t('how.title') }}</h2>
            <p class="mt-4 text-base text-slate-600">{{ t('how.subtitle') }}</p>
          </div>
          <div class="mt-12 space-y-16">
            <div
              v-for="(step, index) in howSteps"
              :key="step.title"
              class="grid items-center gap-10 rounded-3xl border border-slate-100 bg-white p-6 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-md lg:grid-cols-2"
            >
              <div :class="index % 2 === 0 ? '' : 'lg:order-2'">
                <img
                  :src="step.image"
                  :alt="step.title"
                  class="h-full w-full rounded-2xl object-cover"
                  loading="lazy"
                />
              </div>
              <div :class="['space-y-4', index % 2 === 0 ? '' : 'lg:order-1']">
                <span class="text-sm font-semibold uppercase tracking-wide text-emerald-600">{{ t('how.stepLabel', { number: index + 1 }) }}</span>
                <h3 class="text-2xl font-semibold text-slate-900">{{ step.title }}</h3>
                <p class="text-base leading-relaxed text-slate-600">{{ step.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="demo" class="bg-gray-50">
        <div class="mx-auto max-w-6xl px-6 py-20">
          <div class="mx-auto max-w-3xl text-center">
            <h2 class="text-3xl font-semibold text-slate-900 sm:text-4xl">{{ t('demo.title') }}</h2>
            <p class="mt-4 text-base text-slate-600">{{ t('demo.subtitle') }}</p>
          </div>
          <div class="mt-12 grid gap-10 rounded-3xl border border-slate-100 bg-white p-8 shadow-sm transition-all hover:shadow-md lg:grid-cols-2">
            <form class="space-y-6" @submit.prevent="submitDemo">
              <div class="space-y-2">
                <label for="demo-number" class="text-sm font-semibold text-slate-700">{{ t('demo.form.number') }}</label>
                <input
                  id="demo-number"
                  v-model="demoForm.number"
                  type="tel"
                  class="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-100"
                  placeholder="+55 11 99999-0000"
                  required
                />
              </div>
              <div class="space-y-3">
                <span class="text-sm font-semibold text-slate-700">{{ t('demo.form.topicsLabel') }}</span>
                <div class="space-y-2">
                  <label
                    v-for="topic in demoTopics"
                    :key="topic.value"
                    class="flex cursor-pointer items-center gap-3 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600 transition-all hover:border-emerald-300"
                  >
                    <input
                      v-model="demoForm.topics"
                      type="checkbox"
                      class="h-4 w-4 rounded border-slate-300 text-emerald-500 focus:ring-emerald-400"
                      :value="topic.value"
                    />
                    <span>{{ topic.label }}</span>
                  </label>
                </div>
              </div>
              <button
                type="submit"
                class="inline-flex w-full items-center justify-center gap-2 rounded-full bg-emerald-500 px-6 py-3 text-sm font-semibold text-white shadow-sm transition-all hover:scale-105 hover:shadow-md"
              >
                {{ t('demo.form.submit') }}
              </button>
            </form>
            <div class="flex flex-col gap-6 rounded-2xl border border-slate-100 bg-slate-50 p-6">
              <div class="space-y-3">
                <h3 class="text-lg font-semibold text-slate-900">{{ t('demo.chat.title') }}</h3>
                <p class="text-sm text-slate-600">{{ t('demo.chat.subtitle') }}</p>
              </div>
              <div class="space-y-4">
                <div
                  v-for="message in demoMessages"
                  :key="message.id"
                  class="max-w-sm rounded-2xl border border-emerald-100 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm"
                >
                  {{ message.text }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section id="contact" class="bg-white">
        <div class="mx-auto max-w-6xl px-6 py-20">
          <div class="mx-auto max-w-3xl text-center">
            <h2 class="text-3xl font-semibold text-slate-900 sm:text-4xl">{{ t('contact.title') }}</h2>
            <p class="mt-4 text-base text-slate-600">{{ t('contact.subtitle') }}</p>
          </div>
          <div class="mt-12 grid gap-10 lg:grid-cols-[1.2fr_0.8fr]">
            <form class="space-y-6 rounded-3xl border border-slate-100 bg-white p-8 shadow-sm" @submit.prevent="submitContact">
              <div class="space-y-2">
                <label for="contact-name" class="text-sm font-semibold text-slate-700">{{ t('contact.form.name') }}</label>
                <input
                  id="contact-name"
                  v-model="contactForm.name"
                  type="text"
                  class="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-100"
                  placeholder="Jane Doe"
                  required
                />
              </div>
              <div class="space-y-2">
                <label for="contact-email" class="text-sm font-semibold text-slate-700">{{ t('contact.form.email') }}</label>
                <input
                  id="contact-email"
                  v-model="contactForm.email"
                  type="email"
                  class="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-100"
                  placeholder="name@company.com"
                  required
                />
              </div>
              <div class="space-y-2">
                <label for="contact-channel" class="text-sm font-semibold text-slate-700">{{ t('contact.form.channel') }}</label>
                <select
                  id="contact-channel"
                  v-model="contactForm.channel"
                  class="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-100"
                >
                  <option v-for="channel in contactChannels" :key="channel" :value="channel">{{ channel }}</option>
                </select>
              </div>
              <div class="space-y-2">
                <label for="contact-message" class="text-sm font-semibold text-slate-700">{{ t('contact.form.message') }}</label>
                <textarea
                  id="contact-message"
                  v-model="contactForm.message"
                  rows="4"
                  class="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm shadow-sm focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-100"
                  placeholder="Share context, goals, and timing so we can tailor our response."
                  required
                ></textarea>
              </div>
              <button
                type="submit"
                class="inline-flex w-full items-center justify-center gap-2 rounded-full bg-emerald-500 px-6 py-3 text-sm font-semibold text-white shadow-sm transition-all hover:scale-105 hover:shadow-md"
              >
                {{ t('contact.form.submit') }}
              </button>
            </form>
            <div class="flex flex-col gap-6">
              <div class="overflow-hidden rounded-3xl border border-slate-100 bg-white shadow-sm">
                <img
                  src="https://images.unsplash.com/photo-1556742393-d75f468bfcb0?q=80&w=800"
                  alt="Team collaboration"
                  class="h-56 w-full object-cover"
                  loading="lazy"
                />
                <div class="space-y-4 p-6">
                  <h3 class="text-xl font-semibold text-slate-900">{{ t('contact.quick.title') }}</h3>
                  <p class="text-sm leading-relaxed text-slate-600">{{ t('contact.quick.subtitle') }}</p>
                  <button
                    type="button"
                    class="inline-flex items-center justify-center gap-2 rounded-full bg-emerald-500 px-5 py-3 text-sm font-semibold text-white shadow-sm transition-all hover:scale-105 hover:shadow-md"
                    @click="openWhatsApp"
                  >
                    {{ t('contact.quick.button') }}
                  </button>
                </div>
              </div>
              <div class="grid gap-4 sm:grid-cols-2">
                <div
                  v-for="card in contactCards"
                  :key="card.title"
                  class="rounded-2xl border border-slate-200 bg-slate-50 p-5 shadow-sm transition-all hover:-translate-y-1 hover:shadow-md"
                >
                  <div class="text-2xl">{{ card.icon }}</div>
                  <h4 class="mt-2 text-lg font-semibold text-slate-900">{{ card.title }}</h4>
                  <p class="mt-1 text-sm text-slate-600">{{ card.description }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <footer class="border-t border-slate-200 bg-white">
      <div class="mx-auto flex max-w-6xl flex-col items-center justify-between gap-4 px-6 py-8 text-sm text-slate-600 sm:flex-row">
        <p>{{ t('footer.copy') }}</p>
        <div class="flex items-center gap-6">
          <a href="#hero" class="transition-colors hover:text-emerald-600" @click.prevent="scrollTo('hero')">{{ t('footer.links.home') }}</a>
          <a href="#pricing" class="transition-colors hover:text-emerald-600" @click.prevent="scrollTo('pricing')">{{ t('footer.links.pricing') }}</a>
          <a href="#demo" class="transition-colors hover:text-emerald-600" @click.prevent="scrollTo('demo')">{{ t('footer.links.demo') }}</a>
          <a href="#contact" class="transition-colors hover:text-emerald-600" @click.prevent="scrollTo('contact')">{{ t('footer.links.contact') }}</a>
          <a
            href="https://www.linkedin.com/company/"
            target="_blank"
            rel="noreferrer"
            class="transition-colors hover:text-emerald-600"
          >
            LinkedIn
          </a>
          <a
            href="https://www.instagram.com/"
            target="_blank"
            rel="noreferrer"
            class="transition-colors hover:text-emerald-600"
          >
            Instagram
          </a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import axios from '@/services/api';
import RegisterUserPaymentModal from '@/components/landing/RegisterUserPaymentModal.vue';

const messages = {
  en: {
    nav: {
      features: 'Features',
      pricing: 'Pricing',
      demo: 'Demo',
      how: 'How it works',
      contact: 'Contact',
      chat: 'Chat on WhatsApp',
      login: 'Log in',
    },
    hero: {
      badge: 'AI-powered nutrition',
      title: 'NutriVision AI',
      subtitle:
        'Imagine being able to point your camera at any meal and, in seconds, receive a complete nutritional analysis directly to your WhatsApp! With NutriVision AI, this reality is within reach.',
      demo: 'Try WhatsApp Demo',
      watch: 'Watch Video',
    },
    features: {
      title: 'What makes NutriVision different',
      subtitle: 'Technology and coaching combined to deliver precise, contextual, and human nutrition support.',
      cards: [
        {
          icon: '🧠',
          title: 'Instant Insights',
          description: 'Snap a photo and get nutritional breakdowns, calories, and smarter swaps.',
        },
        {
          icon: '💬',
          title: 'WhatsApp Native',
          description: 'No app installs—just chat and get personalized feedback instantly.',
        },
        {
          icon: '⚙️',
          title: 'Smart Learning',
          description: 'Adapts to your preferences, restrictions, and habits over time.',
        },
        {
          icon: '🌿',
          title: 'Holistic Health',
          description: 'Goes beyond calories: sleep, hydration, and movement-friendly choices.',
        },
      ],
    },
    pricing: {
      title: 'Choose your plan',
      subscribe: 'Subscribe Now',
      empty: 'We are preparing new plans for you. Please check back soon.',
      loadError: 'Unable to load subscription plans. Please try again later.',
      interval: {
        day: 'day',
        week: 'week',
        month: 'month',
        year: 'year',
      },
    },
    register: {
      title: 'Create your account',
      subtitle: 'You’re subscribing to {plan}',
      planFallback: 'our plan',
      name: 'Full name',
      email: 'Email',
      phone: 'WhatsApp number',
      validationError: 'Please fill in all required fields before continuing.',
      planRequired: 'Select a plan to continue with your subscription.',
      successTitle: 'Subscription confirmed',
      successMessage: 'All set! Redirecting you to your dashboard...',
      errorTitle: 'Unable to complete subscription',
      errorMessage: 'We could not finish your subscription. Please try again in a few minutes.',
    },
    how: {
      title: 'From photo to insights in seconds',
      subtitle: 'See how NutriVision turns a meal photo into instant analysis via WhatsApp.',
      stepLabel: 'Step {number}',
        steps: [
        {
          title: 'Capture your meal',
          description: 'Take a photo or paste a link directly in WhatsApp to kick off the experience.',
          image: 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=800',
        },
        {
          title: 'AI analyzes instantly',
          description: 'Our vision models estimate calories, macros, and spot potential allergens.',
          image: 'https://images.unsplash.com/photo-1522336572468-97b06e8ef143?q=80&w=800',
        },
        {
          title: 'Get actionable insights',
          description: 'Receive tips that keep your meal tasty while nudging better nutritional balance.',
          image: 'https://geekyants.com/_next/image?url=https%3A%2F%2Fstatic-cdn.geekyants.com%2Farticleblogcomponent%2F46872%2F2025-09-04%2F018572424-1756964266.png&w=1920&q=75',
        },
        { 
          title: 'Track over time',
          description: 'Daily and weekly summaries reveal progress, habits, and improvement opportunities.',
          image: 'https://pub-83a83e2ebd0e4cdf8d1e1b42e7e0e711.r2.dev/track_over_time.png',
        },
        {
          title: 'Stay engaged on WhatsApp',
          description: 'Ask follow-ups, request grocery lists, or plan meals without leaving the chat.',
          image: 'https://images.unsplash.com/photo-1599058917212-d750089bc07e?q=80&w=800',
        },
      ],
    },
    demo: {
      title: 'Experience the WhatsApp flow',
      subtitle: 'Choose what you want to explore and receive a personalized demo straight to your phone.',
      form: {
        number: 'WhatsApp number',
        topicsLabel: 'Pick your focus areas',
        submit: 'Send demo link',
      },
      topics: [
        { value: 'tips', label: 'Nutrition Tips' },
        { value: 'macros', label: 'Macro Breakdown' },
        { value: 'allergens', label: 'Allergen Scan' },
      ],
      chat: {
        title: 'Simulated WhatsApp chat',
        subtitle: 'A preview of the tone, insights, and clarity your clients will receive.',
        messages: [
          { id: 1, text: '👋 Hi! I see salmon, rice, and broccoli.' },
          { id: 2, text: '🍽 Estimated 520 kcal.' },
          { id: 3, text: '🥩 Protein 36g · Carbs 48g · Fat 18g · Fiber 7g' },
          { id: 4, text: '💡 Tip: Swap mayo for greek yogurt to save ~90 kcal.' },
        ],
      },
    },
    contact: {
      title: 'Talk to our team',
      subtitle: 'Questions, partnerships, or support—send us a note and we’ll get back within 24 hours.',
      form: {
        name: 'Full name',
        namePlaceholder: 'Jane Doe',
        email: 'Work email',
        channel: 'Preferred channel',
        message: 'How can we help?',
        messagePlaceholder: 'Share context, goals, and timing so we can tailor our response.',
        submit: 'Send message',
      },
      channels: ['WhatsApp', 'Email', 'Video call'],
      quick: {
        title: 'Need an answer now?',
        subtitle: 'Reach our specialists instantly for guidance on pricing, onboarding, or integrations.',
        button: 'Chat on WhatsApp',
      },
      cards: [
        {
          icon: '📱',
          title: 'Product Support',
          description: 'Live troubleshooting and best-practice guidance for your team.',
        },
        {
          icon: '🤝',
          title: 'Partnerships',
          description: 'Co-marketing, reseller, and affiliate opportunities tailored to your audience.',
        },
        {
          icon: '📰',
          title: 'Press & Media',
          description: 'Get assets and story angles for covering NutriVision AI.',
        },
        {
          icon: '🕓',
          title: 'Business Hours',
          description: 'Mon–Fri, 9:00–18:00 UTC. We respond within one business day.',
        },
      ],
    },
    footer: {
      copy: '© 2025 NutriVision AI · All rights reserved',
      links: {
        home: 'Home',
        pricing: 'Pricing',
        demo: 'Demo',
        contact: 'Contact',
      },
    },
  },
  pt: {
    nav: {
      features: 'Funcionalidades',
      pricing: 'Planos',
      demo: 'Demonstração',
      how: 'Como funciona',
      contact: 'Contato',
      chat: 'Conversar no WhatsApp',
      login: 'Entrar',
    },
    hero: {
      badge: 'Nutrição com Inteligência Artificial',
      title: 'NutriVision AI',
      subtitle:
        'Imagine apontar sua câmera para qualquer refeição e, em segundos, receber uma análise nutricional completa diretamente no WhatsApp! Com o NutriVision AI, isso já é possível.',
      demo: 'Testar no WhatsApp',
      watch: 'Ver Vídeo',
    },
    features: {
      title: 'Por que escolher o NutriVision',
      subtitle: 'Tecnologia e acompanhamento humano para entregar suporte nutricional preciso e contextual.',
      cards: [
        {
          icon: '🧠',
          title: 'Análises Instantâneas',
          description: 'Fotografe e receba em segundos calorias, macros e sugestões inteligentes.',
        },
        {
          icon: '💬',
          title: 'Nativo no WhatsApp',
          description: 'Sem instalar apps — basta conversar e receber feedback personalizado na hora.',
        },
        {
          icon: '⚙️',
          title: 'Aprendizado Inteligente',
          description: 'Adapta-se às suas preferências, restrições e hábitos com o tempo.',
        },
        {
          icon: '🌿',
          title: 'Saúde Holística',
          description: 'Vai além das calorias: sono, hidratação e escolhas alinhadas ao bem-estar.',
        },
      ],
    },
    pricing: {
      title: 'Escolha seu plano',
      subscribe: 'Assinar agora',
      empty: 'Estamos preparando novos planos. Volte em breve.',
      loadError: 'Não foi possível carregar os planos no momento. Tente novamente mais tarde.',
      interval: {
        day: 'dia',
        week: 'semana',
        month: 'mês',
        year: 'ano',
      },
    },
    register: {
      title: 'Crie sua conta',
      subtitle: 'Você está assinando o plano {plan}',
      planFallback: 'nosso plano',
      name: 'Nome completo',
      email: 'E-mail',
      phone: 'Número do WhatsApp',
      validationError: 'Preencha todos os campos obrigatórios para continuar.',
      planRequired: 'Selecione um plano para concluir sua assinatura.',
      successTitle: 'Assinatura confirmada',
      successMessage: 'Tudo pronto! Vamos direcionar você para o painel...',
      errorTitle: 'Não foi possível concluir a assinatura',
      errorMessage: 'Não conseguimos finalizar sua assinatura. Tente novamente em alguns instantes.',
    },
    how: {
      title: 'Da foto ao insight em segundos',
      subtitle: 'Veja como o NutriVision transforma a foto de uma refeição em análise instantânea no WhatsApp.',
      stepLabel: 'Passo {number}',
      steps: [
        {
          title: 'Capture sua refeição',
          description: 'Tire uma foto ou cole um link direto no WhatsApp para iniciar a experiência.',
          image: 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=800',
        },
        {
          title: 'A IA analisa na hora',
          description: 'Nossos modelos estimam calorias, macros e identificam possíveis alérgenos.',
          image: 'https://images.unsplash.com/photo-1522336572468-97b06e8ef143?q=80&w=800',
        },
        {
          title: 'Receba insights práticos',
          description: 'Dicas para manter o sabor enquanto equilibra melhor a nutrição.',
          image: 'https://images.unsplash.com/photo-1586201375754-1a9f94b6c03e?q=80&w=800',
        },
          {
            title: 'Acompanhe sua evolução',
            description: 'Sumários diários e semanais mostram progresso, hábitos e oportunidades.',
            image: 'https://images.unsplash.com/photo-1611078489935-0cb964f06fd6?auto=format&fit=crop&w=800&q=80',
          },
        {
          title: 'Fique engajado no WhatsApp',
          description: 'Faça perguntas, solicite listas de compras ou planeje refeições sem sair do chat.',
          image: 'https://images.unsplash.com/photo-1599058917212-d750089bc07e?q=80&w=800',
        },
      ],
    },
    demo: {
      title: 'Experimente o fluxo no WhatsApp',
      subtitle: 'Escolha o que deseja explorar e receba uma demo personalizada diretamente no seu celular.',
      form: {
        number: 'Número do WhatsApp',
        topicsLabel: 'Selecione os temas de interesse',
        submit: 'Enviar link da demo',
      },
      topics: [
        { value: 'tips', label: 'Dicas de Nutrição' },
        { value: 'macros', label: 'Detalhamento de Macros' },
        { value: 'allergens', label: 'Varredura de Alérgenos' },
      ],
      chat: {
        title: 'Simulação do chat no WhatsApp',
        subtitle: 'Uma prévia do tom, dos insights e da clareza que seus clientes recebem.',
        messages: [
          { id: 1, text: '👋 Olá! Identifiquei salmão, arroz e brócolis.' },
          { id: 2, text: '🍽 Estimativa de 520 kcal.' },
          { id: 3, text: '🥩 Proteína 36g · Carboidratos 48g · Gorduras 18g · Fibras 7g' },
          { id: 4, text: '💡 Dica: troque a maionese por iogurte grego e economize ~90 kcal.' },
        ],
      },
    },
    contact: {
      title: 'Fale com nossa equipe',
      subtitle: 'Dúvidas, parcerias ou suporte — envie uma mensagem e retornaremos em até 24 horas.',
      form: {
        name: 'Nome completo',
        namePlaceholder: 'Maria Silva',
        email: 'E-mail corporativo',
        channel: 'Canal preferido',
        message: 'Como podemos ajudar?',
        messagePlaceholder: 'Compartilhe contexto, objetivos e prazos para personalizarmos a resposta.',
        submit: 'Enviar mensagem',
      },
      channels: ['WhatsApp', 'E-mail', 'Chamada de vídeo'],
      quick: {
        title: 'Precisa de uma resposta agora?',
        subtitle: 'Fale com nossos especialistas sobre preços, onboarding ou integrações em tempo real.',
        button: 'Conversar no WhatsApp',
      },
      cards: [
        {
          icon: '📱',
          title: 'Suporte ao Produto',
          description: 'Orientação ao vivo e melhores práticas para sua equipe.',
        },
        {
          icon: '🤝',
          title: 'Parcerias',
          description: 'Cocriação, canais de venda e programas de afiliados sob medida.',
        },
        {
          icon: '📰',
          title: 'Imprensa e Mídia',
          description: 'Receba materiais e histórias para divulgar o NutriVision AI.',
        },
        {
          icon: '🕓',
          title: 'Horário Comercial',
          description: 'Seg–Sex, 9h às 18h (UTC). Respondemos em até um dia útil.',
        },
      ],
    },
    footer: {
      copy: '© 2025 NutriVision AI · Todos os direitos reservados',
      links: {
        home: 'Início',
        pricing: 'Planos',
        demo: 'Demo',
        contact: 'Contato',
      },
    },
  },
};

const { t, tm, locale } = useI18n({ legacy: false, locale: 'en', fallbackLocale: 'en', messages, useScope: 'local' });

const isMenuOpen = ref(false);

const featureCards = computed(() => tm('features.cards'));
const howSteps = computed(() => tm('how.steps'));
const demoTopics = computed(() => tm('demo.topics'));
const demoMessages = computed(() => tm('demo.chat.messages'));
const contactChannels = computed(() => tm('contact.channels'));
const contactCards = computed(() => tm('contact.cards'));

const plans = ref([]);
const plansLoading = ref(false);
const planError = ref('');
const showModal = ref(false);
const selectedPlan = ref(null);

const localeTag = computed(() => (locale.value === 'pt' ? 'pt-PT' : 'en-US'));

const demoForm = reactive({
  number: '',
  topics: [],
});

const contactForm = reactive({
  name: '',
  email: '',
  channel: '',
  message: '',
});

watch(
  contactChannels,
  (channels) => {
    if (Array.isArray(channels) && channels.length) {
      contactForm.channel = channels[0];
    }
  },
  { immediate: true }
);

async function fetchPlans() {
  plansLoading.value = true;
  planError.value = '';
  try {
    const { data } = await axios.get('/api/v1/plans');
    const records = Array.isArray(data) ? data : data?.content ?? [];
    plans.value = records;
  } catch (error) {
    console.error('Failed to load plans', error);
    planError.value = error?.response?.data?.details ?? error?.response?.data?.message ?? t('pricing.loadError');
  } finally {
    plansLoading.value = false;
  }
}

function formatPlanPrice(plan) {
  const amount = Number(plan?.amount ?? plan?.price ?? 0);
  const currency = plan?.currency ?? 'EUR';
  return new Intl.NumberFormat(localeTag.value, {
    style: 'currency',
    currency,
  }).format(amount);
}

function intervalLabel(plan) {
  const key = String(plan?.interval ?? 'month').toLowerCase();
  const path = `pricing.interval.${key}`;
  const label = t(path);
  return label !== path ? label : key;
}

function openRegisterModal(plan) {
  selectedPlan.value = plan;
  showModal.value = true;
}

function handleSubscriptionSuccess() {
  showModal.value = false;
  window.location.href = '/app/nutrition/dashboard';
}

const scrollTo = (sectionId) => {
  const element = document.getElementById(sectionId);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
};

const handleMobileNav = (sectionId) => {
  isMenuOpen.value = false;
  scrollTo(sectionId);
};

const setLocale = (value) => {
  locale.value = value;
};

const openWhatsApp = () => {
  window.open('https://wa.me/', '_blank', 'noopener');
};

const submitDemo = () => {
  console.log('Demo form submitted', { ...demoForm });
};

const submitContact = () => {
  console.log('Contact form submitted', { ...contactForm });
};

onMounted(fetchPlans);
</script>

<style scoped>
:global(html) {
  scroll-behavior: smooth;
}
</style>

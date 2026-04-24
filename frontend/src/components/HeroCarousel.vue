<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'

/** 仅图片展示；文案请做进图片本身，或后续接运营配置的图链 */
const slides = [
  { image: '/61DCYDCQuwL._SX3000_.jpg', label: '活动图 1' },
  { image: '/61MaTtH8HQL._SX3000_.jpg', label: '活动图 2' },
  { image: '/71oTLED8qoL._SX3000_.jpg', label: '活动图 3' },
]

const n = slides.length
const current = ref(0)
const paused = ref(false)
let timer

const trackStyle = computed(() => ({
  width: `${n * 100}%`,
  transform: `translateX(-${(current.value * 100) / n}%)`,
}))

const slideFlex = computed(() => `0 0 ${100 / n}%`)

function go(delta) {
  current.value = (current.value + delta + n) % n
}

function goTo(i) {
  current.value = i
}

function startTimer() {
  clearInterval(timer)
  timer = setInterval(() => {
    if (!paused.value) go(1)
  }, 5200)
}

onMounted(() => {
  startTimer()
})

onUnmounted(() => {
  clearInterval(timer)
})

function onEnter() {
  paused.value = true
}

function onLeave() {
  paused.value = false
}

function onNav(delta) {
  go(delta)
  startTimer()
}
</script>

<template>
  <section
    class="hero-carousel"
    aria-roledescription="carousel"
    aria-label="首页活动图轮播"
    @mouseenter="onEnter"
    @mouseleave="onLeave"
  >
    <div class="viewport">
      <div class="track" :style="trackStyle">
        <div
          v-for="(s, i) in slides"
          :key="i"
          class="slide"
          :style="{ flex: slideFlex }"
          :aria-hidden="i !== current"
        >
          <img class="slide-img" :src="s.image" :alt="s.label" width="1600" height="420" decoding="async" />
        </div>
      </div>
    </div>

    <button type="button" class="nav prev" aria-label="上一张" @click="onNav(-1)">
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
        <path d="M15 6l-6 6 6 6" />
      </svg>
    </button>
    <button type="button" class="nav next" aria-label="下一张" @click="onNav(1)">
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
        <path d="M9 6l6 6-6 6" />
      </svg>
    </button>

    <div class="dots" role="tablist" aria-label="选择活动图">
      <button
        v-for="(_, i) in slides"
        :key="'dot-' + i"
        type="button"
        role="tab"
        class="dot"
        :class="{ active: i === current }"
        :aria-selected="i === current"
        :aria-label="'第 ' + (i + 1) + ' 张'"
        @click="
          goTo(i);
          startTimer();
        "
      />
    </div>
  </section>
</template>

<style scoped>
.hero-carousel {
  position: relative;
  border-bottom: 1px solid var(--mall-border);
  background: var(--mall-black);
}

.viewport {
  overflow: hidden;
  width: 100%;
}

.track {
  display: flex;
  transition: transform 0.5s cubic-bezier(0.33, 1, 0.68, 1);
  will-change: transform;
}

.slide {
  position: relative;
  flex-grow: 0;
  flex-shrink: 0;
  min-height: min(52vw, 520px);
  max-height: 520px;
}

.slide-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
  background: var(--mall-black);
  pointer-events: none;
}

.nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 3;
  width: 44px;
  height: 72px;
  border: 1px solid var(--mall-border);
  border-radius: 4px;
  background: rgba(26, 26, 32, 0.75);
  color: var(--mall-text);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(6px);
}

.nav:hover {
  background: rgba(34, 34, 40, 0.92);
  color: var(--mall-orange-bright);
}

.prev {
  left: max(8px, calc((100vw - 1280px) / 2 + 8px));
}

.next {
  right: max(8px, calc((100vw - 1280px) / 2 + 8px));
}

@media (max-width: 1320px) {
  .prev {
    left: 8px;
  }
  .next {
    right: 8px;
  }
}

.dots {
  position: absolute;
  bottom: 14px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
  display: flex;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(10, 10, 11, 0.55);
  backdrop-filter: blur(4px);
}

.dot {
  width: 9px;
  height: 9px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: #52525b;
  cursor: pointer;
}

.dot:hover {
  background: #a1a1aa;
}

.dot.active {
  background: var(--mall-orange);
  box-shadow: 0 0 0 2px rgba(255, 138, 0, 0.35);
}

@media (max-width: 640px) {
  .nav {
    width: 36px;
    height: 56px;
  }
}
</style>

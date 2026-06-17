<template>
  <div class="fids-container">
    <!-- Header Bar -->
    <div class="fids-header">
      <div class="header-left">
        <span class="header-brand">FUKUOKA AIRPORT</span>
        <span class="header-divider">|</span>
        <span class="header-label">DEPARTURES</span>
      </div>
      <div class="header-right">
        <span class="header-time-label">LOCAL</span>
        <span class="header-time">{{ currentTime }}</span>
        <span class="header-blink" :class="{ on: dotOn }">&#9679;</span>
      </div>
    </div>

    <!-- Column Headers -->
    <div class="fids-head">
      <span class="h-logo">CO</span>
      <span class="h-time">SCHED</span>
      <span class="h-dest">DESTINATION</span>
      <span class="h-flight">FLIGHT</span>
      <span class="h-cntr">CNTR</span>
      <span class="h-gate">GATE</span>
      <span class="h-stat">STATUS</span>
    </div>

    <!-- Flight List -->
    <div class="fids-body" ref="bodyRef">
      <div class="fids-scroll" :style="scrollStyle" ref="scrollRef">
        <template v-for="round in renderRounds" :key="round">
          <div
            v-for="(f, i) in displayFlights"
            :key="`${round}-${i}`"
            class="fids-row"
            :class="rowClass(f)"
          >
            <!-- Logo / Airline Badge -->
            <div class="c-logo">
              <img
                v-if="hasBundledLogo(f.airlineCode)"
                :src="logoUrl(f.airlineCode)"
                class="logo-img"
                @error="onLogoErr(f.airlineCode)"
              />
              <span v-else class="logo-badge" :style="badgeStyle(f.airlineCode)">
                {{ f.airlineCode }}
              </span>
            </div>

            <!-- Scheduled Time -->
            <span class="c-time">{{ f.scheduled }}</span>

            <!-- Destination -->
            <span class="c-dest">{{ f.destination }}</span>

            <!-- Flight Number -->
            <span class="c-flight">{{ f.flightNo }}</span>

            <!-- Counter -->
            <span class="c-cntr">{{ f.counter }}</span>

            <!-- Gate -->
            <span class="c-gate">{{ f.gate }}</span>

            <!-- Status -->
            <span class="c-stat" :class="statusClass(f)">
              {{ f.computedStatus }}
            </span>
          </div>
        </template>
      </div>
    </div>

    <!-- Footer -->
    <div class="fids-footer">
      <span>TOTAL {{ displayFlights.length }} FLIGHTS</span>
      <span class="footer-sep">|</span>
      <span>{{ currentDate }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { getAirlineConfig, hasBundledLogo } from '../utils/airlineLogos.js'
import flightsData from '../assets/fukuoka_flights.json'

// ---- Constants ----
const MAX_ROWS = 10
const ROW_HEIGHT = 54
const WIN_BEFORE = 15  // mins before now
const WIN_AFTER = 120   // mins after now

// ---- Reactive State ----
const currentTime = ref('--:--')
const currentDate = ref('----/--/--')
const displayFlights = ref([])
const scrollOffset = ref(0)
const renderRounds = ref(1)
const logoOk = ref({})
const dotOn = ref(true)

const bodyRef = ref(null)
const scrollRef = ref(null)

let clockTimer = null
let scrollTimer = null
let dotTimer = null

// ---- Clock ----
function tick() {
  const d = new Date()
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  currentTime.value = `${hh}:${mm}`
  const yy = d.getFullYear()
  const mo = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  currentDate.value = `${yy}/${mo}/${dd}`
  filterFlights(hh, mm)
}

// ---- Filtering ----
function toMin(h, m) { return parseInt(h) * 60 + parseInt(m) }
function schedMin(t) { const p = t.split(':'); return parseInt(p[0]) * 60 + parseInt(p[1]) }

function computeStatus(sMin, nowMin, raw) {
  if (raw === 'Cancelled') return '欠航'
  if (raw === 'Departed') return 'Departed'
  if (raw === 'Gate Closed') return 'Gate Closed'
  if (raw === 'Now Boarding') return 'BOARDING'
  if (raw === 'Final Call') return 'FINAL CALL'
  if (raw === 'Code Share') return 'Code Share'
  if (raw === 'Immigration') return 'Immigration'
  if (raw === 'Weather Check') return 'Weather'
  if (raw === 'Extra Flight') return 'Extra'
  if (raw === 'Check-in Closed') return 'Check-In End'

  const diff = sMin - nowMin
  if (diff > 45) return 'Check-In'
  if (diff > 15) return 'BOARDING'
  if (diff > 5)  return 'FINAL CALL'
  if (diff >= -15) return 'Gate Closed'
  return 'Departed'
}

function filterFlights(hh, mm) {
  const now = toMin(hh, mm)
  const lo = now - WIN_BEFORE
  const hi = now + WIN_AFTER

  displayFlights.value = flightsData
    .filter(f => { const s = schedMin(f.scheduled); return s >= lo && s <= hi })
    .map(f => ({ ...f, computedStatus: computeStatus(schedMin(f.scheduled), now, f.baseStatus) }))
    .sort((a, b) => schedMin(a.scheduled) - schedMin(b.scheduled))

  renderRounds.value = displayFlights.value.length > MAX_ROWS ? 2 : 1
}

// ---- Row & Status CSS ----
function rowClass(f) {
  const s = f.computedStatus
  if (s === '欠航') return 'row-cxl'
  if (s === 'Departed' || s === 'Gate Closed') return 'row-dim'
  return ''
}

function statusClass(f) {
  const s = f.computedStatus
  if (s === '欠航') return 'st-cxl'
  if (s === 'BOARDING') return 'st-brd'
  if (s === 'FINAL CALL') return 'st-fnl'
  if (s === 'Check-In') return 'st-ckn'
  if (s === 'Departed') return 'st-dep'
  if (s === 'Gate Closed') return 'st-gcl'
  if (s === 'Now Boarding') return 'st-brd'
  if (s === 'Code Share') return 'st-cs'
  if (s === 'Immigration') return 'st-imm'
  if (s === 'Weather' || s === 'Extra') return 'st-warn'
  return 'st-def'
}

// ---- Logo / Badge ----
function logoUrl(code) {
  return new URL(`../assets/logos/${code}.png`, import.meta.url).href
}
function badgeStyle(code) {
  const c = getAirlineConfig(code)
  return { background: c.color, color: c.textColor }
}
function onLogoErr(code) {
  logoOk.value = { ...logoOk.value, [code]: false }
}

// ---- Smooth Scroll (GPU transform) ----
function scrollLoop() {
  if (displayFlights.value.length <= MAX_ROWS) { scrollOffset.value = 0; return }
  let pos = 0
  scrollTimer = setInterval(() => {
    pos += 0.25
    const total = displayFlights.value.length * ROW_HEIGHT
    if (pos >= total) pos = 0
    scrollOffset.value = pos
  }, 16)
}

const scrollStyle = computed(() => ({
  transform: `translateY(-${scrollOffset.value}px)`,
}))

// ---- Dot blink ----
function blinkDot() {
  dotTimer = setInterval(() => { dotOn.value = !dotOn.value }, 1000)
}

// ---- Lifecycle ----
onMounted(() => {
  tick()
  clockTimer = setInterval(tick, 60000)
  scrollLoop()
  blinkDot()
})

onUnmounted(() => {
  clearInterval(clockTimer)
  clearInterval(scrollTimer)
  clearInterval(dotTimer)
})

watch(displayFlights, () => {
  clearInterval(scrollTimer)
  scrollOffset.value = 0
  scrollLoop()
})
</script>

<style scoped>
/* ========== CONTAINER ========== */
.fids-container {
  width: 960px;
  height: 1080px;
  background: #0a0a0a;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'Courier New', 'MS Gothic', 'Osaka-Mono', monospace;
  border-left: 1px solid #1a1a1a;
  user-select: none;
}

/* ========== HEADER ========== */
.fids-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 20px 24px 14px;
  border-bottom: 2px solid #222;
}
.header-brand {
  font-size: 20px;
  font-weight: bold;
  color: #ffd700;
  letter-spacing: 3px;
}
.header-divider {
  color: #444;
  margin: 0 14px;
}
.header-label {
  font-size: 13px;
  color: #888;
  letter-spacing: 5px;
}
.header-right {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.header-time-label {
  font-size: 10px;
  color: #555;
  letter-spacing: 2px;
}
.header-time {
  font-size: 30px;
  font-weight: bold;
  color: #ffd700;
  letter-spacing: 3px;
  font-variant-numeric: tabular-nums;
}
.header-blink {
  color: #00ff41;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}
.header-blink.on { opacity: 1; }

/* ========== COLUMN HEADERS ========== */
.fids-head {
  display: flex;
  align-items: center;
  padding: 8px 24px;
  background: #0d0d0d;
  border-bottom: 1px solid #1a1a1a;
  font-size: 10px;
  color: #555;
  letter-spacing: 2px;
}
.h-logo  { width: 44px; flex-shrink: 0; }
.h-time  { width: 48px; flex-shrink: 0; }
.h-dest  { flex: 1; }
.h-flight{ width: 158px; flex-shrink: 0; }
.h-cntr  { width: 46px; flex-shrink: 0; text-align: center; }
.h-gate  { width: 50px; flex-shrink: 0; text-align: center; }
.h-stat  { width: 130px; flex-shrink: 0; text-align: center; }

/* ========== BODY / SCROLL ========== */
.fids-body {
  flex: 1;
  overflow: hidden;
  position: relative;
}
.fids-scroll {
  will-change: transform;
}

/* ========== ROW ========== */
.fids-row {
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 54px;
  border-bottom: 1px solid #0f0f0f;
  transition: background 0.3s;
}
.fids-row:hover {
  background: rgba(255, 255, 255, 0.02);
}
.row-dim {
  opacity: 0.35;
}
.row-cxl {
  background: rgba(255, 0, 0, 0.06);
  opacity: 1;
}

/* ========== CELLS ========== */
/* Logo */
.c-logo {
  width: 44px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}
.logo-img {
  width: 34px;
  height: 24px;
  object-fit: contain;
  filter: brightness(1.2) drop-shadow(0 0 2px rgba(255,255,255,0.1));
}
.logo-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 24px;
  padding: 0 6px;
  border-radius: 2px;
  font-size: 11px;
  font-weight: bold;
  letter-spacing: 1.5px;
  line-height: 1;
  font-family: 'Courier New', monospace;
  box-shadow: inset 0 0 8px rgba(255,255,255,0.08);
}

/* Time */
.c-time {
  width: 48px;
  flex-shrink: 0;
  font-size: 15px;
  font-weight: bold;
  color: #ffd700;
  letter-spacing: 1px;
  font-variant-numeric: tabular-nums;
}

/* Destination */
.c-dest {
  flex: 1;
  font-size: 13px;
  color: #ddd;
  letter-spacing: 1px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* Flight Number */
.c-flight {
  width: 158px;
  flex-shrink: 0;
  font-size: 11px;
  color: #999;
  letter-spacing: 0.5px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* Counter */
.c-cntr {
  width: 46px;
  flex-shrink: 0;
  text-align: center;
  font-size: 13px;
  font-weight: bold;
  color: #aaa;
  letter-spacing: 1px;
}

/* Gate */
.c-gate {
  width: 50px;
  flex-shrink: 0;
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  color: #ffd700;
}

/* Status */
.c-stat {
  width: 130px;
  flex-shrink: 0;
  text-align: center;
  font-size: 12px;
  font-weight: bold;
  letter-spacing: 2px;
}

/* ========== STATUS COLORS ========== */
.st-ckn  { color: #4caf50; }                             /* Check-In */
.st-brd  { color: #ffd700; animation: blk-y 0.8s step-end infinite; }  /* BOARDING */
.st-fnl  { color: #ff3333; animation: blk-r 0.5s step-end infinite; }  /* FINAL CALL */
.st-gcl  { color: #ff9800; }                             /* Gate Closed */
.st-cxl  { color: #ff2222; background: rgba(255,0,0,0.1); padding: 3px 10px; } /* 欠航 */
.st-dep  { color: #444; }                                /* Departed */
.st-cs   { color: #00bcd4; }                             /* Code Share */
.st-imm  { color: #2196f3; }                             /* Immigration */
.st-warn { color: #ffeb3b; }                             /* Weather/Extra */
.st-def  { color: #888; }

@keyframes blk-y { 0%,100%{opacity:1} 50%{opacity:0.15} }
@keyframes blk-r { 0%,100%{opacity:1} 50%{opacity:0.1} }

/* ========== FOOTER ========== */
.fids-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 24px;
  border-top: 1px solid #1a1a1a;
  font-size: 11px;
  color: #444;
  letter-spacing: 2px;
}
.footer-sep {
  color: #333;
}
</style>

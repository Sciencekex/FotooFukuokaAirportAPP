<template>
  <div class="fids-container">
    <!-- Header -->
    <div class="fids-header">
      <div class="header-title">FUKUOKA AIRPORT</div>
      <div class="header-subtitle">DEPARTURES</div>
      <div class="header-time">{{ currentTime }}</div>
    </div>

    <!-- Column Headers -->
    <div class="fids-col-headers">
      <span class="col-time">TIME</span>
      <span class="col-dest">DESTINATION</span>
      <span class="col-flight">FLIGHT</span>
      <span class="col-gate">GATE</span>
      <span class="col-status">STATUS</span>
    </div>

    <!-- Flight List with Scroll -->
    <div class="fids-list-wrapper" ref="listWrapper">
      <div class="fids-list" :style="scrollStyle" ref="listInner">
        <!-- Render twice for seamless loop -->
        <template v-for="(round) in renderRounds" :key="round">
          <div
            v-for="(flight, idx) in displayFlights"
            :key="`${round}-${idx}`"
            class="fids-row"
            :class="getStatusClass(flight.computedStatus)"
          >
            <!-- Airline Code Badge / Logo -->
            <div class="cell-logo">
              <img
                v-if="logoAttempts[flight.airlineCode] !== false"
                :src="getLogoAssetPath(flight.airlineCode)"
                class="airline-logo-img"
                @error="onLogoError(flight.airlineCode)"
                :alt="flight.airlineCode"
              />
              <span
                v-else
                class="airline-badge"
                :style="getBadgeStyle(flight.airlineCode)"
              >
                {{ flight.airlineCode }}
              </span>
            </div>

            <!-- Time -->
            <span class="cell-time">{{ flight.scheduled }}</span>

            <!-- Destination -->
            <span class="cell-dest">{{ flight.destination }}</span>

            <!-- Flight No -->
            <span class="cell-flight">{{ flight.flightNo }}</span>

            <!-- Gate -->
            <span class="cell-gate">{{ flight.gate }}</span>

            <!-- Status -->
            <span class="cell-status" :class="getStatusClass(flight.computedStatus)">
              {{ flight.computedStatus }}
            </span>
          </div>
        </template>
      </div>
    </div>

    <!-- Footer -->
    <div class="fids-footer">
      <span class="footer-info">FLIGHTS: {{ displayFlights.length }} | LOCAL TIME</span>
      <span class="footer-dot" :style="{ opacity: dotOpacity }">&#9679;</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { getAirlineConfig, hasBundledLogo } from '../utils/airlineLogos.js'
import flightsData from '../assets/fukuoka_flights.json'

// ---- Constants ----
const MAX_VISIBLE_ROWS = 10
const TIME_WINDOW_BEFORE = 15 // minutes before current time
const TIME_WINDOW_AFTER = 120 // minutes after current time (2 hours)

// ---- State ----
const currentTime = ref('--:--')
const displayFlights = ref([])
const scrollOffset = ref(0)
const renderRounds = ref(1) // how many times to render the list for seamless scroll
const logoAttempts = ref({})
const dotOpacity = ref(1)
const listWrapper = ref(null)
const listInner = ref(null)

let timeTimer = null
let scrollTimer = null
let dotTimer = null

// ---- Time Management ----
function updateTime() {
  const now = new Date()
  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')
  currentTime.value = `${hh}:${mm}`
  filterFlights(hh, mm)
}

// ---- Flight Filtering & Status Simulation ----
function timeToMinutes(hh, mm) {
  return parseInt(hh) * 60 + parseInt(mm)
}

function parseScheduledMinutes(timeStr) {
  const parts = timeStr.split(':')
  return parseInt(parts[0]) * 60 + parseInt(parts[1])
}

function computeStatus(scheduledMinutes, currentMinutes, baseStatus) {
  // If originally cancelled, always show 欠航
  if (baseStatus === 'Cancelled') return '欠航'

  const diff = scheduledMinutes - currentMinutes

  if (diff > 45) return 'Check-In'
  if (diff > 15) return 'BOARDING'
  if (diff > 5) return 'FINAL CALL'
  if (diff >= -15) return 'Gate Closed'
  return 'Departed'
}

function filterFlights(hh, mm) {
  const currentMinutes = timeToMinutes(hh, mm)
  const windowStart = currentMinutes - TIME_WINDOW_BEFORE
  const windowEnd = currentMinutes + TIME_WINDOW_AFTER

  const filtered = flightsData
    .filter((f) => {
      const schedMin = parseScheduledMinutes(f.scheduled)
      // Include flights within the time window
      if (schedMin < windowStart) return false
      if (schedMin > windowEnd) return false
      return true
    })
    .map((f) => {
      const schedMin = parseScheduledMinutes(f.scheduled)
      return {
        ...f,
        computedStatus: computeStatus(schedMin, currentMinutes, f.baseStatus),
      }
    })
    .sort((a, b) => parseScheduledMinutes(a.scheduled) - parseScheduledMinutes(b.scheduled))

  displayFlights.value = filtered

  // Determine if we need scrolling (more than MAX_VISIBLE_ROWS)
  renderRounds.value = filtered.length > MAX_VISIBLE_ROWS ? 2 : 1
}

// ---- Status CSS Class ----
function getStatusClass(status) {
  if (status === '欠航' || status === 'Cancelled') return 'status-cancelled'
  if (status === 'BOARDING' || status === 'Now Boarding') return 'status-boarding'
  if (status === 'FINAL CALL' || status === 'Final Call') return 'status-finalcall'
  if (status === 'Gate Closed') return 'status-gateclosed'
  if (status === 'Departed') return 'status-departed'
  if (status === 'Go To Gate') return 'status-gotogate'
  return 'status-checkin'
}

// ---- Logo & Badge ----
function getLogoAssetPath(code) {
  return new URL(`../assets/logos/${code}.png`, import.meta.url).href
}

function getBadgeStyle(code) {
  const config = getAirlineConfig(code)
  return {
    backgroundColor: config.color,
    color: config.textColor,
  }
}

function onLogoError(code) {
  logoAttempts.value = { ...logoAttempts.value, [code]: false }
}

// ---- Scrolling Animation ----
function startScrolling() {
  if (displayFlights.value.length <= MAX_VISIBLE_ROWS) {
    scrollOffset.value = 0
    return
  }

  // Each row is approximately 62px (48px row + gaps)
  const rowHeight = 62
  let position = 0

  scrollTimer = setInterval(() => {
    position += 0.3 // pixels per frame at ~60fps
    const totalHeight = displayFlights.value.length * rowHeight
    if (position >= totalHeight) {
      position = 0
    }
    scrollOffset.value = position
  }, 16) // ~60fps
}

const scrollStyle = computed(() => {
  return {
    transform: `translateY(-${scrollOffset.value}px)`,
    transition: 'none',
  }
})

// ---- Blinking dot ----
function startDotBlink() {
  dotTimer = setInterval(() => {
    dotOpacity.value = dotOpacity.value === 1 ? 0 : 1
  }, 800)
}

// ---- Lifecycle ----
onMounted(() => {
  updateTime()
  timeTimer = setInterval(updateTime, 60000) // every 1 min
  startScrolling()
  startDotBlink()
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
  if (scrollTimer) clearInterval(scrollTimer)
  if (dotTimer) clearInterval(dotTimer)
})

watch(displayFlights, () => {
  if (scrollTimer) clearInterval(scrollTimer)
  scrollOffset.value = 0
  startScrolling()
})
</script>

<style scoped>
.fids-container {
  width: 960px;
  height: 1080px;
  background: #0a0a0a;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'Courier New', 'MS Gothic', 'Osaka-Mono', monospace;
  border-left: 1px solid #1a1a1a;
}

/* Header */
.fids-header {
  padding: 24px 28px 16px;
  border-bottom: 1px solid #222;
  display: flex;
  align-items: baseline;
  gap: 20px;
}

.header-title {
  font-size: 22px;
  font-weight: bold;
  color: #ffd700;
  letter-spacing: 4px;
}

.header-subtitle {
  font-size: 14px;
  color: #888;
  letter-spacing: 6px;
}

.header-time {
  margin-left: auto;
  font-size: 28px;
  color: #ffd700;
  font-weight: bold;
  letter-spacing: 2px;
}

/* Column Headers */
.fids-col-headers {
  display: flex;
  align-items: center;
  padding: 10px 28px;
  border-bottom: 1px solid #1a1a1a;
  font-size: 11px;
  color: #666;
  letter-spacing: 2px;
}

.col-time { width: 70px; }
.col-dest { flex: 1; }
.col-flight { width: 200px; }
.col-gate { width: 70px; text-align: center; }
.col-status { width: 140px; text-align: center; }

/* Flight List */
.fids-list-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.fids-list {
  will-change: transform;
}

/* Flight Row */
.fids-row {
  display: flex;
  align-items: center;
  padding: 10px 28px;
  border-bottom: 1px solid #111;
  min-height: 56px;
  font-size: 15px;
  color: #ddd;
}

.fids-row:nth-child(even) {
  background: rgba(255, 255, 255, 0.01);
}

/* Cells */
.cell-logo {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  flex-shrink: 0;
}

.airline-logo-img {
  max-width: 32px;
  max-height: 32px;
  object-fit: contain;
}

.airline-badge {
  width: 36px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 9px;
  font-weight: bold;
  letter-spacing: 1px;
  border-radius: 2px;
  font-family: 'Courier New', monospace;
}

.cell-time {
  width: 70px;
  color: #ffd700;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 2px;
}

.cell-dest {
  flex: 1;
  font-size: 14px;
  letter-spacing: 1px;
  color: #ccc;
}

.cell-flight {
  width: 200px;
  font-size: 13px;
  color: #999;
  letter-spacing: 1px;
}

.cell-gate {
  width: 70px;
  text-align: center;
  font-size: 16px;
  color: #ffd700;
  font-weight: bold;
}

.cell-status {
  width: 140px;
  text-align: center;
  font-size: 13px;
  font-weight: bold;
  letter-spacing: 2px;
}

/* Status Colors */
.status-checkin .cell-status {
  color: #4caf50;
}

.status-boarding .cell-status {
  color: #ffd700;
  animation: blink-yellow 1s step-end infinite;
}

.status-finalcall .cell-status {
  color: #ff3333;
  animation: blink-red 0.6s step-end infinite;
}

.status-gateclosed .cell-status {
  color: #ff9800;
}

.status-cancelled .cell-status {
  color: #ff0000;
  background: rgba(255, 0, 0, 0.1);
  padding: 2px 12px;
}

.status-departed .cell-status {
  color: #555;
}

.status-gotogate .cell-status {
  color: #00bcd4;
}

@keyframes blink-yellow {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.2; }
}

@keyframes blink-red {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.15; }
}

/* Footer */
.fids-footer {
  padding: 12px 28px;
  border-top: 1px solid #1a1a1a;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 11px;
  color: #555;
  letter-spacing: 2px;
}

.footer-dot {
  color: #00ff00;
  font-size: 10px;
}
</style>

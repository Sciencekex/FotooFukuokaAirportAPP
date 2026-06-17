<template>
  <div class="photo-frame">
    <!-- Ken Burns Photo Display -->
    <transition name="crossfade" mode="out-in">
      <div
        :key="currentIndex"
        class="photo-container"
        :style="kenBurnsStyle"
      >
        <img
          v-if="currentPhoto"
          :src="currentPhoto"
          alt="Photo"
          class="photo-image"
          @error="onImageError"
        />
        <div v-else class="photo-placeholder">
          <span class="placeholder-icon">&#128247;</span>
          <span class="placeholder-text">No Photos</span>
        </div>
      </div>
    </transition>

    <!-- Interval Controls (compact, top-right overlay) -->
    <div class="interval-controls">
      <button
        v-for="opt in intervalOptions"
        :key="opt.value"
        :class="['interval-btn', { active: photoIntervalMs === opt.value }]"
        @click="setInterval(opt.value)"
      >
        {{ opt.label }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { Filesystem, Directory } from '@capacitor/filesystem'
import { Capacitor } from '@capacitor/core'

// ---- Props ----
const props = defineProps({
  albumPath: {
    type: String,
    default: '/storage/emulated/0/Pictures/BoardAlbum/',
  },
})

// ---- State ----
const photos = ref([])
const currentIndex = ref(0)
const photoIntervalMs = ref(5 * 60 * 1000) // default 5 min
const kenBurnsStyle = ref({})
const currentPhoto = computed(() => photos.value[currentIndex.value] || null)

let timer = null
let kenBurnsTimer = null

const intervalOptions = [
  { label: '3m', value: 3 * 60 * 1000 },
  { label: '5m', value: 5 * 60 * 1000 },
  { label: '10m', value: 10 * 60 * 1000 },
]

// ---- Fisher-Yates Shuffle ----
function shuffle(arr) {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

// ---- Ken Burns Effect: random subtle zoom/pan ----
function generateKenBurns() {
  const scaleStart = 1.0
  const scaleEnd = 1.04 + Math.random() * 0.03
  const txStart = Math.random() * 2 - 1 // -1 to 1%
  const txEnd = Math.random() * 2 - 1
  const tyStart = Math.random() * 2 - 1
  const tyEnd = Math.random() * 2 - 1

  const id = `kb_${Date.now()}`
  const styleEl = document.createElement('style')
  styleEl.id = id
  styleEl.textContent = `
    @keyframes ${id} {
      0% { transform: scale(${scaleStart}) translate(${txStart}%, ${tyStart}%); }
      100% { transform: scale(${scaleEnd}) translate(${txEnd}%, ${tyEnd}%); }
    }
  `
  // Remove old animation
  const old = document.getElementById(`kb_${currentIndex.value}`)
  if (old) old.remove()

  document.head.appendChild(styleEl)
  return {
    animation: `${id} 10s ease-in-out infinite alternate`,
  }
}

// ---- Load Photos from Capacitor Filesystem ----
async function loadPhotos() {
  if (!Capacitor.isNativePlatform()) {
    // Dev mode: use sample placeholder
    console.warn('[PhotoFrame] Not on native platform, no photos available')
    photos.value = []
    return
  }

  try {
    const result = await Filesystem.readdir({
      path: props.albumPath,
      directory: Directory.External,
    })

    const imageFiles = result.files
      .filter((f) => /\.(jpg|jpeg|png|webp|bmp)$/i.test(f.name))
      .map((f) => f.uri)

    if (imageFiles.length > 0) {
      // Convert Capacitor file URIs to safe URLs
      const safeUrls = imageFiles.map((uri) => Capacitor.convertFileSrc(uri))
      photos.value = shuffle(safeUrls)
      console.log(`[PhotoFrame] Loaded ${photos.value.length} photos`)
    }
  } catch (err) {
    console.error('[PhotoFrame] Failed to load photos:', err)
    photos.value = []
  }
}

// ---- Cycle to next photo ----
function nextPhoto() {
  if (photos.value.length === 0) return
  currentIndex.value = (currentIndex.value + 1) % photos.value.length
  kenBurnsStyle.value = generateKenBurns()
}

function onImageError() {
  // If an image fails to load, skip it after a short delay
  setTimeout(() => {
    if (photos.value.length > 1) {
      photos.value.splice(currentIndex.value, 1)
      if (currentIndex.value >= photos.value.length) {
        currentIndex.value = 0
      }
      kenBurnsStyle.value = generateKenBurns()
    }
  }, 2000)
}

function setInterval(ms) {
  photoIntervalMs.value = ms
  resetTimer()
}

function resetTimer() {
  if (timer) clearInterval(timer)
  if (photos.value.length === 0) return
  timer = setInterval(nextPhoto, photoIntervalMs.value)
}

// ---- Lifecycle ----
onMounted(async () => {
  await loadPhotos()
  if (photos.value.length > 0) {
    kenBurnsStyle.value = generateKenBurns()
    resetTimer()
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (kenBurnsTimer) clearInterval(kenBurnsTimer)
})

watch(photoIntervalMs, resetTimer)
</script>

<style scoped>
.photo-frame {
  position: relative;
  width: 1440px;
  height: 1080px;
  overflow: hidden;
  background: #000;
}

.photo-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  will-change: transform;
}

.photo-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.photo-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #333;
  gap: 16px;
}

.placeholder-icon {
  font-size: 64px;
  opacity: 0.3;
}

.placeholder-text {
  font-size: 24px;
  font-family: 'Courier New', monospace;
  opacity: 0.3;
}

/* Interval Controls */
.interval-controls {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  gap: 4px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.3s;
}

.photo-frame:hover .interval-controls {
  opacity: 1;
}

.interval-btn {
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.7);
  padding: 4px 10px;
  font-size: 12px;
  font-family: 'Courier New', monospace;
  cursor: pointer;
  border-radius: 2px;
  letter-spacing: 1px;
}

.interval-btn.active {
  background: rgba(255, 215, 0, 0.2);
  border-color: rgba(255, 215, 0, 0.5);
  color: #ffd700;
}

/* Cross-fade Transition */
.crossfade-enter-active,
.crossfade-leave-active {
  transition: opacity 1.8s ease;
}

.crossfade-enter-from,
.crossfade-leave-to {
  opacity: 0;
}

.crossfade-enter-to,
.crossfade-leave-from {
  opacity: 1;
}
</style>

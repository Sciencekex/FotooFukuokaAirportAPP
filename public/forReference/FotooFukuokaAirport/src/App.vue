<template>
  <div class="app-root" ref="rootRef">
    <div class="main-grid" :style="gridStyle">
      <!-- Left: Photo Frame (1440px × 1080px, 4:3 ratio) -->
      <div class="panel-left">
        <PhotoFrame album-path="/storage/emulated/0/Pictures/BoardAlbum/" />
      </div>

      <!-- Right: Fukuoka FIDS (960px × 1080px) -->
      <div class="panel-right">
        <FukuokaFIDS />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import PhotoFrame from './components/PhotoFrame.vue'
import FukuokaFIDS from './components/FukuokaFIDS.vue'

const DESIGN_W = 2400
const DESIGN_H = 1080
const rootRef = ref(null)

const gridStyle = ref({
  width: `${DESIGN_W}px`,
  height: `${DESIGN_H}px`,
})

/**
 * Compute scale factor so the 2400×1080 design fills the screen exactly.
 * 
 * Two scenarios:
 * A) target-densitydpi=device-dpi works → CSS pixels == physical pixels
 *    screen.width == 2400, no scaling needed (scale = 1.0)
 * B) target-densitydpi is ignored → CSS viewport is smaller (e.g. 1200×540)
 *    We scale the 2400×1080 canvas down to fit the CSS viewport.
 *    DPR will render it sharp since the browser has more physical pixels.
 * 
 * We use Math.max(innerWidth, screen.width * devicePixelRatio) to catch
 * the actual physical pixel dimension regardless of WebView behavior.
 */
function recalc() {
  const dpr = window.devicePixelRatio || 1
  const w = Math.max(window.innerWidth, (window.screen?.width || 2400))
  const h = Math.max(window.innerHeight, (window.screen?.height || 1080))

  // If WebView respects target-densitydpi, innerWidth ≈ 2400
  // Otherwise, use screen.width * dpr as fallback
  const effectiveW = Math.max(w, Math.round((window.screen?.width || 2400) * dpr))
  const effectiveH = Math.max(h, Math.round((window.screen?.height || 1080) * dpr))

  const scaleX = effectiveW / DESIGN_W
  const scaleY = effectiveH / DESIGN_H
  const scale = Math.min(scaleX, scaleY)

  gridStyle.value = {
    width: `${DESIGN_W}px`,
    height: `${DESIGN_H}px`,
    transform: `scale(${scale})`,
    transformOrigin: 'center center',
  }
}

onMounted(() => {
  recalc()
  window.addEventListener('resize', recalc)
  // Also recalc on orientation change
  window.addEventListener('orientationchange', () => setTimeout(recalc, 100))
})
</script>

<style>
/* ---- Global Reset ---- */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #000;
  position: fixed;
  top: 0;
  left: 0;
}

/* ---- App Root ---- */
.app-root {
  width: 100vw;
  height: 100vh;
  height: 100dvh; /* dynamic viewport height for modern browsers */
  overflow: hidden;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-grid {
  display: flex;
  flex-shrink: 0;
  overflow: hidden;
  will-change: transform;
  backface-visibility: hidden;
}

/* Panel Sizes */
.panel-left {
  width: 1440px;
  height: 1080px;
  flex-shrink: 0;
  overflow: hidden;
  position: relative;
}

.panel-right {
  width: 960px;
  height: 1080px;
  flex-shrink: 0;
  overflow: hidden;
  position: relative;
}
</style>

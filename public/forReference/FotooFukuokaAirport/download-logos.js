/**
 * Download airline logos from Fukuoka Airport website.
 * Run: node download-logos.js
 */

import axios from 'axios'
import { writeFileSync, mkdirSync, existsSync } from 'fs'
import { resolve, dirname, extname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const LOGO_DIR = resolve(__dirname, 'src', 'assets', 'logos')

// Known logo URLs on fukuoka-airport.jp (stable, codeshare badge images)
const LOGO_MAP = {
  // === Japanese Domestic ===
  'JAL': 'https://www.fukuoka-airport.jp/en/uploads/2020/07/img_flight_JL.jpg',
  'ANA': 'https://www.fukuoka-airport.jp/en/uploads/2024/07/NH_LOGOS_FUK_BADGE2_280X80px.jpg',
  'ADO': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/ADO.png',
  'APJ': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/APJ.png',
  'SFJ': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/SFJ.png',
  'SKY': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/SKY.png',
  'ORC': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/ORC.png',
  'FDA': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/FDA.png',
  'IBX': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/IBX.png',
  'JTA': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/JTA.png',
  'GK':  'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/JJP.png',
  'SNA': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/SNA.png',
  'AMX': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/AMX.png',

  // === Korean ===
  'KE': 'https://www.fukuoka-airport.jp/en/uploads/2025/09/KE_LOGO_202507.jpg',
  'OZ': 'https://www.fukuoka-airport.jp/en/uploads/2025/01/OZ_280x80.png',
  'BX': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/ABL.png',
  'LJ': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/JNA.png',
  '7C': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/JJA.png',
  'TW': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/TWB.png',
  'ZE': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/ESR.png',
  'RS': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/ASV.png',
  'RF': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/EOK.png',

  // === Taiwan / China ===
  'BR': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/EVA.png',
  'CI': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/CAL.png',
  'IT': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/TTW.png',
  'JX': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/SJX.png',
  'CA': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/CCA.png',
  'MU': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/CES.png',
  '9C': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/CQH.png',
  'FM': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/CSH.png',
  'HO': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/DKH.png',

  // === Southeast Asia / Other ===
  'CX': 'https://www.fukuoka-airport.jp/en/uploads/2025/09/CXlogo.jpg',
  'UO': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/HKE.png',
  'HB': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/HGB.png',
  'HX': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/CRK.png',
  'SQ': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/SIA.png',
  'TG': 'https://www.fukuoka-airport.jp/en/uploads/2024/07/TG_LOGOS_FUK_BADGE2_280X80px.jpg',
  'VN': 'https://www.fukuoka-airport.jp/en/uploads/2020/07/img_flight_VN.jpg',
  'PR': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/PAL.png',
  'VJ': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/VJC.png',
  'FD': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/AIQ.png',
  '5J': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/CEB.png',
  'AK': 'https://www.fukuoka-airport.jp/en/assets/flight/images/airline/AXM.png',
}

async function downloadLogo(code, url) {
  const ext = extname(new URL(url).pathname).replace('jpg', 'png') || '.png'
  const filePath = resolve(LOGO_DIR, `${code}${ext}`)

  try {
    const resp = await axios.get(url, {
      responseType: 'arraybuffer',
      timeout: 15000,
      headers: {
        'User-Agent': 'Mozilla/5.0',
        'Referer': 'https://www.fukuoka-airport.jp/',
      },
    })

    // Convert jpg to png for consistent file naming
    const outPath = resolve(LOGO_DIR, `${code}.png`)
    writeFileSync(outPath, Buffer.from(resp.data))
    console.log(`  [OK] ${code} -> ${code}.png (${resp.data.byteLength} bytes)`)
    return true
  } catch (err) {
    console.log(`  [FAIL] ${code}: ${err.message}`)
    return false
  }
}

async function main() {
  console.log('=== Downloading Airline Logos ===\n')

  if (!existsSync(LOGO_DIR)) {
    mkdirSync(LOGO_DIR, { recursive: true })
  }

  let ok = 0, fail = 0
  const codes = Object.keys(LOGO_MAP)

  // Download in batches of 3 to avoid rate limiting
  for (let i = 0; i < codes.length; i += 3) {
    const batch = codes.slice(i, i + 3)
    const results = await Promise.all(
      batch.map(code => downloadLogo(code, LOGO_MAP[code]))
    )
    ok += results.filter(Boolean).length
    fail += results.filter(r => !r).length
  }

  console.log(`\n=== Done: ${ok} downloaded, ${fail} failed ===`)
  console.log(`Logos saved to: ${LOGO_DIR}`)
}

main().catch(err => {
  console.error('Fatal:', err)
  process.exit(1)
})

/**
 * Fukuoka Airport Flight Data Generator
 *
 * The airport website uses client-side JS rendering, so traditional scraping
 * won't work. Instead, this script serves as a reference for how the static
 * flight data in src/assets/fukuoka_flights.json is structured.
 *
 * To update flight data: manually edit src/assets/fukuoka_flights.json
 * or use a headless browser (Puppeteer/Playwright) to scrape the real data.
 */

import { writeFileSync, readFileSync, existsSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const DATA_PATH = resolve(__dirname, 'src', 'assets', 'fukuoka_flights.json')

console.log('=== Fukuoka Airport Flight Data ===')

if (existsSync(DATA_PATH)) {
  const data = JSON.parse(readFileSync(DATA_PATH, 'utf-8'))
  const domestic = data.filter((f) => f.type === 'DOMESTIC').length
  const international = data.filter((f) => f.type === 'INTERNATIONAL').length
  console.log(`  Static data: ${data.length} flights total`)
  console.log(`  Domestic: ${domestic} | International: ${international}`)
  console.log(`  Time range: ${data[0].scheduled} ~ ${data[data.length - 1].scheduled}`)
  console.log('\n  The airport website uses JS rendering.')
  console.log('  To update data, use a headless browser or edit the JSON manually.')
} else {
  console.log('  No data file found. Creating empty template...')
  writeFileSync(DATA_PATH, '[]', 'utf-8')
  console.log('  Please populate src/assets/fukuoka_flights.json with real data.')
}

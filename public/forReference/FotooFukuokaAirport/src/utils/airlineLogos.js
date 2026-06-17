/**
 * Airline Logo & Badge Configuration
 *
 * Maps airline codes to display names, badge colors, and logo paths.
 * Works fully offline: tries to load local PNG first, falls back to CSS badge.
 */

export const AIRLINE_CONFIG = {
  // ===== Japanese Domestic =====
  JAL: { name: '日本航空', color: '#C8102E', textColor: '#FFFFFF' },
  ANA: { name: '全日空', color: '#003C82', textColor: '#FFFFFF' },
  APJ: { name: 'ピーチ', color: '#D5007F', textColor: '#FFFFFF' },
  SFJ: { name: 'スターフライヤー', color: '#000000', textColor: '#FFFFFF' },
  SKY: { name: 'スカイマーク', color: '#0099D8', textColor: '#FFFFFF' },
  ADO: { name: 'エア・ドゥ', color: '#00A54F', textColor: '#FFFFFF' },
  ORC: { name: 'オリエンタルエア', color: '#2354A0', textColor: '#FFFFFF' },
  FDA: { name: 'フジドリーム', color: '#E60012', textColor: '#FFFFFF' },
  IBX: { name: 'アイベックス', color: '#B9975B', textColor: '#FFFFFF' },
  JTA: { name: '日本トランスオーシャン', color: '#FF6B00', textColor: '#FFFFFF' },
  GK:  { name: 'ジェットスター', color: '#F26322', textColor: '#FFFFFF' },
  AMX: { name: '天草エアライン', color: '#009944', textColor: '#FFFFFF' },
  SNA: { name: 'ソラシドエア', color: '#228B22', textColor: '#FFFFFF' },

  // ===== International =====
  BX:  { name: 'AIR BUSAN', color: '#D50032', textColor: '#FFFFFF' },
  OZ:  { name: 'アシアナ航空', color: '#8B0000', textColor: '#FFFFFF' },
  KE:  { name: '大韓航空', color: '#0066B3', textColor: '#FFFFFF' },
  LJ:  { name: 'JIN AIR', color: '#00A0E9', textColor: '#FFFFFF' },
  RS:  { name: 'Air Seoul', color: '#E60012', textColor: '#FFFFFF' },
  TW:  { name: 'T\'way Air', color: '#E30613', textColor: '#FFFFFF' },
  ZE:  { name: 'Eastar Jet', color: '#A50034', textColor: '#FFFFFF' },
  '7C': { name: 'Jeju Air', color: '#F15A22', textColor: '#FFFFFF' },
  RF:  { name: 'AeroK', color: '#00A0E9', textColor: '#FFFFFF' },

  CI:  { name: '中華航空', color: '#D50032', textColor: '#FFFFFF' },
  BR:  { name: 'エバー航空', color: '#00703C', textColor: '#FFFFFF' },
  IT:  { name: 'タイガーエア台湾', color: '#FFB81C', textColor: '#000000' },
  JX:  { name: 'スターラックス', color: '#C4A572', textColor: '#000000' },

  CA:  { name: '中国国際航空', color: '#DA291C', textColor: '#FFFFFF' },
  MU:  { name: '中国東方航空', color: '#DF0A24', textColor: '#FFFFFF' },
  '9C':{ name: '春秋航空', color: '#00AA50', textColor: '#FFFFFF' },
  FM:  { name: '上海航空', color: '#E60012', textColor: '#FFFFFF' },
  HO:  { name: '吉祥航空', color: '#C4161C', textColor: '#FFFFFF' },
  GJ:  { name: '長竜航空', color: '#FF6A00', textColor: '#FFFFFF' },

  SQ:  { name: 'シンガポール航空', color: '#0033A0', textColor: '#FFFFFF' },
  TG:  { name: 'タイ国際航空', color: '#660099', textColor: '#FFFFFF' },
  VN:  { name: 'ベトナム航空', color: '#00519E', textColor: '#FFFFFF' },
  VJ:  { name: 'ベトジェット', color: '#E60012', textColor: '#FFFFFF' },
  PR:  { name: 'フィリピン航空', color: '#0033A0', textColor: '#FFFFFF' },
  '5J':{ name: 'セブパシフィック', color: '#0033A0', textColor: '#FFDD00' },
  CX:  { name: 'キャセイパシフィック', color: '#006564', textColor: '#FFFFFF' },
  UO:  { name: '香港エクスプレス', color: '#D50032', textColor: '#FFFFFF' },
  HB:  { name: 'グレーターベイ', color: '#007A33', textColor: '#FFFFFF' },
  AK:  { name: 'AirAsia', color: '#E42527', textColor: '#FFFFFF' },

  // Codeshare-only codes (for display)
  DL:  { name: 'Delta', color: '#003366', textColor: '#FFFFFF' },
  JL:  { name: '日本航空', color: '#C8102E', textColor: '#FFFFFF' },
  NH:  { name: '全日空', color: '#003C82', textColor: '#FFFFFF' },
  EY:  { name: 'Etihad', color: '#BD8B14', textColor: '#FFFFFF' },
  KL:  { name: 'KLM', color: '#00A1DE', textColor: '#FFFFFF' },
  OM:  { name: 'MIAT Mongolian', color: '#0066CC', textColor: '#FFFFFF' },
  VS:  { name: 'Virgin Atlantic', color: '#DA0530', textColor: '#FFFFFF' },
  AZ:  { name: 'ITA Airways', color: '#006C3C', textColor: '#FFFFFF' },
  AF:  { name: 'Air France', color: '#002157', textColor: '#FFFFFF' },
  QR:  { name: 'Qatar Airways', color: '#8B1F41', textColor: '#FFFFFF' },
  TK:  { name: 'Turkish Airlines', color: '#C8102E', textColor: '#FFFFFF' },
  UA:  { name: 'United', color: '#005DAA', textColor: '#FFFFFF' },
  AC:  { name: 'Air Canada', color: '#E01A2C', textColor: '#FFFFFF' },
  NZ:  { name: 'Air New Zealand', color: '#000000', textColor: '#FFFFFF' },
  ET:  { name: 'Ethiopian', color: '#00873C', textColor: '#FFFFFF' },
}

// Maps airline code (used for logo filename and badge matching)
// Some airline codes need normalization for logo file lookup
export function getAirlineCode(key) {
  // Normalize: some codes like "GK" are actually Jetstar Japan (JJP-code)
  const mapping = {
    'GK': 'GK',  // Jetstar Japan
    '7C': '7C',
    '9C': '9C',
    '5J': '5J',
  }
  return mapping[key] || key
}

export function getAirlineConfig(code) {
  const config = AIRLINE_CONFIG[code]
  if (config) return config
  // Default: grey badge with code
  return { name: code, color: '#555555', textColor: '#FFFFFF' }
}

/**
 * Returns the path to the local airline logo PNG file.
 * Falls back to null if no logo exists (triggers CSS badge).
 */
export function getLogoPath(code) {
  const normalizedCode = getAirlineCode(code)
  return `/src/assets/logos/${normalizedCode}.png`
}

/**
 * Determines if the logo file should be attempted (only for major airlines).
 * In offline mode, we control which logos we have bundled.
 */
const BUNDLED_LOGOS = [
  'ADO', 'AK', 'AMX', 'ANA', 'APJ',
  'BR', 'BX',
  'CA', 'CI', 'CX',
  'FDA', 'FM',
  'GJ', 'GK',
  'HB', 'HO', 'HX',
  'IBX', 'IT',
  'JAL', 'JTA', 'JX',
  'KE', 'KN',
  'LJ',
  'MU',
  'ORC', 'OZ',
  'PR',
  'RF', 'RS',
  'SFJ', 'SKY', 'SNA', 'SQ',
  'TG', 'TW',
  'UO',
  'VJ', 'VN', 'VZ',
  'ZE',
  '5J', '7C', '9C',
  'FD',
]

export function hasBundledLogo(code) {
  return BUNDLED_LOGOS.includes(getAirlineCode(code))
}

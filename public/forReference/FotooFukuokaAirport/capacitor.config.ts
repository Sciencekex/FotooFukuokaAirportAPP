import { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'com.fotoo.fukuoka.airport',
  appName: 'Fukuoka Airport Board',
  webDir: 'dist',
  server: {
    androidScheme: 'https',
  },
  android: {
    allowMixedContent: true,
  },
  plugins: {
    Filesystem: {
      androidPermissions: ['READ_EXTERNAL_STORAGE', 'READ_MEDIA_IMAGES'],
    },
  },
}

export default config

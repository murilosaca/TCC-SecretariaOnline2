/// <reference types="vitest" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
  },
  server: {
    port: 5174,
    proxy: {
      '/academico': { target: 'http://localhost:8080', changeOrigin: true },
      '/publico': { target: 'http://localhost:8080', changeOrigin: true },
      '/auth': { target: 'http://localhost:8080', changeOrigin: true },
      '/me': { target: 'http://localhost:8080', changeOrigin: true },
      '/bff': { target: 'http://localhost:8080', changeOrigin: true },
      '/requests': { target: 'http://localhost:8080', changeOrigin: true },
      '/request-types': { target: 'http://localhost:8080', changeOrigin: true },
      '/events': { target: 'http://localhost:8080', changeOrigin: true },
      '/atendimentos': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          if (req.headers.accept?.includes('text/html')) {
            return '/index.html'
          }
        },
      },
      '/egressos': { target: 'http://localhost:8080', changeOrigin: true },
      '/importacoes': { target: 'http://localhost:8080', changeOrigin: true },
      '/exportacoes': { target: 'http://localhost:8080', changeOrigin: true },
      '/audit-log': { target: 'http://localhost:8080', changeOrigin: true },
      '/diplomas': { target: 'http://localhost:8080', changeOrigin: true },
      '/formativas': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          if (req.headers.accept?.includes('text/html')) {
            return '/index.html'
          }
        },
      },
      '/estagios': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          if (req.headers.accept?.includes('text/html')) {
            return '/index.html'
          }
        },
      },
      '/tccs': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          if (req.headers.accept?.includes('text/html')) {
            return '/index.html'
          }
        },
      },
      '/certificates': { target: 'http://localhost:8080', changeOrigin: true },
      '/communications': { target: 'http://localhost:8080', changeOrigin: true },
      '/comissoes': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          if (req.headers.accept?.includes('text/html')) {
            return '/index.html'
          }
        },
      },
      '/coordenacao': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          if (req.headers.accept?.includes('text/html')) {
            return '/index.html'
          }
        },
      },
      '/reports': { target: 'http://localhost:8080', changeOrigin: true },
      '/admin': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          if (req.headers.accept?.includes('text/html')) {
            return '/index.html'
          }
        },
      },
      '/search': { target: 'http://localhost:8080', changeOrigin: true },
      '/suporte': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          if (req.headers.accept?.includes('text/html')) {
            return '/index.html'
          }
        },
      },
      '/iam': { target: 'http://localhost:8080', changeOrigin: true },
      '/.well-known': { target: 'http://localhost:8080', changeOrigin: true },
      '/v3': { target: 'http://localhost:8080', changeOrigin: true },
      '/swagger-ui': { target: 'http://localhost:8080', changeOrigin: true },
      '/actuator': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})

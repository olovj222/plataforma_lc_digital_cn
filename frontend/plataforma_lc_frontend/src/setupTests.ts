import '@testing-library/jest-dom';
import { beforeAll, afterEach, afterAll, vi } from 'vitest'
import { server } from './mocks/server'

vi.mock('./src/keycloak', () => {
  return {
    default: {
      token: 'token-falso-para-que-axios-sea-feliz',
      authenticated: true,
      login: vi.fn(),
      logout: vi.fn(),
    }
  }
})

// 1. Enciende el servidor falso ANTES de que corran los tests
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))

// 2. Limpia cualquier interceptor temporal DESPUÉS de cada test
// (para que un test no ensucie al siguiente)
afterEach(() => server.resetHandlers())

// 3. Apaga el servidor al terminar toda la suite
afterAll(() => server.close())
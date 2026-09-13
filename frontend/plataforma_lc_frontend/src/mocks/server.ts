
import { setupServer } from 'msw/node'
import { handlers } from './handlers'

// Configuramos un servidor de peticiones con nuestros handlers
export const server = setupServer(...handlers)
import Keycloak from 'keycloak-js'

const keycloak = new Keycloak({
  url: 'http://localhost:8090',
  realm: 'plataforma_lc',
  clientId: 'plataforma_lc_frontend'
})

export default keycloak
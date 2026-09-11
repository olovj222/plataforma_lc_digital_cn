import { PublicClientApplication } from '@azure/msal-browser'; //[cite: 1]

export const msalConfig = {
    auth: {
        // Estos campos hay que configurarlos dependiendo de con que cuenta vamos a trabajar 
        clientId: '70af68d7-f0b7-4897-9d64-4a0b0791ca70', 
        authority: 'https://login.microsoftonline.com/2dcf78c8-4359-4115-8b06-50c5a455e4e0',
        redirectUri: window.location.origin,
    },
    cache: {
        cacheLocation: 'localStorage' 
    }
};

export const msalInstance = new PublicClientApplication(msalConfig); 
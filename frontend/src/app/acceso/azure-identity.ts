import { Injectable, InjectionToken, Provider, inject } from '@angular/core';
import { MSAL_INSTANCE, MsalService } from '@azure/msal-angular';
import { PublicClientApplication, BrowserCacheLocation } from '@azure/msal-browser';
import { switchMap, map } from 'rxjs';
export interface AzureIdentitySettings {clientId:string;authority:string;redirectUri:string;scopes:string[];}
const AZURE_SETTINGS=new InjectionToken<AzureIdentitySettings>('AzureIdentitySettings');
export function provideAzureIdentity(settings?:AzureIdentitySettings):Provider[]{
 if(!settings)return [];
 if(!settings.clientId||!settings.authority||!settings.redirectUri||!settings.scopes.length)throw new Error('Configuración empresarial incompleta');
 return [{provide:AZURE_SETTINGS,useValue:settings},{provide:MSAL_INSTANCE,useFactory:()=>new PublicClientApplication({auth:{clientId:settings.clientId,authority:settings.authority,redirectUri:settings.redirectUri},cache:{cacheLocation:BrowserCacheLocation.MemoryStorage}})},MsalService,AzureIdentityAdapter];
}
@Injectable()
export class AzureIdentityAdapter {
 private readonly msal=inject(MsalService);private readonly settings=inject(AZURE_SETTINGS);
 accessToken(){return this.msal.initialize().pipe(switchMap(()=>this.msal.loginPopup({scopes:this.settings.scopes})),map(result=>result.accessToken));}
}

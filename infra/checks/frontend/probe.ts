import {Component} from '@angular/core';
import {ButtonModule} from 'primeng/button';
import {OverlayModule} from '@angular/cdk/overlay';
import {MsalService} from '@azure/msal-angular';
import {PublicClientApplication} from '@azure/msal-browser';
import {Observable} from 'rxjs';

// Compile only: no page is served, no authentication flow or business feature.
@Component({
  selector: 'infrastructure-probe',
  standalone: true,
  imports: [ButtonModule, OverlayModule],
  template: '<p-button [label]="label" />',
})
export class InfrastructureProbe {
  readonly label: string = 'Infrastructure';
  identity$?: Observable<string>;
}
export type MsalProbe = {service: MsalService; client: PublicClientApplication};
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  template: `
<div style="min-height:100vh;display:flex;flex-direction:column;align-items:center;justify-content:center;background:var(--bg-void);text-align:center;padding:24px;position:relative;overflow:hidden">
  <div style="position:fixed;width:600px;height:600px;border-radius:50%;background:rgba(99,102,241,0.08);filter:blur(120px);top:-100px;left:50%;transform:translateX(-50%);pointer-events:none"></div>
  <div style="font-family:var(--font-display);font-size:clamp(6rem,20vw,12rem);font-weight:700;line-height:1;letter-spacing:-0.05em;background:linear-gradient(135deg,var(--accent-primary),var(--accent-tertiary));-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text;animation:fadeUp 0.5s ease forwards">404</div>
  <h2 style="font-size:1.5rem;margin:16px 0 8px;animation:fadeUp 0.5s 0.1s ease both">Page not found</h2>
  <p style="color:var(--text-secondary);margin-bottom:32px;animation:fadeUp 0.5s 0.2s ease both">The page you're looking for doesn't exist or has been moved.</p>
  <a routerLink="/" style="animation:fadeUp 0.5s 0.3s ease both" class="btn-primary"><i class="pi pi-home"></i> Back to Home</a>
</div>`
})
export class NotFoundComponent {}

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

import { SidebarComponent } from './shared/sidebar/sidebar.component';
import { ToastComponent } from './shared/components/toast/toast.component';
import { SessionService } from './core/services/session.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    SidebarComponent,
    ToastComponent
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent {
  showSidebar = false;

  constructor(
    private router: Router,
    private sessionService: SessionService
  ) {
    this.updateSidebarVisibility(this.router.url);

    if (this.sessionService.isLogged()) {
      this.sessionService.iniciarMonitorSesion();
    }

    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.updateSidebarVisibility(event.urlAfterRedirects);

        if (this.sessionService.isLogged()) {
          this.sessionService.iniciarMonitorSesion();
        }
      });
  }

  private updateSidebarVisibility(url: string): void {
    this.showSidebar = !url.startsWith('/login');
  }
}
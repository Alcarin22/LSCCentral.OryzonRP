import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { SidebarComponent } from './shared/sidebar/sidebar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent {
  showSidebar = false;

  constructor(private router: Router) {
    this.updateSidebarVisibility(this.router.url);

    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.updateSidebarVisibility(event.urlAfterRedirects);
      });
  }

  private updateSidebarVisibility(url: string): void {
    this.showSidebar = !url.startsWith('/login');
  }
}
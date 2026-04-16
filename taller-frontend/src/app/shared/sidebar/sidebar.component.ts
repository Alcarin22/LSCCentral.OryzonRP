import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SessionEmpleado, SessionService } from '../../core/services/session.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit {
  usuario: SessionEmpleado | null = null;
  isDarkMode = true;

  constructor(
    private session: SessionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.usuario = this.session.getEmpleado();

    const savedTheme = localStorage.getItem('theme');
    this.isDarkMode = savedTheme !== 'light';

    if (!this.isDarkMode) {
      document.body.classList.add('light');
    }
  }

  logout(): void {
    this.session.logout();
    this.router.navigate(['/login']);
  }

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;

    if (this.isDarkMode) {
      document.body.classList.remove('light');
    } else {
      document.body.classList.add('light');
    }

    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
  }

  get nombre(): string {
    return this.usuario?.nickServidor || this.usuario?.nombre || '';
  }

  get rango(): string {
    return this.usuario?.rango?.nombre || '';
  }

  get avatar(): string {
    return this.usuario?.avatarUrl || 'assets/default-avatar.png';
  }
}
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

import { SessionEmpleado, SessionService } from '../../core/services/session.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  empleado: SessionEmpleado | null = null;

  isDarkMode = true;
  avatar = '';
  nombre = 'Invitado';
  rango = 'Sin rango';

  constructor(
    private sessionService: SessionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.empleado = this.sessionService.getEmpleado();

    this.avatar = this.empleado?.avatarUrl || 'https://via.placeholder.com/64';
    this.nombre = this.empleado?.nickServidor || this.empleado?.nombre || 'Invitado';
    this.rango = this.empleado?.rango?.nombre || 'Sin rango';

    const savedTheme = localStorage.getItem('theme');
    this.isDarkMode = savedTheme ? savedTheme === 'dark' : true;
    this.aplicarTema();
  }

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
    this.aplicarTema();
  }

  logout(): void {
    this.sessionService.logout();
    this.router.navigate(['/login']);
  }

  private aplicarTema(): void {
    document.body.classList.toggle('light-mode', !this.isDarkMode);
    document.body.classList.toggle('dark-mode', this.isDarkMode);
  }
}
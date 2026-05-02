import { Component, OnInit } from '@angular/core';

@Component({
  standalone: false,
  selector: 'app-home',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements OnInit {
  isDark = false;

  ngOnInit(): void {
    const saved = localStorage.getItem('theme');
    this.isDark = saved === 'dark';
    document.documentElement.setAttribute('data-theme', this.isDark ? 'dark' : '');
  }

  toggleTheme(): void {
    this.isDark = !this.isDark;
    document.documentElement.setAttribute('data-theme', this.isDark ? 'dark' : '');
    localStorage.setItem('theme', this.isDark ? 'dark' : 'light');
  }
}

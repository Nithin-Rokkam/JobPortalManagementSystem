import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  standalone: false,
  selector: 'app-root',
  templateUrl: './app.html',
  styles: []
})
export class App implements OnInit {
  isLandingPage = false;

  constructor(private router: Router) { }

  ngOnInit(): void {
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd)
    ).subscribe((e: any) => {
      this.isLandingPage = e.urlAfterRedirects === '/' || e.urlAfterRedirects === '';
    });
    // Check on initial load
    this.isLandingPage = this.router.url === '/' || this.router.url === '';
  }
}

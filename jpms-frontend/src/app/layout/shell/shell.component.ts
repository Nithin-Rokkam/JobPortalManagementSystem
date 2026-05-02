import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { routeSlideAnimation } from '../../shared/animations/route-animations';

@Component({
    standalone: false,
    selector: 'app-shell',
    templateUrl: './shell.component.html',
    styleUrls: ['./shell.component.css'],
    animations: [routeSlideAnimation]
})
export class ShellComponent implements OnInit {
    sidebarCollapsed = false;
    currentUser: any;

    constructor(
        private auth: AuthService, 
        private router: Router,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.auth.currentUser$.subscribe(user => {
            this.currentUser = user;
            this.cdr.detectChanges();
        });
    }

    getRouteState(outlet: RouterOutlet): string {
        return this.router.url;
    }
}

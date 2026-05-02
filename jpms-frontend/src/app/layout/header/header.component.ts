import { Component, Input, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Subscription } from 'rxjs';

@Component({
    standalone: false,
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {
    @Input() pageTitle = '';
    user: any = null;
    showDropdown = false;
    isDark = false;

    private userSub!: Subscription;

    constructor(
        private auth: AuthService,
        private cdr: ChangeDetectorRef
    ) {
        // Read theme synchronously in constructor so the value is stable
        // before Angular's first change detection pass runs
        const saved = localStorage.getItem('theme');
        this.isDark = saved === 'dark';
        document.documentElement.setAttribute('data-theme', this.isDark ? 'dark' : '');
    }

    ngOnInit(): void {
        this.userSub = this.auth.currentUser$.subscribe(user => {
            this.user = user;
            this.cdr.detectChanges();
        });
    }

    ngOnDestroy(): void {
        this.userSub?.unsubscribe();
    }

    toggleTheme(): void {
        this.isDark = !this.isDark;
        document.documentElement.setAttribute('data-theme', this.isDark ? 'dark' : '');
        localStorage.setItem('theme', this.isDark ? 'dark' : 'light');
    }

    getInitials(name: string): string {
        return name?.split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2) || '??';
    }

    /** Returns the correct home route based on login state */
    get logoRoute(): string {
        if (!this.user) return '/';
        const roleMap: Record<string, string> = {
            JOB_SEEKER: '/seeker/dashboard',
            RECRUITER: '/recruiter/dashboard',
            ADMIN: '/admin/dashboard'
        };
        return roleMap[this.user.role] ?? '/';
    }

    /** Returns the correct profile route based on role */
    get profileRoute(): string {
        const roleMap: Record<string, string> = {
            JOB_SEEKER: '/seeker/profile',
            RECRUITER: '/recruiter/profile',
            ADMIN: '/admin/profile'
        };
        return this.user ? (roleMap[this.user.role] ?? '/') : '/';
    }

    logout(): void {
        this.auth.logout();
    }
}


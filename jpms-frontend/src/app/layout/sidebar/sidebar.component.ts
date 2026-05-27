import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
    label: string;
    icon: string;
    link: string;
    roles: string[];
}

@Component({
    standalone: false,
    selector: 'app-sidebar',
    templateUrl: './sidebar.component.html',
    styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
    @Input() collapsed = false;
    @Output() toggleCollapse = new EventEmitter<void>();

    user: any;
    role: string | null = null;

    navItems: NavItem[] = [
        // Seeker
        { label: 'Dashboard', icon: '🏠', link: '/seeker/dashboard', roles: ['JOB_SEEKER'] },
        { label: 'Browse Jobs', icon: '🔍', link: '/seeker/browse', roles: ['JOB_SEEKER'] },
        { label: 'Applications', icon: '📋', link: '/seeker/applications', roles: ['JOB_SEEKER'] },
        { label: 'Profile', icon: '👤', link: '/seeker/profile', roles: ['JOB_SEEKER'] },
        // Recruiter
        { label: 'Dashboard', icon: '🏠', link: '/recruiter/dashboard', roles: ['RECRUITER'] },
        { label: 'Post Job', icon: '➕', link: '/recruiter/post-job', roles: ['RECRUITER'] },
        { label: 'My Jobs', icon: '📁', link: '/recruiter/my-jobs', roles: ['RECRUITER'] },
        { label: 'Profile', icon: '👤', link: '/recruiter/profile', roles: ['RECRUITER'] },
        // Admin
        { label: 'Dashboard', icon: '🏠', link: '/admin/dashboard', roles: ['ADMIN'] },
        { label: 'Users', icon: '👥', link: '/admin/users', roles: ['ADMIN'] },
        { label: 'Jobs', icon: '💼', link: '/admin/jobs', roles: ['ADMIN'] },
        { label: 'Reports', icon: '📊', link: '/admin/reports', roles: ['ADMIN'] },
        { label: 'Audit Logs', icon: '🗂️', link: '/admin/audit-logs', roles: ['ADMIN'] },
        { label: 'Profile', icon: '👤', link: '/admin/profile', roles: ['ADMIN'] },
    ];

    constructor(private auth: AuthService, private router: Router, private cdr: ChangeDetectorRef) { }

    ngOnInit(): void {
        this.auth.currentUser$.subscribe(user => {
            this.user = user;
            this.role = user?.role ?? null;
            this.cdr.detectChanges();
        });
    }

    get filteredNav(): NavItem[] {
        return this.navItems.filter(item => this.role && item.roles.includes(this.role!));
    }

    get dashboardRoute(): string {
        const roleMap: Record<string, string> = {
            JOB_SEEKER: '/seeker/dashboard',
            RECRUITER: '/recruiter/dashboard',
            ADMIN: '/admin/dashboard'
        };
        return this.role ? (roleMap[this.role] ?? '/') : '/';
    }

    get profileRoute(): string {
        const roleMap: Record<string, string> = {
            JOB_SEEKER: '/seeker/profile',
            RECRUITER: '/recruiter/profile',
            ADMIN: '/admin/profile'
        };
        return this.role ? (roleMap[this.role] ?? '/') : '/';
    }

    getInitials(name: string): string {
        return name?.split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2) || '??';
    }

    logout(): void {
        this.auth.logout();
    }
}


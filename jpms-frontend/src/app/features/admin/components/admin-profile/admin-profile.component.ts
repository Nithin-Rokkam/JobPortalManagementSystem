import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AuthApiService } from '../../../auth/services/auth-api.service';
import { AdminApiService } from '../../services/admin-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
    standalone: false,
    selector: 'app-admin-profile',
    templateUrl: './admin-profile.component.html',
    styleUrls: ['./admin-profile.component.css']
})
export class AdminProfileComponent implements OnInit {
    profile: any = null;
    report: any = null;
    loading = true;
    uploadingPic = false;
    profileMode: 'view' | 'edit' = 'view';

    constructor(
        private authApi: AuthApiService,
        private adminApi: AdminApiService,
        private auth: AuthService,
        private toast: ToastService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.authApi.getProfile().subscribe({
            next: p => {
                this.profile = p;
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.loading = false;
                this.toast.error('Failed to load profile');
                this.cdr.detectChanges();
            }
        });

        this.adminApi.getReport().subscribe({
            next: r => { this.report = r; this.cdr.detectChanges(); },
            error: () => { /* report is optional on profile */ }
        });
    }

    getInitials(name: string): string {
        return name?.split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2) || '??';
    }

    onPictureSelect(event: Event): void {
        const file = (event.target as HTMLInputElement).files?.[0];
        if (!file) return;
        this.uploadingPic = true;
        this.authApi.uploadProfilePicture(file).subscribe({
            next: (res: any) => {
                this.profile.profilePictureUrl = res.profilePictureUrl;
                this.auth.saveUser({ ...this.auth.getUser(), profilePictureUrl: res.profilePictureUrl });
                this.uploadingPic = false;
                this.toast.success('Profile picture updated');
                this.cdr.detectChanges();
            },
            error: () => {
                this.uploadingPic = false;
                this.toast.error('Upload failed');
                this.cdr.detectChanges();
            }
        });
    }

    get shortlistRate(): string {
        if (!this.report?.totalApplications) return '—';
        return ((this.report.applicationsByStatus.SHORTLISTED / this.report.totalApplications) * 100).toFixed(1) + '%';
    }

    logout(): void {
        this.auth.logout();
    }
}

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { AuthApiService } from '../../../auth/services/auth-api.service';
import { AuthService } from '../../../../core/services/auth.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
    standalone: false,
    selector: 'app-recruiter-profile',
    templateUrl: './recruiter-profile.component.html',
    styleUrls: ['./recruiter-profile.component.css']
})
export class RecruiterProfileComponent implements OnInit {
    profile: any = null;
    loading = true;
    uploadingPic = false;
    savingCompany = false;

    /** 'view' shows read-only info; 'edit' shows upload controls */
    profileMode: 'view' | 'edit' = 'view';

    /** Editable company name field value */
    companyNameInput = '';

    constructor(
        private authApi: AuthApiService,
        private auth: AuthService,
        private toast: ToastService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.auth.currentUser$.subscribe(() => this.cdr.detectChanges());
        this.authApi.getProfile().subscribe({
            next: p => {
                this.profile = p;
                this.companyNameInput = p.companyName || '';
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => { this.loading = false; this.toast.error('Failed to load profile'); this.cdr.detectChanges(); }
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
            error: () => { this.uploadingPic = false; this.toast.error('Upload failed'); this.cdr.detectChanges(); }
        });
    }

    onSaveCompany(): void {
        const name = this.companyNameInput.trim();
        if (!name) {
            this.toast.error('Company name cannot be empty');
            return;
        }
        this.savingCompany = true;
        this.cdr.detectChanges();

        this.authApi.updateCompanyName(name).subscribe({
            next: () => {
                this.profile.companyName = name;
                this.savingCompany = false;
                this.toast.success('Company name updated successfully');
                this.cdr.detectChanges();
            },
            error: () => {
                this.savingCompany = false;
                this.toast.error('Failed to update company name');
                this.cdr.detectChanges();
            }
        });
    }

    logout(): void {
        this.auth.logout();
    }
}

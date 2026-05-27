import { Component, OnInit } from '@angular/core';
import { AdminApiService } from '../../services/admin-api.service';
import { ToastService } from '../../../../core/services/toast.service';

@Component({
    standalone: false,
    selector: 'app-user-management',
    templateUrl: './user-management.component.html',
    styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
    users: any[] = [];
    loading = true;
    search = '';
    roleFilter = '';
    statusFilter = '';
    confirmAction: { type: 'ban' | 'unban' | 'delete'; user: any } | null = null;

    constructor(private adminApi: AdminApiService, private toast: ToastService) { }

    ngOnInit(): void {
        this.adminApi.getUsers().subscribe({
            next: u => { this.users = u; this.loading = false; },
            error: () => { this.loading = false; }
        });
    }

    get filtered(): any[] {
        return this.users.filter(u => {
            const matchSearch = !this.search || u.name?.toLowerCase().includes(this.search.toLowerCase()) || u.email?.toLowerCase().includes(this.search.toLowerCase());
            const matchRole = !this.roleFilter || u.role === this.roleFilter;
            const matchStatus = !this.statusFilter || u.status === this.statusFilter;
            return matchSearch && matchRole && matchStatus;
        });
    }

    getInitials(name: string): string {
        return name?.split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2) || '??';
    }

    confirm(type: 'ban' | 'unban' | 'delete', user: any): void { this.confirmAction = { type, user }; }

    execute(): void {
        if (!this.confirmAction) return;
        const { type, user } = this.confirmAction;
        this.confirmAction = null;

        if (type === 'ban') {
            this.adminApi.banUser(user.id).subscribe({
                next: () => { user.status = 'BANNED'; this.toast.success(`${user.name} banned`); },
                error: () => this.toast.error('Action failed')
            });
        } else if (type === 'unban') {
            this.adminApi.unbanUser(user.id).subscribe({
                next: () => { user.status = 'ACTIVE'; this.toast.success(`${user.name} unbanned`); },
                error: () => this.toast.error('Action failed')
            });
        } else {
            this.adminApi.deleteUser(user.id).subscribe({
                next: () => { this.users = this.users.filter(u => u.id !== user.id); this.toast.success('User deleted'); },
                error: () => this.toast.error('Action failed')
            });
        }
    }
}


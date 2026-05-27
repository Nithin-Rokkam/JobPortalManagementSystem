import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { BaseChartDirective } from 'ng2-charts';

import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { UserManagementComponent } from './components/user-management/user-management.component';
import { JobManagementComponent } from './components/job-management/job-management.component';
import { PlatformReportComponent } from './components/platform-report/platform-report.component';
import { AuditLogsComponent } from './components/audit-logs/audit-logs.component';
import { AdminProfileComponent } from './components/admin-profile/admin-profile.component';
import { BrowseJobsComponent } from '../seeker/components/browse-jobs/browse-jobs.component';
import { JobDetailComponent } from '../seeker/components/job-detail/job-detail.component';

const routes: Routes = [
    { path: 'dashboard', component: AdminDashboardComponent },
    { path: 'users', component: UserManagementComponent },
    { path: 'jobs', component: JobManagementComponent },
    { path: 'reports', component: PlatformReportComponent },
    { path: 'audit-logs', component: AuditLogsComponent },
    { path: 'profile', component: AdminProfileComponent },
    { path: 'browse', component: BrowseJobsComponent },
    { path: 'browse/:id', component: JobDetailComponent },
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];

@NgModule({
    declarations: [
        AdminDashboardComponent, UserManagementComponent, JobManagementComponent,
        PlatformReportComponent, AuditLogsComponent, AdminProfileComponent
    ],
    imports: [SharedModule, BaseChartDirective, RouterModule.forChild(routes)]
})
export class AdminModule { }

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';

import { SeekerDashboardComponent } from './components/seeker-dashboard/seeker-dashboard.component';
import { MyApplicationsComponent } from './components/my-applications/my-applications.component';
import { SeekerProfileComponent } from './components/seeker-profile/seeker-profile.component';
import { SavedJobsComponent } from './components/saved-jobs/saved-jobs.component';
import { BrowseJobsComponent } from './components/browse-jobs/browse-jobs.component';
import { JobDetailComponent } from './components/job-detail/job-detail.component';

const routes: Routes = [
    { path: 'dashboard', component: SeekerDashboardComponent },
    { path: 'browse', component: BrowseJobsComponent },
    { path: 'jobs/:id', component: JobDetailComponent },
    { path: 'applications', component: MyApplicationsComponent },
    { path: 'profile', component: SeekerProfileComponent },
    { path: 'saved-jobs', component: SavedJobsComponent },
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];

@NgModule({
    declarations: [
        SeekerDashboardComponent,
        MyApplicationsComponent, SeekerProfileComponent, SavedJobsComponent
    ],
    imports: [SharedModule, RouterModule.forChild(routes)]
})
export class SeekerModule { }

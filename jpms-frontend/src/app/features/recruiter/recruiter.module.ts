import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';

import { RecruiterDashboardComponent } from './components/recruiter-dashboard/recruiter-dashboard.component';
import { PostJobComponent } from './components/post-job/post-job.component';
import { MyJobsComponent } from './components/my-jobs/my-jobs.component';
import { EditJobComponent } from './components/edit-job/edit-job.component';
import { ViewApplicantsComponent } from './components/view-applicants/view-applicants.component';
import { RecruiterProfileComponent } from './components/recruiter-profile/recruiter-profile.component';
import { AllApplicantsComponent } from './components/all-applicants/all-applicants.component';
import { BrowseJobsComponent } from '../seeker/components/browse-jobs/browse-jobs.component';
import { JobDetailComponent } from '../seeker/components/job-detail/job-detail.component';

const routes: Routes = [
    { path: 'dashboard', component: RecruiterDashboardComponent },
    { path: 'post-job', component: PostJobComponent },
    { path: 'my-jobs', component: MyJobsComponent },
    { path: 'all-applicants', component: AllApplicantsComponent },
    { path: 'edit-job/:id', component: EditJobComponent },
    { path: 'jobs/:jobId/applicants', component: ViewApplicantsComponent },
    { path: 'browse', component: BrowseJobsComponent },
    { path: 'jobs/:id', component: JobDetailComponent },
    { path: 'profile', component: RecruiterProfileComponent },
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];

@NgModule({
    declarations: [
        RecruiterDashboardComponent, PostJobComponent, MyJobsComponent,
        EditJobComponent, ViewApplicantsComponent, RecruiterProfileComponent,
        AllApplicantsComponent
    ],
    imports: [SharedModule, RouterModule.forChild(routes)]
})
export class RecruiterModule { }

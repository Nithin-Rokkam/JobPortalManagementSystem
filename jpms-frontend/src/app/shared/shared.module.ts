import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { BadgeComponent } from './components/badge/badge.component';
import { LoaderComponent } from './components/loader/loader.component';
import { StatCardComponent } from './components/stat-card/stat-card.component';
import { JobCardComponent } from './components/job-card/job-card.component';
import { ConfirmModalComponent } from './components/confirm-modal/confirm-modal.component';
import { EmptyStateComponent } from './components/empty-state/empty-state.component';
import { PageHeaderComponent } from './components/page-header/page-header.component';
import { BrowseJobsComponent } from '../features/seeker/components/browse-jobs/browse-jobs.component';
import { JobDetailComponent } from '../features/seeker/components/job-detail/job-detail.component';

import { CapitalizePipe } from './pipes/capitalize.pipe';
import { TimeAgoPipe } from './pipes/time-ago.pipe';
import { SalaryRangePipe } from './pipes/salary-range.pipe';
import { RoleHideDirective } from './directives/role-hide.directive';

const COMPONENTS = [
    BadgeComponent, LoaderComponent, StatCardComponent,
    JobCardComponent, ConfirmModalComponent, EmptyStateComponent, PageHeaderComponent,
    BrowseJobsComponent, JobDetailComponent
];
const PIPES = [CapitalizePipe, TimeAgoPipe, SalaryRangePipe];
const DIRECTIVES = [RoleHideDirective];

@NgModule({
    declarations: [...COMPONENTS, ...PIPES, ...DIRECTIVES],
    imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule],
    exports: [
        CommonModule, RouterModule, FormsModule, ReactiveFormsModule,
        ...COMPONENTS, ...PIPES, ...DIRECTIVES
    ]
})
export class SharedModule { }

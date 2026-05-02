import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { JobsApiService } from '../../services/jobs-api.service';
import { SavedJobsService } from '../../services/saved-jobs.service';
import { ToastService } from '../../../../core/services/toast.service';
import { Job } from '../../../../shared/models/job.model';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
    standalone: false,
    selector: 'app-saved-jobs',
    templateUrl: './saved-jobs.component.html',
    styleUrls: ['./saved-jobs.component.css']
})
export class SavedJobsComponent implements OnInit {
    jobs: Job[] = [];
    loading = true;

    constructor(
        private jobsApi: JobsApiService,
        private savedJobs: SavedJobsService,
        private toast: ToastService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.loadSavedJobs();
    }

    loadSavedJobs(): void {
        const ids = this.savedJobs.getSavedIds();

        if (ids.length === 0) {
            this.jobs = [];
            this.loading = false;
            this.cdr.detectChanges();
            return;
        }

        // Fetch all saved jobs in parallel
        const requests = ids.map(id =>
            this.jobsApi.getById(id).pipe(catchError(() => of(null)))
        );

        forkJoin(requests).subscribe({
            next: results => {
                this.jobs = results.filter((j): j is Job => j !== null);
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.loading = false;
                this.toast.error('Failed to load saved jobs');
                this.cdr.detectChanges();
            }
        });
    }

    unsave(job: Job, event: Event): void {
        event.stopPropagation();
        this.savedJobs.toggle(job.id);
        this.jobs = this.jobs.filter(j => j.id !== job.id);
        this.toast.info(`"${job.title}" removed from saved jobs`);
        this.cdr.detectChanges();
    }

    isSaved(jobId: number): boolean {
        return this.savedJobs.isSaved(jobId);
    }
}

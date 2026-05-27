import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { JobsApiService } from '../../services/jobs-api.service';
import { Job } from '../../../../shared/models/job.model';

@Component({
    standalone: false,
    selector: 'app-browse-jobs',
    templateUrl: './browse-jobs.component.html',
    styleUrls: ['./browse-jobs.component.css']
})
export class BrowseJobsComponent implements OnInit {
    jobs: Job[] = [];
    loading = true;
    totalElements = 0;
    totalPages = 0;
    currentPage = 0;
    pageSize = 10;

    filterForm: FormGroup;
    jobTypes = [
        { label: 'All', value: '' },
        { label: 'Full Time', value: 'FULL_TIME' },
        { label: 'Part Time', value: 'PART_TIME' },
        { label: 'Remote', value: 'REMOTE' },
        { label: 'Contract', value: 'CONTRACT' }
    ];
    experienceOptions = [
        { label: 'Any', value: '' },
        { label: '0-1 yr', value: 0 },
        { label: '1-3 yrs', value: 1 },
        { label: '3-5 yrs', value: 3 },
        { label: '5+ yrs', value: 5 }
    ];

    constructor(private fb: FormBuilder, private jobsApi: JobsApiService, private cdr: ChangeDetectorRef) {
        this.filterForm = this.fb.group({
            title: [''], location: [''], jobType: [''], experienceYears: ['']
        });
    }

    ngOnInit(): void {
        this.loadJobs(0);
        this.filterForm.valueChanges.pipe(debounceTime(350), distinctUntilChanged()).subscribe(() => this.loadJobs(0));
    }

    get hasFilters(): boolean {
        const v = this.filterForm.value;
        return !!(v.title || v.location || v.jobType || v.experienceYears !== '');
    }

    loadJobs(page: number): void {
        this.loading = true;
        this.currentPage = page;
        const { title, location, jobType, experienceYears } = this.filterForm.value;
        const obs = this.hasFilters
            ? this.jobsApi.search({ title, location, jobType, experienceYears: experienceYears !== '' ? experienceYears : undefined, page, size: this.pageSize })
            : this.jobsApi.getAll(page, this.pageSize);

        obs.subscribe({
            next: res => { this.jobs = res.content; this.totalElements = res.totalElements; this.totalPages = res.totalPages; this.loading = false; this.cdr.detectChanges(); },
            error: () => { this.loading = false; this.cdr.detectChanges(); }
        });
    }

    clearFilters(): void { this.filterForm.reset({ title: '', location: '', jobType: '', experienceYears: '' }); }

    get pages(): number[] { return Array.from({ length: this.totalPages }, (_, i) => i); }
}


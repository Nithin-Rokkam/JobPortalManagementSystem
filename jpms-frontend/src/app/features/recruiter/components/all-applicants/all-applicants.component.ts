import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { RecruiterJobsApiService } from '../../services/recruiter-jobs-api.service';
import { Job } from '../../../../shared/models/job.model';

@Component({
  standalone: false,
  selector: 'app-all-applicants',
  template: `
    <div class="all-applicants-container animate-fade-in-up">
      <header class="page-header">
        <h1 class="heading-celestial">Applicants by Job</h1>
        <p style="color: var(--text-secondary);">Select a job posting to view its applicants.</p>
      </header>

      <div class="jobs-grid" *ngIf="!loading">
        <div *ngFor="let job of jobs" class="celestial-card job-applicant-card" [routerLink]="['/recruiter/jobs', job.id, 'applicants']">
          <div class="card-body">
            <h3 class="job-title">{{ job.title }}</h3>
            <p class="job-meta">{{ job.location }} • {{ job.jobType.replace('_', ' ') }}</p>
            <div class="status-badge" [class]="job.status.toLowerCase()">{{ job.status }}</div>
          </div>
          <div class="card-footer">
            <span class="view-link">View Talent →</span>
          </div>
        </div>
      </div>

      <div *ngIf="loading" class="loading-state">
        <div class="celestial-card skeleton" *ngFor="let i of [1,2,3,4]" style="height: 150px;"></div>
      </div>

      <div *ngIf="!loading && !jobs.length" class="empty-state">
        <p>No job postings found. Post a job to start receiving applications!</p>
        <button class="btn-celestial" routerLink="/recruiter/post-job">Post New Job</button>
      </div>
    </div>
  `,
  styles: [`
    .all-applicants-container { max-width: 1200px; margin: 0 auto; padding: 2rem 1.5rem; }
    .page-header { margin-bottom: 3rem; }
    .jobs-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; }
    .job-applicant-card { cursor: pointer; transition: transform 0.2s; padding: 0; display: flex; flex-direction: column; }
    .job-applicant-card:hover { transform: translateY(-5px); }
    .card-body { padding: 1.5rem; flex: 1; }
    .job-title { margin: 0 0 0.5rem; font-size: 1.125rem; }
    .job-meta { font-size: 0.875rem; color: var(--text-muted); margin-bottom: 1rem; }
    .card-footer { padding: 1rem 1.5rem; border-top: 1px solid var(--glass-border); background: rgba(255,255,255,0.02); }
    .view-link { color: var(--royal-blue); font-weight: 700; font-size: 0.875rem; }
    .status-badge { display: inline-block; padding: 0.25rem 0.625rem; border-radius: 4px; font-size: 0.7rem; font-weight: 800; text-transform: uppercase; }
    .status-badge.active { background: rgba(16, 185, 129, 0.1); color: #10b981; }
    .status-badge.closed { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
  `]
})
export class AllApplicantsComponent implements OnInit {
  jobs: Job[] = [];
  loading = true;

  constructor(private recruiterJobsApi: RecruiterJobsApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.recruiterJobsApi.getMyJobs(0, 100).subscribe({
      next: res => { this.jobs = res.content; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }
}

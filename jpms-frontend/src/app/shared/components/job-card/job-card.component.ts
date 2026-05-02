import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Job } from '../../models/job.model';
import { SavedJobsService } from '../../../features/seeker/services/saved-jobs.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    standalone: false,
    selector: 'app-job-card',
    templateUrl: './job-card.component.html',
    styleUrls: ['./job-card.component.css']
})
export class JobCardComponent {
    @Input() job!: Job;
    @Input() showApplyButton = false;
    @Input() showActions = false;

    @Output() applyClick = new EventEmitter<number>();
    @Output() editClick = new EventEmitter<number>();
    @Output() deleteClick = new EventEmitter<number>();

    constructor(
        private savedJobs: SavedJobsService,
        private toast: ToastService,
        private auth: AuthService
    ) { }

    get isSeeker(): boolean {
        return this.auth.getRole() === 'JOB_SEEKER';
    }

    get isSaved(): boolean {
        return this.savedJobs.isSaved(this.job?.id);
    }

    getInitials(name: string): string {
        return name?.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2) || '??';
    }

    onApply(event: Event): void {
        event.stopPropagation();
        this.applyClick.emit(this.job.id);
    }

    onToggleSave(event: Event): void {
        event.stopPropagation();
        const saved = this.savedJobs.toggle(this.job.id);
        this.toast.success(saved ? 'Job saved!' : 'Job removed from saved');
    }
}


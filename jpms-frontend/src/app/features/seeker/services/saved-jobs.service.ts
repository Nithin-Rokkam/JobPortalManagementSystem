import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

const STORAGE_KEY = 'joblix_saved_jobs';

@Injectable({ providedIn: 'root' })
export class SavedJobsService {

    private savedIds: Set<number>;
    private savedIds$ = new BehaviorSubject<Set<number>>(new Set());

    constructor() {
        this.savedIds = this.load();
        this.savedIds$.next(this.savedIds);
    }

    private load(): Set<number> {
        try {
            const raw = localStorage.getItem(STORAGE_KEY);
            return raw ? new Set<number>(JSON.parse(raw)) : new Set<number>();
        } catch {
            return new Set<number>();
        }
    }

    private persist(): void {
        localStorage.setItem(STORAGE_KEY, JSON.stringify([...this.savedIds]));
        this.savedIds$.next(new Set(this.savedIds));
    }

    isSaved(jobId: number): boolean {
        return this.savedIds.has(jobId);
    }

    toggle(jobId: number): boolean {
        if (this.savedIds.has(jobId)) {
            this.savedIds.delete(jobId);
            this.persist();
            return false; // removed
        } else {
            this.savedIds.add(jobId);
            this.persist();
            return true; // saved
        }
    }

    getSavedIds(): number[] {
        return [...this.savedIds];
    }

    get count(): number {
        return this.savedIds.size;
    }

    /** Observable for reactive updates */
    get changes$() {
        return this.savedIds$.asObservable();
    }
}

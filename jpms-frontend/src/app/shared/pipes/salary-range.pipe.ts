import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'salaryRange',
    standalone: false
})
export class SalaryRangePipe implements PipeTransform {
    transform(job: { salaryMin?: number; salaryMax?: number }): string {
        if (!job) return 'Not disclosed';
        const fmt = (n: number) => {
            if (n >= 100000) return `₹${(n / 100000).toFixed(0)}L`;
            if (n >= 1000) return `₹${(n / 1000).toFixed(0)}K`;
            return `₹${n}`;
        };
        if (job.salaryMin && job.salaryMax) return `${fmt(job.salaryMin)} – ${fmt(job.salaryMax)}`;
        if (job.salaryMin) return `From ${fmt(job.salaryMin)}`;
        if (job.salaryMax) return `Up to ${fmt(job.salaryMax)}`;
        return 'Not disclosed';
    }
}

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'salaryRange',
    standalone: false
})
export class SalaryRangePipe implements PipeTransform {
    transform(job: { salary?: number }): string {
        if (!job || !job.salary) return 'Not disclosed';
        const n = job.salary;
        if (n >= 100000) return `₹${(n / 100000).toFixed(0)}L`;
        if (n >= 1000) return `₹${(n / 1000).toFixed(0)}K`;
        return `₹${n}`;
    }
}

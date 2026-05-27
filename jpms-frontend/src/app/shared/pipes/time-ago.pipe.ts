import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'timeAgo',
    standalone: false
})
export class TimeAgoPipe implements PipeTransform {
    transform(value: string | Date): string {
        if (!value) return '';
        const now = new Date();

        // Backend returns LocalDateTime without timezone (e.g. "2026-05-05T10:30:00").
        // Appending 'Z' tells the browser to treat it as UTC, which matches the
        // Docker container timezone (UTC), giving accurate relative times.
        let date: Date;
        if (typeof value === 'string') {
            const utcString = value.endsWith('Z') || value.includes('+') ? value : value + 'Z';
            date = new Date(utcString);
        } else {
            date = value;
        }

        const diff = Math.floor((now.getTime() - date.getTime()) / 1000);

        if (diff < 0) return 'just now';
        if (diff < 60) return 'just now';
        if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
        if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
        if (diff < 2592000) return `${Math.floor(diff / 86400)}d ago`;
        if (diff < 31536000) return `${Math.floor(diff / 2592000)}mo ago`;
        return `${Math.floor(diff / 31536000)}y ago`;
    }
}

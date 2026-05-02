import { Component, Input } from '@angular/core';

@Component({
    standalone: false,
    selector: 'app-stat-card',
    templateUrl: './stat-card.component.html',
    styleUrls: ['./stat-card.component.css']
})
export class StatCardComponent {
    @Input() title: string = '';
    @Input() value: number | string = 0;
    @Input() icon: string = '📊';
    @Input() color: 'primary' | 'success' | 'warning' | 'danger' = 'primary';
    @Input() trend?: string;
}


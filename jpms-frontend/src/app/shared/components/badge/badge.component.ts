import { Component, Input } from '@angular/core';

@Component({
    standalone: false,
    selector: 'app-badge',
    templateUrl: './badge.component.html',
    styleUrls: ['./badge.component.css']
})
export class BadgeComponent {
    @Input() status: string = '';
}


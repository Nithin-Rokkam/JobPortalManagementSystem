import { Component, Input } from '@angular/core';

@Component({
    standalone: false,
    selector: 'app-empty-state',
    templateUrl: './empty-state.component.html',
    styleUrls: ['./empty-state.component.css']
})
export class EmptyStateComponent {
    @Input() message = 'Nothing here yet';
    @Input() icon = '📭';
    @Input() actionLabel?: string;
    @Input() actionLink?: string;
}


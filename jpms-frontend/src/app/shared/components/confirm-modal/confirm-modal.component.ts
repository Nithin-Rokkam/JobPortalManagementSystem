import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
    standalone: false,
    selector: 'app-confirm-modal',
    templateUrl: './confirm-modal.component.html',
    styleUrls: ['./confirm-modal.component.css']
})
export class ConfirmModalComponent {
    @Input() title = 'Confirm Action';
    @Input() message = 'Are you sure?';
    @Input() confirmLabel = 'Confirm';
    @Input() isDanger = false;

    @Output() confirmed = new EventEmitter<void>();
    @Output() cancelled = new EventEmitter<void>();
}


import { Component, Input } from '@angular/core';

@Component({
    standalone: false,
    selector: 'app-page-header',
    templateUrl: './page-header.component.html',
    styleUrls: ['./page-header.component.css']
})
export class PageHeaderComponent {
    @Input() title = '';
    @Input() subtitle?: string;
    @Input() breadcrumbs: { label: string; link?: string }[] = [];
}


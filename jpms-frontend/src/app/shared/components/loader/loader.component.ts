import { Component } from '@angular/core';
import { LoaderService } from '../../../core/services/loader.service';

@Component({
    standalone: false,
    selector: 'app-loader',
    templateUrl: './loader.component.html',
    styleUrls: ['./loader.component.css']
})
export class LoaderComponent {
    loading$;
    constructor(private loaderService: LoaderService) {
        this.loading$ = this.loaderService.loading$;
    }
}


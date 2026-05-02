import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

@Injectable({ providedIn: 'root' })
export class ToastService {
    constructor(private toastr: ToastrService) { }

    success(msg: string, title = 'Success'): void {
        this.toastr.success(msg, title, { timeOut: 3000, progressBar: true });
    }

    error(msg: string, title = 'Error'): void {
        this.toastr.error(msg, title, { timeOut: 4000, progressBar: true });
    }

    info(msg: string, title = 'Info'): void {
        this.toastr.info(msg, title, { timeOut: 3000 });
    }

    warning(msg: string, title = 'Warning'): void {
        this.toastr.warning(msg, title, { timeOut: 3500 });
    }
}

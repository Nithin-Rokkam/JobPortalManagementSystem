import { Directive, Input, TemplateRef, ViewContainerRef, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Directive({
    selector: '[appRoleHide]',
    standalone: false
})
export class RoleHideDirective implements OnInit {
    @Input('appRoleHide') roles: string[] = [];

    constructor(
        private tpl: TemplateRef<any>,
        private vcr: ViewContainerRef,
        private auth: AuthService
    ) { }

    ngOnInit(): void {
        const role = this.auth.getRole();
        if (!role || !this.roles.includes(role)) {
            this.vcr.createEmbeddedView(this.tpl);
        }
    }
}

import { Component } from '@angular/core';

@Component({
    standalone: false,
    selector: 'app-footer',
    template: `<footer class="footer"><span>© 2026 Joblix — Your Career Partner</span></footer>`,
    styles: [`.footer { padding: 16px 40px; border-top: 1px solid var(--border-subtle); font-size: 0.78rem; color: var(--text-muted); text-align: center; }`]
})
export class FooterComponent { }


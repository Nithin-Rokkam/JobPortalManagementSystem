import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { SharedModule } from '../shared/shared.module';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';
import { SidebarComponent } from './sidebar/sidebar.component';
import { ShellComponent } from './shell/shell.component';

@NgModule({
    declarations: [HeaderComponent, FooterComponent, SidebarComponent, ShellComponent],
    imports: [CommonModule, RouterModule, SharedModule],
    exports: [HeaderComponent, FooterComponent, SidebarComponent, ShellComponent]
})
export class LayoutModule { }

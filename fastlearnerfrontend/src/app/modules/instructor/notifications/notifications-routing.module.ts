import { NgModule, AfterViewInit } from '@angular/core';
import { NavigationEnd, Router, RouterModule, Routes } from '@angular/router';
import { NotificationsComponent } from './notifications.component';
import { AuthGuard } from 'src/app/core/guards/auth.guard';
import { Title } from '@angular/platform-browser';

const routes: Routes = [{ path: '', component: NotificationsComponent, data: { title: 'Notifications' }, canActivate: [AuthGuard], }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NotificationsRoutingModule implements AfterViewInit {
  constructor(private titleService: Title, private router: Router) {}

   // Implement AfterViewInit to ensure the view is initialized before subscribing to router events
   ngAfterViewInit() {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        const title = this.getTitle(this.router.routerState, this.router.routerState.root).join('-');
        this.titleService.setTitle(title);
      }
    });
  }

  // Recursively find the first child with a title property
  getTitle(state, parent) {
    const data = [];
    if (parent && parent.snapshot.data && parent.snapshot.data.title) {
      data.push(parent.snapshot.data.title);
    }

    if (state && parent) {
      const children = parent.children;
      if (children && children.length > 0) {
        return this.getTitle(state, children[0]);
      }
    }

    return data;
  }
 }

import { Component, HostListener } from '@angular/core';

@Component({
  selector: 'app-root',
  template: '<router-outlet></router-outlet>'
})
export class AppComponent {

  /** Drive the CSS scroll-progress bar via a custom property */
  @HostListener('window:scroll', [])
  onScroll(): void {
    const scrollTop = window.scrollY || document.documentElement.scrollTop;
    const docHeight = document.documentElement.scrollHeight - window.innerHeight;
    const progress  = docHeight > 0 ? scrollTop / docHeight : 0;
    document.body.style.setProperty('--scroll-progress', String(progress));
  }
}

import { Component, ViewContainerRef } from '@angular/core';
import { Router } from '@angular/router';
import { PreviewVideoModalComponent } from '../../dynamic-modals/preview-video-modal/preview-video-modal.component';
import { NzModalService } from 'ng-zorro-antd/modal';
import { Meta, Title } from '@angular/platform-browser';

@Component({
  selector: 'app-become-instructor',
  templateUrl: './become-instructor.component.html',
  styleUrls: ['./become-instructor.component.scss'],
})
export class BecomeInstructorComponent {
  texts = ['an Instructor', 'a Collaborator'];
  currentText = this.texts[0];
  private index = 0;

  constructor(
    private _router: Router,
    private _modal: NzModalService,
    private _viewContainerRef: ViewContainerRef,
    private metaService: Meta,
    private titleService: Title
  ) {}

  ngOnInit() {
    this.titleService.setTitle(
      'Become an Instructor at Fast Learner – Share Your Knowledge'
    );
    this.metaService.updateTag({
      name: 'description',
      content: `Become an instructor at Fast Learner. Share your expertise, design your courses, earn, and empower learners worldwide with AI-powered teaching tools.`,
    });
    this.changeText();
  }

  changeText() {
    setInterval(() => {
      this.index = (this.index + 1) % this.texts.length;
      this.fadeOutText();
    }, 3000); // Change text every 3 seconds
  }

  fadeOutText() {
    const transitionTextElement = document.getElementById('transitionText');
    if (transitionTextElement) {
      transitionTextElement.style.opacity = '0';

      setTimeout(() => {
        this.currentText = this.texts[this.index];
        this.fadeInText();
      }, 500); // Match this delay with the CSS transition duration
    }
  }

  fadeInText() {
    const transitionTextElement = document.getElementById('transitionText');
    if (transitionTextElement) {
      transitionTextElement.style.opacity = '1';
    }
  }

  routeToInstructorWelcomePage() {
    this._router.navigate(['welcome-instructor']);
  }
}

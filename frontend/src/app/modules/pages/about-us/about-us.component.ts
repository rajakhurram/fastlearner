import { Component, OnInit } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';

@Component({
  selector: 'app-about-us',
  templateUrl: './about-us.component.html',
  styleUrls: ['./about-us.component.scss'],
})
export class AboutUsComponent implements OnInit {
  constructor(private metaService: Meta, private titleService: Title) {}
  ngOnInit(): void {
    this.titleService.setTitle('About Us | Transforming Learning at Fast Learner'); // Set the title of the page
    this.metaService.updateTag({
      name: 'description',
      content: 'Discover Fast Learner’s mission, vision, and journey in revolutionizing learning. Learn how we empower learners worldwide to grow smarter and faster.',
    });
  }
}

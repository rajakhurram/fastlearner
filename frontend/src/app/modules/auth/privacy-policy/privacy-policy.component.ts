import { Component, OnInit } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';

@Component({
  selector: 'app-privacy-policy',
  templateUrl: './privacy-policy.component.html',
  styleUrls: ['./privacy-policy.component.scss'],
})
export class PrivacyPolicyComponent implements OnInit {
  constructor(private metaService: Meta, private titleService: Title) {}
  ngOnInit(): void {
    this.titleService.setTitle('Privacy Policy | Data Protection at Fast Learner'); // Set the title of the page
    this.metaService.updateTag({
      name: 'description',
      content: 'Learn how Fast Learner protects your privacy. Explore our commitment to data security, transparency, and user trust in our Privacy Policy.',
    });
  }
}

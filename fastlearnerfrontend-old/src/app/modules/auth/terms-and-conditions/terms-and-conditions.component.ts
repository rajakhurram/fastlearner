import { Component, OnInit } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { AccordionItems } from 'src/app/core/interfaces/accordian.interafce';

@Component({
  selector: 'app-terms-and-conditions',
  templateUrl: './terms-and-conditions.component.html',
  styleUrls: ['./terms-and-conditions.component.scss'],
})
export class TermsAndConditionsComponent implements OnInit {
  constructor(private metaService: Meta, private titleService: Title) {}
  ngOnInit(): void {
    this.titleService.setTitle('Terms and Conditions | Fast Learner Usage Guidelines'); // Set the title of the page
    this.metaService.updateTag({
      name: 'description',
      content: 'Read Fast Learner’s Terms and Conditions for platform use. Understand your rights, obligations, and guidelines for a seamless learning experience.',
    });
  }
}

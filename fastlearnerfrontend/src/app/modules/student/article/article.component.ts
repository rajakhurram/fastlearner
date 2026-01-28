import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';

@Component({
  selector: 'app-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.scss'],
})
export class ArticleComponent implements OnInit, OnChanges {
  @Input() currentSelectedTopic: any;
  article: any;

  constructor() {}

  ngOnInit(): void {
    this.article = this.currentSelectedTopic?.article;
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.article = this.currentSelectedTopic?.article;
  }
}

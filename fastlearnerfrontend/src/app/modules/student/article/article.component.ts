import {
  Component,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  EventEmitter,
} from '@angular/core';

@Component({
  selector: 'app-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.scss'],
})
export class ArticleComponent implements OnInit, OnChanges {
  @Input() currentSelectedTopic: any;
  @Output() continueArticleEmitter = new EventEmitter<void>();
  article: any;

  constructor() {}

  ngOnInit(): void {
    this.article = this.currentSelectedTopic?.article;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentSelectedTopic']) {
      this.article = this.currentSelectedTopic?.article;
    }
  }

  continue(): void {
    this.continueArticleEmitter.emit();
  }
}

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ArticleComponent } from './article.component';
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from '@angular/core';

describe('ArticleComponent', () => {
  let component: ArticleComponent;
  let fixture: ComponentFixture<ArticleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ArticleComponent],
      schemas :[CUSTOM_ELEMENTS_SCHEMA , NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArticleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should initialize with article from currentSelectedTopic', () => {
    const mockTopic = { article: 'Mock Article Content' };
    component.currentSelectedTopic = mockTopic;
    component.ngOnInit();

    expect(component.article).toBe('Mock Article Content');
  });

  it('should update article when currentSelectedTopic changes', () => {
    const mockTopic = { article: 'New Article Content' };
    component.currentSelectedTopic = mockTopic;

    component.ngOnChanges({
      currentSelectedTopic: {
        currentValue: mockTopic,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true,
      },
    });

    expect(component.article).toBe('New Article Content');
  });

  it('should handle undefined article gracefully', () => {
    const mockTopic = { article: undefined };
    component.currentSelectedTopic = mockTopic;

    component.ngOnInit();

    expect(component.article).toBeUndefined();
  });

  it('should handle null currentSelectedTopic gracefully', () => {
    component.currentSelectedTopic = null;

    component.ngOnInit();

    expect(component.article).toBeUndefined();
  });
});

import {
  emptyManualQuestionForm,
  ManualQuestionForm,
} from './manual-question-form.model';

describe('ManualQuestionForm', () => {
  it('emptyManualQuestionForm returns defaults', () => {
    const form: ManualQuestionForm = emptyManualQuestionForm();
    expect(form.obtainedMarks).toBeNull();
    expect(form.outOfMarks).toBeNull();
  });
});

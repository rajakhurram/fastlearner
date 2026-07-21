/** UI-only helpers for bulk quiz import performance (data shape unchanged for API). */

export const BULK_QUIZ_IMPORT_CHUNK_SIZE = 25;
export const QUIZ_INITIAL_RENDER_COUNT = 5;
export const QUIZ_LOAD_MORE_BATCH = 5;

let clientKeySequence = 0;

export function nextQuizUiClientKey(prefix: string): string {
  clientKeySequence += 1;
  return `${prefix}-${Date.now()}-${clientKeySequence}`;
}

export function assignQuizQuestionClientKeys(question: any): void {
  if (!question) {
    return;
  }
  if (!question._clientKey) {
    question._clientKey = nextQuizUiClientKey('qq');
  }
  (question.answers ?? []).forEach((answer: any, index: number) => {
    if (answer && !answer._clientKey) {
      answer._clientKey = nextQuizUiClientKey(`qa-${index}`);
    }
  });
}

export function countActiveQuizQuestions(topic: any): number {
  return (topic?.quiz?.questions ?? []).filter((q: any) => !q?.delete).length;
}

export function getQuizRenderLimit(topic: any): number {
  return topic?._quizRenderLimit ?? QUIZ_INITIAL_RENDER_COUNT;
}

export function resetQuizRenderLimit(topic: any): void {
  if (topic) {
    topic._quizRenderLimit = QUIZ_INITIAL_RENDER_COUNT;
  }
}

export function shouldRenderQuizQuestion(topic: any, index: number): boolean {
  const questions = topic?.quiz?.questions ?? [];
  const question = questions[index];
  if (!question || question.delete) {
    return false;
  }

  const limit = getQuizRenderLimit(topic);
  let activeIndex = 0;
  for (let i = 0; i < index; i++) {
    if (!questions[i]?.delete) {
      activeIndex++;
    }
  }
  return activeIndex < limit;
}

export function hasMoreQuizQuestionsToLoad(topic: any): boolean {
  return countActiveQuizQuestions(topic) > getQuizRenderLimit(topic);
}

export function getRemainingQuizQuestionsCount(topic: any): number {
  return Math.max(
    0,
    countActiveQuizQuestions(topic) - getQuizRenderLimit(topic),
  );
}

export function loadMoreQuizQuestions(topic: any): void {
  if (!topic) {
    return;
  }
  const total = countActiveQuizQuestions(topic);
  const current = getQuizRenderLimit(topic);
  topic._quizRenderLimit = Math.min(
    current + QUIZ_LOAD_MORE_BATCH,
    total,
  );
}

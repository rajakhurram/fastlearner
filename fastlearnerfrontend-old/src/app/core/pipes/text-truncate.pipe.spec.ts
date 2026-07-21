import { TextTruncate } from './text-truncate.pipe'; // Adjust the path as needed

describe('TextTruncate Pipe', () => {
  let pipe: TextTruncate;

  beforeEach(() => {
    pipe = new TextTruncate();
  });

  it('should truncate text longer than the max length and add ellipsis', () => {
    const result = pipe.transform('This is a long text that needs to be truncated', 10);
    expect(result).toBe('This is a ...');
  });

  it('should not truncate text shorter than the max length', () => {
    const result = pipe.transform('Short text', 20);
    expect(result).toBe('Short text');
  });

  it('should handle empty strings', () => {
    const result = pipe.transform('', 10);
    expect(result).toBe('');
  });

  it('should handle maxLength less than or equal to 0', () => {
    const result = pipe.transform('Text', 0);
    expect(result).toBe('...');
    
    const resultNegative = pipe.transform('Text', -5);
    expect(resultNegative).toBe('...');
  });

  it('should handle maxLength equal to text length', () => {
    const result = pipe.transform('Exact length', 12);
    expect(result).toBe('Exact length');
  });
});

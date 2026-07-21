import { InitialCharactorPipe } from './initial-charactor.pipe'; // Adjust the path as needed

describe('InitialCharactorPipe', () => {
  let pipe: InitialCharactorPipe;

  beforeEach(() => {
    pipe = new InitialCharactorPipe();
  });

  it('should return initials from full name', () => {
    expect(pipe.transform('John Doe')).toBe('JD');
    expect(pipe.transform('Jane Smith')).toBeDefined();
    expect(pipe.transform('Alice')).toBe('A');
    expect(pipe.transform('')).toBe('');
    expect(pipe.transform(null)).toBe('');
  });

  it('should handle names with more than two parts', () => {
    expect(pipe.transform('John Michael Doe')).toBe('JM');
    expect(pipe.transform('Jane Ann Smith')).toBe('JA');
  });
});

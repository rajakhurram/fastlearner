import { SearchPipe } from './search.pipe'; // Adjust the path as needed

describe('SearchPipe', () => {
  let pipe: SearchPipe;

  beforeEach(() => {
    pipe = new SearchPipe();
  });

  it('should filter items based on the search value', () => {
    const items = [
      { fullName: 'John Doe', email: 'john@example.com' },
      { fullName: 'Jane Smith', email: 'jane@example.com' },
      { fullName: 'Alice Johnson', email: 'alice@example.com' },
    ];

    expect(pipe.transform(items, 'John')).toBeDefined()

    expect(pipe.transform(items, 'Jane')).toEqual([
      { fullName: 'Jane Smith', email: 'jane@example.com' },
    ]);

    expect(pipe.transform(items, 'example.com')).toEqual([
      { fullName: 'John Doe', email: 'john@example.com' },
      { fullName: 'Jane Smith', email: 'jane@example.com' },
      { fullName: 'Alice Johnson', email: 'alice@example.com' },
    ]);

    expect(pipe.transform(items, '')).toEqual(items);
    expect(pipe.transform(items, 'Not Found')).toEqual([]);
  });

  it('should return the original array when value is undefined, null, or empty string', () => {
    const items = [
      { fullName: 'John Doe', email: 'john@example.com' },
      { fullName: 'Jane Smith', email: 'jane@example.com' },
    ];

    expect(pipe.transform(items, undefined)).toEqual(items);
    expect(pipe.transform(items, null)).toEqual(items);
    expect(pipe.transform(items, '')).toEqual(items);
  });
});

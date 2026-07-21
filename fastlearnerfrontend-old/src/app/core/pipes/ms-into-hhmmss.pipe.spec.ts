import { MsIntoHHMMSSPipe } from './ms-into-hhmmss.pipe'; // Adjust the path as needed

describe('MsIntoHHMMSSPipe', () => {
  let pipe: MsIntoHHMMSSPipe;

  beforeEach(() => {
    pipe = new MsIntoHHMMSSPipe();
  });

  it('should correctly convert milliseconds to HH:MM:SS format', () => {
    expect(pipe.transform(3600000)).toBe('1h : 0m : 0s'); // 1 hour
    expect(pipe.transform(60000)).toBe('0h : 1m : 0s');   // 1 minute
    expect(pipe.transform(1000)).toBe('0h : 0m : 1s');     // 1 second
    expect(pipe.transform(3661000)).toBe('1h : 1m : 1s');   // 1 hour, 1 minute, 1 second
    expect(pipe.transform(36610000)).toBe('10h : 10m : 10s'); // 10 hours, 10 minutes
  });

  it('should handle edge cases', () => {
    expect(pipe.transform(0)).toBe('0h : 0m : 0s');           // 0 milliseconds
    expect(pipe.transform(-5000)).toBe('-1h : -1m : -5s');     // Negative milliseconds
  });
});

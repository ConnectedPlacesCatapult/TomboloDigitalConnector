import {Dataset} from '../../src/db/models/Dataset';

describe('Dataset', () => {

  describe('adjustDuplicateQuantiles', () => {
    it('do nothing to a set of quantiles with no duplicates', () => {
      expect(Dataset.adjustDuplicateQuantiles([0, 1, 2, 3, 4])).toEqual([0, 1, 2, 3, 4]);
    });

    it('should interpolate duplicates at the start', () => {
      const actual = Dataset.adjustDuplicateQuantiles([0, 0, 0, 0, 1, 2]);
      const expected = [0, 0.25, 0.5, 0.75, 1, 2];
      actual.forEach((x, i) => expect(x).toBeCloseTo(expected[i], 0.01));
    });

    it('should interpolate duplicates at the end', () => {
      const actual = Dataset.adjustDuplicateQuantiles([0, 1, 2, 4, 4, 4]);
      const expected = [0, 1, 2, 2.666, 3.333, 4];
      actual.forEach((x, i) => expect(x).toBeCloseTo(expected[i], 0.01));
    });

    it('should interpolate duplicates at the end', () => {
      const actual = Dataset.adjustDuplicateQuantiles([20, 50, 100, 120, 120]);
      const expected = [20, 50, 100, 110, 120];
      actual.forEach((x, i) => expect(x).toBeCloseTo(expected[i], 0.01));
    });

    it('should interpolate duplicates in the middle', () => {
      const actual = Dataset.adjustDuplicateQuantiles([0, 1, 2, 2, 2, 3, 4]);
      const expected = [0, 1, 2, 2.333, 2.666, 3, 4];
      actual.forEach((x, i) => expect(x).toBeCloseTo(expected[i], 0.01));
    });

    it('should interpolate 2 adjacent duplicate sets', () => {
      const actual = Dataset.adjustDuplicateQuantiles([0, 0, 1, 1, 3]);
      const expected = [0, 0.5, 1, 2, 3];
      actual.forEach((x, i) => expect(x).toBeCloseTo(expected[i], 0.01));
    });

    it('should interpolate a complete set of duplicates', () => {
      expect(Dataset.adjustDuplicateQuantiles([4, 4, 4, 4, 4])).toEqual([4, 5, 6, 7, 8]);
    });

    it('should interpolate 2 runs of duplicates', () => {
      const actual = Dataset.adjustDuplicateQuantiles([0, 1, 1, 1, 2, 3, 4, 4, 4, 5]);
      const expected = [0, 1, 1.333, 1.666, 2, 3, 4, 4.333, 4.666, 5];
      actual.forEach((x, i) => expect(x).toBeCloseTo(expected[i], 0.01));
    });
  });
});

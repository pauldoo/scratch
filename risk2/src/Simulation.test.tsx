import {sortDescending, rollOneDice} from './Simulation';

test('sortDescending', () => {
    expect(sortDescending([2, 3, 1])).toStrictEqual([3, 2, 1]);
    expect(sortDescending([3, 1, 2])).toStrictEqual([3, 2, 1]);
});

test('rollOneDice', () => {
    for (let i = 0; i < 1000; i++) {
        const value = rollOneDice();
        expect(value).toBeGreaterThanOrEqual(1);
        expect(value).toBeLessThanOrEqual(6);
    }
});

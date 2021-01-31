import { MersenneTwister19937, integer } from 'random-js';
import { strict as assert } from 'assert';
import { Stopwatch } from "ts-stopwatch";

export type State = {
    attackerCount: number,
    defenderCount: number
}

export type OutcomeDistribution = {
    outcomes: {state: State, count: number}[],
    totalCount: number,
    simulationTimeMs: number,
    p25: State,
    p50: State,
    p75: State,
    attackerWinCount: number
    defenderWinCount: number
}

export const rollOneDice: () => number = (() => {
    const rng = MersenneTwister19937.autoSeed();
    const distribution = integer(1, 6);

    return () => distribution(rng)
})();

function rollMultipleDice(count: number): number[] {
    assert(Number.isInteger(count) && count >= 1 && count <= 3);

    let result:number[] = [];
    for (let i = 0; i < count; i++) {
        result.push(rollOneDice());
    }

    assert(result.length === count);

    return result;
}

export function sortDescending(nums: number[]): number[] {
    nums.sort((a, b) => (b-a));
    return nums;
}

function singleTurnLosses(attackerDiceCount: number, defenderDiceCount: number) : State {
    assert(attackerDiceCount >= 1 && attackerDiceCount <= 3);
    assert(defenderDiceCount >= 1 && defenderDiceCount <= 2);

    const attackerDice = sortDescending(rollMultipleDice(attackerDiceCount));
    const defenderDice = sortDescending(rollMultipleDice(defenderDiceCount));

    let result :State = {
        attackerCount: 0,
        defenderCount: 0
    };

    for (let i = 0; i < attackerDice.length && i < defenderDice.length; i++) {
        if (attackerDice[i] > defenderDice[i]) {
            // defender loss
            result.defenderCount += 1;
        } else {
            // attacker loss
            result.attackerCount += 1;
        }
    }

    return result;
}

function finalOutcome(state: State): State {
    if (state.attackerCount >= 2 && state.defenderCount >= 1) {
        const losses: State = singleTurnLosses(
            Math.min(3, state.attackerCount - 1),
            Math.min(2, state.defenderCount));
        
        return finalOutcome({
            attackerCount: state.attackerCount - losses.attackerCount,
            defenderCount: state.defenderCount - losses.defenderCount
        });
    } else {
        assert(state.attackerCount >= 1);
        assert(state.defenderCount >= 0);
        return state;
    }
}


export function simulate(initialState: State) : OutcomeDistribution {
    const totalTrials :number = 10000;
    const outcomeCounts : { [s: number]: number } = {};
    const sw = new Stopwatch();

    function initialise() : void {
        for (let i = -initialState.defenderCount; i <= initialState.attackerCount; i++) {
            if (i === 0) {
                continue;
            }
            outcomeCounts[i] = 0;
        }
    }

    function runSimulations(): void {
        for (let i = 0; i < totalTrials; i++) {
            const endState = finalOutcome(initialState);
            const index = (endState.defenderCount > 0) ?
                (-endState.defenderCount) :
                (endState.attackerCount);
            assert(!(index === 0 || index === 1));
            outcomeCounts[index] += 1;
        }
    }

    function buildOutput(): OutcomeDistribution {
        const outcomes: {state: State, count: number}[] = [];

        let p25: State | undefined = undefined;
        let p50: State | undefined = undefined;
        let p75: State | undefined = undefined;
        let attackerWinCount: number = 0;
        let defenderWinCount: number = 0;
    

        let cumulative = 0;
        for (let i = -initialState.defenderCount; i <= initialState.attackerCount; i++) {
            if (i === 0 || i === 1) {
                continue;
            }
            const s: State = {
                attackerCount: (i > 0) ? i : 1,
                defenderCount: (i < 0) ? (-i) : 0
            };

            if (s.defenderCount > 0) {
                defenderWinCount += outcomeCounts[i];
            } else {
                attackerWinCount += outcomeCounts[i];
            }
            
            cumulative += outcomeCounts[i];
            if (!p25 && cumulative >= (totalTrials * 0.25)) {
                p25 = s;
            }
            if (!p50 && cumulative >= (totalTrials * 0.5)) {
                p50 = s;
            }
            if (!p75 && cumulative >= (totalTrials * 0.75)) {
                p75 = s;
            }

            outcomes.push({
                state: s,
                count: outcomeCounts[i]
            });

        }

        return {
            outcomes,
            totalCount: totalTrials,
            simulationTimeMs: sw.getTime(),
            p25: p25!,
            p50: p50!,
            p75: p75!,
            attackerWinCount,
            defenderWinCount
        };
    }

    initialise();
    sw.start();
    runSimulations();
    sw.stop();
    return buildOutput();
}


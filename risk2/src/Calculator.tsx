import { useState } from 'react';
import { Bar } from 'react-chartjs-2';
import { MersenneTwister19937, Engine } from 'random-js';
import { Simulate } from 'react-dom/test-utils';
import { assert } from 'console';

export interface Props {

}

type State = {
    attackerCount: number,
    defenderCount: number
}

type OutcomeDistribution = {
    likelihoods: Map<number, number>
}

class Simulation {
    rng = MersenneTwister19937.autoSeed();

    private simulateLosses(attackerDiceCount: number, defenderDiceCount: number) {
        assert(attackerDiceCount >= 1 && attackerDiceCount <= 3);
        assert(defenderDiceCount >= 1 && defenderDiceCount <= 2);
    
        const attacker:number[] = [];
        const defender:number[] = [];
    
        for (let i = 0; i < attackerDiceCount; i++) {
            attacker.push()
        }
    
    }

    private simulateOne(s: State): State {
        if (s.attackerCount >= 2) {
            simulateLosses(
                min(3, s.attackerCount - 1),
                min(2, s.defenderCount)
            );


        } else {
            return s;
        }
    }

   
    calculate(initialState: State, rng: Engine) : OutcomeDistribution {

        const trialCount = 1000;
    
        
    
        for (let i = 0; i < trialCount; i++) {
            const endState = simulateOne(initialState, rng);
        }
    
    }    
}





const Outcomes: React.FC<State> = (props) => {
    


    const data = {
        labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'],
        datasets: [
          {
            label: 'My First dataset',
            backgroundColor: 'rgba(255,99,132,0.2)',
            borderColor: 'rgba(255,99,132,1)',
            borderWidth: 1,
            hoverBackgroundColor: 'rgba(255,99,132,0.4)',
            hoverBorderColor: 'rgba(255,99,132,1)',
            data: [65, 59, 80, 81, 56, 55, 40]
          }
        ]
      };

    return (
        <div>
            <p>{props.attackerCount} attacking {props.defenderCount}</p>
            <Bar data={data} />
        </div>
    )
}

export const Calculator: React.FC<Props> = (props) => {

    const [attackerCount, setAttackerCount] = useState(5);
    const [defenderCount, setDefenderCount] = useState(5);

    return (
        <div>
            <div>
                Attackers:
                <input
                    type="number"
                    min="1"
                    max="100"
                    value={attackerCount}
                    onChange={(e) => setAttackerCount(Number(e.target.value))}
                />

                Defenders:
                <input
                    type="number"
                    min="1"
                    max="100"
                    value={defenderCount}
                    onChange={(e) => setDefenderCount(Number(e.target.value))}
                />
            
            </div>
            <Outcomes attackerCount={attackerCount} defenderCount={defenderCount}/>
        </div>
    )
}

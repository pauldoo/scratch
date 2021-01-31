import { useMemo, useState } from 'react';
import { Bar } from 'react-chartjs-2';
import { OutcomeDistribution, simulate, State } from './Simulation';

export interface Props {

}

const Outcomes: React.FC<State> = (props) => {
    
    const simData: OutcomeDistribution = useMemo( () => {
        return simulate(props); 
    }, [props.attackerCount, props.defenderCount]);

    const labels :string[] = simData.outcomes.map( (e) => {
        return e.state.attackerCount + " - " + e.state.defenderCount;
    });

    const values : number[] = simData.outcomes.map( (e) => {
        return (100 * e.count) / simData.totalCount;
    });

    const data = {
        labels: labels,
        datasets: [
          {
            label: 'Liklihood game ends with <attacker> vs <defender>',
            backgroundColor: 'rgba(255,99,132,0.2)',
            borderColor: 'rgba(255,99,132,1)',
            borderWidth: 1,
            hoverBackgroundColor: 'rgba(255,99,132,0.4)',
            hoverBorderColor: 'rgba(255,99,132,1)',
            data: values
          }
        ]
      };

    return (
        <div>
            <h2></h2>
            <p>All out attacks with {props.attackerCount} against {props.defenderCount}</p>
            <Bar data={data} />
            <p>Liklihood attacker wins: {(100 * simData.attackerWinCount) / simData.totalCount}%</p>
            <p>Liklihood defender wins: {(100 * simData.defenderWinCount) / simData.totalCount}%</p>
            <p>p25: {simData.p25.attackerCount} - {simData.p25.defenderCount}</p>
            <p>p50: {simData.p50.attackerCount} - {simData.p50.defenderCount}</p>
            <p>p75: {simData.p75.attackerCount} - {simData.p75.defenderCount}</p>
            <p>(Simulated {simData.totalCount} tials in {simData.simulationTimeMs}ms.)</p>
        </div>
    )
}

export const Calculator: React.FC<Props> = (props) => {

    const [attackerCount, setAttackerCount] = useState(5);
    const [defenderCount, setDefenderCount] = useState(5);

    let calculator;
    if (attackerCount >= 2 && defenderCount >= 1) {
        calculator = <Outcomes attackerCount={attackerCount} defenderCount={defenderCount}/>;
    } else {
        calculator = <div />
    }

    return (
        <div>
            <div>
                Attackers:
                <input
                    type="number"
                    min="2"
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
            {calculator}
        </div>
    )
}

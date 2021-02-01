import { useMemo, useState, Fragment } from 'react';
import { Bar } from 'react-chartjs-2';
import { getAutomaticTypeDirectiveNames } from 'typescript';
import { OutcomeDistribution, simulate, State } from './Simulation';

export interface Props {

}

const Outcomes: React.FC<State> = (props) => {
    
    const simData: OutcomeDistribution = useMemo( () => {
        return simulate(props); 
    }, [props]);

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
            label: 'Liklihood',
            backgroundColor: 'rgba(255,99,132,0.5)',
            //borderColor: 'rgba(255,99,132,1)',
            //borderWidth: 1,
            hoverBackgroundColor: 'rgba(255,99,132,1.0)',
            //hoverBorderColor: 'rgba(255,99,132,1)',
            data: values,
            barPercentage: 1.0,
            categoryPercentage: 1.0
          }
        ]
      };

    const options = {
        title: {
            display: true,
            text: 'End state likelihoods'
        },
        scales: {
            xAxes: [{
                scaleLabel: {
                    display: true,
                    labelString: 'Remaining troops ([attacker] - [defender])'
                }
            }],
            yAxes: [{
                ticks: {
                    beginAtZero: true
                },
                scaleLabel: {
                    display: true,
                    labelString: 'Likelihood (%)'
                }
            }]            
        },
        legend: {
            display: false
        }
    };

    return (
        <Fragment>
            <div>
                <h2>All out attack with {props.attackerCount} against {props.defenderCount}</h2>
                <Bar data={data} options={options} />
            </div>
            <div>
                <h2>Summary</h2>
                <div className='tablewrapper'>
                    <table>
                        <tr>
                            <td>Liklihood attacker wins</td>
                            <td>{(100 * simData.attackerWinCount) / simData.totalCount}%</td>
                        </tr>
                        <tr>
                            <td>Liklihood defender wins</td>
                            <td>{(100 * simData.defenderWinCount) / simData.totalCount}%</td>
                        </tr>
                        <tr>
                            <td>P25</td>
                            <td>{simData.p25.attackerCount} - {simData.p25.defenderCount}</td>
                        </tr>
                        <tr>
                            <td>P50</td>
                            <td>{simData.p50.attackerCount} - {simData.p50.defenderCount}</td>
                        </tr>
                        <tr>
                            <td>P75</td>
                            <td>{simData.p75.attackerCount} - {simData.p75.defenderCount}</td>
                        </tr>
                    </table>
                </div>
                <p><small>(Simulated {simData.totalCount} tials in {simData.simulationTimeMs}ms.)</small></p>
            </div>
        </Fragment>
    );
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
        <Fragment>
            <div>
                <h2>Setup</h2>
                <div className='tablewrapper'>
                    <table>
                        <tr>
                            <td>Attackers:</td>
                            <td>
                                <input
                                    type="number"
                                    min="2"
                                    max="100"
                                    value={attackerCount}
                                    onChange={(e) => setAttackerCount(Number(e.target.value))}
                                />
                            </td>
                        </tr>
                        <tr>
                            <td>Defenders:</td>
                            <td>
                                <input
                                    type="number"
                                    min="1"
                                    max="100"
                                    value={defenderCount}
                                    onChange={(e) => setDefenderCount(Number(e.target.value))}
                                />
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
            {calculator}
        </Fragment>
    );
}

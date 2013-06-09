function debt = computeBalance(initialDebt, yearlyInterestRate, monthlyPayment, yearlyInflationRate, termInYears)

    monthlyInterestRate = (yearlyInterestRate + 1)^(1 / 12) - 1;
    monthlyInflationRate = (yearlyInflationRate + 1)^(1 / 12) - 1;
    
    debt = initialDebt;
    for i = 1:(termInYears*12)
        interest = debt * monthlyInterestRate;
        payment = monthlyPayment * (1 + monthlyInflationRate)^i;
        debt = debt + interest - payment;
    end

end


function solvePayments(mortgageSize, yearlyInterestRate, yearlyInflationRate, termInYears)

    monthlyPayments = fminunc(@(p) (computeBalance(mortgageSize, yearlyInterestRate, p, yearlyInflationRate, termInYears)^2), 0);

    printf("Mortgage size, interest rate, inflation rate, term: %f, %f, %f, %f\n", mortgageSize, yearlyInterestRate, yearlyInflationRate, termInYears);
    printf("Monthly payments: %.2f\n", monthlyPayments);

end

